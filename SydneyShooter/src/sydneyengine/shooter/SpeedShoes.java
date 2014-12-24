package sydneyengine.shooter;

import sydneyengine.superserializable.*;
import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.geom.*;
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
	
	public String getName(){
		return this.getClass().getSimpleName();
	}
	public void assignToPlayer(Player player, double assignTimeSeconds){
		this.hitPlayer = player;		
		player.setSpeedMultiplier(this.playerSpeedIncrease, assignTimeSeconds, this.speedLengthTimeSeconds);
		player.newPersonalMessage("Obtained Speed Shoes.", assignTimeSeconds);
	}
	public Item createNewItem(GameWorld world){
		return new SpeedShoes(world);
	}

	public Player getPlayer() {
		return hitPlayer;
	}
}
