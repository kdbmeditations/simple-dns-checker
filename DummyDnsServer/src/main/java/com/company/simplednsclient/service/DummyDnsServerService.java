package com.company.simplednsclient.service;

import com.company.simplednsclient.core.DummyDnsServer;

/**
 * The DummyDnsServerService glues everything together and runs it
 * It is a Singleton - initialised only once
 */
public class DummyDnsServerService {
    private static DummyDnsServerService instance;

    /**
     * Private constructor for DummyDnsServerService
     */
    private DummyDnsServerService() {}

    /**
     * Initialise only one instance of DummyDnsServerService
     * @return the instance
     */
    public static synchronized DummyDnsServerService getInstance() {
        if (instance == null) {
            instance = new DummyDnsServerService();
        }
        return instance;
    }

    public void run() throws InterruptedException{
        DummyDnsServer dummyDnsServer = new DummyDnsServer();
        dummyDnsServer.start();
        dummyDnsServer.run();
    }

}
