package com.xuyue.nio.EchoServer3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ClientHandler implements Handler {
	ByteBuffer buffer = null;

	public ClientHandler() {
		buffer = ByteBuffer.allocate(1024);
	}

	@Override
	public void execute(Selector selector, SelectionKey sleKey) {
		try {
			if (sleKey.isReadable()) {
				readKey(selector, sleKey);
			} else if (sleKey.isWritable()) {
				writeKey(selector, sleKey);
			}
		} catch (IOException e) {
			sleKey.cancel();
			try {
				sleKey.channel().close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	private void readKey(Selector selector, SelectionKey key)
			throws IOException {
		SocketChannel client = (SocketChannel) key.channel();
		int n = client.read(buffer);
		if (n > 0) {
			buffer.flip();
			key.interestOps(SelectionKey.OP_WRITE);
		}
	}

	private void writeKey(Selector selector, SelectionKey key)
			throws IOException {
		SocketChannel client = (SocketChannel) key.channel();
		client.write(buffer);
		if (buffer.remaining() == 0) {
			buffer.clear();
			// buffer.flip();
			key.interestOps(SelectionKey.OP_READ);
		}
	}

}