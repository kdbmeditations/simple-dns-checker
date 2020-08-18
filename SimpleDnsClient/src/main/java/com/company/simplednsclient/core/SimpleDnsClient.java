package com.company.simplednsclient.core;

public class SimpleDnsClient {
    private DnsTrackers dnsTrackers;
    private static final int NUM_DNS_TRACKERS = 5;

    public SimpleDnsClient() {
        this.dnsTrackers = new DnsTrackers();
    }

    public void run() throws InterruptedException {
        init();

        for (;;) {
            System.out.println("DNS Client performing check...");
            dnsTrackers.check();
            Thread.sleep(50);
        }
    }

    public void init() {
        for (int i = 0; i < NUM_DNS_TRACKERS; i++) {
            dnsTrackers.addDnsTracker(i);
        }

        dnsTrackers.start();
    }
}
