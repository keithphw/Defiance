/*
 * ServerController.java
 *
 * Created on 15 November 2007, 13:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package sydneyengine;

import java.io.IOException;
import java.util.ArrayList;

import sydneyengine.shooter.GameFrame;
import sydneyengine.shooter.GameWorld;
import sydneyengine.shooter.Player;
import sydneyengine.shooter.ViewPane;
/**
 * 
 * @author CommanderKeith
 */

public class ServerController extends ServingController implements PlayingController{

	protected GameFrame gameFrame;
	protected Player player;	    // the player that the person on this computer controls
	protected ViewPane viewPane;	// the thing we call render() on.
	

	public ServerController(GameFrame gameFrame, Player player, GameWorld world, EventStoreServer eventStore) {
		this(gameFrame, player, world, eventStore, null);
	}

	public ServerController(GameFrame gameFrame, Player player, GameWorld world, EventStoreServer eventStore, Sender sender) {
		super();
		this.gameFrame = gameFrame;
		setPlayer(player);
		this.eventStore = eventStore;
		this.sender = sender;
		setWorld(world);
		fpsCounter = new FPSCounter(this);
		System.out.println("ServerController successfully started: Running: " + this.isRunning());
	}

	@Override
	public void run() {
		System.out.println(this.getClass().getSimpleName() + ": starting");
		long nanoTimeNow = MockSystem.nanoTime() + getServerClockDiffNanos();	// getServerClockDiffNanos() will always be zero, so don't really need it...
		setTimeClientsLastUpdatedNanos(nanoTimeNow);
		getWorld().setSystemNanosAtStart(nanoTimeNow);
		// must set the time for the tail too!
		getWorld().getTail().setSystemNanosAtStart(nanoTimeNow);
		oldSystemTimeNanos = nanoTimeNow;
		shouldRun = true;
		//receiver.start();
		while (shouldRun) {
			fpsCounter.update();
			// Send a world update to all the clients if it's time
			if (this.shouldSendClientWorldUpdates() && getNumNexuses() > 0) {
				long nanoTimeNowForClientUpdate = MockSystem.nanoTime();
				if (getTimeClientsLastUpdatedNanos() + getMinTimeBetweenClientUpdatesNanos() < nanoTimeNowForClientUpdate) {
					setTimeClientsLastUpdatedNanos(nanoTimeNowForClientUpdate);
					getWorld().getHead().makeEqualTo(getWorld().getTail());
					ClientWorldUpdate clientWorldUpdate = new ClientWorldUpdate(getWorld().getHead());
					// The serialize method must be called in this thread to prevent 
					// updates to the world from conflicting with the serialization of the world.
					try {
						clientWorldUpdate.serialize(getSSOut());
					} catch (IOException e) {
						e.printStackTrace();
						close();
					}
					ArrayList<Nexus> copyOfNexuses = this.getCopyOfNexuses();
					//System.out.println(this.getClass().getSimpleName() + ": about to send update to clients, copyOfNexuses.size() == " + copyOfNexuses.size());
					for (int i = 0; i < copyOfNexuses.size(); i++) {
						try {
							// The head needs to step back, be sent, and then step forward again.  The step forward will happen in world.update().
							// This is because the tail objects have the same SS code as the head, and 
							// it must only be head objects in the SS input streams, not tail objects.
							// So tail objects can not be written.
							copyOfNexuses.get(i).send(Nexus.WORLD_UPDATE, clientWorldUpdate, getSSOut());

						} catch (IOException e) {
							e.printStackTrace();
							this.nexusThrewException(copyOfNexuses.get(i));
						}
					}
				}
			}
			
			// Add any new nexuses
			addNewQueuedNexuses();
			
			// check for exit or pause
			if (shouldRun == false) {
				break;
			}
			if (shouldPause == true) {
				doPause();
				continue;
			}
			
			/// Make new game events from any relevant java.awt.event.KeyEvents or MouseEvents
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
			assert makeSureNexusesHaveCorrectPlayer();
			// Let the viewPane update any components that implement Updatable:
			getViewPane().doMove(timeElapsedNanos, world.getTotalElapsedSeconds()-timeElapsedNanos);

			// check for exit or pause
			if (shouldRun == false) {
				break;
			}
			if (shouldPause == true) {
				doPause();
				continue;
			}

			viewPane.render();

			// check for exit or pause
			if (shouldRun == false) {
				break;
			}
			if (shouldPause == true) {
				doPause();
				continue;
			}
			doMinSleep();	// gives the other threads a turn
		}
		System.out.println(this.getClass().getSimpleName() + ": game loop finished 1");
		synchronized (closeAndWaitMutex) {
			closeAndWaitMutex.notifyAll();
		}
		System.out.println(this.getClass().getSimpleName() + ": game loop finished 2");
	}

	@Override
	public void close() {
		closeConnectionListener();
		if (getReceiver() != null) {
			getReceiver().close();
		}
		ArrayList<Nexus> copyOfNexuses = getCopyOfNexuses();
		for (Nexus n : copyOfNexuses) {
			try {
				n.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (getSender() != null) {
			getSender().close();
		}

		super.close();
	}
	
	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public void setPlayer(Player player) {
		this.player = player;
	}

	@Override
	public ViewPane getViewPane() {
		return viewPane;
	}

	@Override
	public void setViewPane(ViewPane viewPane) {
		this.viewPane = viewPane;
	}
	@Override
	public GameFrame getGameFrame() {
		return gameFrame;
	}

	@Override
	public void setGameFrame(GameFrame gameFrame) {
		this.gameFrame = gameFrame;
	}
}
