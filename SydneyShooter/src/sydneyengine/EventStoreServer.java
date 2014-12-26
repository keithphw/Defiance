/*
 * EventStoreServer.java
 *
 * Created on 24 November 2007, 01:19
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine;

import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Nastia
 */
public class EventStoreServer extends EventStore{
	ServingController controller;
	
	@Override
	public ServingController getController(){
		return controller;
	}
	public void setController(ServingController controller){
		this.controller = controller;
	}
	@Override
	public void addEventFromReceiver(EventWrapper e, Nexus fromNexus){
		synchronized (newEventsTempParkingMutex){
			newEventsTempParking.add(new WrapperEventHolder(e, true, fromNexus));
		}
	}
	@Override
	protected void sendNewEvents(ArrayList<WrapperEventHolder> eventsToSend){
		ArrayList<Nexus> copyOfNexuses = getController().getCopyOfNexuses();
		for (int i = 0; i < eventsToSend.size(); i++){
			WrapperEventHolder e = eventsToSend.get(i);
			for (int j = 0; j < copyOfNexuses.size(); j++){
				if (copyOfNexuses.get(j) == e.getNexusToNotSendTo()){
					continue;
				}
				try {
					copyOfNexuses.get(j).send(Nexus.EVENT, e.getEvent(), getController().getSSOut());
				} catch (IOException ex) {
					ex.printStackTrace();
					getController().nexusThrewException(copyOfNexuses.get(j));
				}
			}
		}
	}
}
