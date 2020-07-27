package main.java.com.company.simplednschecker.service;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.xbill.DNS.*;

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

    public void run() throws UnknownHostException, TextParseException {
        String domainName = "google.com";

        // Using java.net.InetAddress - can retrieve A and AAAA DNS records only
        System.out.println("\nUsing java.net.InetAddress");
        InetAddress inetAddress = Address.getByName(domainName);
        System.out.println(inetAddress.getHostAddress());

        // Using Java DNS library: https://github.com/dnsjava/dnsjava - can retrieve various types of DNS records
        System.out.println("\nUsing Java DNS library");
        Record [] records = new Lookup(domainName).run();

        if (records != null) {
            for (int i = 0; i < records.length; i++) {
                // Get all record data as a string
                System.out.println(records[i].toString());

                // Get only IP address or CNAME etc. (depending on record type)
                //System.out.println(records[i].rdataToString());
            }
        } else {
            System.out.println("No records found...");
        }
    }

}
