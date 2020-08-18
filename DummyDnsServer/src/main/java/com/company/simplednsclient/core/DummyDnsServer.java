package com.company.simplednsclient.core;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class DummyDnsServer {
    private InetSocketAddress serverAddress;
    private DatagramChannel serverChannel;
    private Selector selector;
    private ByteBuffer receiveBuffer;

    public DummyDnsServer() {
    }

    public void run() {
        for (;;) {
            checkForRequest();
        }
    }

    public void start() {
        try {
            serverAddress = new InetSocketAddress("localhost", 3883);
            serverChannel = DatagramChannel.open();
            serverChannel.socket().bind(serverAddress);
            serverChannel.configureBlocking(false);

            System.out.println("Server Started: " + serverAddress);

            selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_READ);

            receiveBuffer = ByteBuffer.allocate(1024);
        } catch (Exception e) {
            System.out.println("Exception thrown when attempting to start Dummy DNS Server: " + e.getMessage());
        }
    }

    private void checkForRequest() {
        try {
            if (selector.selectNow() >= 1) {
                Set keys = selector.selectedKeys();

                for(Iterator keyIterator = keys.iterator(); keyIterator.hasNext();) {
                    SelectionKey key = (SelectionKey)keyIterator.next();
                    keyIterator.remove();

                    if (key.isReadable()) {
                        processRequest();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception thrown when attempting to check for Request: " + e.getMessage());
        }
    }

    private void processRequest() {
        try {
            SocketAddress clientAddress = serverChannel.receive(receiveBuffer);
            receiveBuffer.flip();
            int limits = receiveBuffer.limit();
            byte bytes[] = new byte[limits];
            receiveBuffer.get(bytes, 0, limits);
            String sentMessage = new String(bytes);
            System.out.println("Server: Client at " + clientAddress + " sent: " + sentMessage);
            sendResponse(clientAddress);

            receiveBuffer.clear();
        } catch (Exception e) {
            System.out.println("Exception thrown when attempting to process a Request: " + e.getMessage());
        }
    }

    private void sendResponse(SocketAddress clientAddress) {
        try {
            String responseMessage = "Response from Server at: " + serverAddress;
            ByteBuffer responseBuffer = ByteBuffer.wrap(responseMessage.getBytes());

            if (clientAddress != null)
                serverChannel.send(responseBuffer, clientAddress);
        } catch (Exception e) {
            System.out.println("Exception thrown when attempting to send a Response: " + e.getMessage());
        }
    }
}
