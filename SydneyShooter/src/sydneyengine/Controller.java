package sydneyengine;

// author: Keith Woodward, keithphw@hotmail.com

import java.io.IOException;

import sydneyengine.shooter.GameWorld;
import sydneyengine.shooter.Player;
import sydneyengine.shooter.ViewPane;
import sydneyengine.superserializable.SSObjectInputStream;
import sydneyengine.superserializable.SSObjectOutputStream;
/**
 * Controller contains the game loop which runs everything. It should get new messages, update the game world, render and sleep. 
 * @author woodwardk
 */
public abstract class Controller implements Runnable{
	
	protected volatile boolean shouldRun = false;
	protected volatile boolean shouldPause = false;
	protected volatile boolean closed = false;
	
	// the minimum time to sleep after each update. if negative, no sleep.
	protected volatile int minSleepMillisBetweenUpdates = 20;
	
	long oldSystemTimeNanos = -1;
	
	protected Object closeAndWaitMutex = new Object();
	protected Object pauseMutex = new Object();
	protected Object waitForPauseMutex = new Object();
	
	protected FPSCounter fpsCounter;
	
	protected GameWorld world;
	protected Sender sender;
	
	protected SSObjectOutputStream ssout;
	protected SSObjectInputStream ssin;
	public SSObjectInputStream getSSIn(){
		return ssin;
	}
	public SSObjectOutputStream getSSOut(){
		return ssout;
	}
	
	public Controller(){
		try{
			ssout = new SSObjectOutputStream();
			ssin = new SSObjectInputStream();
			ssout.syncStoredObjectsWith(ssin);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public abstract ReceiverPolling getReceiver();
	public abstract EventStore getEventStore();
	public abstract void nexusThrewException(Nexus nexus);
	public abstract long getLatencyToServerNanos();
	public abstract long getServerClockDiffNanos();
	public abstract Player getPlayer();
	public abstract ViewPane getViewPane();
	
	
	void doPause(){
		System.out.println(this.getClass().getSimpleName()+": doPause()");
		synchronized(waitForPauseMutex){
			System.out.println(this.getClass().getSimpleName()+": waitForPauseMutex.notifyAll();");
			waitForPauseMutex.notifyAll();
		}
		synchronized (pauseMutex){
			if (shouldPause == true){
				try{
					System.out.println(this.getClass().getSimpleName()+": pauseMutex.wait();");
					pauseMutex.wait();
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}
		}
	}
	
	public void doMinSleep(){
		if (minSleepMillisBetweenUpdates >= 0){
			try{
				Thread.sleep(minSleepMillisBetweenUpdates);
				//System.out.println(this.getClass().getSimpleName()+": sleeping for "+minSleepMillisBetweenUpdates);
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		Thread.yield();
	}
	
	public void close(){
		System.out.println(this.getClass().getSimpleName()+": closing");
		shouldRun = false;
		unpause();
		closed = true;
	}
	public boolean isClosed(){
		return closed;
	}
	
	public boolean isRunning(){
		return shouldRun;
	}
	public void closeAndWait(){
		unpause();
		synchronized (closeAndWaitMutex){
			this.shouldRun = false;
			try{
				closeAndWaitMutex.wait();
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		close();
	}
	public void closeAndWait(long millis){
		unpause();
		synchronized (closeAndWaitMutex){
			this.shouldRun = false;
			try{
				closeAndWaitMutex.wait(millis);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		close();
	}
	public boolean isPaused(){
		return shouldPause;
	}
	public void pause(){
		shouldPause = true;
	}
	public void pauseAndWait(){
		synchronized (waitForPauseMutex){
			pause();
			try{
				System.out.println(this.getClass().getSimpleName()+": waitForPauseMutex.wait();");
				waitForPauseMutex.wait();
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	public void unpause(){
		System.out.println("Controller: Unpause() method:");
		synchronized (waitForPauseMutex){
			shouldPause = false;
			System.out.println(this.getClass().getSimpleName()+": waitForPauseMutex.notifyAll();");
			waitForPauseMutex.notifyAll();
		}
		synchronized (pauseMutex){
			shouldPause = false;
			System.out.println(this.getClass().getSimpleName()+": pauseMutex.notifyAll();");
			pauseMutex.notifyAll();
		}
	}
	public int getSleepBetweenUpdatesMillis(){
		return minSleepMillisBetweenUpdates;
	}
	public void setSleepBetweenUpdatesMillis(int newMinSleepMillisBetweenUpdates){
		minSleepMillisBetweenUpdates = newMinSleepMillisBetweenUpdates;
	}

	public boolean isShouldRun() {
		return shouldRun;
	}

	public boolean isShouldPause() {
		return shouldPause;
	}

	public FPSCounter getFPSCounter() {
		return fpsCounter;
	}

	public void setFPSCounter(FPSCounter fpsCounter) {
		this.fpsCounter = fpsCounter;
	}

	public GameWorld getWorld() {
		return world;
	}

	public void setWorld(GameWorld world) {
		this.world = world;
	}

	public Sender getSender() {
		return sender;
	}

	public void setSender(Sender sender) {
		this.sender = sender;
	}
	
}