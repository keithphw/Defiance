package sydneyengine.network;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

public class ByteServerMina  extends IoHandlerAdapter implements ByteServer{

	protected IoSession refToIoSession;
	Object receivedBytesMutex = new Object();
	ArrayList<byte[]> receivedBytes;

	public ByteServerMina(IoSession refToIoSessionForSending){
		this.refToIoSession = refToIoSessionForSending;
		receivedBytes = new ArrayList<byte[]>();
	}
	
	@Override
	public void sendTCP(byte[] bytes) throws IOException {
		if (refToIoSession.isConnected() == false){
			throw new IOException("IoSession closed!");
		}
		//System.out.println(this.getClass().getSimpleName()+": sendTCP, bytes.length == "+bytes.length);
		this.refToIoSession.write(bytes);
	}

	@Override
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
		System.out.println(this.getClass().getSimpleName()+": sessionOpened!");
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
