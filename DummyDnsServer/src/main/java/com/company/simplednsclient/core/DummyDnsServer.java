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

        for (;;) {
            processQuery();
        }
    }

    public void init() throws IOException, ExecutionException, InterruptedException {
        serverChannel = AsynchronousServerSocketChannel.open();
        InetSocketAddress hostAddress = new InetSocketAddress("localhost", 3883);
        serverChannel.bind(hostAddress);

        System.out.println("Server channel bound to port: " + hostAddress.getPort());
    }

    public void processQuery() {
        try {
            clientChannel = serverChannel.accept().get();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            clientChannel.read(buffer).get();
            String message = new String(buffer.array()).trim();
            System.out.println("Received message: " + message);
            sendResponse(message);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                clientChannel.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void sendResponse(String message) {
        try {
            String msg = "Acknowledged message: " + message;
            byte[] messageByteArray = msg.getBytes();
            ByteBuffer buffer = ByteBuffer.wrap(messageByteArray);
            clientChannel.write(buffer).get();
            System.out.println("Send message: " + msg);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
