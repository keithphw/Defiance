/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.network;

public class ByteServerJGN{}
/*
import sydneyengine.*;
import com.captiveimagination.jgn.*;
import com.captiveimagination.jgn.clientserver.*;
import com.captiveimagination.jgn.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.zip.*;

public class ByteServerJGN extends DynamicMessageAdapter implements ByteServer {
	MessageClient client;
	Object receivedBytesMutex = new Object();
	ArrayList<byte[]> receivedBytes;
	public ByteServerJGN(MessageClient client){
		this.client = client;
		receivedBytes = new ArrayList<byte[]>();
		client.addMessageListener(this);
	}
	
	public void sendTCP(byte[] bytes) throws IOException{
		System.out.println(this.getClass().getSimpleName()+": sendTCP, bytes.length == "+bytes.length);
		client.sendMessage(new JGNByteArrayMessage(bytes));
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
		//System.err.println(this.getClass().getSimpleName()+": messageReceived!");
		assert message.getMessageClient() == client;
		synchronized (receivedBytesMutex){
			receivedBytes.add(message.getBytes());
			//System.err.println(this.getClass().getSimpleName()+": messageReceived, adding complete byte array to the list. receivedBytes.size() == "+receivedBytes.size());//+", count == "+count);
		}
	}
}*/