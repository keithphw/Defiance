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
public class MachineGunBullet extends Bullet {

	static float canNotHitOwnPlayerTimeSeconds = 1.0f;
	float radius;
	float length;
	float maxSpeed;
	float damage;
	float angle;
	protected float lifeTimeSeconds;

	public MachineGunBullet() {
		super();
		player = null;
	}

	public MachineGunBullet(MachineGun gun, Player player, float newX, float newY, float angle, double spawnTimeSeconds, float xLaunchSpeed, float yLaunchSpeed) {
		super(player, newX, newY, angle, spawnTimeSeconds, xLaunchSpeed, yLaunchSpeed);
		assert Point2D.distance(player.getX(), player.getY(), newX, newY) < player.getR() : Point2D.distance(player.getX(), player.getY(), newX, newY);
		radius = 2.0f;
		length = 2*radius;
		damage = 4.0f;
		this.angle = angle;
		/*gun.getWorld().getRandom().setSeed(gun.getSeed());
		gun.setSeed(gun.getSeed()+3041);
		float randomSpeedIncrement = world.getRandom().nextFloat()*200;*/
		float startSpeed = 800;// + randomSpeedIncrement;
		gun.getWorld().getRandom().setSeed(gun.getSeed());
		gun.setSeed(gun.getSeed()+1761);
		float randomRangeIncrement = world.getRandom().nextFloat()*200;
		float range = 600 + randomRangeIncrement;
		speedX = xLaunchSpeed + (float) Math.cos(angle) * startSpeed;
		speedY = yLaunchSpeed + (float) Math.sin(angle) * startSpeed;
		//float accel = -50;
		//accelX = (float) Math.cos(angle) * accel;
		//accelY = (float) Math.sin(angle) * accel;
		float launchSpeed = startSpeed;//(float)Math.sqrt(Math.pow(speedX, 2) + Math.pow(speedY, 2));
		lifeTimeSeconds = range / launchSpeed;
		
		this.x = newX + (float) Math.cos(angle) * length;
		this.y = newY + (float) Math.sin(angle) * length;
		backX = newX;
		backY = newY;
		oldBackX = backX;
		oldBackY = backY;
	}
	/*
	public MachineGunBullet(MachineGun gun, Player player, float newX, float newY, float angle, double spawnTimeSeconds, float xLaunchSpeed, float yLaunchSpeed) {
		super(player, newX, newY, angle, spawnTimeSeconds, xLaunchSpeed, yLaunchSpeed);
		assert Point2D.distance(player.getX(), player.getY(), newX, newY) < player.getR() : Point2D.distance(player.getX(), player.getY(), newX, newY);
		radius = 1.5f;
		length = 2*radius;;
		this.angle = angle;
		maxSpeed = 700;
		damage = 3.33333f;
		maxRange = 1500;
		speedX = xLaunchSpeed + (float) Math.cos(angle) * maxSpeed;
		speedY = yLaunchSpeed + (float) Math.sin(angle) * maxSpeed;

		this.x = newX + (float) Math.cos(angle) * length;
		this.y = newY + (float) Math.sin(angle) * length;
		backX = newX;
		backY = newY;
		oldBackX = backX;
		oldBackY = backY;
	}
*/
	//transient BasicStroke stroke = new BasicStroke(3);
	public void render(ViewPane viewPane) {
		Graphics2D g = viewPane.getBackImageGraphics2D();
		g.setColor(Color.BLACK);
		g.fill(new java.awt.geom.Ellipse2D.Float(getX() - radius, getY() - radius, radius * 2f, radius * 2f));
	}

	public float getDamage(){
		return damage;
	}

	public double getLifeTimeSeconds(){
		return lifeTimeSeconds;
	}
	public float getLength(){
		return length;
	}
	public float getCanNotHitOwnPlayerTimeSeconds(){
		return canNotHitOwnPlayerTimeSeconds;
	}
	
	
}
