package com.company.simplednsclient.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class DummyDnsServer {

    public DummyDnsServer() { }

    public void run() throws IOException {
        InetSocketAddress serverAddress = new InetSocketAddress("localhost", 3883);
        DatagramChannel server = DatagramChannel.open();
        server.socket().bind(serverAddress);
        server.configureBlocking(false);

        System.out.println("Server Started: " + serverAddress);

        Selector selector = Selector.open();
        server.register(selector, SelectionKey.OP_READ);

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        for (;;) {
            //If there's a packet available, fetch it:
            if (selector.selectNow() >= 1) {
                Set keys = selector.selectedKeys();

                // Iterate through the Set of keys.
                for(Iterator keyIterator = keys.iterator(); keyIterator.hasNext();) {
                    // Get a key from the set, and remove it from the set
                    SelectionKey key = (SelectionKey)keyIterator.next();
                    keyIterator.remove();

                    if (key.isReadable()) {
                        SocketAddress clientAddress = server.receive(buffer);
                        buffer.flip();
                        int limits = buffer.limit();
                        byte bytes[] = new byte[limits];
                        buffer.get(bytes, 0, limits);
                        String sentMessage = new String(bytes);
                        System.out.println("Server: Client at " + clientAddress + " sent: " + sentMessage);
                        String responseMessage = "Response from Server at: " + serverAddress;
                        ByteBuffer response = ByteBuffer.wrap(responseMessage.getBytes());

                        if (clientAddress != null)
                            server.send(response, clientAddress);

                        buffer.clear();
                    }
                }
            }
        }
    }
}
