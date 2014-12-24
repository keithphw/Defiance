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
public class FlameBall extends Bullet {

	static float canNotHitOwnPlayerTimeSeconds = 1.0f;
	static float radius;
	static float length;
	float lifeTimeSeconds;
	static float damage;
	float angle;
	float accelX;
	float accelY;
	
	int colorNum;
	public static ArrayList<Color> colors = new ArrayList<Color>();
	static{
		colors.add(Color.RED);
		colors.add(Color.ORANGE);
		colors.add(Color.YELLOW);
		//colors.add(Color.WHITE);
	}


	public FlameBall() {
		super();
		player = null;
	}

	public FlameBall(FlameThrower gun, Player player, float newX, float newY, float angle, float mouseX, float mouseY, double spawnTimeSeconds, float xLaunchSpeed, float yLaunchSpeed, int colorNum) {
		super(player, newX, newY, angle, spawnTimeSeconds, xLaunchSpeed, yLaunchSpeed);
		assert Point2D.distance(player.getX(), player.getY(), newX, newY) < player.getR() : Point2D.distance(player.getX(), player.getY(), newX, newY);
		this.colorNum = colorNum;
		radius = 3f;
		length = 2*radius;
		damage = 6f;
		this.angle = angle;
		gun.getWorld().getRandom().setSeed(gun.getSeed());
		gun.setSeed(gun.getSeed()+341);
		float randomSpeedIncrement = world.getRandom().nextFloat()*120;
		float startSpeed = 150 + randomSpeedIncrement;
		gun.getWorld().getRandom().setSeed(gun.getSeed());
		gun.setSeed(gun.getSeed()+3410);
		float randomRangeIncrement = world.getRandom().nextFloat()*100;
		float range = 200 + randomRangeIncrement;
		speedX = xLaunchSpeed + (float) Math.cos(angle) * startSpeed;
		speedY = yLaunchSpeed + (float) Math.sin(angle) * startSpeed;

		
		float launchSpeed = startSpeed;//(float)Math.sqrt(Math.pow(speedX, 2) + Math.pow(speedY, 2));
		lifeTimeSeconds = range / launchSpeed+18;
		
		this.x = newX + (float) Math.cos(angle) * length;
		this.y = newY + (float) Math.sin(angle) * length;
		backX = newX;
		backY = newY;
		oldBackX = backX;
		oldBackY = backY;
	}

	public void render(ViewPane viewPane) {
		Graphics2D g = viewPane.getBackImageGraphics2D();
		Random r= new Random();
		if(r.nextInt(400)==9) { colorNum= r.nextInt(2); }
		g.setColor(colors.get(colorNum));
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
	//added by KT 12/20/2014
	public float getAccelX(){
		//if(Math.abs(speedX)>10)
		accelX = -speedX/2; //else {accelX=0; speedX=0;}
			
		return accelX;
	}
	public float getAccelY(){
		
		//if(Math.abs(speedY)>10)
		accelY = -speedY/2; //else { accelY=0; speedY=0;}
		return accelY;
	}
	
	
}
