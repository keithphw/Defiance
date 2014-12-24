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
public class RocketLauncher extends Gun{
	
	static int playerGunNum = 6;
	//float coneAngle;
	
	public RocketLauncher(){
		respawn();
	}
	public RocketLauncher(GameWorld world) {
		this();
		this.world = world;
	//System.err.println("getSSCode() == "+getSSCode()+", dead == "+dead);
	}
	
	public void respawn() {
		super.respawn();
		gunLength = 6;
		reloadSeconds = 1.00f;
		reloadClipSeconds = 1;
		ammo = 7;
		maxAmmoInClip = 1;
		ammoInCurrentClip = maxAmmoInClip;
		maxGunRotationSpeed = (float)Math.PI;
		//coneAngle = (float)Math.PI/12f;
	}
	
	public float getRangeForBotAiming(){
		return 700;
	}
	public int getPlayerGunNum(){
		return playerGunNum;
	}
	public Item createNewItem(GameWorld world){
		return new RocketLauncher(world);
	}
	public Bullet createBullet(Player player, float xPosWhenFired, float yPosWhenFired, float gunAngle, float mouseX, float mouseY, double lastTimeFiredSeconds, float xLaunchSpeed, float yLaunchSpeed){
		return new BallisticRocket(player, xPosWhenFired, yPosWhenFired, gunAngle, lastTimeFiredSeconds, xLaunchSpeed, yLaunchSpeed);
	}
	
	transient BasicStroke stroke = new BasicStroke(7);
	public void render(ViewPane viewPane) {
		super.render(viewPane);
		Graphics2D g = viewPane.getBackImageGraphics2D();
		g.setColor(Color.GREEN);
		Stroke oldStroke = g.getStroke();
		g.setStroke(stroke);
		g.draw(new Line2D.Float(player.getX(), player.getY(), (float) (player.getX() + gunLength * Math.cos(gunAngleRelativeToPlayer+player.getAngle())), (float) (player.getY() + gunLength * Math.sin(gunAngleRelativeToPlayer+player.getAngle()))));
		g.setStroke(oldStroke);
	}
	
	
}