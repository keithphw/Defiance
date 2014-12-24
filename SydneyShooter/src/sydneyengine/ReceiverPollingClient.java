/*
 * ReceiverPollingClient.java
 *
 * Created on 22 November 2007, 18:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package sydneyengine;

import sydneyengine.superserializable.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author CommanderKeith
 */
public class ReceiverPollingClient extends ReceiverPolling {

	ClientController controller;

	/**
	 * Creates a new instance of ReceiverPollingClient
	 */
	public ReceiverPollingClient(ClientController controller) {
		this.controller = controller;
		setName(this.getClass().getSimpleName() + " Thread");
		try {
			tempSSIn = new SSObjectInputStream();
			tempSSOut = new SSObjectOutputStream();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void run() {
		while (shouldRun) {
			Nexus nexus = controller.getNexus();
			assert nexus.getPlayer() != null;
			assert nexus.getPlayer().getWorld() != null;
			MessagePack messagePack = null;
			try {
				messagePack = (MessagePack) nexus.recieve();
			} catch (IOException e) {
				e.printStackTrace();
				controller.nexusThrewException(nexus);
				continue;
			}
			if (messagePack == null) {
				doMinSleep();
				continue;
			}
			try {
				// this should be done in the controller thread during the world update.
					// It should be de-serialised at thr event's timeStamp (so need to find out the timeStamp before de-serialization somehow).
				messagePack.constructObject(getTempSSIn());
			} catch (IOException e) {
				e.printStackTrace();
				controller.nexusThrewException(nexus);
				continue;
			}
			//System.out.println(this.getClass().getSimpleName()+": got a message from nexus "+i);
				// deal with the messagePack here, eg forward it clients
			boolean addToRecievedMessages = true;
			try {
				// the below code lets RelayerHandler filter out the receivedEvents that
					// the Nexus's do not need to know about, such as LatencyRecords which are
					// just passed between LatencyCalculaters
				addToRecievedMessages = handleMessage(messagePack, nexus);
			} catch (IOException e) {
				e.printStackTrace();
				controller.nexusThrewException(nexus);
				continue;
			}
			if (addToRecievedMessages) {
				//System.out.println(this.getClass().getSimpleName()+": copyOfNexuses.size() == "+copyOfNexuses.size()+", numJoinEventsReceivedThruNexuses == "+numJoinEventsReceivedThruNexuses);
					// First we do addEvent, and it may block until a new Nexus is added.
				EventWrapper wrapperEvent = (EventWrapper) messagePack.getObject();
				assert wrapperEvent.isDeserialized() == false : "Hmm, these events are meant to be initialised with a deserialised boolean of false";
				controller.getWorld().getEventStore().addEventFromReceiver(wrapperEvent, nexus);
				// continue OuterLoop so that a new list of nexuses is got from Controller, because a new one could have been added when addEvent was called above.
				continue;
			}

		}
		System.out.println(this.getClass().getSimpleName() + ": closed");
	}
	
	public boolean handleMessage(MessagePack messagePack, Nexus fromNexus) throws IOException {
		// if the below is true, then the message was a latency thing and it was dealt with, so return false.
		if (doLatencyMessage(messagePack, fromNexus) == true){
			return false;
		}else if (messagePack.getType() == Nexus.WORLD_UPDATE){
			this.getController().setLatestClientWorldUpdate((ClientWorldUpdate)messagePack.getObject());
			return false;
		}
		// Since the message was not to do with latency, it must be an event so return true.
		return true;
	}
	public ClientController getController(){
		return controller;
	}
}