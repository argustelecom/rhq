/*
 * RHQ Management Platform
 * Copyright (C) 2005-2010 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation, and/or the GNU Lesser
 * General Public License, version 2.1, also as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License and the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.rhq.core.domain.bundle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.rhq.core.domain.content.Repo;

/**
 * Defines a bundle of content that can be versioned.
 *
 * @author John Mazzitelli
 */
@Entity
@NamedQueries( {
    // Below queries primarily used for domain test code.    
    @NamedQuery(name = Bundle.QUERY_FIND_ALL, query = "SELECT b FROM Bundle b"), //    
    @NamedQuery(name = Bundle.QUERY_FIND_BY_NAME, query = "SELECT b FROM Bundle b WHERE :name = b.name"),
    @NamedQuery(name = Bundle.QUERY_FIND_BY_TYPE_AND_NAME, query = "SELECT b FROM Bundle b WHERE "
        + "(:type = b.bundleType.name OR :type IS NULL) AND (:name = b.name OR :name IS NULL)")

})
@SequenceGenerator(name = "SEQ", sequenceName = "RHQ_BUNDLE_ID_SEQ")
@Table(name = "RHQ_BUNDLE")
@XmlAccessorType(XmlAccessType.FIELD)
public class Bundle implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String QUERY_FIND_ALL = "Bundle.findAll";
    public static final String QUERY_FIND_BY_NAME = "Bundle.findByName";
    public static final String QUERY_FIND_BY_TYPE_AND_NAME = "Bundle.findByTypeAndName";

    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ")
    @Id
    private int id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @JoinColumn(name = "BUNDLE_TYPE_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(fetch = FetchType.EAGER)
    private BundleType bundleType;

    @JoinColumn(name = "REPO_ID", referencedColumnName = "ID", nullable = false)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private Repo repo;

    @OneToMany(mappedBy = "bundle", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BundleVersion> bundleVersions = new ArrayList<BundleVersion>();

    public Bundle() {
        // for JPA use
    }

    public Bundle(String name, BundleType type) {
        setName(name);
        setBundleType(type);
        // add implicit Repo
        Repo repo = new Repo(name);
        repo.setCandidate(false);
        repo.setSyncSchedule(null);
        setRepo(repo);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BundleType getBundleType() {
        return bundleType;
    }

    public void setBundleType(BundleType bundleType) {
        this.bundleType = bundleType;
    }

    public Repo getRepo() {
        return repo;
    }

    public void setRepo(Repo repo) {
        this.repo = repo;
    }

    public List<BundleVersion> getBundleVersions() {
        return bundleVersions;
    }

    public void addBundleVersion(BundleVersion bundleVersion) {
        this.bundleVersions.add(bundleVersion);
        bundleVersion.setBundle(this);
    }

    public void setBundleVersions(List<BundleVersion> bundleVersions) {
        this.bundleVersions = bundleVersions;
    }

    @Override
    public String toString() {
        return "Bundle[id=" + id + ",name=" + name + ",bundleType=" + bundleType + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Bundle)) {
            return false;
        }

        final Bundle other = (Bundle) obj;

        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }

        return true;
    }
}