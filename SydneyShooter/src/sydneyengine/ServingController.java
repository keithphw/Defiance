/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine;

import java.io.IOException;
import java.util.ArrayList;

import sydneyengine.network.ConnectionServer;
import sydneyengine.shooter.GameWorld;
import sydneyengine.shooter.Player;
import sydneyengine.shooter.PlayerJoinEvent;
import sydneyengine.superserializable.ArrayListSS;

/**
 *
 * @author Joanne Woodward
 */
public abstract class ServingController extends Controller{
	
	//protected ConnectionListener connectionListener;	//can be null
	protected ConnectionServer connectionServer;
	protected boolean shouldSendClientWorldUpdates = true;
	protected long minTimeBetweenClientUpdatesNanos = 1000000000L;	// 1000000000L nanos == 1 second

	protected long timeClientsLastUpdatedNanos = -1;
	Object nexusesMutex = new Object();
	protected ArrayList<Nexus> nexuses = new ArrayList<Nexus>();
	protected ReceiverPollingServer receiver = null;
	protected EventStoreServer eventStore;
	ArrayList<Nexus> nexusesToAdd = new ArrayList<Nexus>();
	Object nexusesToAddMutex = new Object();
	
	@Override
	public void setWorld(GameWorld world){
		this.world = world;
		world.getHead().setController(this);
		world.getTail().setController(this);
	}
	
	public void addNexus(Nexus nexus) {
		synchronized (nexusesToAddMutex) {
			nexusesToAdd.add(nexus);
		}
	}
	protected void addNewQueuedNexuses(){
	// Add any new nexuses
		ArrayList<Nexus> copyOfNexusesToBeAdded = null;
		synchronized (nexusesToAddMutex) {
			if (nexusesToAdd.size() > 0) {
				copyOfNexusesToBeAdded = new ArrayList<Nexus>();
				copyOfNexusesToBeAdded.addAll(nexusesToAdd);
				nexusesToAdd.clear();
				System.out.println(this.getClass().getSimpleName() + ": copyOfNexusesToBeAdded.size() == " + copyOfNexusesToBeAdded.size());
			}
		}
		if (copyOfNexusesToBeAdded != null) {
			System.out.println(this.getClass().getSimpleName() + ": addNexuses == true, copyOfNexusesToBeAdded.size() == " + copyOfNexusesToBeAdded.size());
			ArrayListSS<EventWrapper> copyOfAllEventsSoFar = new ArrayListSS<EventWrapper>();
			copyOfAllEventsSoFar.addAll(getWorld().getEventStore().getEvents());
			for (int i = 0; i < copyOfNexusesToBeAdded.size(); i++) {
				try {
					// The head needs to step back, be sent, and then step forward again.  The step forward will happen in world.update().
					// This is because the tail objects have the same SS code as the head, and 
					// it must only be head objects in the SS input streams, not tail objects.
					// So tail objects can not be written.
					System.out.println(this.getClass().getSimpleName()+": doing getWorld().getHead().makeEqualTo(getWorld().getTail())");
					getWorld().getHead().makeEqualTo(getWorld().getTail());
					//System.err.println(this.getClass().getSimpleName()+": getWorld().getLastUpdatedWorldSeconds() == "+getWorld().getLastUpdatedWorldSeconds()+", getWorld().getTotalElapsedSeconds() == "+getWorld().getTotalElapsedSeconds()+", getWorld().getElapsedSeconds() == "+getWorld().getElapsedSeconds()+", getWorld().getTail().getTotalElapsedSeconds() == "+getWorld().getTail().getTotalElapsedSeconds()+", getWorld().getTail().getElapsedSeconds() == "+getWorld().getTail().getElapsedSeconds());
					copyOfNexusesToBeAdded.get(i).send(Nexus.WORLD_FOR_CLIENT_JOIN, getWorld().getHead(), getSSOut());
					System.out.println(this.getClass().getSimpleName() + ": world sent to a client that wants to join!");
					copyOfNexusesToBeAdded.get(i).send(Nexus.ALL_EVENTS_FOR_CLIENT_JOIN, copyOfAllEventsSoFar, getSSOut());
					System.out.println(this.getClass().getSimpleName()+": sent copyOfAllEventsSoFar! copyOfAllEventsSoFar.size() == "+copyOfAllEventsSoFar.size());
				} catch (IOException e) {
					e.printStackTrace();
				}
				assert copyOfNexusesToBeAdded.get(i).getPlayer() != null : copyOfNexusesToBeAdded.get(i).getPlayer();
				PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(copyOfNexusesToBeAdded.get(i).getPlayer());
				playerJoinEvent.setTimeStamp(getWorld().getEventTimeStampNowSeconds());
				playerJoinEvent.getEventWrapper().setId(copyOfNexusesToBeAdded.get(i).getPlayer().getSSCode());// the id is just used for error-checking.
				playerJoinEvent.getEventWrapper().setCount(EventWrapper.COUNT_TO_IGNORE);
				getWorld().getEventStore().addEventFromViewPane(playerJoinEvent.getEventWrapper());
				System.out.println(this.getClass().getSimpleName() + ": done addEventFromViewPane(playerJoinEvent.getEventWrapper())");
				// start the latencyChecker thread which is designed to keep a track of the latency between the server and all clients.
				copyOfNexusesToBeAdded.get(i).getLatencyCalculator().startLatencyChecker();
			}
			synchronized (nexusesMutex) {
				nexuses.addAll(copyOfNexusesToBeAdded);
				System.out.println(this.getClass().getSimpleName() + ": "+copyOfNexusesToBeAdded.size()+" new nexuses added. nexuses.size() == " + nexuses.size());
			}
		}
	}

	@Override
	public void nexusThrewException(Nexus nexus) {
		assert nexus != null;
		//ArrayList<Nexus> copyOfNexuses = getCopyOfNexuses();
		//assert copyOfNexuses.contains(nexus);
		removeNexus(nexus);
	}

	protected void removeNexus(Nexus nexus) {
		synchronized (nexusesMutex) {
			nexuses.remove(nexus);
		}
		try {
			nexus.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(this.getClass().getSimpleName() + ": removed a nexus, nexuses.size() == " + nexuses.size() + ", removed nexus.toString() == " + nexus);
	}
	
	public void sendEvents(ArrayList<EventWrapper> events, ArrayList<Nexus> nexusesToSendTo) throws IOException {
		//ArrayList<Nexus> copyOfNexuses = getCopyOfNexuses();
		for (int i = 0; i < nexusesToSendTo.size(); i++) {
			for (EventWrapper e : events) {
				nexusesToSendTo.get(i).send(Nexus.EVENT, e, getSSOut());
			}
		}
	}
	
	public void closeConnectionListener(){
		if (connectionServer != null){
			connectionServer.close();
		}
	}

	public void setConnectionServer(ConnectionServer connectionServer) {
		this.connectionServer = connectionServer;
	}
	
	
/*
	public ConnectionListener getConnectionListener() {
		return connectionListener;
	}

	public void setConnectionListener(ConnectionListener connectionListener) {
		this.connectionListener = connectionListener;
	}
*/
	// Thread safe
	public ArrayList<Nexus> getCopyOfNexuses() {
		ArrayList<Nexus> copyOfNexuses = new ArrayList<Nexus>(nexuses.size());
		synchronized (nexusesMutex) {
			copyOfNexuses.addAll(nexuses);
		//System.out.println(this.getClass().getSimpleName()+": this == "+this+", nexuses.size() == "+nexuses.size());
		}
		return copyOfNexuses;
	}
	
	// Should only be called by the Controller thread since that thread modifies the world.players list.
	public boolean makeSureNexusesHaveCorrectPlayer(){
		ArrayList<Nexus> copyOfNexuses = this.getCopyOfNexuses();
		boolean flag = true;
		for (Nexus nexus : copyOfNexuses){
			if (nexus.getPlayer() == null){
				System.err.println(this.getClass().getSimpleName()+": nexus.getPlayer() == "+nexus.getPlayer());
				flag = false;
			}
			if (flag == true){
				for (Player currentPlayer : world.getPlayers()){
					if (nexus.getPlayer().getSSCode() == currentPlayer.getSSCode() && nexus.getPlayer() != currentPlayer){
						flag = false;
						System.err.println(this.getClass().getSimpleName()+": nexus.getPlayer() has the same ssCode as a player in the list world.getPlayers() but they are not ==, so they are two separate objects which is a problem. nexus.getPlayer() == "+nexus.getPlayer()+", getWorld().getPlayers() == "+getWorld().getPlayers());
					}
				}
			}
		}
		return flag;
	}
	public int getNumNexuses() {
		synchronized (nexusesMutex) {
			return nexuses.size();
		}
	}

	@Override
	public ReceiverPollingServer getReceiver() {
		return receiver;
	}

	public void setReceiver(ReceiverPollingServer receiver) {
		this.receiver = receiver;
	}

	@Override
	public EventStoreServer getEventStore() {
		return eventStore;
	}

	public void setEventStore(EventStoreServer eventStore) {
		this.eventStore = eventStore;
	}

	public long getMinTimeBetweenClientUpdatesNanos() {
		return minTimeBetweenClientUpdatesNanos;
	}

	public void setMinTimeBetweenClientUpdatesNanos(long minTimeBetweenClientUpdatesNanos) {
		this.minTimeBetweenClientUpdatesNanos = minTimeBetweenClientUpdatesNanos;
	}

	public long getTimeClientsLastUpdatedNanos() {
		return timeClientsLastUpdatedNanos;
	}

	public void setTimeClientsLastUpdatedNanos(long timeClientsLastUpdatedNanos) {
		this.timeClientsLastUpdatedNanos = timeClientsLastUpdatedNanos;
	}

	public boolean shouldSendClientWorldUpdates() {
		return shouldSendClientWorldUpdates;
	}

	public void setShouldSendClientWorldUpdates(boolean shouldSendClientWorldUpdates) {
		this.shouldSendClientWorldUpdates = shouldSendClientWorldUpdates;
	}
	@Override
	public long getLatencyToServerNanos() {
		return 0;
	}

	@Override
	public long getServerClockDiffNanos() {
		return 0;
	}
}
