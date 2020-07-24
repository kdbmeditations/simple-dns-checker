package main.java.com.company.simplednschecker.service;

/**
 * The SimpleDNSCheckerService glues everything together and runs it
 * It is a Singleton - initialised only once
 *
 * Each MessagePublisher/MessageConsumer is treated as a Task
 *
 * Tasks are executed by a FixedThreadPool, the size of which is determined by the number of logical CPU cores
 * This limits the excessive creation of threads which can be expensive
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
        System.out.println("Hello from Simple DNS Checker Service!");
    }

}
