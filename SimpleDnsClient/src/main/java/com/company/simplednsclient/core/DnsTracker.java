package com.company.simplednsclient.core;

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
    private enum ExternalState {
        UNKNOWN,
        HEALTHY,
        UNHEALTHY
    }

    private enum InternalState {
        CHECK_RESPONSE,
        SEND_REQUEST,
        TIMED_OUT,
        SLEEPING,
        RESPONSE_RECEIVED
    }

    private InetSocketAddress serverAddress;
    private DatagramChannel dnsTrackerDatagramChannel;
    private Selector dnsTrackerSelector;

    private InternalState internalState;
    private ExternalState externalState;
    private int dnsTrackerId;

    private ByteBuffer requestMessageBuffer;
    private ByteBuffer responseMessageBuffer;

    private long timeOutThresholdCount = 0;
    private long timeOutThresholdMax = 5;

    private long nextTimeInterval = 5;
    private long delayedTimeInterval = nextTimeInterval;

    private long wakeUpTime;

    public DnsTracker(int dnsTrackerId) {
        this.dnsTrackerId = dnsTrackerId;
        this.internalState = InternalState.SEND_REQUEST;
        this.externalState = ExternalState.UNKNOWN;
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
        } catch (Exception e) {
            System.out.println("Exception thrown when attempting to start a DNS Tracker (ID: " + dnsTrackerId + "): " + e.getMessage());
        }
    }

    public void check() {
        try {
            checkState();
        } catch (Exception e) {
            System.out.println("Exception thrown when attempting to perform a DNS Tracker (ID: " + dnsTrackerId + ") check: " + e.getMessage());
        }
    }

    private void checkState() throws IOException {
        long now = System.nanoTime();
        switch (internalState) {
            case SEND_REQUEST:
                sendRequest();
                break;
            case CHECK_RESPONSE:
                checkForResponse();
                break;
            case TIMED_OUT:
                internalState = InternalState.SLEEPING;
                externalState = ExternalState.UNHEALTHY;
                // call event handler to handle external state
                delayedTimeInterval += nextTimeInterval;
                wakeUpTime = now + (delayedTimeInterval * NanoTo.SECOND);
                System.out.println("Current delayed time interval (in seconds): " + delayedTimeInterval);
                break;
            case SLEEPING:
                if (now - wakeUpTime >= 0) {
                    internalState = InternalState.SEND_REQUEST;
                }
                break;
            case RESPONSE_RECEIVED:
                internalState = InternalState.SLEEPING;
                externalState = ExternalState.HEALTHY;
                // call event handler to handle external state
                wakeUpTime = now + (nextTimeInterval * NanoTo.SECOND);
                delayedTimeInterval = nextTimeInterval;
                break;
        }
    }

    private void checkForResponse() throws IOException {
        if (dnsTrackerSelector.selectNow() >= 1) {
            handleResponse();
        } else {
            handleNoResponse();
        }
    }

    private void handleResponse() {
        Set keys = dnsTrackerSelector.selectedKeys();

        for(Iterator keyIterator = keys.iterator(); keyIterator.hasNext(); ) {
            SelectionKey key = (SelectionKey)keyIterator.next();
            keyIterator.remove();

            if (key.isReadable()) {
                processResponse();
            }
        }

        internalState = InternalState.RESPONSE_RECEIVED;
    }

    private void handleNoResponse() {
        timeOutThresholdCount++;
        System.out.println("DNS Tracker (ID: " + dnsTrackerId + ") running on PORT " + dnsTrackerDatagramChannel.socket().getLocalPort() + " failed to receive response - timeout treshold count: " + timeOutThresholdCount);

        if (timeOutThresholdCount >= timeOutThresholdMax) {
            internalState = InternalState.TIMED_OUT;
            timeOutThresholdCount = 0;
        }
    }

    private void sendRequest() {
        try {
            System.out.println("DNS Tracker (ID: " + dnsTrackerId + ") running on PORT " + dnsTrackerDatagramChannel.socket().getLocalPort() + " sending request...");

            String msg = "I am DNS Tracker (ID: " + dnsTrackerId + ") running on PORT " + dnsTrackerDatagramChannel.socket().getLocalPort() + " requesting information...";
            requestMessageBuffer = ByteBuffer.wrap(msg.getBytes());
            dnsTrackerDatagramChannel.send(requestMessageBuffer, serverAddress);

            requestMessageBuffer.clear();
            internalState = InternalState.CHECK_RESPONSE;
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

            System.out.println("DNS Tracker (ID: " + dnsTrackerId + ") received a response: Server at " + serverAddress + " sent: " + message);

            responseMessageBuffer.clear();
            internalState = InternalState.SEND_REQUEST;

            timeOutThresholdCount = 0;
        } catch (Exception e) {
            System.out.println("Exception thrown when DNS Tracker (ID: " + dnsTrackerId + ") running on PORT " + dnsTrackerDatagramChannel.socket().getLocalPort() +  " attempted process a Request: " + e.getMessage());
        }
    }
}
