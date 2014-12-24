/*
 * ClientController.java
 *
 * Created on 15 November 2007, 15:01
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine;

import sydneyengine.shooter.Player;
import sydneyengine.shooter.GameFrame;
import sydneyengine.shooter.ViewPane;
import java.io.*;
import java.util.*;

/**
 *
 * @author CommanderKeith
 */
public class ClientController extends Controller implements PlayingController{
	protected GameFrame gameFrame;
	protected Player player;	    // the player that the person on this computer controls
	protected ViewPane viewPane;	// the thing we call render() on.
	
	protected Nexus nexus;
	protected Sender sender;
	protected ReceiverPollingClient receiver = null;
	protected EventStoreClient eventStore;
	Object latestClientWorldUpdateMutex = new Object();
	protected ClientWorldUpdate latestClientWorldUpdate = null;
	volatile protected boolean newClientWorldUpdateAvailable = false;
		
	public ClientController(GameFrame gameFrame, Player player, Nexus nexus, EventStoreClient eventStore){
		this(gameFrame, player, nexus, eventStore, null);
	}
	
	/** Creates a new instance of ClientController */
	public ClientController(GameFrame gameFrame, Player player, Nexus nexus, EventStoreClient eventStore, Sender sender){
		super();
		this.gameFrame = gameFrame;
		setPlayer(player);
		this.nexus = nexus;
		this.eventStore = eventStore;
		this.sender = sender;
		receiver = new ReceiverPollingClient(this);
		
		fpsCounter = new FPSCounter(this);
	}
	
	public void run(){
		System.out.println(this.getClass().getSimpleName()+": starting");
		long nanoTimeNow = MockSystem.nanoTime() + getServerClockDiffNanos();	// getServerClockDiffNanos() will always be zero, so don't really need it...
		oldSystemTimeNanos = nanoTimeNow;
		shouldRun = true;
		receiver.start();
		while(shouldRun){
			fpsCounter.update();
			// check for exit or pause
			if (shouldRun == false){break;}
			if (shouldPause == true){
				doPause();
				continue;
			}
			
			// Check if a world update is available from the server and if so, 
			// deserialize it which will automatically update this controller's head world.
			if (isNewClientWorldUpdateAvailable()){
				newClientWorldUpdateAvailable = false;
				ClientWorldUpdate clientWorldUpdate = this.getLatestClientWorldUpdate();
				try{
					clientWorldUpdate.deserialize(getSSIn());
				}catch(IOException e){
					e.printStackTrace();
					close();
				}
				// Note that deserialization will automatically update this 
				// controller's head world since the ssin stream has the head 
				// world in its storedObjects, so all deserializations update that world. 
				// This is also why the deserialize method must be called in this 
				// thread - to prevent updates to the world from clashing with 
				// updates from deserialization.
				assert clientWorldUpdate.getWorld() == getWorld().getHead() : "clientWorldUpdate.getWorld() == "+clientWorldUpdate.getWorld()+", getWorld.getHead() == "+getWorld().getHead();
				// Make the tail world equal to the head world, because otherwise 
				// the update from the server will be over-written when the client
				// goes back in time to its un-updated tail world.
				// The world just received from the server is a head world, but 
				// since it had makeEqualTo(getWorld().getTail()) called on it before 
				// sending, the server head world can be used as a tail world.
				getWorld().getTail().makeEqualTo(getWorld().getHead());
			}
			// Make new game events from any relevant java.awt.event.KeyEvents or MouseEvents
			//this.getViewPane().makeNewEvents();
			// Serialize and send any new game events, and re-order the main events list 
			// after adding any new events recieved from the server.
			this.getWorld().getEventStore().processNewEvents();
			
			long currentSystemTimeNanos = MockSystem.nanoTime() + getServerClockDiffNanos();	// get the current time	
			long actualTimeElapsedNanos = currentSystemTimeNanos - oldSystemTimeNanos;
			this.oldSystemTimeNanos = currentSystemTimeNanos;
			long timeElapsedNanos = (currentSystemTimeNanos - (world.getSystemNanosAtStart() + world.getPureTotalElapsedNanos()));// work out the time since the last updateNanos    // russian: 'good-night' == 'do svedanye', bye == 'poka', shut up = 'zat nis', how's it going = 'cac de la?', it is good = 'e ta harashaw'
			// Update the world using the time elapsed
			world.updateNanos(timeElapsedNanos, actualTimeElapsedNanos);
			
			// Let the viewPane update any AWT Components that implement Updatable:
			getViewPane().doMove(timeElapsedNanos, world.getTotalElapsedSeconds() - timeElapsedNanos);
			
			// check for exit or pause
			if (shouldRun == false){break;}
			if (shouldPause == true){
				doPause();
				continue;
			}
			viewPane.render();
			// check for exit or pause
			if (shouldRun == false){break;}
			if (shouldPause == true){
				doPause();
				continue;
			}
			doMinSleep();	// gives the other threads a turn
		}
		System.out.println(this.getClass().getSimpleName()+": game loop finished 1");
		synchronized(closeAndWaitMutex){
			closeAndWaitMutex.notifyAll();
		}
		System.out.println(this.getClass().getSimpleName()+": game loop finished 2");
	}
	
	public void close(){
		super.close();
		if (getReceiver() != null){
			getReceiver().close();
		}
		try{
			nexus.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		if (getSender() != null){
			getSender().close();
		}
	}
	
	public void nexusThrewException(Nexus nexus){
		assert getNexus() == nexus : "How can the nexus that threw the error be different to the nexus in this ClientController?! There's only meant to be one nexus.";
		getGameFrame().doStart();
	}
	public long getLatencyToServerNanos(){
		return this.getNexus().getLatencyCalculator().getLatencyInfo().getLatencyToServerNanos();
	}
	public long getServerClockDiffNanos(){
		return this.getNexus().getLatencyCalculator().getLatencyInfo().getServerClockDiffNanos();
	}

	public Nexus getNexus() {
		return nexus;
	}

	public void setNexus(Nexus nexus) {
		this.nexus = nexus;
	}

	public ReceiverPollingClient getReceiver() {
		return receiver;
	}

	public void setReceiver(ReceiverPollingClient receiver) {
		this.receiver = receiver;
	}

	public EventStoreClient getEventStore() {
		return eventStore;
	}

	public void setEventStore(EventStoreClient eventStore) {
		this.eventStore = eventStore;
	}
	
	public ClientWorldUpdate getLatestClientWorldUpdate() {
		synchronized (latestClientWorldUpdateMutex){
			return latestClientWorldUpdate;
		}
	}

	public void setLatestClientWorldUpdate(ClientWorldUpdate latestClientWorldUpdate) {
		synchronized (latestClientWorldUpdateMutex){
			this.latestClientWorldUpdate = latestClientWorldUpdate;
			newClientWorldUpdateAvailable = true;
		}
	}

	public boolean isNewClientWorldUpdateAvailable() {
		return newClientWorldUpdateAvailable;
	}
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public ViewPane getViewPane() {
		return viewPane;
	}

	public void setViewPane(ViewPane viewPane) {
		this.viewPane = viewPane;
	}
	
	public GameFrame getGameFrame() {
		return gameFrame;
	}

	public void setGameFrame(GameFrame gameFrame) {
		this.gameFrame = gameFrame;
	}

}
