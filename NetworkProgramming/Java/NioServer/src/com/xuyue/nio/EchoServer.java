package com.xuyue.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class EchoServer {

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

				try {
					if (key.isAcceptable()) {
						System.out.println("----is acceptable...");
						ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
								.channel();
						SocketChannel socketChannel = serverSocketChannel
								.accept();
						socketChannel.configureBlocking(false);
						SelectionKey selectionKey = socketChannel.register(
								selector, SelectionKey.OP_READ
										| SelectionKey.OP_WRITE);
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						selectionKey.attach(buffer);
					} else if (key.isReadable()) {
						System.out.println("----is readable...");
						SocketChannel socketChannel = (SocketChannel) key
								.channel();
						ByteBuffer buffer = (ByteBuffer) key.attachment();
						socketChannel.read(buffer);
						// System.out.println(buffer.toString());
						// socketChannel.write(buffer);
					} else if (key.isWritable()) {
						System.out.println("----is writable...");
						SocketChannel socketChannel = (SocketChannel) key
								.channel();
						ByteBuffer buffer = (ByteBuffer) key.attachment();
						buffer.flip();
						socketChannel.write(buffer);
						buffer.compact();
					}
				} catch (Exception e) {
					key.cancel();
					try {
						key.channel().close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		}
	}

	private static void init(Selector selector) throws IOException {
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		ServerSocket serverSocket = serverSocketChannel.socket();
		serverSocket.bind(new InetSocketAddress(DEFAULT_PORT));
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
	}
}