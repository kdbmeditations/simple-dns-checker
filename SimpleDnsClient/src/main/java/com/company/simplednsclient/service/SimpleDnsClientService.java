package com.company.simplednsclient.service;

import com.company.simplednsclient.core.SimpleDnsClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * The SimpleDnsClientService glues everything together and runs it
 * It is a Singleton - initialised only once
 */
public class SimpleDnsClientService {
    private static SimpleDnsClientService instance;

    /**
     * Private constructor for SimpleDnsClientService
     */
    private SimpleDnsClientService() {}

    /**
     * Initialise only one instance of SimpleDnsClientService
     * @return the instance
     */
    public static synchronized SimpleDnsClientService getInstance() {
        if (instance == null) {
            instance = new SimpleDnsClientService();
        }
        return instance;
    }

    public void run() throws IOException, ExecutionException, InterruptedException {
        SimpleDnsClient simpleDnsClient = new SimpleDnsClient();
        simpleDnsClient.run();
    }

}
