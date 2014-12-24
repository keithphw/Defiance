/*
 * SenderLagSimulator.java
 *
 * Created on 15 November 2007, 14:05
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine;

import java.util.*;
import java.io.*;

/**
 * Delays sending messages for a random time between min and max lag nanos, but gaurantees order.  Note that if the lag from server to client is not the same as from client to server (ie lag is asymetrical), then there will be problem with clock-sync'ing the client's clock to the server's.
 * @author Keith
 */

public class SenderLagSimulator extends Sender{
	
	// delays sending messages for a random time between min and max lag nanos, but gaurantees order.
	protected long minLagNanos = 0;	//1000000000L == 1 second;
	protected long maxLagNanos = 0;	//1000000000L == 1 second;
	protected int sleepIncrementBeforeCheckForSufficientLag = 1;
	/**
	 * Creates a new instance of SenderLagSimulator
	 */
	public SenderLagSimulator() {
		setName(this.getClass().getSimpleName()+" Thread");
	}
	public SenderLagSimulator(Controller controller) {
		this();
		this.controller = controller;
		controller.setSender(this);
	}
	public void setMinLagNanos(long minLagNanos){
		this.minLagNanos = minLagNanos;
	}
	public void setMaxLagNanos(long maxLagNanos){
		this.maxLagNanos = maxLagNanos;
	}
	public long getMinLagNanos(){
		return minLagNanos;
	}
	public long getMaxLagNanos(){
		return maxLagNanos;
	}
	
	public void run(){
		ArrayList<LaggedQueuedMessage> copyOfMessages = new ArrayList<LaggedQueuedMessage>(50);
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
				//copyOfMessages.addAll(messages);
				for (QueuedMessage queuedMessage : messages){
					copyOfMessages.add((SenderLagSimulator.LaggedQueuedMessage)queuedMessage);
				}
				messages.clear();
			}
			
			// send the messages
			for (LaggedQueuedMessage message : copyOfMessages){
				while (message.getNanoTime() + message.getLagNanos() > MockSystem.nanoTime()){
					try { Thread.sleep(sleepIncrementBeforeCheckForSufficientLag); } catch (InterruptedException ex) {ex.printStackTrace();}
				}
				try {
					message.getNexus().getByteServerOrClient().sendTCP(message.getBytes());
				} catch (Exception ex){//IOException ex) {
					// if the player has already thrown an exception and been removed, then don't bother throwing another exception etc.
					//if (getController().getWorld().getPlayers().contains(message.getNexus().getPlayer())){
					ex.printStackTrace();
					System.err.println(this.getClass().getSimpleName()+": removing a nexus since it threw an error. message.getNexus().toString() == "+message.getNexus().toString());
					getController().nexusThrewException(message.getNexus());
				}
			}
			copyOfMessages.clear();
		}
	}
	public void queueSend(Nexus nexus, byte[] bytes){
		QueuedMessage message = new LaggedQueuedMessage(nexus, bytes, MockSystem.nanoTime());
		addQueuedMessage(message);
	}
	
	public class LaggedQueuedMessage extends Sender.QueuedMessage{
		long nanoTime;
		long lagNanos = 0;
		public LaggedQueuedMessage(Nexus nexus, byte[] bytes, long nanoTime){
			super(nexus, bytes);
			this.nanoTime = nanoTime;
			lagNanos = (long)(minLagNanos+(Math.random()*(maxLagNanos-minLagNanos)));
		}
		public long getNanoTime(){
			return nanoTime;
		}
		public long getLagNanos(){
			return lagNanos;
		}
	}
}
