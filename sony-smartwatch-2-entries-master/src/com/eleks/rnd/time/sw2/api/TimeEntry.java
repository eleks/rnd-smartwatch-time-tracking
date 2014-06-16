package com.eleks.rnd.time.sw2.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.Interval;

public class TimeEntry implements Serializable {

    private String client;
    private String matter;
    private String narrative;
    private List<Interval> intervals;

    public static final TimeEntry EMPTY = new TimeEntry("Client", "Matter", "Sample narrative.");

    public TimeEntry(String client, String matter, String narrative) {
        this.client = client;
        this.matter = matter;
        this.narrative = narrative;
        intervals = new ArrayList<Interval>();
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getMatter() {
        return matter;
    }

    public void setMatter(String matter) {
        this.matter = matter;
    }

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    public List<Interval> getIntervals() {
        return Collections.unmodifiableList(intervals);
    }

    public void addInterval(Interval interval) {
        this.intervals.add(interval);
    }

}