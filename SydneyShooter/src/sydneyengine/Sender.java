/*
 * Sender.java
 *
 * Created on 15 November 2007, 14:02
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine;

import java.io.IOException;
import java.util.ArrayList;


/**
 * Sender is a thread that queues and sends byte arrays in a thread-safe manner. 
 * 
 * 
 * @author Keith Woodward
 */
public class Sender extends Thread{//, Runnable{
	
	volatile protected boolean shouldRun = true;
	protected Controller controller;
	protected ArrayList<QueuedMessage> messages = new ArrayList<QueuedMessage>(50);
	volatile protected Object messagesMutex = new Object();
	
	/**
	 * Creates a new instance of MessageRelayer
	 */
	public Sender() {
		setName(this.getClass().getSimpleName()+" Thread");
	}
	public Sender(Controller controller) {
		this();
		this.controller = controller;
		controller.setSender(this);
	}
	
	public Controller getController(){
		return controller;
	}
	public void setController(Controller controller){
		this.controller = controller;
	}
	
	@Override
	public void run(){
		ArrayList<QueuedMessage> copyOfMessages = new ArrayList<QueuedMessage>(50);
		while(shouldRun){
			// wait until there's some messages to send'
			synchronized(messagesMutex){
				if (messages.size() == 0){
					try {
						messagesMutex.wait();
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
					if (shouldRun == false){
						break;
					}
				}
				// make a copy of the list of messages so we can deal with them outside of the synchronized block
				copyOfMessages.addAll(messages);
				messages.clear();
			}
			
			// send the messages
			for (QueuedMessage message : copyOfMessages){
				try {
					message.getNexus().getByteServerOrClient().sendTCP(message.getBytes());
				} catch (IOException ex) {
					ex.printStackTrace();
					System.err.println(this.getClass().getSimpleName()+": removing a nexus since it threw an error. message.getNexus().toString() == "+message.getNexus().toString());
					getController().nexusThrewException(message.getNexus());
				}
			}
			copyOfMessages.clear();
		}
	}
	public void queueSend(Nexus nexus, byte[] bytes){
		QueuedMessage message = new QueuedMessage(nexus, bytes);
		addQueuedMessage(message);
	}
	protected void addQueuedMessage(QueuedMessage message){
		synchronized(messagesMutex){
			messages.add(message);
			messagesMutex.notifyAll();
		}
	}
	
	public class QueuedMessage{
		Nexus nexus;
		byte[] bytes;
		public QueuedMessage(Nexus nexus, byte[] bytes){
			this.nexus = nexus;
			this.bytes = bytes;
		}
		public Nexus getNexus(){
			return nexus;
		}
		public byte[] getBytes(){
			return bytes;
		}
	}
	public void close(){
		shouldRun = false;
		synchronized(messagesMutex){
			messagesMutex.notifyAll();
		}
	}
}
