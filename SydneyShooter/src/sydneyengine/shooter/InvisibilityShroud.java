/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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
public class InvisibilityShroud extends SSAdapter implements Item{
	GameWorld world;
	protected Player hitPlayer;
	float invisibilityLengthTimeSeconds = 15;
	
	public InvisibilityShroud(){
	}
	public InvisibilityShroud(GameWorld world){
		this.world = world;
	}
	
	public String getName(){
		return this.getClass().getSimpleName();
	}
	public void assignToPlayer(Player player, double assignTimeSeconds){
		this.hitPlayer = player;
		hitPlayer.setInvisible(assignTimeSeconds, invisibilityLengthTimeSeconds);
		player.newPersonalMessage("Obtained Invisibility Shroud", assignTimeSeconds);
	}
	public Item createNewItem(GameWorld world){
		return new InvisibilityShroud(world);
	}

	public Player getPlayer() {
		return hitPlayer;
	}
}
