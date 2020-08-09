package com.company.simplednsclient.core;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;

public class SimpleDnsClient {
    private AsynchronousSocketChannel clientSocketChannel;
    private Future futureConnectResult;
    private Future futureWriteResult;
    private Future futureReadResult;
    private String query;
    private InetSocketAddress hostAddress;
    private Boolean waitingForResponse;
    private int messageId;
    private ByteBuffer buffer;

    public SimpleDnsClient() { }

    public void run() throws InterruptedException {
        init();

        for (;;) {
            check();
            Thread.sleep(100);
        }
    }

    private void check() {
        checkSendQuery();
        checkSendQueryDone();
        checkResponseReceived();
    }

    public void init() {
        messageId = 0;
        waitingForResponse = false;
        System.out.println("Client is started.");
        hostAddress = new InetSocketAddress("localhost", 3883);
    }

    public void checkSendQuery() {
        if (waitingForResponse)
            return;

        try {
            if (clientSocketChannel == null) {
                System.out.println("Connecting to DNS server");
                clientSocketChannel = AsynchronousSocketChannel.open();
                futureConnectResult = clientSocketChannel.connect(hostAddress);
            }

            if (futureConnectResult != null && futureConnectResult.isDone() && futureWriteResult == null) {
                futureConnectResult.get();
                futureConnectResult = null;
                System.out.println("Connected to DNS server");
                messageId++;
                query = "Message with messsage Id: " + messageId;
                byte[] messageByteArray = query.getBytes();
                ByteBuffer buffer = ByteBuffer.wrap(messageByteArray);
                futureWriteResult = clientSocketChannel.write(buffer);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void checkSendQueryDone() {
        try {
            if (futureWriteResult != null && futureWriteResult.isDone()) {
                futureWriteResult.get();
                futureWriteResult = null;
                System.out.println("Send message: " + query);
                waitingForResponse = true;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void checkResponseReceived() {
        if (!waitingForResponse)
            return;

        try {
            if (futureReadResult == null) {
                buffer = ByteBuffer.allocate(1024);
                futureReadResult = clientSocketChannel.read(buffer);
            }

            if (futureReadResult.isDone()) {
                futureReadResult.get();
                futureReadResult = null;
                String message = new String(buffer.array()).trim();
                System.out.println("Received response: " + message);
                clientSocketChannel.close();
                clientSocketChannel = null;
                waitingForResponse = false;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
