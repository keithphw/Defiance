/*
 * LatencyInfo.java
 *
 * Created on 30 June 2007, 01:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine;

import java.util.*;
import java.io.*;
import sydneyengine.superserializable.*;
/**
 * Stores the server clock difference and latency between the Nexus on the client and the Nexus on the server.
 * The server clock difference is defined as the time that you have to add onto the client's clock time to get the server's clock time. So (client)System.nanoTIme() + getServerClockDiffNanos() == (server)System.nanoTime().
 *
 * @author CommanderKeith
 */
public class LatencyInfo extends SSAdapter{
	protected long latencyToServerNanos = 0;
	protected long serverClockDiffNanos = 0;
	
	public LatencyInfo() {
	}
	
	public long getLatencyToServerNanos(){
		return latencyToServerNanos;
	}
	public void setLatencyToServerNanos(long latencyToServerNanos){
		this.latencyToServerNanos = latencyToServerNanos;
	}
	public long getServerClockDiffNanos(){
		return serverClockDiffNanos;
	}
	public void setServerClockDiffNanos(long serverClockDiffNanos){
		this.serverClockDiffNanos = serverClockDiffNanos;
	}
}

