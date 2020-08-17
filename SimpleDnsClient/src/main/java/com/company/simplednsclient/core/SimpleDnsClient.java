package com.company.simplednsclient.core;

public class SimpleDnsClient {

    public SimpleDnsClient() { }

    public void run() throws InterruptedException {
        DnsTracker dnsTracker = new DnsTracker();
        dnsTracker.start();

        for (;;) {
            System.out.println("Performing check...");
            dnsTracker.check();
            Thread.sleep(2000);
        }
    }
}
