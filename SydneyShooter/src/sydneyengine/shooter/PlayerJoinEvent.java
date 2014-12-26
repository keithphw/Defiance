/*
 * PlayerJoinEvent.java
 *
 * Created on 14 November 2007, 18:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package sydneyengine.shooter;

import java.io.IOException;
import java.util.ArrayList;

import sydneyengine.Controller;
import sydneyengine.EventWrapper;
import sydneyengine.superserializable.SSObject;
import sydneyengine.superserializable.SSObjectInputStream;
import sydneyengine.superserializable.SSObjectOutputStream;
/**
 *
 * @author CommanderKeith
 */
public class PlayerJoinEvent extends GameEvent {
	
	transient protected Player player;

	public PlayerJoinEvent() {
	}

	public PlayerJoinEvent(Player player) {
		setEventWrapper(new EventWrapper());
		this.player = player;
	}

	@Override
	public void applyNow(GameWorld world) {
		assert world != null;
		assert world.isHead();
		// Must set the world for the player as it will be null since the
		// world is excluded from being written when the player is written
		// to the SS streams in the below writeSS method.
		if (player.getWorld() == null) {
			player.setWorld(world);
		}
		world.addPlayer(player);
		assert world.isHead();
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Controller getController() {
		return getEventWrapper().getController();
	}

	public void setController(Controller controller) {
		getEventWrapper().setController(controller);
	}

	@Override
	public void writeSS(SSObjectOutputStream out) throws IOException {		// this is the method that you over-ride if you want custom serialization
		// Write the player object and its fields without writing the whole game world.
		// Note that this means that the player object will have a null World when it is created on the server in the readSS method below.
		// Maybe this code should be in the Player object?
		if (player.getWorld() != null) {
			ArrayList<SSObject> ssObjectsToIgnore = new ArrayList<SSObject>();
			ssObjectsToIgnore.add(player.getWorld());
			out.writeObject(player, ssObjectsToIgnore);
		} else {
			out.writeObject(player);
		}
		out.writeFields(this);
	}

	@Override
	public void readSS(SSObjectInputStream in) throws java.io.IOException {	// this is the method that you over-ride if you want custom serialization
		assert this.getEventWrapper() == null : "this.wrapperEvent should be null when this event is deserialized because, the fact that it is non-null may indicate that this event has been deserailized more than once, or there is some other problem.";
		player = (Player) in.readObject();
		assert player != null : printCurrentPlayers();
		in.readFields(this);
		// Note that player.getWorld() will return null until this event is applied.
		//System.out.println(this.getClass().getSimpleName() + ": just did readSS on a PlayerJoinEvent! player.getName() == " + player.getName() + ", player.getWorld().getPlayers().size() == " + player.getWorld().getPlayers().size());
	}

	protected ArrayList<Player> printCurrentPlayers() {
		ArrayList<Player> players = getController().getWorld().getPlayers();
		return players;
	}
}