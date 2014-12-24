
/*
 * ReceiverPollingServer.java
 *
 * Created on 4 March 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine;

import sydneyengine.superserializable.*;
import java.util.*;
import java.io.*;


/**
 * <p>ReceiverPollingServer is a thread typically used on the server (ServingController) that continuously polls the Nexus's for messages by calling recieve() on them.
 * Clients generally don't need them since they do not need to relay messages instantly to minimise latency, unlike the server.
 * Clients can afford to wait until the next frame and call nexus.receive directly from the ClientController thread.</p>
 * <p>If a non-null message is recieved, then getRelayerHandler().handleMessage(messagePack, fromPlayer, allPlayers) is called.
 * This method serves two purposes - if it returns true then the message should be processed by the Nexus
 * so it is added to the nexus's message queue using Nexus.enqueue().
 * The handleMessage method can also deal with the message in whatever way it likes, for example
 * it can relay the message to all other clients (but return true since the message may still need to be dealt with), or if the message's type is
 * GameConstants.LATENCY_REQUEST then the relayerHandler can instantly send back a
 * latency response (and return false since there's no reason why the Nexus needs to know about that message).</p>
 * 
 * 
 * 
 * 
 * @author Keith
 */
public class ReceiverPollingServer extends ReceiverPolling {
	
	ServingController controller;

	/**
	 * Creates a new instance of ReceiverPollingServer
	 */
	public ReceiverPollingServer(ServingController controller) {
		this.controller = controller;
		setName(this.getClass().getSimpleName()+" Thread");
		try {
			tempSSIn = new SSObjectInputStream();
			tempSSOut = new SSObjectOutputStream();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	public void run(){
		OuterLoop: while(shouldRun){
			ArrayList<Nexus> copyOfNexuses = controller.getCopyOfNexuses();
			//System.out.println(this.getClass().getSimpleName()+": controller == "+controller+", copyOfNexuses.size() == "+copyOfNexuses.size());
			for (int i = 0; i < copyOfNexuses.size(); i++){
				Nexus nexus = copyOfNexuses.get(i);
				assert nexus.getPlayer() != null;
				//assert nexus.getPlayer().getWorld() != null;
				MessagePack messagePack = null;
				try{
					messagePack = (MessagePack)nexus.recieve();
				}catch(IOException e){
					e.printStackTrace();
					controller.nexusThrewException(nexus);
					continue;
				}
				if (messagePack == null){
					continue;
				}
				try{
					// this should be done in the controller thread during the world update.
					// It should be de-serialised at thr event's timeStamp (so need to find out the timeStamp before de-serialization somehow).
					messagePack.constructObject(getTempSSIn());
				}catch(IOException e){
					e.printStackTrace();
					controller.nexusThrewException(nexus);
					continue;
				}
				//System.out.println(this.getClass().getSimpleName()+": got a message from nexus "+i);
				// deal with the messagePack here, eg forward it clients
				boolean addToRecievedMessages = true;
				try{
					// the below code lets RelayerHandler filter out the receivedEvents that
					// the Nexus's do not need to know about, such as LatencyRecords which are
					// just passed between LatencyCalculaters
					addToRecievedMessages = handleMessage(messagePack, nexus, copyOfNexuses);
				}catch(IOException e){
					e.printStackTrace();
					controller.nexusThrewException(nexus);
					continue;
				}
				if (addToRecievedMessages){
					//System.out.println(this.getClass().getSimpleName()+": copyOfNexuses.size() == "+copyOfNexuses.size()+", numJoinEventsReceivedThruNexuses == "+numJoinEventsReceivedThruNexuses);
					// First we do addEvent, and it may block until a new Nexus is added.
					assert messagePack.getObject() instanceof EventWrapper;
					EventWrapper wrapperEvent = (EventWrapper)messagePack.getObject();
					assert wrapperEvent.isDeserialized() == false : "Any message that is added to recieved messages should be deserialized in the Controller thread, so it shouldn't yet be deserialized."; 
					//controller.getWorld().getEventStore().addEvent(wrapperEvent, false);
					//System.out.println(this.getClass().getSimpleName()+": about to call addEventFromViewPane, wrapperEvent == "+wrapperEvent);
					controller.getWorld().getEventStore().addEventFromReceiver(wrapperEvent, nexus);

					
					// continue OuterLoop so that a new list of nexuses is got from Controller, because a new one could have been added when addEvent was called above.
					continue OuterLoop;
					//i--;
					//continue;
				}
			}
			doMinSleep();
			
		}
		System.out.println(this.getClass().getSimpleName()+": closed");
	}
	
	
	public boolean handleMessage(MessagePack messagePack, Nexus fromNexus, ArrayList<Nexus> copyOfNexuses) throws IOException{
		// if the below is true, then the message was a latency thing and it was dealt with, so return false.
		if (doLatencyMessage(messagePack, fromNexus) == true){
			return false;
		}
		// Since the message was not to do with latency, it must be an event so return true.
		return true;
	}
	
	public void relayMessage(MessagePack messagePack, Nexus fromNexus, ArrayList<Nexus> copyOfNexuses){
		for(int i = 0; i < copyOfNexuses.size(); i++){
			if (copyOfNexuses.get(i) != fromNexus){
				//System.err.println(this.getClass().getSimpleName()+": sending ((EventWrapper)messagePack.getObject()).getTimeStamp() == "+((EventWrapper)messagePack.getObject()).getTimeStamp()+", to nexus == "+copyOfNexuses.get(i)+", fromNexus == "+fromNexus);
				try{
					copyOfNexuses.get(i).send(Nexus.EVENT, messagePack.getObject(), getTempSSOut());
				}catch(IOException e){
					e.printStackTrace();
					controller.nexusThrewException(copyOfNexuses.get(i));
				}
			}
		}
	}
	public ServingController getController(){
		return controller;
	}
}