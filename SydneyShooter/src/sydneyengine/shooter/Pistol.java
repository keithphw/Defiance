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
public class Pistol extends Gun{
	
	static int playerGunNum = 1;
	long seed = -Long.MAX_VALUE;	// seed for random number generation.
	float coneAngle;
	
	public Pistol(){
		respawn();
	}
	public Pistol(GameWorld world) {
		this();
		this.world = world;
	//System.err.println("getSSCode() == "+getSSCode()+", dead == "+dead);
	}
	
	public void respawn() {
		super.respawn();
		gunLength = 4;
		reloadSeconds = 0.22222f;
		reloadClipSeconds = 2;
		ammo = 42;
		maxAmmoInClip = 14;
		ammoInCurrentClip = maxAmmoInClip;
		maxGunRotationSpeed = (float)Math.PI*6f;
		coneAngle = (float)Math.PI/48f;
	}
	public int getPlayerGunNum(){
		return playerGunNum;
	}
	public Item createNewItem(GameWorld world){
		return new Pistol(world);
	}
	public Bullet createBullet(Player player, float xPosWhenFired, float yPosWhenFired, float gunAngle, float mouseX, float mouseY, double lastTimeFiredSeconds, float xLaunchSpeed, float yLaunchSpeed){
		world.getRandom().setSeed(seed);
		seed += 3491;
		float randomAngleIncrement = (float)((world.getRandom().nextFloat()-0.5)*coneAngle);
		return new PistolBullet(player, xPosWhenFired, yPosWhenFired, gunAngle + randomAngleIncrement, lastTimeFiredSeconds, xLaunchSpeed, yLaunchSpeed);
	}
	
	transient BasicStroke stroke = new BasicStroke(2);
	public void render(ViewPane viewPane) {
		super.render(viewPane);
		Graphics2D g = viewPane.getBackImageGraphics2D();
		g.setColor(Color.PINK);
		Stroke oldStroke = g.getStroke();
		g.setStroke(stroke);
		g.draw(new Line2D.Float(player.getX(), player.getY(), (float) (player.getX() + gunLength * Math.cos(gunAngleRelativeToPlayer+player.getAngle())), (float) (player.getY() + gunLength * Math.sin(gunAngleRelativeToPlayer+player.getAngle()))));
		g.setStroke(oldStroke);
	}
	
	
}
