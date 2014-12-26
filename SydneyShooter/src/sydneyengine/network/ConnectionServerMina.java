/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sydneyengine.network;

import java.net.InetSocketAddress;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.compression.CompressionFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class ConnectionServerMina extends IoHandlerAdapter implements ConnectionServer{
	SocketAcceptor acceptor;
	ConnectionServerListener listener;
	
	public ConnectionServerMina(){
	}
	
	@Override
	public void bindAndListen(int port) throws java.io.IOException {
		acceptor = new NioSocketAcceptor();
		acceptor.setReuseAddress(true);
		acceptor.getFilterChain().addLast("compressor", new CompressionFilter());
		acceptor.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new ByteArrayCodecFactory()));
		acceptor.getSessionConfig().setTcpNoDelay(true);
		//chain.addLast("logger", new LoggingFilter());

		acceptor.setDefaultLocalAddress(new InetSocketAddress(port));
		acceptor.setHandler(this);
		acceptor.bind();
		
		System.out.println(this.getClass().getSimpleName()+": Listening...");
	}
	
	@Override
	public void close(){
		System.out.println(this.getClass().getSimpleName() + ": closing 1");
		acceptor.unbind();
		System.out.println(this.getClass().getSimpleName() + ": closing 2");
	}
	
	@Override
	public void sessionCreated(IoSession session) throws Exception {
    }

    @Override
	public void sessionOpened(IoSession session) throws Exception {
		System.out.println(this.getClass().getSimpleName()+": sessionOpened!");
		// Make a new ByteServerMina:
		ByteServerMina byteServerImplMina = new ByteServerMina(session);
		session.setAttribute("",byteServerImplMina);
		// Then let the ByteServerMina do its thing
		byteServerImplMina.sessionOpened(session);
		getConnectionServerListener().connectionMade(byteServerImplMina);
    }

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		// pass on the method call:
		
		((ByteServerMina)session.getAttribute("")).messageReceived(session, message);
	}
	
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		// pass on the method call:

		((ByteServerMina)session.getAttribute("")).sessionClosed(session);
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) {
		// pass on the method call:

		((ByteServerMina)session.getAttribute("")).exceptionCaught(session, cause);
	}

	@Override
	public ConnectionServerListener getConnectionServerListener() {
		return listener;
	}

	@Override
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
