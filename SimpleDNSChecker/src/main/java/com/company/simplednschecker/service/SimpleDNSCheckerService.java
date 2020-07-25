package main.java.com.company.simplednschecker.service;

import java.net.InetAddress;

/**
 * The SimpleDNSCheckerService glues everything together and runs it
 * It is a Singleton - initialised only once
 */
public class SimpleDNSCheckerService {
    private static SimpleDNSCheckerService instance;

    /**
     * Private constructor for SimpleDNSCheckerService
     */
    private SimpleDNSCheckerService() {}

    /**
     * Initialise only one instance of SimpleDNSCheckerService
     * @return the instance
     */
    public static synchronized SimpleDNSCheckerService getInstance() {
        if (instance == null) {
            instance = new SimpleDNSCheckerService();
        }
        return instance;
    }

    public void run() {
        String domainName = "www.google.com";

        try {
            InetAddress[] inetAddressArray = InetAddress.getAllByName(domainName);
            for (int i = 0; i < inetAddressArray.length; i++) {
                displayDnsDetails(domainName +  " #" + (i + 1), inetAddressArray[i]);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void displayDnsDetails(String host, InetAddress inetAddress) {
        System.out.println("--------------------------");
        System.out.println("Which Host: " + host);
        System.out.println("Canonical Host Name: " + inetAddress.getCanonicalHostName());
        System.out.println("Host Name: " + inetAddress.getHostName());
        System.out.println("Host Address: " + inetAddress.getHostAddress());
    }

}
