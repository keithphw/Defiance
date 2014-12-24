/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.network;

public class ConnectionServerJGN{}
/*
import sydneyengine.*;
import java.net.*;
import java.io.*;
import com.captiveimagination.jgn.*;
import com.captiveimagination.jgn.clientserver.*;
import com.captiveimagination.jgn.event.*;

public class ConnectionServerJGN extends DynamicMessageAdapter implements ConnectionServer, com.captiveimagination.jgn.event.ConnectionListener{

	JGNServer server;
	ConnectionServerListener listener;
	
	public ConnectionServerJGN(){//ConnectionWelcomer connectionWelcomer){
		//this.connectionWelcomer = connectionWelcomer;
	}
	
	public void bindAndListen(int tcpPort) throws java.io.IOException {
		JGN.register(JGNByteArrayMessage.class);
		
		InetSocketAddress reliableAddress = new InetSocketAddress(InetAddress.getLocalHost(), tcpPort);
		InetSocketAddress fastAddress = null;//new InetSocketAddress(InetAddress.getLocalHost(), udpPort);
		server = new JGNServer(reliableAddress, fastAddress);
		server.getReliableServer().addConnectionListener(this);
		//server.getFastServer().addConnectionListener(this);
		JGN.createThread(server).start();

		System.out.println(this.getClass().getSimpleName()+": Listening...");
	}
	public void connected(MessageClient client) {
		System.out.println(this.getClass().getSimpleName()+": connected!");
		// Make a new ByteServerJGN:
		ByteServerJGN byteServerImpl = new ByteServerJGN(client);
		getConnectionServerListener().connectionMade(byteServerImpl);
	}

	public void negotiationComplete(MessageClient client) {
	}

	public void disconnected(MessageClient client) {
		System.out.println(this.getClass().getSimpleName()+": disconnected!");
	}
	public void kicked(MessageClient client, String reason) {// TODO this should be managed internally
	}

	public ConnectionServerListener getConnectionServerListener() {
		return listener;
	}

	public void setConnectionServerListener(ConnectionServerListener listener) {
		this.listener = listener;
	}
	public void close(){
		System.out.println(this.getClass().getSimpleName() + ": closing 1");
		if (server != null){
			try{
			server.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		System.out.println(this.getClass().getSimpleName() + ": closing 2");
	}
	

}*/
