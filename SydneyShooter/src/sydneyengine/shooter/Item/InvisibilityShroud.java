/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.shooter.Item;

import sydneyengine.shooter.GameWorld;
import sydneyengine.shooter.Player;
import sydneyengine.superserializable.SSAdapter;
/**
 *
 * @author CommanderKeith
 */
public class InvisibilityShroud extends SSAdapter implements Item{
	GameWorld world;
	protected Player hitPlayer;
	float invisibilityLengthTimeSeconds = 15;
	
	public InvisibilityShroud(){
	}
	public InvisibilityShroud(GameWorld world){
		this.world = world;
	}
	
	@Override
	public String getName(){
		return this.getClass().getSimpleName();
	}
	@Override
	public void assignToPlayer(Player player, double assignTimeSeconds){
		this.hitPlayer = player;
		hitPlayer.setInvisible(assignTimeSeconds, invisibilityLengthTimeSeconds);
		player.newPersonalMessage("Obtained Invisibility Shroud", assignTimeSeconds);
	}
	@Override
	public Item createNewItem(GameWorld world){
		return new InvisibilityShroud(world);
	}

	public Player getPlayer() {
		return hitPlayer;
	}
}
