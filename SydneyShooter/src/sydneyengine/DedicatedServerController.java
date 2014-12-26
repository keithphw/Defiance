/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine;

import java.io.IOException;
import java.util.ArrayList;

import sydneyengine.network.ConnectionServerMina;
import sydneyengine.shooter.GameWorld;
import sydneyengine.shooter.Player;
import sydneyengine.shooter.ViewPane;
import sydneyengine.superserializable.FieldCache;
import sydneyengine.superserializable.SSObject;
import sydneyengine.superserializable.WeakSSObjectMap;
/**
 * 
 * @author CommanderKeith
 */

public class DedicatedServerController extends ServingController implements GameConstants{

	public DedicatedServerController(GameWorld world, EventStoreServer eventStore) {
		this(world, eventStore, null);
	}

	public DedicatedServerController(GameWorld world, EventStoreServer eventStore, Sender sender) {
		super();
		this.eventStore = eventStore;
		this.sender = sender;
		setWorld(world);
		fpsCounter = new FPSCounter(this);
	}
	
	@Override
	public Player getPlayer(){
		return null;
	}
	@Override
	public ViewPane getViewPane(){
		return null;
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
			
			this.getWorld().getEventStore().processNewEvents();

			long currentSystemTimeNanos = MockSystem.nanoTime() + getServerClockDiffNanos();	// get the current time
			long actualTimeElapsedNanos = currentSystemTimeNanos - oldSystemTimeNanos;
			this.oldSystemTimeNanos = currentSystemTimeNanos;
			
			long timeElapsedNanos = (currentSystemTimeNanos - (world.getSystemNanosAtStart() + world.getPureTotalElapsedNanos()));// work out the time since the last updateNanos    // russian: 'good-night' == 'do svedanye', bye == 'poka', shut up = 'zat nis', how's it going = 'cac de la?', it is good = 'e ta harashaw'

			// Update the world using the time elapsed
			world.updateNanos(timeElapsedNanos, actualTimeElapsedNanos);
			assert makeSureNexusesHaveCorrectPlayer();
			// Let the viewPane update any components that implement Updatable:
			//getViewPane().doMove(timeElapsedNanos, world.getTotalElapsedSeconds()-timeElapsedNanos);

			// check for exit or pause
			if (shouldRun == false) {
				break;
			}
			if (shouldPause == true) {
				doPause();
				continue;
			}

			//viewPane.render();

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
		/*if (connectionListener != null) {
			connectionListener.closeNow();
		}*/
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
	
	public static void main(String[] args){
		sydneyengine.shooter.GameFrame.setupSSToolsInstalledClasses();
		boolean internetGame = true;
		System.out.println(DedicatedServerController.class.getSimpleName() + ": doCreate method");

		ConnectionServerMina connectionServer = null;
		try {
			connectionServer = new ConnectionServerMina();
			connectionServer.bindAndListen(GameConstants.DEFAULT_PORT_TCP);
		} catch (java.io.IOException ex) {
			ex.printStackTrace();
			java.awt.Toolkit.getDefaultToolkit().beep();
			return;
		}
		/*
		ConnectionServerJGN connectionServer = null;
		try {
			connectionServer = new ConnectionServerJGN();
			connectionServer.bindAndListen(GameFrame.DEFAULT_PORT_TCP, GameFrame.DEFAULT_PORT_UDP);
		} catch (java.io.IOException ex) {
			ex.printStackTrace();
			java.awt.Toolkit.getDefaultToolkit().beep();
			return;
		}
		*/

		GameWorld world = new GameWorld();
		EventStoreServer eventStore = new EventStoreServer();
		world.setEventStore(eventStore);
		GameWorld tailWorld = null;

		tailWorld = (GameWorld) world.deepClone(new FieldCache(), new WeakSSObjectMap<SSObject, Object>());

		world.setTwin(tailWorld);
		world.setHead(true);
		tailWorld.setTwin(world);
		tailWorld.setHead(false);

		assert world.headAndTailWorldObjectsNotMixed();

		final DedicatedServerController controller = new DedicatedServerController(world, eventStore);
		world.setController(controller);
		tailWorld.setController(controller);
		Sender sender = new SenderLagSimulator(controller);
		sender.start();
		eventStore.setController(controller);

		if (internetGame) {
			controller.setSleepBetweenUpdatesMillis(DEDICATED_SERVER_CONTROLLER_SLEEP_BETWEEN_UPDATES_MILLIS);
			controller.setMinTimeBetweenClientUpdatesNanos(SERVER_CONTROLLER_MIN_TIME_BETWEEN_CLIENT_UPDATES_NANOS_INTERNET);
			world.setDoMoveBetweenEventsIfTimeStampsEqual(WORLD_DO_MOVE_BETWEEN_EVENTS_IF_TIME_STAMPS_EQUAL);
			world.setEventTimeStampMultipleSeconds(WORLD_EVENT_TIME_STAMP_MULTIPLE_SECONDS);
			world.setMaxUpdateElapsedSeconds(WORLD_MAX_UPDATE_ELAPSED_SECONDS);
			world.setMinTimeGapSeconds(WORLD_MIN_TIME_GAP_SECONDS_INTERNET);
			world.setMaxTimeGapSeconds(WORLD_MAX_TIME_GAP_SECONDS_INTERNET);
			tailWorld.setMinTimeGapSeconds(WORLD_MIN_TIME_GAP_SECONDS_INTERNET);
			tailWorld.setMaxTimeGapSeconds(WORLD_MAX_TIME_GAP_SECONDS_INTERNET);
			eventStore.setMinSecondsToKeepUserEvents(EVENT_STORE_MIN_SECONDS_TO_KEEP_USER_EVENTS_INTERNET);
		} else { // LAN game
			controller.setSleepBetweenUpdatesMillis(DEDICATED_SERVER_CONTROLLER_SLEEP_BETWEEN_UPDATES_MILLIS);
			controller.setMinTimeBetweenClientUpdatesNanos(SERVER_CONTROLLER_MIN_TIME_BETWEEN_CLIENT_UPDATES_NANOS_LAN);
			world.setDoMoveBetweenEventsIfTimeStampsEqual(WORLD_DO_MOVE_BETWEEN_EVENTS_IF_TIME_STAMPS_EQUAL);
			world.setEventTimeStampMultipleSeconds(WORLD_EVENT_TIME_STAMP_MULTIPLE_SECONDS);
			world.setMaxUpdateElapsedSeconds(WORLD_MAX_UPDATE_ELAPSED_SECONDS);
			world.setMinTimeGapSeconds(WORLD_MIN_TIME_GAP_SECONDS_LAN);
			world.setMaxTimeGapSeconds(WORLD_MAX_TIME_GAP_SECONDS_LAN);
			tailWorld.setMinTimeGapSeconds(WORLD_MIN_TIME_GAP_SECONDS_LAN);
			tailWorld.setMaxTimeGapSeconds(WORLD_MAX_TIME_GAP_SECONDS_LAN);
			eventStore.setMinSecondsToKeepUserEvents(EVENT_STORE_MIN_SECONDS_TO_KEEP_USER_EVENTS_LAN);
		}

		ReceiverPollingServer receiver = new ReceiverPollingServer(controller);
		receiver.setSleepTimeAfterNoMoreRecievesMillis(10);
		controller.setReceiver(receiver);
		receiver.start();


		// start the latencyChecker thread which is designed to keep a track of the latency between the server and all clients.
		// Note that it will never affect the nexus.getLatencyInfo()'s serverClockDiff, only its latency.
		// This should be done at the end of all of the direct calls to nexus.recieve since it will interfere with that.

		Thread gameThread;
		gameThread = new Thread(controller);
		gameThread.setDaemon(true);
		gameThread.setName("ServerController " + gameThread.getName());
		
		
		ConnectionWelcomer connectionWelcomer = new ConnectionWelcomer(controller);
		connectionServer.setConnectionServerListener(connectionWelcomer);
		controller.setConnectionServer(connectionServer);
		
		//ConnectionListener connectionListener = new ConnectionListener(controller, connectionServer, connectionWelcomer);
		//controller.setConnectionListener(connectionListener);

		// Use this code to shut down the DedicatedServer after a set period of time. 
		// For example, it's useful for shutting it down after profiling with -Xprof.
//		Thread shutDownThread = new Thread(new Runnable(){
//			public void run(){
//				System.out.println(this.getClass().getSimpleName()+": starting**************************..");
//				try{
//					Thread.sleep(100000);
//				}catch(Exception e){}
//				System.out.println(this.getClass().getSimpleName()+": exiting..");
//				controller.closeAndWait(1000);
//				System.exit(0);
//			}
//		});
//		shutDownThread.start();
		
		gameThread.start();
		//connectionListener.startTakingConnections();
		sydneyengine.lobby.LobbyClient lobbyClient = new sydneyengine.lobby.LobbyClient();
		lobbyClient.start();
		if (internetGame) {
			while (lobbyClient.isConnected() == false){
				try{Thread.sleep(1000);}catch(Exception e){e.printStackTrace();}
			}
			try {
				lobbyClient.sendNotificationOfNewHostedGame();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}

