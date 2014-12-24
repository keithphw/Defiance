package sydneyengine.shooter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

public class RocketShrapnel extends Bullet{
	static float canNotHitOwnPlayerTimeSeconds = 0.01f;
	static float radius;
	static float length;
	float lifeTimeSeconds;
	static float damage;
	float angle;
	float startX; float startY;


	public RocketShrapnel() {
		super();
		player = null;
	}

	public RocketShrapnel(Player player, float newX, float newY, float angle, double spawnTimeSeconds, float xLaunchSpeed, float yLaunchSpeed) {
		super(player, newX, newY, angle, spawnTimeSeconds, xLaunchSpeed, yLaunchSpeed);
		assert Point2D.distance(player.getX(), player.getY(), newX, newY) < player.getR() 
		: Point2D.distance(player.getX(), player.getY(), newX, newY);
		radius = 2f;
		length = 2*radius;
		damage = 8f;
		this.angle = angle;

		float startSpeed = 300;
		//float randomRangeIncrement = world.getRandom().nextFloat()*140;
		float range = 200;
				//randomRangeIncrement;
		speedX = xLaunchSpeed + (float) Math.cos(angle) * startSpeed;
		speedY = yLaunchSpeed + (float) Math.sin(angle) * startSpeed;

		
		float launchSpeed = startSpeed;//(float)Math.sqrt(Math.pow(speedX, 2) + Math.pow(speedY, 2));
		lifeTimeSeconds = range / launchSpeed;
		
		this.x = newX + (float) Math.cos(angle) * length;
		this.y = newY + (float) Math.sin(angle) * length;
		backX = newX;
		backY = newY;
		oldBackX = backX;
		oldBackY = backY;
		
		startX= newX; startY= newY;
	}

	public void render(ViewPane viewPane) {
		Graphics2D g = viewPane.getBackImageGraphics2D();
		g.setColor(Color.red);
		g.fill(new java.awt.geom.Ellipse2D.Float(getX() - radius, getY() - radius, radius * 2f, radius * 2f));
		g.setColor(Color.yellow);
		g.drawLine((int)(this.startX+ getX())/2, (int)(this.startY+ getY())/2, (int)this.getX(), (int)this.getY());
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
