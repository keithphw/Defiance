/*
 * MockSystem.java
 *
 * Created on 27 November 2007, 15:44
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine;

/**
 *
 * @author CommanderKeith
 */
public class MockSystem {

	public static long getClockIncrementNanos() {
		return clockIncrementNanos;
	}

	public static void setClockIncrementNanos(long aClockIncrementNanos) {
		clockIncrementNanos = aClockIncrementNanos;
	}
	//volatile  protected static boolean systemNanoTimeAltered = false;
	volatile protected static long clockIncrementNanos = 0;
	
	public static long nanoTime(){
		return System.nanoTime() - clockIncrementNanos;
	}
/*
	public static boolean isSystemNanoTimeAltered() {
		return systemNanoTimeAltered;
	}

	public static void setSystemNanoTimeAltered(boolean aSystemNanoTimeAltered) {
		systemNanoTimeAltered = aSystemNanoTimeAltered;
	}
	*/
}
