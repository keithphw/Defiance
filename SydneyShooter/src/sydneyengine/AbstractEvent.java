/*
 * AbstractEvent.java
 *
 * Created on 21 November 2007, 21:28
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine;

import sydneyengine.superserializable.SSAdapter;
/**
 * This class should be extended to make events for every action that affects the World.
 * 
 * @author CommanderKeith
 */
public abstract class AbstractEvent extends SSAdapter implements Comparable{
	transient protected EventWrapper eventWrapper = null;
	// id and count are just used for error-checking in assert statements to make 
	// sure that events are processed in order.
	protected int id;
	protected int count;
	
	public AbstractEvent(){
	}
	public AbstractEvent(EventWrapper wrapperEvent){
		this.eventWrapper = wrapperEvent;
	}
	
	public abstract void apply(RewindableWorld world);
	
	public double getTimeStamp(){
		return eventWrapper.getTimeStamp();
	}
	public void setTimeStamp(double timeStamp){
		eventWrapper.setTimeStamp(timeStamp);
	}
	@Override
	public int compareTo(Object ev){
		assert ev instanceof AbstractEvent : ev;
		EventWrapper e = ((AbstractEvent)ev).getEventWrapper();
		return eventWrapper.compareTo(e);
	}

	public EventWrapper getEventWrapper() {
		return eventWrapper;
	}

	public void setEventWrapper(EventWrapper eventWrapper) {
		this.eventWrapper = eventWrapper;
		if (eventWrapper.getUnderlyingEvent() == null){
			eventWrapper.setUnderlyingEvent(this);
		}
	}
	
	@Override
	public String toString(){
		return super.toString()+"_"+getTimeStamp()+"_"+getSSCode()+"_id"+id+"_count"+count;
	}

}