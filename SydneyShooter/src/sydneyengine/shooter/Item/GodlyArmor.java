package sydneyengine.shooter.Item;

import sydneyengine.shooter.GameWorld;
import sydneyengine.shooter.Player;
import sydneyengine.superserializable.SSAdapter;
/**
 *
 * @author CommanderKeith
 */
public class GodlyArmor extends SSAdapter implements Item{
	GameWorld world;
	protected Player hitPlayer;
	float armoredLengthTimeSeconds = 15;
	
	public GodlyArmor(){
	}
	public GodlyArmor(GameWorld world){
		this.world = world;
	}
	
	@Override
	public String getName(){
		return this.getClass().getSimpleName();
	}
	@Override
	public void assignToPlayer(Player player, double assignTimeSeconds){
		this.hitPlayer = player;
		hitPlayer.setArmored(assignTimeSeconds, armoredLengthTimeSeconds);
		player.newPersonalMessage("Obtained Godly Armor of Invincibility.", assignTimeSeconds);
	}
	@Override
	public Item createNewItem(GameWorld world){
		return new GodlyArmor(world);
	}

	public Player getPlayer() {
		return hitPlayer;
	}
}

