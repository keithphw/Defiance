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
public class PistolBullet extends Bullet {

	static float canNotHitOwnPlayerTimeSeconds = 1.0f;
	protected float maxRange;
	float radius;
	float length;
	float maxSpeed;
	float damage;
	float angle;


	public PistolBullet() {
		super();
		player = null;
	}

	public PistolBullet(Player player, float newX, float newY, float angle, double spawnTimeSeconds, float xLaunchSpeed, float yLaunchSpeed) {
		super(player, newX, newY, angle, spawnTimeSeconds, xLaunchSpeed, yLaunchSpeed);
		assert Point2D.distance(player.getX(), player.getY(), newX, newY) < player.getR() : Point2D.distance(player.getX(), player.getY(), newX, newY);
		radius = 2.0f;
		length = 2*radius;;
		this.angle = angle;
		maxSpeed = 700;
		damage = 7.0f;
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

	//transient BasicStroke stroke = new BasicStroke(3);
	public void render(ViewPane viewPane) {
		Graphics2D g = viewPane.getBackImageGraphics2D();
		g.setColor(Color.DARK_GRAY);
		g.fill(new java.awt.geom.Ellipse2D.Float(getX() - radius, getY() - radius, radius * 2f, radius * 2f));
	}

	public float getDamage(){
		return damage;
	}

	public double getLifeTimeSeconds(){
		return maxRange / maxSpeed;
	}
	public float getLength(){
		return length;
	}
	public float getCanNotHitOwnPlayerTimeSeconds(){
		return canNotHitOwnPlayerTimeSeconds;
	}
	
	
}
