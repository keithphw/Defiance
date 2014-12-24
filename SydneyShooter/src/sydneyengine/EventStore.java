// .VOB, BUP and IFO
// grater
// lights
// telephone
//

/*
 * EventStore.java
 *
 * Created on 14 November 2007, 18:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine;

import java.io.*;
import java.util.*;
import sydneyengine.superserializable.*;

/**
 *
 * @author CommanderKeith
 */
public abstract class EventStore{
	
	// At the very begining 'events' will be empty, then after the first EventWrapper is recieved,
	// there will always be at least one EventWrapper in events, and that will be the latest one.
	protected ArrayListSS<EventWrapper> events = new ArrayListSS<EventWrapper>(300);
	protected double minSecondsToKeepUserEvents = 5f;
	transient ArrayList<WrapperEventHolder> newEventsTempParking = new ArrayList<WrapperEventHolder>();
	protected Object newEventsTempParkingMutex = new Object();
	EventWrapper earliestNewEvent = null;

	
	/**
	 * This class is just for holding the EventWrapper together with a boolean 
	 * which indicates if the event should be sent to the other Computers or not.
	*/		
	public class WrapperEventHolder implements Comparable{
		protected EventWrapper event;
		protected boolean shouldSend;
		protected Nexus nexusToNotSendTo;
		public WrapperEventHolder(EventWrapper event, boolean shouldSend, Nexus nexusToNotSendTo){
			this.event = event;
			this.shouldSend = shouldSend;
			this.nexusToNotSendTo = nexusToNotSendTo;;
		}
		public int compareTo(Object ev){
			EventWrapper e = ((WrapperEventHolder)ev).getEvent();
			EventWrapper thisE = this.getEvent();
			
			if (thisE.getTimeStamp() > e.getTimeStamp()){
				return 1;
			}else if (thisE.getTimeStamp() < e.getTimeStamp()){
				return -1;
			}else{
				return 0;
			}
		}
		public EventWrapper getEvent() {
			return event;
		}
		public boolean shouldSend() {
			return shouldSend;
		}
		public Nexus getNexusToNotSendTo() {
			return nexusToNotSendTo;
		}
	}
	
	public abstract Controller getController();
	protected abstract void sendNewEvents(ArrayList<WrapperEventHolder> copyOfNewEventHoldersTempParking);
	
	public abstract void addEventFromReceiver(EventWrapper e, Nexus fromNexus);
	
	public void addEventFromViewPane(EventWrapper e){
		synchronized (newEventsTempParkingMutex){
			newEventsTempParking.add(new WrapperEventHolder(e, true, null));
		}
	}
	
	/**
	 * Looks at all events added using addEventFromViewPane or addEventFromReceiver 
	 * and serializes and sends them to other computers if necessary. 
	 * Also adds these new events to the events list and re-sorts events to be in timeStamp order.
	 */
	public void processNewEvents(){
		ArrayList<WrapperEventHolder> copyOfNewEventHoldersTempParking = new ArrayList<WrapperEventHolder>(newEventsTempParking.size());
		synchronized (newEventsTempParkingMutex){
			copyOfNewEventHoldersTempParking.addAll(newEventsTempParking);
			newEventsTempParking.clear();
		}
		ArrayList<WrapperEventHolder> eventsToSend = null;
		for (int i = 0; i < copyOfNewEventHoldersTempParking.size(); i++){
			EventWrapper e = copyOfNewEventHoldersTempParking.get(i).getEvent();
			//System.out.println(this.getClass().getSimpleName()+": processNewEvents, e == "+e);
			if (e.isSerialized() == false){
				try {
					e.serialize(getController().getSSOut());
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			if (copyOfNewEventHoldersTempParking.get(i).shouldSend()){
				if (eventsToSend == null){
					eventsToSend = new ArrayList<WrapperEventHolder>();
				}
				eventsToSend.add(copyOfNewEventHoldersTempParking.get(i));
			}
			//System.out.println(this.getClass().getSimpleName()+": copyOfNewEventHoldersTempParking.get(i).getEvent().getClass().getSimpleName() == "+copyOfNewEventHoldersTempParking.get(i).getEvent().getClass().getSimpleName()+", copyOfNewEventHoldersTempParking.get(i).getEvent() == "+copyOfNewEventHoldersTempParking.get(i).getEvent()+", copyOfNewEventHoldersTempParking.size() == "+copyOfNewEventHoldersTempParking.size());
		}
		if (eventsToSend != null){
			sendNewEvents(eventsToSend);
		}
		
		ArrayList<EventWrapper> copyOfNewEventsTempParking = new ArrayList<EventWrapper>(copyOfNewEventHoldersTempParking.size());
		for (int i = 0; i < copyOfNewEventHoldersTempParking.size(); i++){
			copyOfNewEventsTempParking.add(copyOfNewEventHoldersTempParking.get(i).getEvent());
		}
		earliestNewEvent = null;
		if (copyOfNewEventsTempParking.size() > 0){
			// reorder copyOfNewEventsTempParking by timeStamp in increasing order. (so that the oldest event is first)
			Collections.sort(copyOfNewEventsTempParking);
			earliestNewEvent = copyOfNewEventsTempParking.get(0);
		}
		// Add any new events to the main event list:
		if (copyOfNewEventsTempParking.size() > 0){
			// If the first event in copyOfNewEventsTempParking has a timeStamp less than the
			//last event in events, then the events list will need re-sorting after
			//copyOfNewEventsTempParking is added to it.
			boolean needToReSortEventsList = false;
			if (events.size() > 0 && events.get(events.size()-1).compareTo(copyOfNewEventsTempParking.get(0)) > 0){
				needToReSortEventsList = true;
			}
			assert checkForDoubles(events, copyOfNewEventsTempParking) == false : "There are doubles";
			events.addAll(copyOfNewEventsTempParking);
			//System.out.println(this.getClass().getSimpleName()+": events.addAll(copyOfNewEventsTempParking); called. earliestNewEvent.getClass().getSimpleName() == "+earliestNewEvent.getClass().getSimpleName()+", earliestNewEvent == "+earliestNewEvent);
			if (needToReSortEventsList){
				// reorder copyOfNewEventsTempParking by timeStamp in increasing order. (so that the oldest event is first)
				Collections.sort(events);
			}
			assert checkEventsForIncreasingTimeStampOrder(events) : "Events are not in order.";
		}
	}
	
	/**
	 * Guaranteed not to return the lastAppliedEvent. Will return the earlier of:
	 * the event with the earliest timeStamp in newEventsTempParking, or
	 * the event in the events list that is after (using compareTo) lastAppliedEvent.
	 */
	public EventWrapper getEarliestNewEvent(EventWrapper lastAppliedEvent, double endTimeSeconds){
		assert checkEventsForIncreasingTimeStampOrder(events) : "Events are not in order.";
		if (earliestNewEvent != null && earliestNewEvent.equals(lastAppliedEvent)){
			System.out.println(this.getClass().getSimpleName()+": earliestNewEvent.equals(lastAppliedEvent) == "+earliestNewEvent.equals(lastAppliedEvent)+", so setting earliestNewEvent = null.");
			earliestNewEvent = null;
		}
		// this loop relies on the WorldEvents in events being in increasing time stamp order.
		for (int i = events.size()-1; i >= 0; i--){
			if (lastAppliedEvent != null && events.get(i).getSSCode() == lastAppliedEvent.getSSCode()){
				break;
			}
			if (events.get(i).getTimeStamp() >= endTimeSeconds){
				// this event happened in the future, so skip it for now.
				//System.out.println(this.getClass().getSimpleName()+": in future: endTimeSeconds == "+endTimeSeconds+" events.get(i).getTimeStampSeconds() == "+events.get(i).getTimeStampSeconds());
				continue;
			}else{
				if (earliestNewEvent == null || earliestNewEvent.compareTo(events.get(i)) > 0){
					earliestNewEvent = events.get(i);
				}
				//System.out.println(this.getClass().getSimpleName()+": found an earlier event");
			}
		}
		assert earliestNewEvent == null || lastAppliedEvent == null || earliestNewEvent.equals(lastAppliedEvent) == false : "earliestNewEvent == "+earliestNewEvent+", lastAppliedEvent == "+lastAppliedEvent;
		return earliestNewEvent;
	}
	/** Chucks out any old events from the events list which have a timeStamp less than 
	 * world.getTotalElapsedSeconds() + world.getElapsedSeconds() - getMinSecondsToKeepUserEvents()).
	 * This avoids a memory leak where the events list becomes larger and larger.
	 * Note that the most recent event is never discarded no matter how old it is.
	 * @param world
	 */
	public void clearOutOldEvents(RewindableWorld world){
		// Clear out old events to avoid having a memory leak
		// but don't clear out the latest event since it will be used in unapplyEvent method to undo changes of any new UserEvent.
		if (events.size() > 1 &&
			world.getTotalElapsedSeconds() + world.getElapsedSeconds() - events.get(0).getTimeStamp() > minSecondsToKeepUserEvents){
			//System.out.println(this.getClass().getSimpleName()+": events.size() before cleanup == "+events.size());
			//System.out.println(this.getClass().getSimpleName()+": getWorld().getTotalSecondsElapsed()+getWorld().getSecondsElapsed() == "+ (getWorld().getTotalSecondsElapsed()+getWorld().getSecondsElapsed())+ " events.get(0).getTimeStampSeconds() == "+events.get(0).getTimeStampSeconds());
			//System.out.println(this.getClass().getSimpleName()+": about to chuck out: "+events.get(0).getCheckNum());
			events.remove(0);
			for (int i = 0; i < events.size()-1; i++){
				assert events.get(i) != null : "events.get(i) == "+events.get(i)+", events.size() == "+events.size()+", i == "+i;
				if (world.getTotalElapsedSeconds() + world.getElapsedSeconds() - events.get(i).getTimeStamp() > minSecondsToKeepUserEvents){
					events.remove(i);
					i--;
				}
			}
			//System.out.println(this.getClass().getSimpleName()+": events.size() after cleanup == "+events.size());
		}
	}
	/**
	 * Gets a list of all events since the given event, excluding the given event and events with a timeStamp after endTimeSeconds.
	 */ 
	public ArrayList<EventWrapper> getAllEventsSince(EventWrapper lastAppliedEvent, double endTimeSeconds){
		assert checkEventsForIncreasingTimeStampOrder(events) : "Events are not in order.";
		ArrayList<EventWrapper> tempList = new ArrayList<EventWrapper>();
		for (int i = events.size()-1; i >= 0; i--){
			if (events.get(i).getTimeStamp() >= endTimeSeconds){
				// this event happened in the future, so skip it for now.
				//System.out.println(this.getClass().getSimpleName()+": in future: endTimeSeconds == "+endTimeSeconds+" events.get(i).getTimeStampSeconds() == "+events.get(i).getTimeStampSeconds());
				continue;
			} else if (lastAppliedEvent != null && events.get(i).getSSCode() == lastAppliedEvent.getSSCode()){
				// this event was before the one we want, so we've got all the events that we want, so break.
				//System.out.println(this.getClass().getSimpleName()+": in future: endTimeSeconds == "+endTimeSeconds+" events.get(i).getTimeStampSeconds() == "+events.get(i).getTimeStampSeconds());
				break;
			}else{
				tempList.add(0, events.get(i));
				//System.out.println(this.getClass().getSimpleName()+": added an event to events");
			}
		}
		assert checkEventsForIncreasingTimeStampOrder(tempList) : "Events are not in order.";
		return tempList;
	}
	
	/** Used in assert statements to check that there are no two events the same
	 * in copyOfNewEventsTempParkingList and that no events in that list are 
	 * already contained in the main events list.
	 */ 
	protected boolean checkForDoubles(ArrayList<EventWrapper> eventsList, ArrayList<EventWrapper> copyOfNewEventsTempParkingList){
		boolean doubles = false;
		for (int i = 0; i < copyOfNewEventsTempParkingList.size(); i++){
			if (copyOfNewEventsTempParkingList.lastIndexOf(copyOfNewEventsTempParkingList.get(i)) != i){
				doubles = true;
				System.err.println(this.getClass().getSimpleName()+": copyOfNewEventsTempParkingList.lastIndexOf(copyOfNewEventsTempParkingList.get("+i+")) == "+copyOfNewEventsTempParkingList.lastIndexOf(copyOfNewEventsTempParkingList.get(i)));
			}
		}
		for (int i = 0; i < copyOfNewEventsTempParkingList.size(); i++){
			if (eventsList.contains(copyOfNewEventsTempParkingList.get(i)) == true){
				doubles = true;
				System.err.println(this.getClass().getSimpleName()+": eventsList.lastIndexOf(copyOfNewEventsTempParkingList.get("+i+")) == "+eventsList.lastIndexOf(copyOfNewEventsTempParkingList.get(i)));
			}
		}
		if (doubles){
			System.err.println(this.getClass().getSimpleName()+": copyOfNewEventsTempParkingList == "+copyOfNewEventsTempParkingList);
			System.err.println(this.getClass().getSimpleName()+": eventsList == "+eventsList);
		}
		return doubles;
	}
	/** Used in assert statements to check that event lists are in increasing timeStamp order.
	 */ 
	protected boolean checkEventsForIncreasingTimeStampOrder(ArrayList<EventWrapper> list){
		boolean ordered = true;
		EventWrapper eBefore = null;
		for (int i = 0; i < list.size(); i++){
			EventWrapper e = list.get(i);
			if (eBefore == null){
				eBefore = e;
				continue;
			}
			if (e.compareTo(eBefore) < 0){
				System.err.println(this.getClass().getSimpleName()+": list.get("+i+") == "+list.get(i)+", eBefore == "+eBefore+", list.size() == "+list.size());
				ordered = false;
			}
			// The below checks for doubles but it really taxes the CPU, so best not to use it
			/*if (list.indexOf(e) != list.lastIndexOf(e)){
				System.err.println(this.getClass().getSimpleName()+": there are doubles of an event, list.indexOf(e) == "+list.indexOf(e)+", list.lastIndexOf(e) == "+list.lastIndexOf(e)+", list.get("+i+").getTimeStamp() == "+list.get(i).getTimeStamp()+", eBefore.getTimeStamp() == "+eBefore.getTimeStamp()+", list.size() == "+list.size());
				ordered = false;
			}*/
			//System.err.println(this.getClass().getSimpleName()+": no error but, list.get(i).getTimeStamp() == "+list.get(i).getTimeStamp()+", eBefore.getTimeStamp() == "+eBefore.getTimeStamp());
			eBefore = e;
		}
		return ordered;
	}
	
	/**
	 * This should only be called when joining to an existing game.
	 * @param events
	 */
	public void setEvents(ArrayListSS<EventWrapper> events){
		this.events = events;
		assert checkEventsForIncreasingTimeStampOrder(this.events);
	}

	public ArrayListSS<EventWrapper> getEvents() {
		return events;
	}
	
	
	/**
	 * The minimum amount of time in seconds that EventWrappers are kept in the events list 
	 * before they are chucked out by the method clearOutOldEvents.
	 * 
	 * This should be at least as big as GameWorld.getMaxTimeGapSeconds() or else 
	 * events may be chucked out when they are still needed by the GameWorld for 
	 * doing updates. Note that you don't want this time to be too big or else events 
	 * will not be efficiently garbage collected in the juvenile/eden heap and will 
	 * be promoted to the older objects' heap and garbage collections will take longer.
	 * @return The minimum amount of seconds that EventWrappers are kept in the events list.
	 */
	public double getMinSecondsToKeepUserEvents() {
		return minSecondsToKeepUserEvents;
	}

	public void setMinSecondsToKeepUserEvents(double minSecondsToKeepUserEvents) {
		this.minSecondsToKeepUserEvents = minSecondsToKeepUserEvents;
	}
}
