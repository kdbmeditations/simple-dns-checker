package com.company.simplednsclient.core;

import com.company.simplednsclient.utils.NanoTo;

import java.io.*;
import java.net.DatagramPacket;
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
            //serverAddress = new InetSocketAddress("localhost",3883);
            // DNS
            serverAddress = new InetSocketAddress("8.8.8.8",53);
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

            ByteArrayOutputStream dnsQuery = buildDnsQuery();

            String msg = "I am DNS Tracker (ID: " + dnsTrackerId + ") running on PORT " + dnsTrackerDatagramChannel.socket().getLocalPort() + " requesting information...";
            requestMessageBuffer = ByteBuffer.wrap(msg.getBytes());
            //dnsTrackerDatagramChannel.send(requestMessageBuffer, serverAddress);
            dnsTrackerDatagramChannel.send(ByteBuffer.wrap(dnsQuery.toByteArray()), serverAddress);

            requestMessageBuffer.clear();
            internalState = InternalState.CHECK_RESPONSE;
        } catch (Exception e) {
            System.out.println("Exception thrown when DNS Tracker (ID: " + dnsTrackerId + ") running on PORT " + dnsTrackerDatagramChannel.socket().getLocalPort() +  " attempted to send a Request: " + e.getMessage());
        }
    }

    private ByteArrayOutputStream buildDnsQuery() throws IOException {
        String domain = "google.com";

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        // *** Build a DNS Request Frame ****

        // Identifier: A 16-bit identification field generated by the device that creates the DNS query.
        // It is copied by the server into the response, so it can be used by that device to match that
        // query to the corresponding reply received from a DNS server. This is used in a manner similar
        // to how the Identifier field is used in many of the ICMP message types.
        dataOutputStream.writeShort(0x1234);

        // Write Query Flags
        dataOutputStream.writeShort(0x0100);

        // Question Count: Specifies the number of questions in the Question section of the message.
        dataOutputStream.writeShort(0x0001);

        // Answer Record Count: Specifies the number of resource records in the Answer section of the message.
        dataOutputStream.writeShort(0x0000);

        // Authority Record Count: Specifies the number of resource records in the Authority section of
        // the message. (NS stands for name server)
        dataOutputStream.writeShort(0x0000);

        // Additional Record Count: Specifies the number of resource records in the Additional section of the message.
        dataOutputStream.writeShort(0x0000);

        String[] domainParts = domain.split("\\.");
        //System.out.println(domain + " has " + domainParts.length + " parts");

        for (int i = 0; i < domainParts.length; i++) {
            //System.out.println("Writing: " + domainParts[i]);
            byte[] domainBytes = domainParts[i].getBytes("UTF-8");
            dataOutputStream.writeByte(domainBytes.length);
            dataOutputStream.write(domainBytes);
        }

        // No more parts
        dataOutputStream.writeByte(0x00);

        // Type 0x01 = A (Host Request)
        dataOutputStream.writeShort(0x0001);

        // Class 0x01 = IN
        dataOutputStream.writeShort(0x0001);

        byte[] dnsFrame = byteArrayOutputStream.toByteArray();

        System.out.println("Sending: " + dnsFrame.length + " bytes");
//        for (int i =0; i< dnsFrame.length; i++) {
//            System.out.print("0x" + String.format("%x", dnsFrame[i]) + " " );
//        }
        return byteArrayOutputStream;
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

            //System.out.println("DNS Tracker (ID: " + dnsTrackerId + ") received a response: Server at " + serverAddress + " sent: " + message);

            parseDnsResponse(bytes);

            responseMessageBuffer.clear();
            internalState = InternalState.SEND_REQUEST;

            timeOutThresholdCount = 0;
        } catch (Exception e) {
            System.out.println("Exception thrown when DNS Tracker (ID: " + dnsTrackerId + ") running on PORT " + dnsTrackerDatagramChannel.socket().getLocalPort() +  " attempted process a Request: " + e.getMessage());
        }
    }

    private void parseDnsResponse(byte[] bytes) throws IOException {
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);

        System.out.println("Received: " + packet.getLength() + " bytes");

//        for (int i = 0; i < packet.getLength(); i++) {
//            System.out.print(" 0x" + String.format("%x", bytes[i]) + " " );
//        }
//        System.out.println("\n");

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        System.out.println("Transaction ID: 0x" + String.format("%x", dataInputStream.readShort()));
        System.out.println("Flags: 0x" + String.format("%x", dataInputStream.readShort()));
        System.out.println("Questions: 0x" + String.format("%x", dataInputStream.readShort()));
        System.out.println("Answers RRs: 0x" + String.format("%x", dataInputStream.readShort()));
        System.out.println("Authority RRs: 0x" + String.format("%x", dataInputStream.readShort()));
        System.out.println("Additional RRs: 0x" + String.format("%x", dataInputStream.readShort()));

        int recLen = 0;
        while ((recLen = dataInputStream.readByte()) > 0) {
            byte[] record = new byte[recLen];

            for (int i = 0; i < recLen; i++) {
                record[i] = dataInputStream.readByte();
            }

            //System.out.println("Record: " + new String(record, "UTF-8"));
        }

        System.out.println("Record Type: 0x" + String.format("%x", dataInputStream.readShort()));
        System.out.println("Class: 0x" + String.format("%x", dataInputStream.readShort()));
        System.out.println("Field: 0x" + String.format("%x", dataInputStream.readShort()));
        System.out.println("Type: 0x" + String.format("%x", dataInputStream.readShort()));
        System.out.println("Class: 0x" + String.format("%x", dataInputStream.readShort()));
        System.out.println("TTL: 0x" + String.format("%x", dataInputStream.readInt()));

        short addressLength = dataInputStream.readShort();
        System.out.println("Len: 0x" + String.format("%x", addressLength));

        System.out.print("Address: ");
        for (int i = 0; i < addressLength; i++ ) {
            System.out.print("" + String.format("%d", (dataInputStream.readByte() & 0xFF)) + ".");
        }

        System.out.println("\n");
    }
}
