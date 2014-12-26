/*
 * LatencyCalculator.java
 *
 * Created on 28 June 2007, 00:22
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine;

import java.io.IOException;
import java.util.ArrayList;

import sydneyengine.superserializable.SSObjectInputStream;
import sydneyengine.superserializable.SSObjectOutputStream;
/**
 * The purpose of this class is to work out the latency and clock difference between the Nexus on the client and its corresponding Nexus on the server.
 * The server clock difference is defined as the time that you have to add onto the client's clock time to get the server's clock time. So (client)System.nanoTime() + getServerClockDiffNanos() == (server)System.nanoTime().
 *
 * @author CommanderKeith
 */
public class LatencyCalculator{
	
	protected Nexus nexus;
	
	protected LatencyInfo latencyInfo = new LatencyInfo();
	
	// the max size of latencies and clockDeltas.
	protected int numLatencyChecksToKeep = 1;
	protected ArrayList<Long> latencies = new ArrayList<Long>();
	protected ArrayList<Long> clockDeltas = new ArrayList<Long>();
	
	protected LatencyChecker latencyChecker = null;
	protected volatile int sleepTimeLatencyCheckerMillis = 3000;
	
	protected SSObjectInputStream tempSSIn;
	protected SSObjectOutputStream tempSSOut;
	
	/**
	 * Creates a new instance of LatencyCalculator
	 */
	public LatencyCalculator(Nexus nexus) {
		this.nexus = nexus;
		try {
			tempSSIn = new SSObjectInputStream();
			tempSSOut = new SSObjectOutputStream();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	public void startLatencyChecker(){
		latencyChecker = new LatencyChecker();
		latencyChecker.start();
	}
	public void stopLatencyChecker(){
		if (latencyChecker != null){
			latencyChecker.close();
		}
	}
	public void initiateLatencyRequest() throws IOException{
		LatencyPostCard latencyInfoRecord = new LatencyPostCard(MockSystem.nanoTime());
		getNexus().send(Nexus.LATENCY_REQUEST, latencyInfoRecord, getTempSSOut());
	}
	public void respondToLatencyRequest(LatencyPostCard latencyInfoRecord) throws IOException{
		latencyInfoRecord.setResponderTimeAtRecieveNanos(MockSystem.nanoTime());
		getNexus().send(Nexus.LATENCY_RESPONSE, latencyInfoRecord, getTempSSOut());
	}
	public void computeLatencyAndClockDiff(LatencyPostCard latencyInfoRecord){
		latencyInfoRecord.setInitiatorTimeAtRecieveNanos(MockSystem.nanoTime());
		long nanoTimeClientAtSend = latencyInfoRecord.geInitiatorTimeAtSendNanos();
		long nanoTimeServer = latencyInfoRecord.getResponderTimeAtRecieveNanos();
		long nanoTimeClientAtRecieve = latencyInfoRecord.getInitiatorTimeAtRecieveNanos();
		System.out.println(this.getClass().getSimpleName()+": nanoTimeClientAtSend == "+nanoTimeClientAtSend+", nanoTimeServer == "+nanoTimeServer+", nanoTimeClientAtRecieve == "+nanoTimeClientAtRecieve);
		long latency = (long)((nanoTimeClientAtRecieve - nanoTimeClientAtSend)/2f);
		//long clockDelta = nanoTimeServer - nanoTimeClientAtRecieve + latency;
		long clockDelta = nanoTimeClientAtRecieve - latency - nanoTimeServer;
		
		latencies.add(latency);
		clockDeltas.add(clockDelta);
		
		if (latencies.size() > getNumLatencyChecksToKeep()){
			latencies.remove(0);
			clockDeltas.remove(0);
		}
		
		long avLatency = 0;
		long avClockDelta = 0;
		for (int i = 0; i < latencies.size(); i++){
			System.out.println(this.getClass().getSimpleName()+": latencies.get(i) == "+latencies.get(i));
			System.out.println(this.getClass().getSimpleName()+": clockDeltas.get(i) == "+clockDeltas.get(i));
			avLatency += latencies.get(i);
			avClockDelta += clockDeltas.get(i);
		}
		if (latencies.size() != 0){
			avLatency /= latencies.size();
			avClockDelta /= clockDeltas.size();
		}
		latencyInfo.setLatencyToServerNanos(avLatency);
		latencyInfo.setServerClockDiffNanos(avClockDelta);
		System.out.println(this.getClass().getSimpleName()+".computeLatencyAndClockDiff: avLatency == "+avLatency+", avClockDelta == "+avClockDelta);
		/*LatencyInfo tempLatencyInfo = new LatencyInfo();	// note that it's player field is null
		tempLatencyInfo.setLatencyToServerNanos(avLatency);
		tempLatencyInfo.setServerClockDiffNanos(avClockDelta);
		//System.out.println(this.getClass().getSimpleName()+"computeLatencyAndClockDiff: this.getNexus().getPlayer().getName() == "+((AbstractGamePlayer)this.getNexus().getPlayer()).getName()+" tempLatencyInfo.getLatencyToServerNanos() == "+tempLatencyInfo.getLatencyToServerNanos());
		//System.out.println(this.getClass().getSimpleName()+"computeLatencyAndClockDiff: this.getNexus().getPlayer().getName() == "+((AbstractGamePlayer)this.getNexus().getPlayer()).getName()+" tempLatencyInfo.getServerClockDiffNanos() == "+tempLatencyInfo.getServerClockDiffNanos());
		return tempLatencyInfo;*/
	}
	public void computeLatency(LatencyPostCard latencyInfoRecord){
		latencyInfoRecord.setInitiatorTimeAtRecieveNanos(MockSystem.nanoTime());
		long nanoTimeClientAtSend = latencyInfoRecord.geInitiatorTimeAtSendNanos();
		long nanoTimeServer = latencyInfoRecord.getResponderTimeAtRecieveNanos();
		long nanoTimeClientAtRecieve = latencyInfoRecord.getInitiatorTimeAtRecieveNanos();
		
		long latency = (long)((nanoTimeClientAtRecieve - nanoTimeClientAtSend)/2f);
		//long clockDelta = nanoTimeClientAtRecieve - latency - nanoTimeServer;
		latencies.add(latency);
		//clockDeltas.add(clockDelta);
		
		if (latencies.size() > getNumLatencyChecksToKeep()){
			latencies.remove(0);
			//clockDeltas.remove(0);
		}
		
		long avLatency = 0;
		long avClockDelta = 0;
		for (int i = 0; i < latencies.size(); i++){
			//System.out.println(this.getClass().getSimpleName()+": latencies.get(i) == "+latencies.get(i));
			//System.out.println(this.getClass().getSimpleName()+": clockDeltas.get(i) == "+clockDeltas.get(i));
			avLatency += latencies.get(i);
			//avClockDelta += clockDeltas.get(i);
		}
		if (latencies.size() != 0){
			avLatency /= latencies.size();
			//avClockDelta /= clockDeltas.size();
		}
		latencyInfo.setLatencyToServerNanos(avLatency);
		
		/*
		LatencyInfo tempLatencyInfo = new LatencyInfo();	// note that it's player field is null
		tempLatencyInfo.setLatencyToServerNanos(avLatency);
		//tempLatencyInfo.setServerClockDiffNanos(avClockDelta);
		//System.out.println(this.getClass().getSimpleName()+"computeLatencyAndClockDiff: this.getNexus().getPlayer().getName() == "+((AbstractGamePlayer)this.getNexus().getPlayer()).getName()+" tempLatencyInfo.getLatencyToServerNanos() == "+tempLatencyInfo.getLatencyToServerNanos());
		//System.out.println(this.getClass().getSimpleName()+"computeLatencyAndClockDiff: this.getNexus().getPlayer().getName() == "+((AbstractGamePlayer)this.getNexus().getPlayer()).getName()+" tempLatencyInfo.getServerClockDiffNanos() == "+tempLatencyInfo.getServerClockDiffNanos());
		return tempLatencyInfo;*/
	}
	public void initiatorSendLatencyResults() throws IOException{
		getNexus().send(Nexus.LATENCY_RESULTS, latencyInfo, getTempSSOut());
	}
	
	public class LatencyChecker extends Thread{
		volatile boolean latencyCheckerShouldRun = true;
		public LatencyChecker(){
			this.setName("LatencyCalculator.LatencyChecker "+this.getName());
			this.setDaemon(true);
		}
		@Override
		public void run(){
			while(latencyCheckerShouldRun){
				//System.out.println(this.getClass().getSimpleName()+": running");
				try{
					initiateLatencyRequest();
					try {
						sleep(getSleepTimeLatencyCheckerMillis());	// 1 second
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}
		public void close(){
			latencyCheckerShouldRun = false;
			System.out.println(this.getClass().getSimpleName()+": closed");
		}
	}
	
	public Nexus getNexus(){
		return nexus;
	}
	
	public int getSleepTimeLatencyCheckerMillis() {
		return sleepTimeLatencyCheckerMillis;
	}
	
	public void setSleepTimeLatencyCheckerMillis(int sleepTimeLatencyCheckerMillis) {
		this.sleepTimeLatencyCheckerMillis = sleepTimeLatencyCheckerMillis;
	}
	
	public int getNumLatencyChecksToKeep() {
		return numLatencyChecksToKeep;
	}
	
	public void setNumLatencyChecksToKeep(int numLatencyChecksToKeep) {
		this.numLatencyChecksToKeep = numLatencyChecksToKeep;
	}
	
	public LatencyInfo getLatencyInfo() {
		return latencyInfo;
	}
	
	public void setLatencyInfo(LatencyInfo latencyInfo) {
		this.latencyInfo = latencyInfo;
	}
	//Convenience method.
	public long getLatencyToServerNanos(){
		return getLatencyInfo().getLatencyToServerNanos();
	}
	//Convenience method. serverClockDiff is the time that you have to add onto the client's clock time to get the server's clock time.
	public long getServerClockDiffNanos(){
		return getLatencyInfo().getServerClockDiffNanos();
	}
	public SSObjectInputStream getTempSSIn() {
		return tempSSIn;
	}
	public SSObjectOutputStream getTempSSOut() {
		return tempSSOut;
	}
	
}
