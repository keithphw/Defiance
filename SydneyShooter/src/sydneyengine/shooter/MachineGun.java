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
public class MachineGun extends Gun{
	
	static int playerGunNum = 4;
	long seed = -Long.MAX_VALUE;	// seed for random number generation.
	float coneAngle;
	
	public MachineGun(){
		respawn();
	}
	public MachineGun(GameWorld world) {
		this();
		this.world = world;
	//System.err.println("getSSCode() == "+getSSCode()+", dead == "+dead);
	}
	
	public void respawn() {
		super.respawn();
		gunLength = 6;
		reloadSeconds = 0.0444444f;
		reloadClipSeconds = 4;
		ammo = 400;
		maxAmmoInClip = 200;
		ammoInCurrentClip = maxAmmoInClip;
		maxGunRotationSpeed = (float)Math.PI*2f;
		coneAngle = (float)Math.PI/24f;
	}
	public float getRangeForBotAiming(){
		return 650;
	}
	public int getPlayerGunNum(){
		return playerGunNum;
	}
	public Item createNewItem(GameWorld world){
		return new MachineGun(world);
	}
	public Bullet createBullet(Player player, float xPosWhenFired, float yPosWhenFired, float gunAngle, float mouseX, float mouseY, double lastTimeFiredSeconds, float xLaunchSpeed, float yLaunchSpeed){
		world.getRandom().setSeed(seed);
		seed += 3491;
		float randomAngleIncrement = (float)((world.getRandom().nextFloat()-0.5)*coneAngle);
		return new MachineGunBullet(this, player, xPosWhenFired, yPosWhenFired, gunAngle + randomAngleIncrement, lastTimeFiredSeconds, xLaunchSpeed, yLaunchSpeed);
	}
	
	transient BasicStroke stroke = new BasicStroke(3);
	public void render(ViewPane viewPane) {
		super.render(viewPane);
		Graphics2D g = viewPane.getBackImageGraphics2D();
		g.setColor(Color.YELLOW);
		Stroke oldStroke = g.getStroke();
		g.setStroke(stroke);
		g.draw(new Line2D.Float(player.getX(), player.getY(), (float) (player.getX() + gunLength * Math.cos(gunAngleRelativeToPlayer+player.getAngle())), (float) (player.getY() + gunLength * Math.sin(gunAngleRelativeToPlayer+player.getAngle()))));
		g.setStroke(oldStroke);
	}
	
	
}
