package com.company.simplednsclient.core;

import com.company.simplednsclient.utils.DnsTrackerState;
import com.company.simplednsclient.utils.NanoTo;

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
    private DatagramChannel dnsTrackerDatagramChannel;
    private Selector dnsTrackerSelector;
    private DnsTrackerState dnsTrackerState;
    private int dnsTrackerId;

    private ByteBuffer requestMessageBuffer;
    private ByteBuffer responseMessageBuffer;

    private long timeOutThresholdCount;
    private long timeOutThresholdMax = 5;

    private long nextTimeInterval;
    private long checkTimeInterval = 5;

    public DnsTracker(int dnsTrackerId) {
        this.dnsTrackerId = dnsTrackerId;
        this.timeOutThresholdCount = 0;
        this.dnsTrackerState = DnsTrackerState.STANDBY;
    }

    public void start() {
        try {
            serverAddress = new InetSocketAddress("localhost",3883);
            dnsTrackerDatagramChannel = DatagramChannel.open();
            dnsTrackerDatagramChannel.socket().bind(null);
            dnsTrackerDatagramChannel.configureBlocking(false);

            System.out.println("DNS Tracker (ID: " + dnsTrackerId + ") Started: PORT - " + dnsTrackerDatagramChannel.socket().getLocalPort());

            dnsTrackerSelector = Selector.open();
            dnsTrackerDatagramChannel.register(dnsTrackerSelector, SelectionKey.OP_READ);
            responseMessageBuffer = ByteBuffer.allocate(1024);

            long now = System.nanoTime();
            nextTimeInterval = now + (checkTimeInterval * NanoTo.SECOND);
        } catch (Exception e) {
            System.out.println("Exception thrown when attempting to start a DNS Tracker: " + e.getMessage());
        }
    }

    public void check() {
        try {
            long now = System.nanoTime();

            if ((now - nextTimeInterval) >= 0) {
                checkState();
                nextTimeInterval = now + (checkTimeInterval * NanoTo.SECOND);
            }
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
                System.out.println("DNS Tracker (ID: " + dnsTrackerId + ") running on PORT " + dnsTrackerDatagramChannel.socket().getLocalPort() + " has timed out...");
                break;
        }
    }

    private void checkForResponse() throws IOException {
        if (dnsTrackerSelector.selectNow() >= 1) {
            Set keys = dnsTrackerSelector.selectedKeys();

            for(Iterator keyIterator = keys.iterator(); keyIterator.hasNext(); ) {
                SelectionKey key = (SelectionKey)keyIterator.next();
                keyIterator.remove();

                if (key.isReadable()) {
                    processResponse();
                }
            }
        } else {
            timeOutThresholdCount++;
            System.out.println("DNS Tracker (ID: " + dnsTrackerId + ") running on PORT " + dnsTrackerDatagramChannel.socket().getLocalPort() + " failed to receive response - timeout treshold count: " + timeOutThresholdCount);

            if (timeOutThresholdCount == timeOutThresholdMax) {
                dnsTrackerState = DnsTrackerState.TIMED_OUT;
            }
        }
    }

    private void sendRequest() {
        try {
            System.out.println("DNS Tracker (ID: " + dnsTrackerId + ") running on PORT " + dnsTrackerDatagramChannel.socket().getLocalPort() + " sending request...");

            String msg = "I am DNS Tracker (ID: " + dnsTrackerId + ") running on PORT " + dnsTrackerDatagramChannel.socket().getLocalPort() + " requesting information...";
            requestMessageBuffer = ByteBuffer.wrap(msg.getBytes());
            dnsTrackerDatagramChannel.send(requestMessageBuffer, serverAddress);

            requestMessageBuffer.clear();
            dnsTrackerState = DnsTrackerState.WAITING_FOR_RESPONSE;
        } catch (Exception e) {
            System.out.println("Exception thrown when DNS Tracker (ID: " + dnsTrackerId + ") running on PORT " + dnsTrackerDatagramChannel.socket().getLocalPort() +  " attempted to send a Request: " + e.getMessage());
        }
    }

    private void processResponse() {
        try {
            System.out.println("DNS Tracker (ID: " + dnsTrackerId + ") running on PORT " + dnsTrackerDatagramChannel.socket().getLocalPort() + " processing response..");

            dnsTrackerDatagramChannel.receive(responseMessageBuffer);
            responseMessageBuffer.flip();
            int limits = responseMessageBuffer.limit();
            byte bytes[] = new byte[limits];
            responseMessageBuffer.get(bytes, 0, limits);
            String message = new String(bytes);

            System.out.println("DNS Tracker (ID: " + dnsTrackerId + "): Server at " + serverAddress + " sent: " + message);

            responseMessageBuffer.clear();
            dnsTrackerState = DnsTrackerState.STANDBY;

            timeOutThresholdCount = 0;
        } catch (Exception e) {
            System.out.println("Exception thrown when DNS Tracker (ID: " + dnsTrackerId + ") running on PORT " + dnsTrackerDatagramChannel.socket().getLocalPort() +  " attempted process a Request: " + e.getMessage());
        }
    }
}
