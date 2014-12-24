/*
 * EventWrapper.java
 *
 * Created on 13 November 2007, 00:19
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine;

import sydneyengine.superserializable.*;
import java.io.*;
import java.util.*;
/**
 * This class is the wrapper for all AbstractEvents. If an EventWrapper is recieved 
 * over the network, its AbstractEvent would not exist yet since it would just be
 * a byte array. When this event is applied (apply method is called), the AbstractEvent 
 * is created from the byte array in the deserialize method.  The AbstractEvent 
 * is only deserialized when it is applied since the objects that it refers to 
 * might not exist in the World yet if it is not deserialized at its timeStamp
 * (also note that the timeSTamp should equal world.getTotalElapsedSeconds when the event is applied).
 * 
 * @author CommanderKeith
 */
public class EventWrapper extends SSAdapter implements Comparable{
	protected static float TIME_DELAY_BEFORE_EVENT_APPLIED = 0.0f;
	protected float timeDelayBeforeEventApplied = TIME_DELAY_BEFORE_EVENT_APPLIED;
	// Every event must be timeStamped so that all Controllers (on the server and/or the other clients) know when it was pressed on this VM.
	// This way every GameWorld can apply the move at the same time that this VM's GameWorld saw it applied.
	protected double timeStamp;
	protected AbstractEvent underlyingEvent = null;
	byte[] underlyingEventBytes = null;
	
	ByteArrayOutputStream bout;
	ByteArrayInputStream bin;
	
	protected Controller controller;
	
	int id = 0;
	int count = 0;
	// if count is set to COUNT_TO_IGNORE, testing with this event should not be done. it should be used when sending things like quiting messages or something like that, where the message could be sent from the person quitting, and/or from the server that detects a connection error, so there could be two of the same message and checking should not be done.
	public final static int COUNT_TO_IGNORE = Integer.MAX_VALUE;	
	
	public EventWrapper(){
		bout = new ByteArrayOutputStream();
	}
	public EventWrapper(AbstractEvent underlyingEvent){
		this();
		this.setUnderlyingEvent(underlyingEvent);
	}
	
	public void apply(RewindableWorld world){
		if (isDeserialized() == false){
			try {
				this.deserialize(world.getEventStore().getController().getSSIn());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		// this throws exceptions if events are ignored: assert world.getLastAppliedEvent() == null || world.getLastAppliedEvent().getId() != this.getId() || world.getLastAppliedEvent().getCount() == this.getCount()-1 : "world.getLastAppliedEvent() == "+world.getLastAppliedEvent()+", getUnderlyingEvent().getClass().getSimpleName() == "+getUnderlyingEvent().getClass().getSimpleName()+", this == "+this;
		// this check just makes sure that order is kept, but is OK with events being ignored in the GameWorld.update method.
		assert world.getLastAppliedEvent() == null || world.getLastAppliedEvent().getId() != this.getId() || world.getLastAppliedEvent().getCount() < this.getCount() || world.getLastAppliedEvent().getCount() == COUNT_TO_IGNORE || this.getCount() == COUNT_TO_IGNORE: "world.getLastAppliedEvent() == "+world.getLastAppliedEvent()+" this == "+this+", this.getUnderlyingEvent().getClass().getSimpleName() == "+getUnderlyingEvent().getClass().getSimpleName();
		underlyingEvent.apply(world);
	}
	
	public double getTimeStamp(){
		return (timeStamp + getTimeDelayBeforeEventApplied() < 0 ? 0 : timeStamp + getTimeDelayBeforeEventApplied());
	}
	public void setTimeStamp(double timeStamp){
		this.timeStamp = timeStamp;
	}
	public int compareTo(Object ev){
		assert ev instanceof EventWrapper : ev;
		double thisTimeStamp = getTimeStamp();
		double eTimeStamp = ((EventWrapper)ev).getTimeStamp();
		if (thisTimeStamp > eTimeStamp){
			return 1;
		}else if (thisTimeStamp < eTimeStamp){
			return -1;
		}else{
			if (getCount() > ((EventWrapper)ev).getCount()){
				return 1;
			}else if (getCount() < ((EventWrapper)ev).getCount()){
				return -1;
			}else{
				return 0;
			}
			// if we use ssCodes to do the ordering it may not be the same as using counts since ssCodes are reset to zero, so best do ordering by counts.
//			if (getSSCode() > ((EventWrapper)ev).getSSCode()){
//				return 1;
//			}else if (getSSCode() < ((EventWrapper)ev).getSSCode()){
//				return -1;
//			}else{
//				// The fact that we're here either means that this object is being compared with itself 
//				// or there are two SSObjects with the same ssCode. Having same ssCode is OK since head and tail 
//				// world SSObjects have the same ssCodes, but they should not ever be compared with each other.
//				assert this == ev : "the ssCodes are equal but the objects are not == . This is a problem. "+this+", "+((EventWrapper)ev);
//				return 0;
//			}
		}
	}
	public String toString(){
		return super.toString()+"_"+getTimeStamp()+"getSSCode"+getSSCode()+"_getSSCodeDecoded"+SSCodeAllocator.decode(getSSCode())[0]+"_id"+id+"_count"+count;
	}
	// This method should only be called by the Controller thread.
	public void serialize(SSObjectOutputStream ssout) throws IOException{
		assert underlyingEvent != null;
		bout.reset();
		ssout.setOutputStream(bout);
		ssout.writeObject(underlyingEvent);
		ssout.writeDone();
		underlyingEventBytes = bout.toByteArray();
		ssout.setOutputStream(null);
		//System.err.println(this.getClass().getSimpleName()+": serialize, underlyingEventBytes.length == "+underlyingEventBytes.length+", underlyingEvent.getClass().getSimpleName() == "+underlyingEvent.getClass().getSimpleName());
	}
	// This method should only be called by the Controller thread.
	public void deserialize(SSObjectInputStream ssin) throws IOException{
		assert underlyingEventBytes != null;
		assert underlyingEventBytes.length > 0;
		bin = new ByteArrayInputStream(underlyingEventBytes);
		ssin.setInputStream(bin);
		underlyingEvent = (AbstractEvent)ssin.readObject();
		ssin.readDone();
		//System.err.println(this.getClass().getSimpleName()+": deserialize, underlyingEventBytes.length == "+underlyingEventBytes.length);
		underlyingEvent.setEventWrapper(this);
	}
	
	public AbstractEvent getUnderlyingEvent() {
		return underlyingEvent;
	}
	
	public void setUnderlyingEvent(AbstractEvent underlyingEvent) {
		this.underlyingEvent = underlyingEvent;
		if (underlyingEvent.getEventWrapper() == null){
			underlyingEvent.setEventWrapper(this);
		}
	}
	
	public boolean isDeserialized() {
		return underlyingEvent != null;
	}
	public boolean isSerialized() {
		return underlyingEventBytes != null;
	}
	
	public void writeSS(SSObjectOutputStream out) throws IOException{		// this is the method that you over-ride if you want custom serialization
		assert underlyingEventBytes != null;
		out.writeDouble(timeStamp);
		out.writeFloat(timeDelayBeforeEventApplied);
		out.writeInt(underlyingEventBytes.length);
		out.write(underlyingEventBytes);
		
		out.writeInt(id);
		out.writeInt(count);
	}
	public void readSS(SSObjectInputStream in) throws java.io.IOException{	// this is the method that you over-ride if you want custom serialization
		// question: is this method called for each class level?? or will sub-class vars not get written?
		// answer: no, this method will not be called at each class-level,
		// but in.readFields reads all vars from all class levels so sub-classes won't miss out.
		timeStamp = in.readDouble();
		timeDelayBeforeEventApplied = in.readFloat();
		int underlyingEventBytesLength = in.readInt();
		underlyingEventBytes = new byte[underlyingEventBytesLength];
		in.read(underlyingEventBytes);
		underlyingEvent = null;
		
		id = in.readInt();
		count = in.readInt();
	}
	
	
	
	public int getId() {
		return id;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public Controller getController() {
		return controller;
	}
	
	public void setController(Controller controller) {
		this.controller = controller;
	}

	public static double getStaticTimeDelayBeforeEventApplied() {
		return TIME_DELAY_BEFORE_EVENT_APPLIED;
	}

	public static void setStaticTimeDelayBeforeEventApplied(float aTimeDelayBeforeEventApplied) {
		TIME_DELAY_BEFORE_EVENT_APPLIED = aTimeDelayBeforeEventApplied;
	}

	public float getTimeDelayBeforeEventApplied() {
		return timeDelayBeforeEventApplied;
	}
	
}
