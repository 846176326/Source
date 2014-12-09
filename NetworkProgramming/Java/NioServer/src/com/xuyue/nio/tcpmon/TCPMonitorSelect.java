package com.xuyue.nio.tcpmon;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * <p>TCP閻╂垼顫嬮崳顭掔礉鐎瑰啫婀張顒�婀撮惄鎴濇儔缁旑垰褰涢敍灞界殺鐠囬攱鐪版潪顒�褰傞崚鎷岀箼缁旑垱婀囬崝鈥虫珤閿涘苯鑻熺亸鍡氼嚞濮瑰倽绻戦崶鐐拷鍌氭嫲TCPMonitor閸旂喕鍏樻稉锟介弽鍑ょ礉
 * 娑撳秴鎮撻惃鍕ЦTCPMonitorSelect閻€劎娈戦弰顖炴姜闂冭顢O閿涘本澧嶆禒銉ョ暊閻ㄥ嫭锟窖嗗厴閻炲棜顔戞稉濠佺窗妤傛ü绔存禍娑栵拷锟�</p>
 * 
 * <p><code>java TCPMonitorSelect -a :8080 localhost:8000</code><br> 閻╂垵鎯夐張顒�婀撮惃锟�8080缁旑垰褰涢敍灞界殺鐠囬攱鐪版潪顒�褰傜紒娆愭拱閺堣櫣娈�8000缁旑垰褰涢敍灞借嫙鐏忓棙澧嶉張澶庮嚞濮瑰倸鎼锋惔鏃囩翻閸戝搫鍩岄弽鍥у櫙闁挎瑨顕ゆ潏鎾冲毉閵嗭拷</p>
 * 
 * <p><code>java TCPMonitorSelect :8080 work:22</code><br> 閻╂垵鎯夐張顒�婀撮惃锟�8080缁旑垰褰涢敍灞借嫙鐏忓棜顕Ч鍌濇祮閸欐垹绮皐ork娑撶粯婧�閻拷22缁旑垰褰涢妴锟�</p>
 * 
 * @author marlonyao<yaolei135@gmail.com>
 */
public class TCPMonitorSelect {
	private static final int BUFLEN = 1024;
	
	public static void main(String[] args) throws IOException {
		Options options = null;
		try {
			options = parseArgs(args);
			if (options.help) {
				usage();
				System.exit(0);
			}
		} catch (Exception e) {
			usage();
			System.exit(-1);
		}
		
		Selector selector = Selector.open();
		// initial server, start accept connections
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		ServerSocket serverSocket = serverChannel.socket();
		serverChannel.socket().bind(new InetSocketAddress(options.host, options.port));
		System.out.println("server started on " + serverSocket.getLocalSocketAddress());
		SelectionKey serverKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		serverKey.attach(new ServerHandler(selector, serverChannel,
				options.remoteHost, options.remotePort,
				options.dumpRequest, options.dumpResponse));
		
		while (true) {
			selector.select();
			
			Set<SelectionKey> keys = selector.selectedKeys();
			for (Iterator<SelectionKey> itor = keys.iterator(); itor.hasNext();) {
				SelectionKey key = itor.next();
				Handler handler = (Handler) key.attachment();
				handler.execute(key);
			}
			keys.clear();
		}
	}

	static class Options {
		boolean help;
		boolean dumpRequest;
		boolean dumpResponse;

		String host = "localhost";
		int port;
		String remoteHost;
		int remotePort;
	}

	private static void usage() {
		System.err.print(
				"java TCPMonitorSelect [options] [host]:port remote_host:remote_port\n" +
				"        -h, --help             print this help\n" +
				"        -r, --dump-request     dump request to stderr\n" +
				"        -s, --dump-response    dump response to stderr\n" +
				"        -a, --dump-all         dump request and response to stderr\n"
		);
	}	

	private static Options parseArgs(String[] args) {
		Options options = new Options();
		int i;
		// parse options
		for (i = 0; i < args.length; i++) {
			if (!args[i].startsWith("-")) {
				break;
			}
			if (args[i].equals("-h") || args[i].equals("--help")) {
				options.help = true;
			} else if (args[i].equals("-r") || args[i].equals("--dump-request")) {
				options.dumpRequest = true;
			} else if (args[i].equals("-s") || args[i].equals("--dump-response")) {
				options.dumpResponse = true;
			} else if (args[i].equals("-a") || args[i].equals("--dump-all")) {
				options.dumpRequest = true;
				options.dumpResponse = true;
			} else {
				throw new RuntimeException("unknown option '" + args[i] + "'");
			}
		}
		// parse remainder
		String localPart = args[i++];
		String[] bits = localPart.split(":");
		if (bits[0].length() > 0) {
			options.host = bits[0];
		}
		options.port = Integer.parseInt(bits[1]);

		String remotePart = args[i++];
		bits = remotePart.split(":");
		options.remoteHost = bits[0];
		options.remotePort = Integer.parseInt(bits[1]);

		return options;
	}
	
	interface Handler {
		void execute(SelectionKey key);
	}
	
	/*
	 * process accept request.
	 */
	static class ServerHandler implements Handler {
		private ServerSocketChannel serverChannel;
		private Selector selector;
		private String remoteHost;
		private int remotePort;
		private boolean dumpRequest;
		private boolean dumpResponse;

		public ServerHandler(Selector selector, ServerSocketChannel serverChannel,
				String remoteHost, int remotePort, boolean dumpRequest, boolean dumpResponse) {
			this.selector = selector;
			this.serverChannel = serverChannel;
			this.remoteHost = remoteHost;
			this.remotePort = remotePort;
			this.dumpRequest = dumpRequest;
			this.dumpResponse = dumpResponse;
		}
		
		public void execute(SelectionKey key) {
			SocketChannel lsockChannel = null;
			try {
				lsockChannel = serverChannel.accept();
				System.out.println("accept connection: " + lsockChannel.socket().getRemoteSocketAddress());
				System.out.flush();
			} catch (IOException e) {
				System.err.println("fail to accept connection");
				e.printStackTrace();
				return;
			}
			
			// start client handler
			ClientHandler handler = new ClientHandler(selector, lsockChannel, 
					remoteHost, remotePort, dumpRequest, dumpResponse);
			// start connect to remote host
			handler.startConnect();
		}
	}
	
	/*
	 * process loop: read lsock -> write rsock -> read rsock -> write lsock
	 */
	static class ClientHandler implements Handler {
		private Selector selector;
		private String remoteHost;
		private int remotePort;
		private SocketChannel lsockChannel;
		private SocketChannel rsockChannel;
		private SelectionKey lsockKey;
		private SelectionKey rsockKey;
		private ByteBuffer lrBuffer;	// buffer between read lsock and write rsock
		private ByteBuffer rlBuffer;	// buffer between read rsock and write lsock
		
		private boolean dumpRequest;
		private boolean dumpResponse;
		
		public ClientHandler(Selector selector, SocketChannel lsockChannel, 
				String remoteHost, int remotePort, boolean dumpRequest, boolean dumpResponse) {
			this.selector = selector;
			this.lsockChannel = lsockChannel;
			this.remoteHost = remoteHost;
			this.remotePort = remotePort;
			this.lrBuffer = ByteBuffer.allocate(BUFLEN);
			this.rlBuffer = ByteBuffer.allocate(BUFLEN);
			
			this.dumpRequest = dumpRequest;
			this.dumpResponse = dumpResponse;
		}
		
		public void startConnect() {
			try {
				// connect rsock key
				rsockChannel = SocketChannel.open();
				rsockChannel.configureBlocking(false);
				rsockChannel.connect(new InetSocketAddress(remoteHost, remotePort));
				rsockKey = rsockChannel.register(selector, SelectionKey.OP_CONNECT);
				rsockKey.attach(this);
			} catch (IOException e) {
				e.printStackTrace();
				cancel();
			}
		}
		
		public void execute(SelectionKey key) {
			if (!key.isValid())
				return;
			try {
				if (key.isReadable()) {
					if (key == lsockKey) {
						readLSock();
					} else {
						readRSock();
					}
				} else if (key.isWritable()) {
					if (key == lsockKey) {
						writeLSock();
					} else {
						writeRSock();
					}
				} else if (key.isConnectable()) {
					connectRSock();
				}
			} catch (IOException e) {
				e.printStackTrace();
				cancel();
			}
		}
		
		public void cancel() {
			if (lsockKey != null) {
				lsockKey.cancel();
				try { lsockKey.channel().close(); } catch (IOException ioe) {}
			}
			if (rsockKey != null) {
				rsockKey.cancel();
				try { rsockKey.channel().close(); } catch (IOException ioe) {}
			}
		}
		
		private void readLSock() throws IOException {
			int n = lsockChannel.read(lrBuffer);
			if (n == -1) {
				lsockKey.interestOps(0);
				rsockChannel.socket().shutdownOutput();
			} else {
				if (dumpRequest) {
					System.err.print(new String(lrBuffer.array(), 0, n));
				}
				lrBuffer.flip();
				lsockKey.interestOps(0);
				rsockKey.interestOps(SelectionKey.OP_WRITE);
			}
		}
		
		private void writeRSock() throws IOException {
			/*int n = */rsockChannel.write(lrBuffer);
			if (lrBuffer.remaining() == 0) {
				lrBuffer.clear();		// write finished
				rsockKey.interestOps(SelectionKey.OP_READ);
				lsockKey.interestOps(SelectionKey.OP_READ);
			}
		}
		
		private void readRSock() throws IOException {
			int n = rsockChannel.read(rlBuffer);

			if (n == -1) {
				System.out.println("close connection: " + lsockChannel.socket().getRemoteSocketAddress());
				System.out.flush();
				
				rsockKey.cancel();
				rsockChannel.close();
				lsockKey.interestOps(0);
				lsockKey.cancel();
				lsockChannel.close();
			} else {
				rlBuffer.flip();
				rsockKey.interestOps(0);
				lsockKey.interestOps(SelectionKey.OP_WRITE);
			}
		}
		
		private void writeLSock() throws IOException {
			int n = lsockChannel.write(rlBuffer);
			if (dumpResponse) {
				System.err.print(new String(rlBuffer.array(), rlBuffer.position()-n, n));
			}
			if (rlBuffer.remaining() == 0) {
				rlBuffer.clear();		// write finished
				
				lsockKey.interestOps(SelectionKey.OP_READ);
				rsockKey.interestOps(SelectionKey.OP_READ);
			}
		}
		
		private void connectRSock() throws IOException {
			rsockChannel.finishConnect();
			
			lsockChannel.configureBlocking(false);
			lsockKey = lsockChannel.register(selector, SelectionKey.OP_READ);
			lsockKey.attach(this);
			
			rsockKey.interestOps(SelectionKey.OP_READ);
		}
	}
}
