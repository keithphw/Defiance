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
public class GodlyArmor extends SSAdapter implements Item{
	GameWorld world;
	protected Player hitPlayer;
	float armoredLengthTimeSeconds = 15;
	
	public GodlyArmor(){
	}
	public GodlyArmor(GameWorld world){
		this.world = world;
	}
	
	public String getName(){
		return this.getClass().getSimpleName();
	}
	public void assignToPlayer(Player player, double assignTimeSeconds){
		this.hitPlayer = player;
		hitPlayer.setArmored(assignTimeSeconds, armoredLengthTimeSeconds);
		player.newPersonalMessage("Obtained Godly Armor of Invincibility.", assignTimeSeconds);
	}
	public Item createNewItem(GameWorld world){
		return new GodlyArmor(world);
	}

	public Player getPlayer() {
		return hitPlayer;
	}
}

