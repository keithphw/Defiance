/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.network;

public class ByteClientJGN{}

/*
import sydneyengine.*;
import com.captiveimagination.jgn.*;
import com.captiveimagination.jgn.clientserver.*;
import com.captiveimagination.jgn.event.*;
import com.captiveimagination.jgn.message.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.zip.*;

public class ByteClientJGN  extends DynamicMessageAdapter implements ByteClient {
	private JGNClient client;
	//MessageClient client;
	Object receivedBytesMutex = new Object();
	ArrayList<byte[]> receivedBytes;
	public ByteClientJGN(){
		JGN.register(JGNByteArrayMessage.class);
		receivedBytes = new ArrayList<byte[]>();
	}
	
	public void connect(InetSocketAddress reliableServerAddress) throws java.io.IOException {
		InetSocketAddress reliableAddress = new InetSocketAddress(InetAddress.getLocalHost(), 0);
		client = new JGNClient(reliableAddress, null);
		client.addMessageListener(this);
		JGN.createThread(client).start();
		client.connect(reliableServerAddress, null);
	}
	
	public void sendTCP(byte[] bytes) throws IOException{
		//System.out.println(this.getClass().getSimpleName()+": sendTCP, bytes.length == "+bytes.length);
		client.sendToServer(new JGNByteArrayMessage(bytes));
	}
	
	public byte[] recieveTCP() throws IOException{
		synchronized (receivedBytesMutex){
			if (receivedBytes.size() > 0){
				return receivedBytes.remove(0);
			}
		}
		return null;
	}

	public void close() throws IOException {
		System.out.println(this.getClass().getSimpleName() + ": closing 1");
		System.out.println(this.getClass().getSimpleName() + ": closing 2");
	}
	
	public void messageReceived(JGNByteArrayMessage message) {
		//System.err.println(this.getClass().getSimpleName()+": messageReceived! message.getClass() == "+message.getClass());
		//assert message.getMessageClient() == client;
		if (message instanceof JGNByteArrayMessage){
			synchronized (receivedBytesMutex){
				receivedBytes.add(((JGNByteArrayMessage)message).getBytes());
				//System.err.println(this.getClass().getSimpleName()+": messageReceived, adding complete byte array to the list. receivedBytes.size() == "+receivedBytes.size()+", message.getClass() == "+message.getClass());//+", count == "+count);
			}
		}
	}
	
}*/
