/*
 * FPSCounter.java
 *
 * Created on 22 April 2007, 21:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine;
/**
 * Convenience class that lets you know about performance.
 *
 * @author Keith Woodward
 */
public class FPSCounter{
	
	protected Controller controller;
	
	// the following can be used for calculating frames per second:
	protected long lastUpdateNanos = -1;
	protected long cumulativeTimeBetweenUpdatesNanos = 0;
	protected float avTimeBetweenUpdatesMillis = -1f;
	protected int counter = 0;
	protected long timeBetweenUpdatesNanos = 500000000; // 1/2 second == 500000000 nanoseconds
	
	
	protected long freeMemory = Runtime.getRuntime().freeMemory();
	protected long totalMemory = Runtime.getRuntime().totalMemory();
	protected long usedMemory = totalMemory - freeMemory;
	
	
	
	public FPSCounter(Controller controller) {
		this.controller = controller;
	}
	public void update(){
		if (lastUpdateNanos == -1){
			lastUpdateNanos = MockSystem.nanoTime();
		}
		long newUpdateNanos = MockSystem.nanoTime();
		cumulativeTimeBetweenUpdatesNanos += newUpdateNanos - lastUpdateNanos;//controller.getWorld().getPureElapsedNanos();
		lastUpdateNanos = newUpdateNanos;
		counter++;
		if (cumulativeTimeBetweenUpdatesNanos >= timeBetweenUpdatesNanos){
			avTimeBetweenUpdatesMillis = (cumulativeTimeBetweenUpdatesNanos)/(counter*1000000f);
			freeMemory = Runtime.getRuntime().freeMemory();
			totalMemory = Runtime.getRuntime().totalMemory();
			usedMemory = totalMemory - freeMemory;
			cumulativeTimeBetweenUpdatesNanos = 0;
			counter = 0;
		}
	}
	
	public float getAvTimeBetweenUpdatesMillis(){
		return avTimeBetweenUpdatesMillis;
	}
	public int getAvTimeBetweenUpdatesMillisRounded(){
		return Math.round(getAvTimeBetweenUpdatesMillis());
	}
	public float getFPS(){
		return getAvTimeBetweenUpdatesMillis() != 0 ? 1000f/getAvTimeBetweenUpdatesMillis() : -1;
	}
	public int getFPSRounded(){
		return Math.round(this.getFPS());
	}
	public int getCounter(){
		return counter;
	}
	public long getTimeBetweenUpdatesNanos(){
		return timeBetweenUpdatesNanos;
	}
	public void setTimeBetweenUpdatesNanos(long timeBetweenUpdatesNanos){
		this.timeBetweenUpdatesNanos = timeBetweenUpdatesNanos;
	}

	public long getFreeMemory() {
		return freeMemory;
	}

	public long getTotalMemory() {
		return totalMemory;
	}

	public long getUsedMemory() {
		return usedMemory;
	}
}
