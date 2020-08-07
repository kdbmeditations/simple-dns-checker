package com.company.simplednsclient.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DummyDnsServer {
    private AsynchronousServerSocketChannel serverChannel;
    private AsynchronousSocketChannel clientChannel;

    public DummyDnsServer() { }

    public void run() throws InterruptedException, ExecutionException, IOException {
        init();

        while (true) {
            checkForQuery();
            Thread.sleep(500);
        }
    }

    public void init() throws IOException, ExecutionException, InterruptedException {
        serverChannel = AsynchronousServerSocketChannel.open();
        InetSocketAddress hostAddress = new InetSocketAddress("localhost", 3883);
        serverChannel.bind(hostAddress);

        Future acceptResult = serverChannel.accept();
        clientChannel = (AsynchronousSocketChannel) acceptResult.get();
    }

    public void checkForQuery() {
        try {
            if (clientChannel != null && clientChannel.isOpen()) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                Future readResult = clientChannel.read(buffer);
                readResult.get();

                if (readResult.isDone()) {
                    String message = new String(buffer.array()).trim();
                    System.out.println("Received message: " + message);
                    sendResponse(message);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void sendResponse(String message) {
        try {
            if (clientChannel != null && clientChannel.isOpen()) {
                String msg = "Acknowledged message: " + message;
                byte[] messageByteArray = msg.getBytes();
                ByteBuffer buffer = ByteBuffer.wrap(messageByteArray);
                Future writeResult = clientChannel.write(buffer);
                writeResult.get();

                if (writeResult.isDone()) {
                    System.out.println("Send message: " + msg);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
