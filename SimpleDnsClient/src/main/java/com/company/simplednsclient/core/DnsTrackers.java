package com.company.simplednsclient.core;

import java.util.ArrayList;
import java.util.List;

public class DnsTrackers {
    private List<DnsTracker> dnsTrackerList;

    public DnsTrackers() {
        dnsTrackerList = new ArrayList<>();
    }

    public void start() {
        for (DnsTracker dnsTracker : dnsTrackerList) {
            dnsTracker.start();
        }
    }

    public void check() {
        for (DnsTracker dnsTracker : dnsTrackerList) {
            dnsTracker.check();
        }
    }

    public void addDnsTracker(int dnsTrackerId) {
        dnsTrackerList.add(new DnsTracker(dnsTrackerId));
    }
}
