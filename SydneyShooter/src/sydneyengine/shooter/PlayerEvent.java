/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.shooter;

import java.io.IOException;

import sydneyengine.EventWrapper;
import sydneyengine.superserializable.SSObjectInputStream;
import sydneyengine.superserializable.SSObjectOutputStream;
/**
 *
 * @author CommanderKeith
 */
public abstract class PlayerEvent extends GameEvent{
	
	transient protected Player player;
	
	public PlayerEvent() {
	}
	public PlayerEvent(Player player){
		setEventWrapper(new EventWrapper());
		this.player = player;
	}
	
	/** Sub-classes should over-ride this method.
	 * 
	 * @param world
	 */
	@Override
	public void applyNow(GameWorld world){
		assert world != null;
		assert player.getWorld() != null;
		assert world.isHead();
		assert player.getWorld().isHead() : "player.getWorld().isHead() == "+player.getWorld().isHead();
		assert world == player.getWorld() : "world == "+world+", player.getWorld() == "+player.getWorld();
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	@Override
	public void writeSS(SSObjectOutputStream out) throws IOException{		// this is the method that you over-ride if you want custom serialization
		assert player.getWorld().isHead();
		out.writeInt(player.getSSCode());
	}
	
	@Override
	public void readSS(SSObjectInputStream in) throws java.io.IOException{	// this is the method that you over-ride if you want custom serialization
		assert this.getEventWrapper() == null : "this.wrapperEvent should be null when this event is deserialized because, the fact that it is non-null may indicate that this event has been deserailized more than once, or there is some other problem.";
		// question: is this method called for each class level?? or will sub-class vars not get written?
		// answer: no, this method will not be called at each class-level,
		// but in.readFields reads all vars from all class levels (above this class??) so sub-classes won't miss out.
		int playerSSCode = in.readInt();
		player = (Player)in.getStoredObject(playerSSCode);	// I think that the problem here is that the player object is being garbage collected as soon as it is read...
		assert player != null : "playerSSCode == "+playerSSCode+", players == "+GameFrame.getStaticGameFrame().getController().getWorld().getPlayers();
		assert player.getWorld() != null;
		assert player.getWorld().isHead();
		
	}
	
	
}
