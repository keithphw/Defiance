/*
 * Nexus.java
 *
 * Created on 15 November 2007, 13:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine;

import sydneyengine.shooter.Player;
import java.util.*;
import java.io.*;
import sydneyengine.network.*;
import sydneyengine.superserializable.*;

/**
 *
 * @author CommanderKeith
 */
public class Nexus {
	public final static int WORLD_FOR_CLIENT_JOIN = 1000;
	public final static int ALL_EVENTS_FOR_CLIENT_JOIN = 1001;
	public final static int WORLD_UPDATE = 1002;
	public final static int EVENT = 1003;
	public final static int LATENCY_REQUEST = 1010;
	public final static int LATENCY_RESPONSE = 1011;
	public final static int LATENCY_RESULTS = 1012;
	
	protected static boolean printBytesSentAndRecieved = false;
	
	protected ByteServerOrClient byteAgent;
	protected Object byteAgentMutex = new Object();
	protected Controller controller;
	protected LatencyCalculator latencyCalculator;
	
	// Player is notified when this Nexus closes that it can remove itself from the world.
	protected Player player;
	
	public Sender getSender(){
		if (getController() != null){
			return controller.getSender();
		}
		return null;
	}
	
	
	public Nexus(ByteServerOrClient byteAgent){
		this.byteAgent = byteAgent;
		latencyCalculator = new LatencyCalculator(this);
	}
	
	// Perfectly thread safe when called from different threads at the same time.
	public void send(int messageType, Object object, SSObjectOutputStream ssOutputStream) throws IOException{
		byte[] bytes = null;
		bytes = makeBytes(messageType, object, ssOutputStream);
		if (isPrintBytesSentAndRecieved()){
			System.out.println(this.getClass().getSimpleName()+".send() bytes.length: "+bytes.length);
		}
		if (getSender() == null){
			synchronized(byteAgentMutex){
				// since there's no sender, just send straight away
				getByteServerOrClient().sendTCP(bytes);
			}
		}
		if (getSender() != null){
			// Check if the sender thread is started since otherwise queueSend will not do anything until the thread is started.
			assert getSender().isAlive() : getSender().isAlive();
			getSender().queueSend(this, bytes);
		}
	}
	
	protected byte[] makeBytes(int messageType, Object object, SSObjectOutputStream ssoutStreamToUse) throws IOException{
		// Don't need to synchronize this method so long as it is synchronized by the send method.
		// Note that the AWT Event thread and the Game loop thread can call send so the send methos must be synchronized.
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ssoutStreamToUse.setOutputStream(bout);
		ssoutStreamToUse.writeInt(messageType);
		ssoutStreamToUse.writeObject(object);
		ssoutStreamToUse.writeDone();
		byte[] bytes = bout.toByteArray();
		ssoutStreamToUse.setOutputStream(null);
		return bytes;
	}
	
	// not thread safe when called from different threads at the same time.
	public MessagePack recieve() throws IOException{
		byte[] bytes = null;
		bytes = byteAgent.recieveTCP();
		if (bytes == null){
			return null;
		}
		if (isPrintBytesSentAndRecieved()){
			System.out.println(this.getClass().getSimpleName()+".recieve() bytes.length: "+bytes.length);
		}
		if (bytes.length == 0){
			System.err.println(this.getClass().getSimpleName()+" bytes.length == 0... ?!");
			return null;
		}
		ByteArrayInputStream abin = new ByteArrayInputStream(bytes);
		int messageType = (new DataInputStream(abin)).readInt();
		
		MessagePack messagePack = new MessagePack(messageType, null, abin);
		
		return new MessagePack(messageType, null, abin);	// the first int is the type.
	}
	
	public ByteServerOrClient getByteServerOrClient(){
		return byteAgent;
	}
	public void close() throws IOException{
		if (getPlayer() != null){
			getPlayer().nexusClosing();
		}
		byteAgent.close();
		this.getLatencyCalculator().stopLatencyChecker();
	}
	
	public static boolean isPrintBytesSentAndRecieved() {
		return printBytesSentAndRecieved;
	}
	
	public static void setPrintBytesSentAndRecieved(boolean newPrintBytesSentAndRecieved) {
		printBytesSentAndRecieved = newPrintBytesSentAndRecieved;
	}
	
	public Controller getController() {
		return controller;
	}
	
	public void setController(Controller controller) {
		this.controller = controller;
	}
	
	public LatencyCalculator getLatencyCalculator() {
		return latencyCalculator;
	}

	// ref to player is included just so that player can be removed from the World when this nexus closes (when close() is called).
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
	
}
