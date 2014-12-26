/*
 * ReceiverPolling.java
 *
 * Created on 23 November 2007, 21:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine;

import java.io.IOException;

import sydneyengine.superserializable.SSObjectInputStream;
import sydneyengine.superserializable.SSObjectOutputStream;

/**
 *
 * @author CommanderKeith
 */
public abstract class ReceiverPolling extends Thread{
	
	volatile protected int sleepTimeAfterNoMoreRecievesMillis = 1;
	volatile protected boolean shouldRun = true;
	protected SSObjectInputStream tempSSIn;
	protected SSObjectOutputStream tempSSOut;
	
	public abstract Controller getController();
	
	public void close(){
		shouldRun = false;
	}
	
	public void doMinSleep(){
		Thread.yield();
		if (getSleepTimeAfterNoMoreRecievesMillis() != 0) {
			try {
				Thread.sleep(getSleepTimeAfterNoMoreRecievesMillis());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Deals with the message if it is latency-related, and returns true if it was dealt with.
	 * @param messagePack
	 * @param fromNexus
	 * @return
	 * @throws java.io.IOException
	 */
	public boolean doLatencyMessage(MessagePack messagePack, Nexus fromNexus) throws IOException{
		// this is where the latency requests are handled.
		if (messagePack.getType() == Nexus.LATENCY_REQUEST){
			fromNexus.getLatencyCalculator().respondToLatencyRequest((LatencyPostCard)messagePack.getObject());
			return true;
		}else if (messagePack.getType() == Nexus.LATENCY_RESPONSE){
			LatencyPostCard latencyInfoRecord = (LatencyPostCard)messagePack.getObject();
			fromNexus.getLatencyCalculator().computeLatency(latencyInfoRecord);
			fromNexus.getLatencyCalculator().initiatorSendLatencyResults();
			fromNexus.getPlayer().setLatencyToServerNanos(fromNexus.getLatencyCalculator().getLatencyInfo().getLatencyToServerNanos());
			//System.out.println(this.getClass().getSimpleName()+": received a LATENCY_RESPONSE, fromNexus.getLatencyCalculator().getLatencyInfo().getLatencyToServerNanos() == "+fromNexus.getLatencyCalculator().getLatencyInfo().getLatencyToServerNanos()+", fromNexus.getLatencyCalculator().getLatencyInfo().getServerClockDiffNanos() == "+fromNexus.getLatencyCalculator().getLatencyInfo().getServerClockDiffNanos());
			return true;
		}else if (messagePack.getType() == Nexus.LATENCY_RESULTS){
			LatencyInfo latencyInfo = (LatencyInfo)messagePack.getObject();
			fromNexus.getLatencyCalculator().setLatencyInfo(latencyInfo);
			fromNexus.getPlayer().setLatencyToServerNanos(fromNexus.getLatencyCalculator().getLatencyInfo().getLatencyToServerNanos());
			//System.out.println(this.getClass().getSimpleName()+": received LATENCY_RESULTS, fromNexus.getLatencyCalculator().getLatencyInfo().getLatencyToServerNanos() == "+fromNexus.getLatencyCalculator().getLatencyInfo().getLatencyToServerNanos()+", fromNexus.getLatencyCalculator().getLatencyInfo().getServerClockDiffNanos() == "+fromNexus.getLatencyCalculator().getLatencyInfo().getServerClockDiffNanos());
			return true;
		}
		return false;
	}
	
	
	public int getSleepTimeAfterNoMoreRecievesMillis() {
		return sleepTimeAfterNoMoreRecievesMillis;
	}
	
	public void setSleepTimeAfterNoMoreRecievesMillis(int sleepTimeAfterNoMoreRecievesMillis) {
		this.sleepTimeAfterNoMoreRecievesMillis = sleepTimeAfterNoMoreRecievesMillis;
	}
	public SSObjectInputStream getTempSSIn() {
		return tempSSIn;
	}
	public SSObjectOutputStream getTempSSOut() {
		return tempSSOut;
	}
}
