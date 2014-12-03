package com.xuyue.nio.EchoServer3;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public interface Handler {
	void execute(Selector selector, SelectionKey sleKey);
}