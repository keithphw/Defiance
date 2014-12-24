/*
 * LatencyPostCard.java
 *
 * Created on 29 June 2007, 21:46
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine;

import sydneyengine.superserializable.*;
/**
 *
 * @author CommanderKeith
 */
public class LatencyPostCard extends SSAdapter{
	protected long initiatorTimeAtSendNanos;
	protected long responderTimeAtRecieveNanos = -1;
	protected long initiatorTimeAtRecieveNanos = -1;
	
	/**
	 * Creates a new instance of LatencyPostCard
	 */
	public LatencyPostCard() {
	}
	public LatencyPostCard(long initiatorTimeAtSendNanos) {
		setInitiatorTimeAtSendNanos(initiatorTimeAtSendNanos);
	}

	public long geInitiatorTimeAtSendNanos() {
		return initiatorTimeAtSendNanos;
	}
	public void setInitiatorTimeAtSendNanos(long initiatorTimeAtSendNanos) {
		this.initiatorTimeAtSendNanos = initiatorTimeAtSendNanos;
	}
	public long getResponderTimeAtRecieveNanos() {
		return responderTimeAtRecieveNanos;
	}
	public void setResponderTimeAtRecieveNanos(long responderTimeAtRecieveNanos) {
		this.responderTimeAtRecieveNanos = responderTimeAtRecieveNanos;
	}
	public long getInitiatorTimeAtRecieveNanos() {
		return initiatorTimeAtRecieveNanos;
	}
	public void setInitiatorTimeAtRecieveNanos(long initiatorTimeAtRecieveNanos) {
		this.initiatorTimeAtRecieveNanos = initiatorTimeAtRecieveNanos;
	}
}
