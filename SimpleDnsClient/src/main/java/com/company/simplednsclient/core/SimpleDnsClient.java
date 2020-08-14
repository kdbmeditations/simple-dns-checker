package com.company.simplednsclient.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

public class SimpleDnsClient {

    public SimpleDnsClient() { }

    public void run() throws IOException, InterruptedException {
        InetSocketAddress serverAddress = new InetSocketAddress("localhost",3883);
        DatagramChannel client = DatagramChannel.open();
        client.socket().bind(null);
        client.configureBlocking(false);

        System.out.println("Client Started: PORT - " + client.socket().getLocalPort());

        Selector selector = Selector.open();
        client.register(selector, SelectionKey.OP_READ);

        String msg = "Hello from Client!";
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());

        client.send(buffer, serverAddress);
        buffer.clear();

        for (int i = 0; i < 10; i++) {
            System.out.println("Waiting for response from server");
            if (selector.selectNow() >= 1) {
                Set keys = selector.selectedKeys();

                // Iterate through the Set of keys.
                for(Iterator keyIterator = keys.iterator(); keyIterator.hasNext(); ) {
                    // Get a key from the set, and remove it from the set
                    SelectionKey key = (SelectionKey)keyIterator.next();
                    keyIterator.remove();

                    if (key.isReadable()) {
                        ByteBuffer responseBuffer = ByteBuffer.allocate(1024);
                        client.receive(responseBuffer);
                        responseBuffer.flip();
                        int limits = responseBuffer.limit();
                        byte bytes[] = new byte[limits];
                        responseBuffer.get(bytes, 0, limits);
                        String message = new String(bytes);
                        System.out.println("Client: Server at " + serverAddress + " sent: " + message);
                        responseBuffer.clear();
                    }
                }
            }

            Thread.sleep(100);
        }
    }

    public void check() {

    }
}
