package com.company.simplednsclient.core;

import com.company.simplednsclient.utils.DnsTrackerState;

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
    private DatagramChannel dnsTrackerChannel;
    private Selector selector;
    private DnsTrackerState dnsTrackerState;
    private int dnsTrackerId;

    private int failedAttempts;

    private ByteBuffer requestMessageBuffer;
    private ByteBuffer responseMessageBuffer;

    public DnsTracker(int dnsTrackerId) {
        this.dnsTrackerId = dnsTrackerId;
        this.failedAttempts = 0;
        this.dnsTrackerState = DnsTrackerState.STANDBY;
    }

    public void start() {
        try {
            serverAddress = new InetSocketAddress("localhost",3883);
            dnsTrackerChannel = DatagramChannel.open();
            dnsTrackerChannel.socket().bind(null);
            dnsTrackerChannel.configureBlocking(false);

            System.out.println("DNS Tracker (ID: " + dnsTrackerId + ") Started: PORT - " + dnsTrackerChannel.socket().getLocalPort());

            selector = Selector.open();
            dnsTrackerChannel.register(selector, SelectionKey.OP_READ);
            responseMessageBuffer = ByteBuffer.allocate(1024);
        } catch (Exception e) {
            System.out.println("Exception thrown when attempting to start a DNS Tracker: " + e.getMessage());
        }
    }

    public void check() {
        try {
            checkState();
        } catch (Exception e) {
            System.out.println("Exception thrown when attempting to perform a DNS Tracker check: " + e.getMessage());
        }
    }

    private void checkState() throws IOException {
        switch (dnsTrackerState) {
            case STANDBY:
                sendRequest();
                break;
            case WAITING_FOR_RESPONSE:
                checkForResponse();
                break;
            case TIMED_OUT:
                dnsTrackerState = DnsTrackerState.TIMED_OUT;
                break;
        }
    }

    private void checkForResponse() throws IOException {
        if (selector.selectNow() >= 1) {
            Set keys = selector.selectedKeys();

            for(Iterator keyIterator = keys.iterator(); keyIterator.hasNext(); ) {
                SelectionKey key = (SelectionKey)keyIterator.next();
                keyIterator.remove();

                if (key.isReadable()) {
                    processResponse();
                }
            }
        } else {
            failedAttempts++;
            System.out.println("DNS Tracker (ID: " + dnsTrackerId + ") running on PORT " + dnsTrackerChannel.socket().getLocalPort() + " failed to receive response - attempts: " + failedAttempts);
        }
    }

    private void sendRequest() throws IOException {
        System.out.println("DNS Tracker (ID: " + dnsTrackerId + ") running on PORT " + dnsTrackerChannel.socket().getLocalPort() + " sending request...");

        String msg = "I am DNS Tracker (ID: " + dnsTrackerId + ") running on PORT " + dnsTrackerChannel.socket().getLocalPort() + " requesting information...";
        requestMessageBuffer = ByteBuffer.wrap(msg.getBytes());
        dnsTrackerChannel.send(requestMessageBuffer, serverAddress);

        requestMessageBuffer.clear();
        dnsTrackerState = DnsTrackerState.WAITING_FOR_RESPONSE;
    }

    private void processResponse() throws IOException {
        System.out.println("DNS Tracker (ID: " + dnsTrackerId + ") running on PORT " + dnsTrackerChannel.socket().getLocalPort() + " processing response..");

        dnsTrackerChannel.receive(responseMessageBuffer);
        responseMessageBuffer.flip();
        int limits = responseMessageBuffer.limit();
        byte bytes[] = new byte[limits];
        responseMessageBuffer.get(bytes, 0, limits);
        String message = new String(bytes);

        System.out.println("DNS Tracker (ID: " + dnsTrackerId + "): Server at " + serverAddress + " sent: " + message);

        responseMessageBuffer.clear();
        failedAttempts = 0;
        dnsTrackerState = DnsTrackerState.STANDBY;
    }
}
