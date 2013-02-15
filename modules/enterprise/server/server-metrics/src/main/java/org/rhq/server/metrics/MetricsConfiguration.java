/*
 *
 *  * RHQ Management Platform
 *  * Copyright (C) 2005-2012 Red Hat, Inc.
 *  * All rights reserved.
 *  *
 *  * This program is free software; you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License, version 2, as
 *  * published by the Free Software Foundation, and/or the GNU Lesser
 *  * General Public License, version 2.1, also as published by the Free
 *  * Software Foundation.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  * GNU General Public License and the GNU Lesser General Public License
 *  * for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * and the GNU Lesser General Public License along with this program;
 *  * if not, write to the Free Software Foundation, Inc.,
 *  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */

package org.rhq.server.metrics;

import org.joda.time.Duration;

import org.rhq.server.metrics.domain.MetricsTable;

/**
 * @author John Sanda
 */
public class MetricsConfiguration {

    private Duration rawRetention = Duration.standardDays(7);

    private Duration oneHourRetention = Duration.standardDays(14);

    private Duration sixHourRetention = Duration.standardDays(31);

    private Duration twentyFourHourRetention = Duration.standardDays(365);

    private int rawTTL = MetricsTable.RAW.getTTL();

    private int oneHourTTL = MetricsTable.ONE_HOUR.getTTL();

    private int sixHourTTL = MetricsTable.SIX_HOUR.getTTL();

    private int twentyFourHourTTL = MetricsTable.TWENTY_FOUR_HOUR.getTTL();

    private Duration rawTimeSliceDuration = Duration.standardHours(1);

    private Duration oneHourTimeSliceDuration = Duration.standardHours(6);

    private Duration sixHourTimeSliceDuration = Duration.standardHours(24);

    public int getRawTTL() {
        return rawTTL;
    }

    public void setRawTTL(int rawTTL) {
        this.rawTTL = rawTTL;
    }

    public int getOneHourTTL() {
        return oneHourTTL;
    }

    public void setOneHourTTL(int oneHourTTL) {
        this.oneHourTTL = oneHourTTL;
    }

    public int getSixHourTTL() {
        return sixHourTTL;
    }

    public void setSixHourTTL(int sixHourTTL) {
        this.sixHourTTL = sixHourTTL;
    }

    public int getTwentyFourHourTTL() {
        return twentyFourHourTTL;
    }

    public void setTwentyFourHourTTL(int twentyFourHourTTL) {
        this.twentyFourHourTTL = twentyFourHourTTL;
    }

    public Duration getRawRetention() {
        return rawRetention;
    }

    public void setRawRetention(Duration retention) {
        rawRetention = rawRetention;
    }

    public Duration getOneHourRetention() {
        return oneHourRetention;
    }

    public void setOneHourRetention(Duration retention) {
        oneHourRetention = retention;
    }

    public Duration getSixHourRetention() {
        return sixHourRetention;
    }

    public void setSixHourRetention(Duration retention) {
        sixHourRetention = retention;
    }

    public Duration getTwentyFourHourRetention() {
        return twentyFourHourRetention;
    }

    public void setTwentyFourHourRetention(Duration retention) {
        twentyFourHourRetention = retention;
    }

    public Duration getRawTimeSliceDuration() {
        return rawTimeSliceDuration;
    }

    public void setRawTimeSliceDuration(Duration rawTimeSliceDuration) {
        this.rawTimeSliceDuration = rawTimeSliceDuration;
    }

    public Duration getOneHourTimeSliceDuration() {
        return oneHourTimeSliceDuration;
    }

    public void setOneHourTimeSliceDuration(Duration oneHourTimeSliceDuration) {
        this.oneHourTimeSliceDuration = oneHourTimeSliceDuration;
    }

    public Duration getSixHourTimeSliceDuration() {
        return sixHourTimeSliceDuration;
    }

    public void setSixHourTimeSliceDuration(Duration sixHourTimeSliceDuration) {
        this.sixHourTimeSliceDuration = sixHourTimeSliceDuration;
    }
}
