/*
 * RHQ Management Platform
 * Copyright (C) 2005-2011 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.rhq.modules.plugins.jbossas7;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.ConfigurationUpdateStatus;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.domain.configuration.PropertyMap;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.configuration.definition.ConfigurationDefinition;
import org.rhq.core.domain.configuration.definition.PropertyDefinition;
import org.rhq.core.domain.configuration.definition.PropertyDefinitionList;
import org.rhq.core.domain.configuration.definition.PropertyDefinitionMap;
import org.rhq.core.domain.configuration.definition.PropertyDefinitionSimple;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.DataType;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementDataTrait;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.modules.plugins.jbossas7.json.NameValuePair;
import org.rhq.modules.plugins.jbossas7.json.PROPERTY_VALUE;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BaseComponent implements ResourceComponent, MeasurementFacet, ConfigurationFacet
{
    private final Log log = LogFactory.getLog(this.getClass());

    ResourceContext context;
    Configuration conf;
    String myServerName;
    ASConnection connection;
    String path;
    String key;

    /**
     * Return availability of this resource
     *  @see org.rhq.core.pluginapi.inventory.ResourceComponent#getAvailability()
     */
    public AvailabilityType getAvailability() {
        // TODO supply real implementation
        return AvailabilityType.UP;
    }


    /**
     * Start the resource connection
     * @see org.rhq.core.pluginapi.inventory.ResourceComponent#start(org.rhq.core.pluginapi.inventory.ResourceContext)
     */
    public void start(ResourceContext context) throws InvalidPluginConfigurationException, Exception {
        this.context = context;
        conf = context.getPluginConfiguration();
        // TODO add code to start the resource / connection to it

        String typeName = context.getResourceType().getName();
        String host = conf.getSimpleValue("hostname","localhost");
        String portString = conf.getSimpleValue("port","9990");
        int port = Integer.parseInt(portString);
        connection = new ASConnection(host,port);

        path = conf.getSimpleValue("path", null);
        key = context.getResourceKey();



//        Object o = connection.getLevelData("", false); // BASE entries

        myServerName = context.getResourceKey().substring(context.getResourceKey().lastIndexOf("/")+1);


    }


    /**
     * Tear down the resource connection
     * @see org.rhq.core.pluginapi.inventory.ResourceComponent#stop()
     */
    public void stop() {


    }



    /**
     * Gather measurement data
     * @see org.rhq.core.pluginapi.measurement.MeasurementFacet#getValues(org.rhq.core.domain.measurement.MeasurementReport, java.util.Set)
     */
    public  void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> metrics) throws Exception {


        for (MeasurementScheduleRequest req : metrics) {

            JsonNode obj = connection.getAttributeValue(key, req.getName()); // TODO batching

            String val = obj.getValueAsText();
            if (req.getDataType()== DataType.MEASUREMENT) {

                Double d = Double.parseDouble(val);
                MeasurementDataNumeric data = new MeasurementDataNumeric(req,d);
                report.addData(data);
            } else if (req.getDataType()== DataType.TRAIT) {
                MeasurementDataTrait data = new MeasurementDataTrait(req,val);
                report.addData(data);
            }
        }
    }


    protected ASConnection getASConnection() {
        return connection;
    }


    protected String getPath() { return path; }

    public Configuration loadResourceConfiguration() throws Exception {
        ConfigurationDefinition configDef = context.getResourceType().getResourceConfigurationDefinition();
        BaseComponent parentComponent = (BaseComponent) context.getParentResourceComponent();
        String myPath;
        String parentPath = parentComponent.getPath();
        if (parentPath.endsWith("/") || path.startsWith("/"))
            myPath = parentPath + path;
        else
            myPath = parentPath + "/" + path;


        JsonNode json = connection.getLevelData(myPath,true,false);

        Configuration ret = new Configuration();
        ObjectMapper mapper = new ObjectMapper();

        for (PropertyDefinition propDef: configDef.getNonGroupedProperties()) {
            JsonNode sub = json.findValue(propDef.getName());
            if (propDef instanceof PropertyDefinitionSimple) {
                PropertySimple propertySimple;

                if (sub!=null)
                    propertySimple = new PropertySimple(propDef.getName(),sub.getValueAsText());
                else {
                    propertySimple = new PropertySimple(propDef.getName(),null); // TODO store it at all?
                }
                    ret.put(propertySimple);
            } else if (propDef instanceof PropertyDefinitionList) {
                PropertyList propertyList = new PropertyList(propDef.getName());
                PropertyDefinition memberDefinition = ((PropertyDefinitionList) propDef).getMemberDefinition();
                if (memberDefinition ==null) {
                    Iterator<JsonNode> values = sub.getElements();
                    while (values.hasNext()) {
                        JsonNode node = values.next();
                        String value = node.getTextValue();
                        PropertySimple propertySimple = new PropertySimple(propDef.getName(),value);
                        propertyList.add(propertySimple);
                    }
                }
                else if (memberDefinition instanceof PropertyDefinitionMap) {
                    PropertySimple propertySimple;

                    if (sub.isArray()) {
                        Iterator<JsonNode> entries = sub.getElements();
                        while (entries.hasNext()) {
                            JsonNode entry = entries.next(); // -> one row in the list i.e. one map

                            // Distinguish here?

                            PropertyMap map = new PropertyMap(memberDefinition.getName()); // TODO : name from def or 'entryKey' ?
                            Iterator<JsonNode> fields = entry.getElements(); // TODO loop over fields from map and not from json
                            while (fields.hasNext()) {
                                JsonNode field  = fields.next();
                                if (field.isObject()) {
                                    // TODO only works for tuples at the moment - migrate to some different kind of parsing!
                                    PROPERTY_VALUE prop = mapper.readValue(field,PROPERTY_VALUE.class);
                                    // now need to find the names of the properties
                                    List<PropertyDefinition> defList = ((PropertyDefinitionMap) memberDefinition).getSummaryPropertyDefinitions();
                                    if (defList.isEmpty())
                                        throw new IllegalArgumentException("Map " + memberDefinition.getName() + " has no members");
                                    String key = defList.get(0).getName();
                                    String value = prop.getKey();
                                    propertySimple = new PropertySimple(key,value);
                                    map.put(propertySimple);
                                    if (defList.size()>1) {
                                        key = defList.get(1).getName();
                                        value = prop.getValue();
                                        propertySimple = new PropertySimple(key,value);
                                        map.put(propertySimple);

                                    }
                                } else { // TODO reached?
                                    String key = field.getValueAsText();
                                    if (key.equals("PROPERTY_VALUE")) { // TODO this may change in the future in the AS implementation
                                        JsonNode pv = entry.findValue(key);
                                        String k = pv.toString();
                                        String v = pv.getValueAsText();
                                        propertySimple = new PropertySimple(k,v);
                                        map.put(propertySimple);

                                    }
                                    else {
                                        propertySimple = new PropertySimple(key,entry.findValue(key).getValueAsText());
                                        map.put(propertySimple);

                                    }
                                }
                            }
                            propertyList.add(map);
                        }
                    }
                    else if (sub.isObject()) {
                        Iterator<String> keys = sub.getFieldNames();
                        while(keys.hasNext()) {
                            String entryKey = keys.next();

                            JsonNode node = sub.findPath(entryKey);
                            PropertyMap map = new PropertyMap(memberDefinition.getName()); // TODO : name from def or 'entryKey' ?
                            if (node.isObject()) {
                                Iterator<String> fields = node.getFieldNames(); // TODO loop over fields from map and not from json
                                while (fields.hasNext()) {
                                    String key = fields.next();

                                    propertySimple = new PropertySimple(key,node.findValue(key).getValueAsText());
                                    map.put(propertySimple);
                                }
                                propertyList.add(map);
                            } else if (sub.isNull()) {
                                List<PropertyDefinition> defList = ((PropertyDefinitionMap) memberDefinition).getSummaryPropertyDefinitions();
                                String key = defList.get(0).getName();
                                propertySimple = new PropertySimple(key,entryKey);
                                map.put(propertySimple);
                            }
                        }

                    }
                }
                else if (memberDefinition instanceof PropertyDefinitionSimple) {
                    String name = memberDefinition.getName();
                    Iterator<String> keys = sub.getFieldNames();
                    while(keys.hasNext()) {
                        String entryKey = keys.next();

                        PropertySimple propertySimple = new PropertySimple(name,entryKey);
                        propertyList.add(propertySimple);
                    }
                }
                ret.put(propertyList);
            }
        }


        return ret;
    }

    public void updateResourceConfiguration(ConfigurationUpdateReport report) {


        Configuration conf = report.getConfiguration();
        for (Map.Entry<String, PropertySimple> entry : conf.getSimpleProperties().entrySet()) {

            NameValuePair nvp = new NameValuePair(entry.getKey(),entry.getValue().getStringValue());
            JsonNode result= connection.execute(key,"write-attribute",nvp); // TODO path / key handling
            if(connection.isErrorReply(result)) {
                report.setStatus(ConfigurationUpdateStatus.FAILURE);
                report.setErrorMessage(connection.getFailureDescription(result));
            }
        }

    }
}
