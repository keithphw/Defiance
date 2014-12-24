package sydneyengine.network;

import org.apache.mina.transport.socket.nio.*;
import org.apache.mina.common.*;
import org.apache.mina.filter.*;
import org.apache.mina.filter.codec.*;
import org.apache.mina.filter.codec.textline.*;

import java.util.*;
import java.io.*;
import java.net.*;

public class ByteClientMina extends IoHandlerAdapter implements ByteClient {

	protected SocketConnector connector;
	protected IoSession refToIoSession;
	Object receivedBytesMutex = new Object();
	ArrayList<byte[]> receivedBytes;
	//Throwable exceptionThrown = null;
	

	public ByteClientMina() {
		receivedBytes = new ArrayList<byte[]>();
	}

	public void connect(InetSocketAddress address) throws java.io.IOException {
		if (refToIoSession != null && refToIoSession.isConnected()) {
			throw new java.io.IOException("Already connected. Disconnect first.");
		}
		connector = new SocketConnector();
		//connector.getFilterChain().addLast("logger", new LoggingFilter());
		connector.getFilterChain().addLast("compressor", new CompressionFilter());
		connector.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new ByteArrayCodecFactory()));
		
		SocketConnectorConfig config = new SocketConnectorConfig();
		config.getSessionConfig().setTcpNoDelay(true);
		ConnectFuture future1 = connector.connect(address, this, config);
		future1.join();
		if (!future1.isConnected()) {
			throw new java.io.IOException("Connection didn't happen...");
		}
		refToIoSession = future1.getSession();
	}

	public void sendTCP(byte[] bytes) throws IOException {
		if (isConnected() == false){
			throw new IOException("IoSession not connected!");
		}
		//System.out.println(this.getClass().getSimpleName()+": sendTCP, bytes.length == "+bytes.length);
		this.refToIoSession.write(bytes);
	}

	public byte[] recieveTCP() throws IOException {
		if (isConnected() == false){
			throw new IOException("IoSession not connected!");
		}
		//System.out.println(this.getClass().getSimpleName()+": refToIoSession.getReadBytes() == "+refToIoSession.getReadBytes());
		synchronized (receivedBytesMutex) {
			if (receivedBytes.size() > 0) {
				return receivedBytes.remove(0);
			}
		}
		return null;
	}
	public boolean isConnected(){
		if (refToIoSession == null){
			return false;
		}
		return refToIoSession.isConnected();
	}

	public void close() throws IOException {
		System.out.println(this.getClass().getSimpleName() + ": closing 1");
		if (refToIoSession != null) {
			refToIoSession.close();
		}
		System.out.println(this.getClass().getSimpleName() + ": closing 2");
	}

	public void sessionCreated(IoSession session) throws Exception {
		super.sessionCreated(session);
	}

	public void sessionOpened(IoSession session) throws Exception {
		System.out.println(this.getClass().getSimpleName() + ": sessionOpened!!!!!!!!!!!!!!!!!!");
	}
	public void messageReceived(IoSession session, Object message) throws Exception {
		synchronized (receivedBytesMutex) {
			receivedBytes.add((byte[])message);
			//System.err.println(this.getClass().getSimpleName() + ": messageReceived, adding complete byte array to the list. (byte[])message.length == "+((byte[])message).length+", receivedBytes.size() == " + receivedBytes.size());//+", count == "+count);
		}
	}

	public void sessionClosed(IoSession session) throws Exception {
		session.close();
		try{
			close();
		}catch(IOException e){e.printStackTrace();}
	}

	public void exceptionCaught(IoSession session, Throwable cause) {
		//SessionLog.error(session, "", cause);
		// Close connection when unexpected exception is caught.
		cause.printStackTrace();
		//exceptionThrown = cause;
		session.close();
		try{
			close();
		}catch(IOException e){e.printStackTrace();}
	}
	
	/**
	 * The IoSession returned is the same one used by this class, so don't use it to do anything at the same time or thread-safety will be violated.
	 * @return
	 */
	public IoSession getRefToIoSession() {
		return refToIoSession;
	}
}
