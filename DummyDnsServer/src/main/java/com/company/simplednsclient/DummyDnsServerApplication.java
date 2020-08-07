package com.company.simplednsclient;

import com.company.simplednsclient.service.DummyDnsServerService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main starting point of the Application
 */
public class DummyDnsServerApplication {
    private static final Logger LOGGER = Logger.getLogger(DummyDnsServerApplication.class.getName());

    public static void main(String[] args) {
        try {
            DummyDnsServerService.getInstance().run();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            System.exit(-1);
        }

        System.exit(0);
    }
}
