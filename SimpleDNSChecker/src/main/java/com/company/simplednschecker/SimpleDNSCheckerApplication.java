package main.java.com.company.simplednschecker;

import main.java.com.company.simplednschecker.service.SimpleDNSCheckerService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main starting point of the Application
 */
public class SimpleDNSCheckerApplication {
    private static final Logger LOGGER = Logger.getLogger(SimpleDNSCheckerApplication.class.getName());

    public static void main(String[] args) {
        try {
            SimpleDNSCheckerService.getInstance().run();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            System.exit(-1);
        }

        System.exit(0);
    }
}
