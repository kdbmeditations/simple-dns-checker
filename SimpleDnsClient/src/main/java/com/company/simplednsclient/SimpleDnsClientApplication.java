package com.company.simplednsclient;

import com.company.simplednsclient.service.SimpleDnsClientService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main starting point of the Application
 */
public class SimpleDnsClientApplication {
    private static final Logger LOGGER = Logger.getLogger(SimpleDnsClientApplication.class.getName());

    public static void main(String[] args) {
        try {
            SimpleDnsClientService.getInstance().run();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            System.exit(-1);
        }

        System.exit(0);
    }
}
