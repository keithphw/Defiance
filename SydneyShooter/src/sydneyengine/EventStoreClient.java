/*
 * EventStoreClient.java
 *
 * Created on 1 December 2007, 21:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine;

import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author CommanderKeith
 */
public class EventStoreClient extends EventStore{
	ClientController controller;
	
	@Override
	public ClientController getController(){
		return controller;
	}
	public void setController(ClientController controller){
		this.controller = controller;
	}
	@Override
	public void addEventFromReceiver(EventWrapper e, Nexus fromNexus){
		synchronized (newEventsTempParkingMutex){
			newEventsTempParking.add(new WrapperEventHolder(e, false, null));
		}
	}
	
	@Override
	protected void sendNewEvents(ArrayList<WrapperEventHolder> eventsToSend){
		for (WrapperEventHolder e : eventsToSend){
			try {
				getController().getNexus().send(Nexus.EVENT, e.getEvent(), getController().getSSOut());
			} catch (IOException ex) {
				ex.printStackTrace();
				getController().nexusThrewException(getController().getNexus());
			}
		}
	}
	
}
