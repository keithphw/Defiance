/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine;

import sydneyengine.superserializable.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.net.*;
import java.awt.image.*;
import javax.imageio.*;

/**
 *
 * @author CommanderKeith
 */
public abstract class RewindableWorld extends SSAdapter{

	protected abstract void doMaxTimeMove(double seconds, double timeAtStartOfMoveSeconds);
	protected void doMoveBasic(double seconds, double timeAtStartOfMoveSeconds){
		
	}
	transient protected Controller controller;
	// This flag sets whether the doMove method should be called between events when
	// those events have the same timeStmap. If it is true, doMove(0) is called in between. 
	// Otherwise, doMove is skipped and the next event is just applied.
	// Note that if false, then ViewPane.sendEvent() allocates time stamps to events as simply the current time in this game world. 
	// If true, then timeStamps are grouped together in multiples of eventTimeStampMultipleSeconds, 
	// so multiple events are likely to have the same timeStamp. This can be beneficial for performance 
	// if doMoveBetweenEventsIfTimeStampsEqual is true, since there will be less doMaxTimeMove()'s per update.
	protected boolean doMoveBetweenEventsIfTimeStampsEqual = false;
	protected double eventTimeStampMultipleSeconds = 0.05;
	transient protected int numDoMaxTimeMoves;

	// The maximum seconds that should be processed in doMaxTimeMove(seconds).
	// If the time will be more than this, multiple updates will be done in the 
	// doMaxTimeMove method before the next render.
	// Note that maxUpdateElapsedSeconds should be approximately equal to eventTimeStampMultipleSeconds, 
	// and preferrably slightly bigger. This is because a doMaxTimeMove() must occur between 
	// all events, so a doMaxTimeMove will occur between 2 events that are eventTimeStampMultipleSeconds
	// apart, but if maxUpdateElapsedSeconds is less than eventTimeStampMultipleSeconds,
	// then two doMaxTimeMove()'s will occur in between the events, which would be 
	// unnecessary if maxUpdateElapsedSeconds was a little bigger than eventTimeStampMultipleSeconds.
	protected double maxUpdateElapsedSeconds = eventTimeStampMultipleSeconds + 0.00001;
	protected transient boolean head = true;
	protected transient RewindableWorld twin;
	// The below needs to be smaller than eventStore.maxSecondsToKeepUserEvents
	// The time between the head and tail is guaranteed to never be less than minTimeGapSeconds 
	// unless the controller just began. The same time will never exceed maxTimeGapSeconds
	// unless the elapsedNanos argument given to world.update(elapsedNanos) exceeds 
	// maxTimeGapSeconds-minTimeGapSeconds.
	// Note that head.makeEqualTo(tail) will be called at least every (maxTimeGapSeconds-minTimeGapSeconds),
	// so it's a good idea not to make that gap too small.
	protected double minTimeGapSeconds = 0.5;
	protected double maxTimeGapSeconds = 0.8;
	protected long systemNanosAtStart = -1;
	// these are actual nano-seconds elapsed and are not multiplied by timeMultiplier.
	protected long pureElapsedNanos = 0;
	protected long pureTotalElapsedNanos = 0;
	protected double timeMultiplier = 1.0;
	// this is pureNanosElapsed * timeMultiplier
	protected long elapsedNanos = 0;
	// this is the sum of all pureNanosElapsed * timeMultiplier since the GameWorld started.
	protected long totalElapsedNanos = 0;
	static public double NANOS_IN_A_SECOND = 1000000000.0;
	// this is nanosElapsed multiplied by NANOS_IN_A_SECOND to get seconds, it's just for convenience.
	protected double elapsedSeconds = 0;
	// this is totalNanosElapsed multiplied by 1,000,000,000 to get seconds, it's just for convenience.
	protected double totalElapsedSeconds = 0;		// this has secondsElapsed added to it AT THE END of an updateNanos().
	
	transient protected long pureActualElapsedNanos = 0;
	transient protected long actualElapsedNanos = 0;
	transient protected double actualElapsedSeconds = 0;
	transient protected long actualTotalElapsedNanos = 0;
	transient protected double actualTotalElapsedSeconds = 0;
	transient protected long pureActualTotalElapsedNanos = 0;
	
	// These fields are for the makeEqualTo method:
	transient FieldCache fieldCacheForMakeEqualTo = new FieldCache();
	transient WeakSSObjectMap weakSSObjectMapForMakeEqualTo = new WeakSSObjectMap<SSObject, Object>();
	transient ArrayList<Object> memberObjectsToCopyByReferenceForMakeEqualTo = new ArrayList<Object>();
	
	transient protected EventStore eventStore;
	protected EventWrapper lastAppliedEvent = null;

	public void setPureElapsedNanos(long nanos) {
		pureElapsedNanos = nanos;
		elapsedNanos = Math.round(pureElapsedNanos * timeMultiplier);
		elapsedSeconds = elapsedNanos / NANOS_IN_A_SECOND;
	}
	
	public void setPureActualElapsedNanos(long nanos) {
		pureActualElapsedNanos = nanos;
		actualElapsedNanos = Math.round(pureActualElapsedNanos * timeMultiplier);
		actualElapsedSeconds = actualElapsedNanos / NANOS_IN_A_SECOND;
	}

	public void setElapsedSeconds(double seconds) {
		elapsedSeconds = seconds;
		elapsedNanos = Math.round(elapsedSeconds * NANOS_IN_A_SECOND);
		pureElapsedNanos = Math.round(elapsedNanos / timeMultiplier);
	}

	public void setPureTotalElapsedNanos(long nanos) {
		pureTotalElapsedNanos = nanos;
		totalElapsedNanos = Math.round(pureTotalElapsedNanos * timeMultiplier);
		totalElapsedSeconds = totalElapsedNanos / NANOS_IN_A_SECOND;
	}

	public void setTotalElapsedSeconds(double seconds) {
		totalElapsedSeconds = seconds;
		totalElapsedNanos = Math.round(totalElapsedSeconds * NANOS_IN_A_SECOND);
		pureTotalElapsedNanos = Math.round(totalElapsedNanos / timeMultiplier);
	}
	
	public void setActualTotalElapsedSeconds(double seconds) {
		actualTotalElapsedSeconds = seconds;
		actualTotalElapsedNanos = Math.round(actualTotalElapsedSeconds * NANOS_IN_A_SECOND);
		pureActualTotalElapsedNanos = Math.round(actualTotalElapsedNanos / timeMultiplier);
	}
	

	/**
	 * This method updates the head world by elapsedSeconds, taking into account any new events, provided 
	 * the events are not lagged so much that their timeStmap is less than the tail world's totalElapsedSeconds.
	 * This is a simplified version of the steps that it takes:
	 * Get a sorted list of new events received by the EventStore,
	 * as well as any events which are after the argument lastAppliedEvent in the EventStore's list.
	 * Excludes events which have a timeStamp greater than the end time of this update.
	 * If the first of the new events (the oldest event) has a timeStamp before the world's totalElapsedSeconds,
	 * then the world is teleported back in time by making the world equal to the tail world, which is just an old version of the world.
	 * Whether teleported or not, we get all events since the lastAppliedEvent and call doMove(time) up to each event.
	 * After the last event is moved up to, or if there were no events, then we just doMove up to the current time that the world needs to be.
	 * If the tail world would have fallen too far behind the head world, then it is madeEqualTo the 
	 *  head world such that the tail world's totalElaspedSeconds in minTimeGapSeconds less than the 
	 * current time which the head world reaches at the end of this method.
	 */
	public void updateNanos(long pureElapsedNanos, long actualPureElapsedNanos) {
		assert pureElapsedNanos >= 0;
		assert actualPureElapsedNanos >= 0;
		setPureElapsedNanos(pureElapsedNanos);
		setPureActualElapsedNanos(actualPureElapsedNanos);
		
		numDoMaxTimeMoves = 0;
		//System.out.println(getClass().getSimpleName()+": bullets.size() == "+bullets.size());
		assert this.isHead() : "The tail should not be updated, it is just used to store values so that the head can be reset/rewound";
		// commented out this assert since it fails when the min or maxTimeGapSeconds are changed. 
		assert getHead().getMaxTimeGapSeconds() > getHead().getMinTimeGapSeconds() && getHead().getMaxTimeGapSeconds() == getTail().getMaxTimeGapSeconds() && getHead().getMinTimeGapSeconds() == getTail().getMinTimeGapSeconds() : ", getHead().getMaxTimeGapSeconds() == "+getHead().getMaxTimeGapSeconds()+", getHead().getMinTimeGapSeconds() == "+getHead().getMinTimeGapSeconds()+", getTail().getMaxTimeGapSeconds() == "+getTail().getMaxTimeGapSeconds()+", getTail().getMinTimeGapSeconds() == "+getTail().getMinTimeGapSeconds();
		assert getHead().getMaxTimeGapSeconds() > getHead().getMinTimeGapSeconds() && getTail().getMaxTimeGapSeconds() > getTail().getMinTimeGapSeconds() : getHead().getMaxTimeGapSeconds() + ", " + getHead().getMinTimeGapSeconds() + ", " + getTail().getMaxTimeGapSeconds() + ", " + getTail().getMinTimeGapSeconds();
		// check that the head and tail world are at least minTimeGapSeconds away from each other:
		//assert getHead().getTotalElapsedSeconds() <= this.getMinTimeGapSeconds()+0.0001 || getHead().getTotalElapsedSeconds() - getTail().getTotalElapsedSeconds()+0.0001 >= getMinTimeGapSeconds() : "getHead().getTotalElapsedSeconds() - getTail().getTotalElapsedSeconds() == "+(getHead().getTotalElapsedSeconds() - getTail().getTotalElapsedSeconds())+" is not greater than getMinTimeGapSeconds() == "+getMinTimeGapSeconds()+", getHead().getTotalElapsedSeconds() == "+getHead().getTotalElapsedSeconds()+", getTail().getTotalElapsedSeconds() == "+getTail().getTotalElapsedSeconds();
		// Get a sorted list of any new events. Note that this method also adds any new events to eventStore's main event list.
		EventWrapper earliestNewEvent = getEventStore().getEarliestNewEvent(getLastAppliedEvent(), getTotalElapsedSeconds() + getElapsedSeconds());
		assert earliestNewEvent == null || getLastAppliedEvent() == null || earliestNewEvent.equals(getLastAppliedEvent()) == false : "earliestNewEvent == " + earliestNewEvent + ", getLastAppliedEvent() == " + getLastAppliedEvent();

		// If the earliestNewEvent's timeStamp is before the current time, we need to go back in time to apply it.
		// Also, if the earliestNewEvent is before (using compareTo) the lastAppliedEvent, we need to go back to the tailWorld so that the events can be applied in the right order.
		boolean goBackwardsInTimeToApplyOldestNewEvent = false;
		if (earliestNewEvent != null &&
				(earliestNewEvent.getTimeStamp() < getTotalElapsedSeconds() ||
				(getLastAppliedEvent() != null && earliestNewEvent.compareTo(getLastAppliedEvent()) < 0))) {
			goBackwardsInTimeToApplyOldestNewEvent = true;
			//System.err.println(getClass().getSimpleName()+": goBackwardsInTimeToApplyOldestNewEvent == "+goBackwardsInTimeToApplyOldestNewEvent+", getHead().getTotalElapsedSeconds() == "+getHead().getTotalElapsedSeconds()+", getTail().getTotalElapsedSeconds() == "+getTail().getTotalElapsedSeconds());
		}
		// We also need to go back in time if the tail's last updated time will be greater than maxTimeGapSeconds after this update.
		boolean goBackwardsInTimeForTailToCatchUp = false;
		double timeGapBetweenHeadAndTailAfterUpdate = getTotalElapsedSeconds() + getElapsedSeconds() - getTail().getTotalElapsedSeconds();
		if (timeGapBetweenHeadAndTailAfterUpdate >= getMaxTimeGapSeconds()) {
			goBackwardsInTimeForTailToCatchUp = true;
			//System.err.println(getClass().getSimpleName()+": goBackwardsInTimeForTailToCatchUp == "+goBackwardsInTimeForTailToCatchUp+", timeGapBetweenHeadAndTailAfterUpdate == "+timeGapBetweenHeadAndTailAfterUpdate+", getHead().getTotalElapsedSeconds() == "+getHead().getTotalElapsedSeconds()+", getTail().getTotalElapsedSeconds() == "+getTail().getTotalElapsedSeconds());
		}
		
		//System.err.println(getClass().getSimpleName()+": goBackwardsInTimeForTailToCatchUp == "+goBackwardsInTimeForTailToCatchUp+", goBackwardsInTimeToApplyOldestNewEvent == "+goBackwardsInTimeToApplyOldestNewEvent+", getHead().getTotalElapsedSeconds() == "+getHead().getTotalElapsedSeconds()+", count == "+count+", earliestNewEvent == "+earliestNewEvent);
		if (earliestNewEvent != null || goBackwardsInTimeToApplyOldestNewEvent || goBackwardsInTimeForTailToCatchUp) {
			// Since allNewEvents are in increasing timeStamp order, the oldest one will be the first.
			// Check that the oldest new event happened after the last tail update time since otherwise the oldest new event and maybe others can't be applied.
			// This is not necessarily a really bad thing, since it can happen if the event was badly lagged, it just means that the event will be ignored (not applied).
			//assert earliestNewEvent == null || earliestNewEvent.getTimeStamp() >= getTail().getTotalElapsedSeconds() : "earliestNewEvent.getTimeStamp() == "+earliestNewEvent.getTimeStamp()+" which is less than getTail().getTotalElapsedSeconds() == "+this.getTail().getTotalElapsedSeconds()+". This is not necessarily an error, since it can happen if the event was badly lagged, it just means that the event will be ignored (not applied). If you don't want this to happen, increase the minTimeGap and eventStore.maxSecondsToKeepUserEvents";

			// Here we go backwards in time if necessary!
			if (goBackwardsInTimeToApplyOldestNewEvent || goBackwardsInTimeForTailToCatchUp) {
				// When this world has makeEqualTo(tail,...) called on it then its pureTotalElapsedNanos will be over-written, so we need to save it.
				long oldPureTotalElapsedNanos = pureTotalElapsedNanos;
				long oldPureElapsedNanos = pureElapsedNanos;
				// below 2 are saved for testing in the assert statement below:
				double oldElapsedSeconds = elapsedSeconds;
				double oldTotalElapsedSeconds = totalElapsedSeconds;
				EventWrapper tailsLastAppliedEvent = getTail().getLastAppliedEvent();
				double tailsLastAppliedEventTimeStamp = (tailsLastAppliedEvent != null ? tailsLastAppliedEvent.getTimeStamp() : 0);
				makeEqualTo(getTail());
				assert tailsLastAppliedEvent == getLastAppliedEvent() : "bugger";
				double lastAppliedEventTimeStamp = (this.getLastAppliedEvent() != null ? getLastAppliedEvent().getTimeStamp() : 0);
				assert tailsLastAppliedEventTimeStamp == lastAppliedEventTimeStamp : "bugger";
				/* Here we adjust the pureElapsedNanos, elapsedNanos and elapsedSeconds
				to be much larger than they were before since they need to update from
				the tail world's old update time to the current time.*/
				this.setPureElapsedNanos(oldPureTotalElapsedNanos + oldPureElapsedNanos - getPureTotalElapsedNanos());
				// check that time has not gone missing
				assert Math.abs(getTotalElapsedSeconds() + getElapsedSeconds() - (oldTotalElapsedSeconds + oldElapsedSeconds)) < 0.001 : "this.getTotalElapsedSeconds() + this.getElapsedSeconds() == oldTotalElapsedSeconds + oldElapsedSeconds is " + (this.getTotalElapsedSeconds() + this.getElapsedSeconds()) + " == " + (oldTotalElapsedSeconds + oldElapsedSeconds) + ", or " + this.getTotalElapsedSeconds() + " + " + this.getElapsedSeconds() + " == " + oldTotalElapsedSeconds + " + " + oldElapsedSeconds;	//+", this.getPureElapsedNanos() == "+this.getPureElapsedNanos()+", this.getPureTotalElapsedNanos() == "+this.getPureTotalElapsedNanos();
			//System.err.println(this.getClass().getSimpleName()+": head is going backwards in time! goBackwardsInTimeToApplyOldestNewEvent == "+goBackwardsInTimeToApplyOldestNewEvent+", goBackwardsInTimeForTailToCatchUp == "+goBackwardsInTimeForTailToCatchUp+", this.getTotalElapsedSeconds() == "+this.getTotalElapsedSeconds()+", this.getElapsedSeconds() == "+this.getElapsedSeconds()+", this.getTail().getTotalElapsedSeconds() == "+this.getTail().getTotalElapsedSeconds()+", this.getTail().getElapsedSeconds() == "+this.getTail().getElapsedSeconds()+", getLastAppliedEvent() == "+(getLastAppliedEvent() != null ? getLastAppliedEvent().getTimeStamp() : null)+" getPlayers().size() == "+getPlayers().size()+" getTail().getPlayers().size() == "+getTail().getPlayers().size());
			}
			// Now get an ordered list of all events (new and existing) since the lasAppliedEvent.
			// Note that lasAppliedEvent could still be null if there was no event applied.
			ArrayList<EventWrapper> allEvents = getEventStore().getAllEventsSince(getLastAppliedEvent(), elapsedSeconds + getTotalElapsedSeconds());
			//System.err.println(this.getClass().getSimpleName()+": 22222 this.getTotalElapsedSeconds() == "+this.getTotalElapsedSeconds()+", this.getElapsedSeconds() == "+this.getElapsedSeconds()+", this.getTail().getTotalElapsedSeconds() == "+this.getTail().getTotalElapsedSeconds()+", this.getTail().getElapsedSeconds() == "+this.getTail().getElapsedSeconds()+", allNewEvents.size() == "+allNewEvents.size()+", allEvents.size() == "+allEvents.size()+", getLastAppliedEvent() == "+(getLastAppliedEvent() != null ? getLastAppliedEvent().getTimeStamp() : null));
			assert allEvents.size() == 0 || getLastAppliedEvent() == null || allEvents.get(0).getSSCode() != getLastAppliedEvent().getSSCode() : "The first element in allEvents should not be the same as the lastAppliedEvent!! allEvents.get(0) == " + allEvents.get(0) + ", getLastAppliedEvent() == " + getLastAppliedEvent();
			// move up to then apply every event in allEvents.
			for (int i = 0; i < allEvents.size(); i++) {
				EventWrapper e = allEvents.get(i);
				if (e.getTimeStamp() < getTotalElapsedSeconds()) {
					System.out.println(this.getClass().getSimpleName() + ": e.getTimeStamp() < getTotalElapsedSeconds(), so the event will be ignored. e.getTimeStamp() == "+e.getTimeStamp()+", getHead().getTotalElapsedSeconds() == " + getHead().getTotalElapsedSeconds()+", e == "+e);//+", getTail().getTotalElapsedSeconds() == " + getTail().getTotalElapsedSeconds());
					this.setLastAppliedEvent(e);
					continue;
				}

				// If the event before had the same timeStamp, then don't doMove(zero time), 
				// just apply this event and then continue to the next one.
				// To disable this, set doMoveBetweenEventsIfTimeStampsEqual = false.
				if (isDoMoveBetweenEventsIfTimeStampsEqual() == false && i != 0 && e.getTimeStamp() == allEvents.get(i - 1).getTimeStamp()) {
					e.apply(this);
					setLastAppliedEvent(e);
					//System.out.println(this.getClass().getSimpleName() + ": e.getTimeStamp() == " + e.getTimeStamp() + " == allEvents.get(i-1).getTimeStamp() == " + allEvents.get(i - 1).getTimeStamp() + ", so just applying e without doing doMove(zero time). i == " + i + ", allEvents.size() == " + allEvents.size() + ", e == " + e + ", getTotalElapsedSeconds() == " + getTotalElapsedSeconds());
					continue;
				}

				// Apply time to get to the point when the current event happened.
				// Note that this time update will always be positive.
				double moveTimeSeconds = e.getTimeStamp() - getTotalElapsedSeconds();
				assert moveTimeSeconds >= 0 : ("moveTimeSeconds == " + moveTimeSeconds + ", but it should be positive, allEvents.get(" + i + ").getTimeStamp() == " + e.getTimeStamp() + ", allEvents.size() == " + allEvents.size() + ", this.getTotalElapsedSeconds() == " + this.getTotalElapsedSeconds() + ", goBackwardsInTimeToApplyOldestNewEvent == " + goBackwardsInTimeToApplyOldestNewEvent + ", goBackwardsInTimeForTailToCatchUp == " + goBackwardsInTimeForTailToCatchUp + ", getLastAppliedEvent() == " + (getLastAppliedEvent() != null ? getLastAppliedEvent().getTimeStamp() : null) + ", getLastAppliedEvent() == e is " + (getLastAppliedEvent() == e));//+", lastTimeUpdated == "+lastTimeUpdated);

				if (goBackwardsInTimeForTailToCatchUp) {
					double desiredTotalElapsedSecondsForTail = getTotalElapsedSeconds() + getElapsedSeconds() - getTail().getMinTimeGapSeconds();
					double moveTimeUntilTailMadeEqualTo = desiredTotalElapsedSecondsForTail - getTotalElapsedSeconds();
					if (moveTimeUntilTailMadeEqualTo >= 0 && moveTimeUntilTailMadeEqualTo < moveTimeSeconds) {
						doMove(moveTimeUntilTailMadeEqualTo, getTotalElapsedSeconds());
						this.setTotalElapsedSeconds(this.getTotalElapsedSeconds() + moveTimeUntilTailMadeEqualTo);
						this.setElapsedSeconds(getElapsedSeconds() - moveTimeUntilTailMadeEqualTo);
						moveTimeSeconds -= moveTimeUntilTailMadeEqualTo;
						//System.err.println(getClass().getSimpleName()+": *** tail catch up, moveTimeUntilTailMadeEqualTo == "+moveTimeUntilTailMadeEqualTo+", desiredTotalElapsedSecondsForTail == "+desiredTotalElapsedSecondsForTail+", this.getTotalElapsedSeconds() == "+this.getTotalElapsedSeconds() + ", this.getElapsedSeconds() == "+this.getElapsedSeconds());
						EventWrapper oldLastAppliedEvent = getLastAppliedEvent();
						getTail().makeEqualTo(getHead());
						assert oldLastAppliedEvent == getTail().getLastAppliedEvent() : "bugger";
						getTail().setElapsedSeconds(0);
						goBackwardsInTimeForTailToCatchUp = false;
					//System.err.println(getClass().getSimpleName()+": *** tail catch up, getTail().getTotalElapsedSeconds() == getHead().getTotalElapsedSeconds() == "+getHead().getTotalElapsedSeconds());
					} else {
					//System.err.println(getClass().getSimpleName()+": *** not advancing tail, this.getTotalElapsedSeconds() == "+this.getTotalElapsedSeconds()+", getElapsedSeconds() == "+this.getElapsedSeconds()+", desiredTotalElapsedSecondsForTail == "+desiredTotalElapsedSecondsForTail+", moveTimeUntilTailMadeEqualTo == "+moveTimeUntilTailMadeEqualTo+", moveTimeSeconds == "+moveTimeSeconds);
					}
				}
				assert moveTimeSeconds >= 0 : ("moveTimeSeconds == " + moveTimeSeconds + ", but it should be positive, e.getTimeStamp() == " + e.getTimeStamp() + ", this.getTotalElapsedSeconds() == " + this.getTotalElapsedSeconds() + ", goBackwardsInTimeToApplyOldestNewEvent == " + goBackwardsInTimeToApplyOldestNewEvent + ", goBackwardsInTimeForTailToCatchUp == " + goBackwardsInTimeForTailToCatchUp + ", getLastAppliedEvent() == " + (getLastAppliedEvent() != null ? getLastAppliedEvent().getTimeStamp() : null));//+", lastTimeUpdated == "+lastTimeUpdated);
				doMove(moveTimeSeconds, getTotalElapsedSeconds());
				setTotalElapsedSeconds(getTotalElapsedSeconds() + moveTimeSeconds);
				setElapsedSeconds(getElapsedSeconds() - moveTimeSeconds);
				e.apply(this);
				this.setLastAppliedEvent(e);
			//System.out.println(this.getClass().getSimpleName()+": applied event! e.getTimeStamp() == "+e.getTimeStamp()+", getTotalElapsedSeconds() == "+getTotalElapsedSeconds()+", getElapsedSeconds() == "+getElapsedSeconds()+", moveTimeSeconds == "+moveTimeSeconds+", i == "+i+" allEvents.size() == "+allEvents.size()+", getLastAppliedEvent() == "+(getLastAppliedEvent() != null ? getLastAppliedEvent().getTimeStamp() : null)+", this.getPlayers().get(0).getX() == "+this.getPlayers().get(0).getX()+", this.getPlayers().get(0).getTargetX() == "+this.getPlayers().get(0).getTargetX()+", ((PlayerMouseEvent)getLastAppliedEvent()).getX() == "+(getLastAppliedEvent() != null && getLastAppliedEvent() instanceof PlayerMouseEvent ? ((PlayerMouseEvent)getLastAppliedEvent()).getX() : null));
			}
		}
		// step forward to now - apply enough time to get the world to where it should be now.
		double endMoveTimeSeconds = getElapsedSeconds();

		if (goBackwardsInTimeForTailToCatchUp) {
			double desiredTotalElapsedSecondsForTail = getTotalElapsedSeconds() + getElapsedSeconds() - getTail().getMinTimeGapSeconds();
			double moveTimeUntilTailMadeEqualTo = desiredTotalElapsedSecondsForTail - getTotalElapsedSeconds();
			if (moveTimeUntilTailMadeEqualTo >= 0 && moveTimeUntilTailMadeEqualTo < endMoveTimeSeconds) {
				//doneOnce = true;
				doMove(moveTimeUntilTailMadeEqualTo, getTotalElapsedSeconds());
				this.setTotalElapsedSeconds(this.getTotalElapsedSeconds() + moveTimeUntilTailMadeEqualTo);
				this.setElapsedSeconds(getElapsedSeconds() - moveTimeUntilTailMadeEqualTo);
				endMoveTimeSeconds -= moveTimeUntilTailMadeEqualTo;
				//System.err.println(getClass().getSimpleName()+": ^^^ tail catch up, moveTimeUntilTailMadeEqualTo == "+moveTimeUntilTailMadeEqualTo+", desiredTotalElapsedSecondsForTail == "+desiredTotalElapsedSecondsForTail+", this.getTotalElapsedSeconds() == "+this.getTotalElapsedSeconds() + ", this.getElapsedSeconds() == "+this.getElapsedSeconds()+", getLastAppliedEvent().getTimeStamp() == "+(getLastAppliedEvent() != null ? getLastAppliedEvent().getTimeStamp() : null));
				EventWrapper oldLastAppliedEvent = getLastAppliedEvent();
				getTail().makeEqualTo(getHead());
				assert oldLastAppliedEvent == getTail().getLastAppliedEvent() : "bugger";
				assert oldLastAppliedEvent == getLastAppliedEvent() : "bugger2";
				getTail().setElapsedSeconds(0);
			//System.err.println(getClass().getSimpleName()+": ^^^ tail catch up, getTail().getTotalElapsedSeconds() == getHead().getTotalElapsedSeconds() == "+getHead().getTotalElapsedSeconds()+", this.getTail().pureTotalElapsedNanos == "+this.getTail().pureTotalElapsedNanos+", getLastAppliedEvent().getTimeStamp() == "+(getLastAppliedEvent() != null ? getLastAppliedEvent().getTimeStamp() : null));
			} else {
			//System.err.println(getClass().getSimpleName()+": ^^^ not advancing tail, this.getTotalElapsedSeconds() == "+this.getTotalElapsedSeconds()+", getElapsedSeconds() == "+this.getElapsedSeconds()+", desiredTotalElapsedSecondsForTail == "+desiredTotalElapsedSecondsForTail+", moveTimeUntilTailMadeEqualTo == "+moveTimeUntilTailMadeEqualTo+", moveTimeSeconds == "+endMoveTimeSeconds);
			}
		}
		assert getElapsedSeconds() >= 0 : ("getElapsedSeconds() == " + getElapsedSeconds() + ", but it should be positive, this.getTotalElapsedSeconds() == " + this.getTotalElapsedSeconds() + ", goBackwardsInTimeToApplyOldestNewEvent == " + goBackwardsInTimeToApplyOldestNewEvent + ", goBackwardsInTimeForTailToCatchUp" + goBackwardsInTimeForTailToCatchUp);
		doMove(endMoveTimeSeconds, getTotalElapsedSeconds());
		//System.out.println(this.getClass().getSimpleName()+": done last move, getTotalElapsedSeconds() == "+getTotalElapsedSeconds()+", getElapsedSeconds() == "+getElapsedSeconds()+", moveTimeSeconds == "+endMoveTimeSeconds+", getLastAppliedEvent() == "+(getLastAppliedEvent() != null ? getLastAppliedEvent().getTimeStamp() : null)+", this.getPlayers().get(0).getX() == "+this.getPlayers().get(0).getX()+", this.getPlayers().get(0).getTargetX() == "+this.getPlayers().get(0).getTargetX()+", ((PlayerMouseEvent)getLastAppliedEvent()).getX() == "+(getLastAppliedEvent() != null && getLastAppliedEvent() instanceof PlayerMouseEvent ? ((PlayerMouseEvent)getLastAppliedEvent()).getX() : null));
		this.setTotalElapsedSeconds(this.getTotalElapsedSeconds() + endMoveTimeSeconds);
		this.setElapsedSeconds(0); // since (getElapsedSeconds() - moveTimeSeconds) should always be zero.
		assert this.isHead();
		assert getHead().getTotalElapsedSeconds() - getTail().getTotalElapsedSeconds() < this.getMaxTimeGapSeconds() + 0.0001 : "getHead().getTotalElapsedSeconds() == " + getHead().getTotalElapsedSeconds() + ", getTail().getTotalElapsedSeconds() == " + getTail().getTotalElapsedSeconds() + " getMaxTimeGapSeconds() == " + getMaxTimeGapSeconds();

		doMoveBasic(actualElapsedSeconds, actualTotalElapsedSeconds);
		setActualTotalElapsedSeconds(actualTotalElapsedSeconds + actualElapsedSeconds);
		
		getEventStore().clearOutOldEvents(this);
		assert headAndTailWorldObjectsNotMixed();
	}

	protected void doMove(double seconds, double timeAtStartOfMoveSeconds) {
		assert seconds >= 0 : "seconds == " + seconds + ", but it's meant to be positive";
		//System.out.println(this.getClass().getSimpleName()+".doMove(seconds == "+seconds+"),  maxUpdateElapsedSeconds == "+maxUpdateElapsedSeconds+", elapsedSeconds == "+elapsedSeconds);
		// The following code makes sure that the maximimun update time is not exceeded.
		// This minimizes the effects of differing physics, etc calculations on 
		// client and server that results from different update times.
		double remainingSeconds = seconds;
		if (remainingSeconds >= maxUpdateElapsedSeconds) {
			//System.out.println(this.getClass().getSimpleName()+": doing multiple updates since seconds ("+seconds+") is greater than  maxUpdateElapsedSeconds == "+maxUpdateElapsedSeconds+", elapsedSeconds == "+elapsedSeconds);
			while (remainingSeconds >= maxUpdateElapsedSeconds) {
				doMaxTimeMove(maxUpdateElapsedSeconds, timeAtStartOfMoveSeconds);
				remainingSeconds -= maxUpdateElapsedSeconds;
				if (remainingSeconds < 0){
					remainingSeconds = 0;
				}
				timeAtStartOfMoveSeconds += maxUpdateElapsedSeconds;
			}
		}
		if (remainingSeconds > 0){
			doMaxTimeMove(remainingSeconds, timeAtStartOfMoveSeconds);
		}
	}

	public long getSystemNanosAtStart() {
		return systemNanosAtStart;
	}

	public void setSystemNanosAtStart(long systemNanosAtStart) {
		this.systemNanosAtStart = systemNanosAtStart;
	}

	public long getPureElapsedNanos() {
		return pureElapsedNanos;
	}

	public long getPureTotalElapsedNanos() {
		return pureTotalElapsedNanos;
	}

	public double getElapsedSeconds() {
		return elapsedSeconds;
	}

	public double getTotalElapsedSeconds() {
		return totalElapsedSeconds;
	}

	public float getTimeMultiplier() {
		return (float) timeMultiplier;
	}

	public void setTimeMultiplier(double timeMultiplierToBe) {
		timeMultiplier = timeMultiplierToBe;
	}

	public EventStore getEventStore() {
		return eventStore;
	}

	public void setEventStore(EventStore eventStore) {
		this.eventStore = eventStore;
	}

	public EventWrapper getLastAppliedEvent() {
		return lastAppliedEvent;
	}

	public void setLastAppliedEvent(EventWrapper lastAppliedEvent) {
		this.lastAppliedEvent = lastAppliedEvent;
	}

	public RewindableWorld getHead() {
		if (this.isHead()) {
			return this;
		} else {
			return this.getTwin();
		}
	}

	public RewindableWorld getTail() {
		if (this.isHead()) {
			return this.getTwin();
		} else {
			return this;
		}
	}

	public boolean isHead() {
		return head;
	}

	public void setHead(boolean head) {
		this.head = head;
	}

	public RewindableWorld getTwin() {
		return twin;
	}

	public void setTwin(RewindableWorld twin) {
		this.twin = twin;
	}

	public double getMinTimeGapSeconds() {
		return minTimeGapSeconds;
	}
	
	public double getMaxTimeGapSeconds() {
		return maxTimeGapSeconds;
	}

	public void setMaxTimeGapSeconds(double newMaxTimeGapSeconds) {
		// here we set tail and head world maxTimeGapSeconds.
		getHead().maxTimeGapSeconds = newMaxTimeGapSeconds;
		getTail().maxTimeGapSeconds = newMaxTimeGapSeconds;
	}
	public void setMinTimeGapSeconds(double newMinTimeGapSeconds) {
		// here we set tail and head world maxTimeGapSeconds.
		getHead().minTimeGapSeconds = newMinTimeGapSeconds;
		getTail().minTimeGapSeconds = newMinTimeGapSeconds;
	}

//	
//	transient FieldCache fieldCacheForCheck = new FieldCache();
//	transient WeakSSObjectMap weakSSObjectMapForCheck = new WeakSSObjectMap<SSObject, Object>();
//	transient ArrayList<Object> memberObjectsToCopyByReferenceForCheck = new ArrayList<Object>();
//	public boolean tailWorldSSCodesAreStillTaken(){
//		weakSSObjectMapForCheck.clear();
//		memberObjectsToCopyByReferenceForCheck.clear();
//		RewindableWorld otherWorld = this.getHead();
//		if (otherWorld.getLastAppliedEvent() != null) {
//			memberObjectsToCopyByReferenceForCheck.add(otherWorld.getLastAppliedEvent());
//		}
//		WeakSSObjectMap<SSObject, Object> thisObjectsSSObjects = new WeakSSObjectMap<SSObject, Object>();
//		HashMap<Object, Object> thisObjectsNonSSObjects = new HashMap<Object, Object>();
//		getTail().collectMemberSSObjects(fieldCacheForCheck, thisObjectsSSObjects, thisObjectsNonSSObjects);
//		Set<SSObject> keySet = thisObjectsSSObjects.keySet();
//		for (SSObject sso : keySet){
//			//if (SSTools.getSSObjectFromSSCodeForTesting(sso.getSSCode()) == null){
//			//System.err.println(this.getClass().getSimpleName()+": tail world ssSbject's ssCode does not correspeond to an ssObject in SSTool's list. sso.getClass().getSimpleName() == "+sso.getClass().getSimpleName()+" decoded sso.getSSCode() == "+SSTools.decode(sso.getSSCode())[0]);
//			//}
//			if (SSTools.getSSObjectFromSSCodeForTesting(sso.getSSCode()) != null && SSTools.getSSObjectFromSSCodeForTesting(sso.getSSCode()).getClass() != sso.getClass()){
//				SSObject otherObj = SSTools.getSSObjectFromSSCodeForTesting(sso.getSSCode());
//				System.err.println(this.getClass().getSimpleName()+": tail world ssSbject's ssCode corresponds to the wrong type of ssObject in SSTool's list! sso.getClass().getSimpleName() == "+sso.getClass().getSimpleName()+" decoded sso.getSSCode() == "+SSTools.decode(sso.getSSCode())[0]+", "+SSTools.decode(sso.getSSCode())[1]+", otherObj.getClass().getSimpleName() == "+otherObj.getClass().getSimpleName()+" decoded otherObj.getSSCode() == "+SSTools.decode(otherObj.getSSCode())[0]+", "+SSTools.decode(otherObj.getSSCode())[1]);
//			}
//			//return false;
//		}
//		return true;
//	}
	
	
	//transient ArrayList<SSObject> listOfTailSSObjects;
	// Convenience method
	public void makeEqualTo(RewindableWorld otherWorld) {
		weakSSObjectMapForMakeEqualTo.clear();
		memberObjectsToCopyByReferenceForMakeEqualTo.clear();
		if (otherWorld.getLastAppliedEvent() != null) {
			memberObjectsToCopyByReferenceForMakeEqualTo.add(otherWorld.getLastAppliedEvent());
		}
		this.makeEqualTo(otherWorld, fieldCacheForMakeEqualTo, weakSSObjectMapForMakeEqualTo, memberObjectsToCopyByReferenceForMakeEqualTo);
		assert headAndTailWorldObjectsNotMixed();
		
//		if (this.isHead() == false){
//			Set<SSObject> set = weakSSObjectMapForMakeEqualTo.keySet();
//			for (SSObject sso : set){
//				SSTools.addTailObject(sso);
//			}
//		}
		
//		if (this.isHead() == false){
//			if (listOfTailSSObjects == null){
//				listOfTailSSObjects = new ArrayList<SSObject>();
//			}
//			listOfTailSSObjects.clear();
//			Set<SSObject> set = weakSSObjectMapForMakeEqualTo.keySet();
//			for (SSObject sso : set){
//				listOfTailSSObjects.add(SSTools.getSSObjectFromSSCodeForTesting(sso.getSSCode()));
//			}
//		}
		
		weakSSObjectMapForMakeEqualTo.clear();
		memberObjectsToCopyByReferenceForMakeEqualTo.clear();
	}

	public boolean headAndTailWorldObjectsNotMixed() {
		boolean notMixed = true;
		WeakSSObjectMap<SSObject, Object> headObjectsSSObjects = new WeakSSObjectMap<SSObject, Object>();
		HashMap<Object, Object> headObjectsNonSSObjects = new HashMap<Object, Object>();
		getHead().collectMemberSSObjects(new FieldCache(), headObjectsSSObjects, headObjectsNonSSObjects);

		WeakSSObjectMap<SSObject, Object> tailObjectsSSObjects = new WeakSSObjectMap<SSObject, Object>();
		HashMap<Object, Object> tailObjectsNonSSObjects = new HashMap<Object, Object>();
		getTail().collectMemberSSObjects(new FieldCache(), tailObjectsSSObjects, tailObjectsNonSSObjects);

		Set<SSObject> headSSObjectSet = headObjectsSSObjects.keySet();
		for (SSObject headSSO : headSSObjectSet) {
			assert headSSO instanceof ClientWorldUpdate == false : headSSO;
			if (this.getLastAppliedEvent() != null && (headSSO == this.getLastAppliedEvent() || headSSO == this.getLastAppliedEvent().getUnderlyingEvent())) {
				// Since the lastAppliedEvent will be the same in both worlds since it's meant to be, exclude it from the check.
				continue;
			}
			if (headSSO == tailObjectsSSObjects.modifiedGet(headSSO.getSSCode())) {
				notMixed = false;
				System.err.println(this.getClass().getSimpleName() + ": headSSO == tailObjectsSSObjects.modifiedGet(headSSO.getSSCode()), headSSO == " + headSSO);//+", ((MachineGun)headSSO).getPlayer() == "+((MachineGun)headSSO).getPlayer());
			}
		}
		if (notMixed == false) {
			System.err.println(this.getClass().getSimpleName() + ": headObjectsSSObjects.size() == " + headObjectsSSObjects.size() + ", tailObjectsSSObjects.size() == " + tailObjectsSSObjects.size());
		}
		return notMixed;
	}

	public void writeSS(SSObjectOutputStream out) throws IOException {		// this is the method that you over-ride if you want custom serialization
		// this test fails during GameFrame.doJoinGame method since the ClientController is not set until the game is fully joined: assert GameFrame.getStaticGameFrame().getController() instanceof ServerController : GameFrame.getStaticGameFrame().getController().getClass().getSimpleName() + " should never write its world to the server since the server's world is the main one.";
		assert this.isHead() : "isHead() == false, so the tail is being written which should never happen";
		super.writeSS(out);
	}

	public void readSS(SSObjectInputStream in) throws java.io.IOException {	// this is the method that you over-ride if you want custom serialization
		// this test fails during GameFrame.doJoinGame method since the ClientController is not set until the game is fully joined: assert GameFrame.getStaticGameFrame().getController() instanceof ClientController : GameFrame.getStaticGameFrame().getController().getClass().getSimpleName() + " should never over-write its world with the world sent from the clients.";
		assert this.isHead() : "isHead() == false, so the tail is being written which should never happen";
		super.readSS(in);
	}

	public Controller getController() {
		return controller;
	}
	
	public void setController(Controller controller) {
		this.controller = controller;
	}

	public double getTimeNowSeconds() {
		// Must do timeStamp according to the Server's clock, so we need to get the clock difference.
		long serverClockDiffNanos = getController().getServerClockDiffNanos();
		return getTimeMultiplier() * (MockSystem.nanoTime() + serverClockDiffNanos - getSystemNanosAtStart()) / RewindableWorld.NANOS_IN_A_SECOND;
	}

	public int getNumDoMaxTimeMoves() {
		return numDoMaxTimeMoves;
	}

	// This flag sets whether the doMove method should be called between events when
	// those events have the same timeStmap. If it is true, doMove(0) is called in between.
	// Otherwise, doMove is skipped and the next event is just applied.
	public boolean isDoMoveBetweenEventsIfTimeStampsEqual() {
		return doMoveBetweenEventsIfTimeStampsEqual;
	}

	public double getEventTimeStampMultipleSeconds() {
		return eventTimeStampMultipleSeconds;
	}

	public void setDoMoveBetweenEventsIfTimeStampsEqual(boolean doMoveBetweenEventsIfTimeStampsEqual) {
		this.doMoveBetweenEventsIfTimeStampsEqual = doMoveBetweenEventsIfTimeStampsEqual;
	}
	double previousEventTimeStamp = -Double.MAX_VALUE;

	public double getEventTimeStampNowSeconds() {
		double timeStamp = getTimeNowSeconds();
		//double copyOfOriginalTimeStamp = timeStamp;
		if (isDoMoveBetweenEventsIfTimeStampsEqual() == false) {
			// This code makes it so that timeStamps are in multiples of eventTimeStampMultipleSeconds. 
			// This has the effect of giving multiple timeStamps that are close to one another in time 
			// the exact same timeStamp. This can mean that the GameWorld need not doMove after every  
			// single event if the two events have the same timeStamp and 
			// GameWorld.isDoMoveBetweenEventsIfTimeStampsEqual() is false.
			double newEventTimeStamp = calculateCurrentEventTimeStamp(timeStamp);
			assert newEventTimeStamp >= timeStamp : newEventTimeStamp + ", " + timeStamp;
			timeStamp = newEventTimeStamp;
		}

		// Here we make sure that the timeStamp about to be given to this event is equal to or
		// bigger than the last one we gave.  Must guarantee the time order of all events,
		// or else there will be trouble (for example in the Player class's getNewUserEvents() method).
		while (previousEventTimeStamp > timeStamp) {
			System.err.println(this.getClass().getSimpleName() + ": previousEventTimeStamp == " + previousEventTimeStamp + ", timeStamp == " + timeStamp + ", getSystemNanosAtStart() == " + getSystemNanosAtStart());
			timeStamp = previousEventTimeStamp;
			System.err.println(this.getClass().getSimpleName() + ": newly adjusted timeStamp == " + timeStamp);
		}
		previousEventTimeStamp = timeStamp;
		//System.out.println(this.getClass().getSimpleName()+": copyOfOriginalTimeStamp == "+copyOfOriginalTimeStamp+", timeStamp == "+timeStamp+", isDoMoveBetweenEventsIfTimeStampsEqual() == "+isDoMoveBetweenEventsIfTimeStampsEqual());
		return timeStamp;
	}

	public double calculateCurrentEventTimeStamp(double timeStamp) {
		return getEventTimeStampMultipleSeconds() * Math.ceil(timeStamp / getEventTimeStampMultipleSeconds());
	}

	public void setEventTimeStampMultipleSeconds(double eventTimeStampMultipleSeconds) {
		this.eventTimeStampMultipleSeconds = eventTimeStampMultipleSeconds;
	}

	public double getMaxUpdateElapsedSeconds() {
		return maxUpdateElapsedSeconds;
	}

	public void setMaxUpdateElapsedSeconds(double maxUpdateElapsedSeconds) {
		this.maxUpdateElapsedSeconds = maxUpdateElapsedSeconds;
	}
}
