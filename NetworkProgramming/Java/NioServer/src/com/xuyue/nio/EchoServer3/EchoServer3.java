package com.xuyue.nio.EchoServer3;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

import com.xuyue.nio.EchoServer3.*;

public class EchoServer3 {

	private static int DEFAULT_PORT = 9000;

	public static void main(String[] args) throws IOException {
		System.out.println("Listening for connection on port " + DEFAULT_PORT);
		Selector selector = Selector.open();
		init(selector);

		while (true) {
			System.out.println("in while...");
			selector.select();

			for (Iterator<SelectionKey> itor = selector.selectedKeys()
					.iterator(); itor.hasNext();) {
				System.out.println("--in for...");
				SelectionKey key = itor.next();
				itor.remove();
				Handler handler = (Handler) key.attachment();
				handler.execute(selector, key);
			}
		}
	}

	private static void init(Selector selector) throws IOException {
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		ServerSocket serverSocket = serverSocketChannel.socket();
		serverSocket.bind(new InetSocketAddress(DEFAULT_PORT));
		serverSocketChannel.configureBlocking(false);
		SelectionKey selectionKey = serverSocketChannel.register(selector,
				SelectionKey.OP_ACCEPT);
		selectionKey.attach(new ServerHandler());
	}
}