package com.company.simplednsclient.core;

public class SimpleDnsClient {
    private DnsTrackers dnsTrackers;

    public SimpleDnsClient() {
        dnsTrackers = new DnsTrackers();
    }

    public void run() throws InterruptedException {
        init();

        for (;;) {
            System.out.println("Performing check...");
            dnsTrackers.check();
            Thread.sleep(100);
        }
    }

    public void init() {
        for (int i = 0; i < 2; i++) {
            dnsTrackers.addDnsTracker(i);
        }

        dnsTrackers.start();
    }
}
