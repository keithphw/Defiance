package sydneyengine.network;

import org.apache.mina.transport.socket.nio.*;
import org.apache.mina.common.*;
import org.apache.mina.filter.*;
import org.apache.mina.filter.codec.*;
import org.apache.mina.filter.codec.textline.*;

import java.util.*;
import java.io.*;
import java.net.*;

public class ByteServerMina  extends IoHandlerAdapter implements ByteServer{

	protected IoSession refToIoSession;
	Object receivedBytesMutex = new Object();
	ArrayList<byte[]> receivedBytes;

	public ByteServerMina(IoSession refToIoSessionForSending){
		this.refToIoSession = refToIoSessionForSending;
		receivedBytes = new ArrayList<byte[]>();
	}
	
	public void sendTCP(byte[] bytes) throws IOException {
		if (refToIoSession.isConnected() == false){
			throw new IOException("IoSession closed!");
		}
		//System.out.println(this.getClass().getSimpleName()+": sendTCP, bytes.length == "+bytes.length);
		this.refToIoSession.write(bytes);
	}

	public byte[] recieveTCP() throws IOException {
		if (refToIoSession.isConnected() == false){
			throw new IOException("IoSession closed!");
		}
		synchronized (receivedBytesMutex) {
			if (receivedBytes.size() > 0) {
				return receivedBytes.remove(0);
			}
		}
		return null;
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
		System.out.println(this.getClass().getSimpleName()+": sessionOpened!!!!!!!!!!!!!!!!!!");
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
