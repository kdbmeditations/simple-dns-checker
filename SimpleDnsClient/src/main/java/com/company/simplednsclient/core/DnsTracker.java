package com.company.simplednsclient.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class DnsTracker {
    private InetSocketAddress serverAddress;
    private String domainName;
    private DatagramChannel dnsTrackerChannel;
    private Selector selector;
    private int failedAttempts;

    private ByteBuffer requestMessageBuffer;
    private ByteBuffer responseMessageBuffer;

    public DnsTracker(InetSocketAddress serverAddress, String domainName) {
        this.serverAddress = serverAddress;
        this.domainName = domainName;
        this.failedAttempts = 0;
    }

    public void start() {
        try {
            serverAddress = new InetSocketAddress("localhost",3883);
            dnsTrackerChannel = DatagramChannel.open();
            dnsTrackerChannel.socket().bind(null);
            dnsTrackerChannel.configureBlocking(false);

            System.out.println("DNS Tracker Started: PORT - " + dnsTrackerChannel.socket().getLocalPort());

            selector = Selector.open();
            dnsTrackerChannel.register(selector, SelectionKey.OP_READ);
        } catch (Exception e) {
            System.out.println("Exception thrown when attempting to start a DNS Tracker: " + e.getMessage());
        }
    }

    public void check() {
        try {
            String msg = "I am DNS Tracker running on PORT " + dnsTrackerChannel.socket().getLocalPort() + " requesting information for: " + domainName;
            requestMessageBuffer = ByteBuffer.wrap(msg.getBytes());
            dnsTrackerChannel.send(requestMessageBuffer, serverAddress);
            requestMessageBuffer.clear();

            if (selector.selectNow() >= 1) {
                handleResponse();
            } else {
                failedAttempts++;
            }
        } catch (Exception e) {
            System.out.println("Exception thrown when attempting to perform a DNS Tracker check: " + e.getMessage());
        }
    }

    private void handleResponse() throws IOException {
        Set keys = selector.selectedKeys();

        // Iterate through the Set of keys.
        for(Iterator keyIterator = keys.iterator(); keyIterator.hasNext(); ) {
            // Get a key from the set, and remove it from the set
            SelectionKey key = (SelectionKey)keyIterator.next();
            keyIterator.remove();

            if (key.isReadable()) {
                processResponse();
            }
        }
    }

    private void processResponse() throws IOException {
        responseMessageBuffer = ByteBuffer.allocate(1024);
        dnsTrackerChannel.receive(responseMessageBuffer);
        responseMessageBuffer.flip();
        int limits = responseMessageBuffer.limit();
        byte bytes[] = new byte[limits];
        responseMessageBuffer.get(bytes, 0, limits);
        String message = new String(bytes);
        System.out.println("Dns Tracker: Server at " + serverAddress + " sent: " + message);
        responseMessageBuffer.clear();
    }
}
