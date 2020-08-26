package com.company.simplednsclient.core;

public class SimpleDnsClient {
    private DnsTrackers dnsTrackers;
    private static final int NUM_DNS_TRACKERS = 1;

    public SimpleDnsClient() {
        this.dnsTrackers = new DnsTrackers();
    }

    public void run() throws InterruptedException {
        init();

        for (;;) {
            dnsTrackers.check();
            Thread.sleep(100);
        }
    }

    public void init() {
        for (int i = 0; i < NUM_DNS_TRACKERS; i++) {
            dnsTrackers.addDnsTracker(i);
        }

        dnsTrackers.start();
    }
}
