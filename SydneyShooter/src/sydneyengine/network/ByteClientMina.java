package sydneyengine.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.compression.CompressionFilter;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public class ByteClientMina extends IoHandlerAdapter implements ByteClient {

	protected SocketConnector connector;
	protected IoSession refToIoSession;
	Object receivedBytesMutex = new Object();
	ArrayList<byte[]> receivedBytes;
	//Throwable exceptionThrown = null;
	

	public ByteClientMina() {
		receivedBytes = new ArrayList<byte[]>();
		System.out.println("ByteClientMina(): new ByteClientMina object constructed.");
	}

	@Override
	public void connect(InetSocketAddress address) throws java.io.IOException {
		if (refToIoSession != null && refToIoSession.isConnected()) {
			throw new java.io.IOException("Already connected. Disconnect first.");
		}
		connector = new NioSocketConnector();
		
		//connector.getFilterChain().addLast("logger", new LoggingFilter()); // to see this in action
		connector.getFilterChain().addLast("compressor", new CompressionFilter());
		connector.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new ByteArrayCodecFactory()));
		
		connector.getSessionConfig().setTcpNoDelay(true); // sends message as soon as possible, don't wait for accumulation
		connector.setHandler(this);
		connector.setDefaultRemoteAddress(address);
		ConnectFuture future1 = connector.connect(address);
		
		
		if (!future1.isConnected()) {
			throw new java.io.IOException("ByteClientMina.connect(): Connection failed...");
		}
		refToIoSession = future1.getSession();
		
		/*
		NioDatagramConnector connector = new NioDatagramConnector();
		//connector.setHandler(this);
		ConnectFuture connFuture = connector.connect( new InetSocketAddress("localhost", MemoryMonitor.PORT ));
		*/
	}

	@Override
	public void sendTCP(byte[] bytes) throws IOException {
		if (isConnected() == false){
			throw new IOException("IoSession not connected!");
		}
		//System.out.println(this.getClass().getSimpleName()+": sendTCP, bytes.length == "+bytes.length);
		this.refToIoSession.write(bytes);
	}

	@Override
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

	@Override
	public void close() throws IOException {
		System.out.println(this.getClass().getSimpleName() + ": closing 1");
		if (refToIoSession != null) {
			refToIoSession.close(true);
		}
		System.out.println(this.getClass().getSimpleName() + ": closing 2");
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		super.sessionCreated(session);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		System.out.println(this.getClass().getSimpleName() + ": sessionOpened!");
	}
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		synchronized (receivedBytesMutex) {
			receivedBytes.add((byte[])message);
			//System.err.println(this.getClass().getSimpleName() + ": messageReceived, adding complete byte array to the list. (byte[])message.length == "+((byte[])message).length+", receivedBytes.size() == " + receivedBytes.size());//+", count == "+count);
		}
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		session.close(true);
		try{
			close();
		}catch(IOException e){e.printStackTrace();}
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
		//SessionLog.error(session, "", cause);
		// Close connection when unexpected exception is caught.
		cause.printStackTrace();
		//exceptionThrown = cause;
		session.close(true);
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

	@Override
	public void sendUDP(byte[] bytes) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] recieveUDP() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
