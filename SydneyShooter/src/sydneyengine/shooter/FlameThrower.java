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
public class FlameThrower extends Gun{
	
	static int playerGunNum = 2;
	float coneAngle;
	
	public FlameThrower(){
		respawn();
	}
	public FlameThrower(GameWorld world) {
		this();
		this.world = world;
	//System.err.println("getSSCode() == "+getSSCode()+", dead == "+dead);
	}
	
	public void respawn() {
		super.respawn();
		gunLength = 5;
		reloadSeconds = 0.020f;
		reloadClipSeconds = 2;
		ammo = 0;
		maxAmmoInClip = 500;
		ammoInCurrentClip = maxAmmoInClip;
		maxGunRotationSpeed = (float)Math.PI*2f;
		coneAngle = (float)Math.PI/16f;
	}
	public float getRangeForBotAiming(){
		return 300;
	}
	public int getPlayerGunNum(){
		return playerGunNum;
	}
	public Item createNewItem(GameWorld world){
		return new FlameThrower(world);
	}
	public Bullet createBullet(Player player, float xPosWhenFired, float yPosWhenFired, float gunAngle, float mouseX, float mouseY, double lastTimeFiredSeconds, float xLaunchSpeed, float yLaunchSpeed){
		world.getRandom().setSeed(seed);
		seed += 3491;
		//float randomAngleIncrement = (float)((world.getRandom().nextGaussian()-0.5)*coneAngle);
		float randomAngleIncrement = (float)((world.getRandom().nextFloat()-0.5)*coneAngle);
		world.getRandom().setSeed(seed);
		seed += 1237;
		int colorNum = world.getRandom().nextInt(FlameBall.colors.size());
		assert colorNum < FlameBall.colors.size() : colorNum;
		
		return new FlameBall(this, player, xPosWhenFired, yPosWhenFired, gunAngle + randomAngleIncrement, mouseX, mouseY, lastTimeFiredSeconds, xLaunchSpeed, yLaunchSpeed, colorNum);
	}
	
	transient BasicStroke stroke = new BasicStroke(5);
	public void render(ViewPane viewPane) {
		super.render(viewPane);
		Graphics2D g = viewPane.getBackImageGraphics2D();
		g.setColor(Color.ORANGE);
		Stroke oldStroke = g.getStroke();
		g.setStroke(stroke);
		g.draw(new Line2D.Float(player.getX(), player.getY(), (float) (player.getX() + gunLength * Math.cos(gunAngleRelativeToPlayer+player.getAngle())), (float) (player.getY() + gunLength * Math.sin(gunAngleRelativeToPlayer+player.getAngle()))));
		g.setStroke(oldStroke);
	}
	
	
}