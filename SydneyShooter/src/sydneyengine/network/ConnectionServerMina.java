/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sydneyengine.network;

import org.apache.mina.transport.socket.nio.*;
import org.apache.mina.common.*;
import org.apache.mina.filter.*;
import org.apache.mina.filter.codec.*;
import org.apache.mina.filter.codec.textline.*;
import java.net.*;
import java.util.*;

public class ConnectionServerMina extends IoHandlerAdapter implements ConnectionServer{
	IoAcceptor acceptor;
	ConnectionServerListener listener;
	
	public ConnectionServerMina(){
	}
	
	public void bindAndListen(int port) throws java.io.IOException {
		acceptor = new SocketAcceptor();
		SocketAcceptorConfig config = new SocketAcceptorConfig();
		config.getSessionConfig().setTcpNoDelay(true);
		acceptor.getFilterChain().addLast("compressor", new CompressionFilter());
		acceptor.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new ByteArrayCodecFactory()));
		
		//chain.addLast("logger", new LoggingFilter());

		acceptor.bind(new InetSocketAddress(port), this, config);
		System.out.println(this.getClass().getSimpleName()+": Listening...");
	}
	
	public void close(){
		System.out.println(this.getClass().getSimpleName() + ": closing 1");
		acceptor.unbindAll();
		System.out.println(this.getClass().getSimpleName() + ": closing 2");
	}
	
	public void sessionCreated(IoSession session) throws Exception {
    }

    public void sessionOpened(IoSession session) throws Exception {
		System.out.println(this.getClass().getSimpleName()+": sessionOpened!");
		// Make a new ByteServerMina:
		ByteServerMina byteServerImplMina = new ByteServerMina(session);
		session.setAttachment(byteServerImplMina);
		// Then let the ByteServerMina do its thing
		byteServerImplMina.sessionOpened(session);
		getConnectionServerListener().connectionMade(byteServerImplMina);
    }

	public void messageReceived(IoSession session, Object message) throws Exception {
		// pass on the method call:
		((ByteServerMina)session.getAttachment()).messageReceived(session, message);
	}
	
	public void sessionClosed(IoSession session) throws Exception {
		// pass on the method call:
		((ByteServerMina)session.getAttachment()).sessionClosed(session);
	}
	
	public void exceptionCaught(IoSession session, Throwable cause) {
		// pass on the method call:
		((ByteServerMina)session.getAttachment()).exceptionCaught(session, cause);
	}

	public ConnectionServerListener getConnectionServerListener() {
		return listener;
	}

	public void setConnectionServerListener(ConnectionServerListener listener) {
		this.listener = listener;
	}
/*
	public static void main(String[] args){
		try{
			int thePort = 4100;
			ConnectionServerMina m = new ConnectionServerMina();
			m.bindAndListen(thePort);
			ByteClientMina c = new ByteClientMina();
			c.connect(new InetSocketAddress(InetAddress.getLocalHost(), thePort));
			c.sendTCP("hi!".getBytes());
			try{Thread.sleep(1000);}catch(InterruptedException e){}
			c.close();
			m.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.exit(0);
	}
*/	
}
