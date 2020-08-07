package com.company.simplednsclient.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SimpleDnsClient {
    private AsynchronousSocketChannel clientChannel;

    public SimpleDnsClient() { }

    public void run() throws InterruptedException, ExecutionException, IOException {
        int messageId = 0;
        init();

        //sendQuery(messageId);
        //checkForResponse();

        while (true) {
            sendQuery(messageId);
            checkForResponse();
            messageId++;
            Thread.sleep(500);
        }
    }

    public void init() throws IOException, ExecutionException, InterruptedException {
        clientChannel = AsynchronousSocketChannel.open();
        InetSocketAddress hostAddress = new InetSocketAddress("localhost", 3883);
        Future connectResult = clientChannel.connect(hostAddress);
        connectResult.get();
    }

    public void sendQuery(int messageId) {
        try {
            if (clientChannel != null && clientChannel.isOpen()) {
                String message = "Message with messsage Id: " + messageId;
                byte[] messageByteArray = message.getBytes();
                ByteBuffer buffer = ByteBuffer.wrap(messageByteArray);
                Future writeResult = clientChannel.write(buffer);
                writeResult.get();

                if (writeResult.isDone()) {
                    System.out.println("Send message: " + message);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void checkForResponse() {
        try {
            if (clientChannel != null && clientChannel.isOpen()) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                Future readResult = clientChannel.read(buffer);
                readResult.get();

                if (readResult.isDone()) {
                    String message = new String(buffer.array()).trim();
                    System.out.println("Received response: " + message);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
