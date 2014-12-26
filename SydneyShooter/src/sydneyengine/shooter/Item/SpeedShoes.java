package sydneyengine.shooter.Item;

import sydneyengine.shooter.GameWorld;
import sydneyengine.shooter.Player;
import sydneyengine.superserializable.SSAdapter;
/**
 *
 * @author CommanderKeith
 */
public class SpeedShoes extends SSAdapter implements Item{
	GameWorld world;
	protected Player hitPlayer;
	float playerSpeedIncrease = 2.5f;
	float speedLengthTimeSeconds = 15;
	
	public SpeedShoes(){
	}
	public SpeedShoes(GameWorld world){
		this.world = world;
	}
	
	@Override
	public String getName(){
		return this.getClass().getSimpleName();
	}
	@Override
	public void assignToPlayer(Player player, double assignTimeSeconds){
		this.hitPlayer = player;		
		player.setSpeedMultiplier(this.playerSpeedIncrease, assignTimeSeconds, this.speedLengthTimeSeconds);
		player.newPersonalMessage("Obtained Speed Shoes.", assignTimeSeconds);
	}
	@Override
	public Item createNewItem(GameWorld world){
		return new SpeedShoes(world);
	}

	public Player getPlayer() {
		return hitPlayer;
	}
}
