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
public class HealthPack extends SSAdapter implements Item{
	GameWorld world;
	protected Player hitPlayer;
	float healthIncrease = 33;
	
	public HealthPack(){
	}
	public HealthPack(GameWorld world){
		this.world = world;
	}
	
	@Override
	public String getName(){
		return this.getClass().getSimpleName();
	}
	@Override
	public void assignToPlayer(Player player, double assignTimeSeconds){
		this.hitPlayer = player;
		player.setHitPoints(player.getHitPoints()+healthIncrease);
		if (player.getHitPoints() > player.getMaxHitPoints()){
			player.setHitPoints(player.getMaxHitPoints());
		}
		player.newPersonalMessage("Obtained a health pack.", assignTimeSeconds);
	}
	@Override
	public Item createNewItem(GameWorld world){
		return new HealthPack(world);
	}

	public Player getPlayer() {
		return hitPlayer;
	}
}
