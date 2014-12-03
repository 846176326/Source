package com.xuyue.nio.EchoServer3;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerHandler implements Handler {

	public void execute(Selector selector, SelectionKey sleKey) {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) sleKey
				.channel();
		SocketChannel client = null;

		try {
			client = serverSocketChannel.accept();
			System.out.println("Accept connection from " + client);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		SelectionKey key = null;
		try {
			client.configureBlocking(false);
			key = client.register(selector, SelectionKey.OP_READ);
			key.attach(new ClientHandler());
		} catch (IOException e) {
			if (key != null) {
				key.cancel();
			}
			try {
				client.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

	}

}