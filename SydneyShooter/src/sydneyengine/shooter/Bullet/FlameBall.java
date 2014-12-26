/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sydneyengine.shooter.Bullet;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

import sydneyengine.shooter.CustomColor;
import sydneyengine.shooter.Obstacle;
import sydneyengine.shooter.Player;
import sydneyengine.shooter.ViewPane;
import sydneyengine.shooter.Gun.FlameThrower;

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
	float startX;
	float startY;
	double effective_range;
	
	int colorNum;
	public static ArrayList<Color> colors = new ArrayList<Color>();
	static{
		colors.add(CustomColor.ORANGE_RED);
		colors.add(Color.ORANGE);
		colors.add(Color.YELLOW);
		colors.add(CustomColor.DARK_ORANGE);
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
		
		effective_range = Point2D.distance(newX, newY, mouseX, mouseY);
		float randomSpeedIncrement = (float) (world.getRandom().nextFloat()* effective_range / 8);
		effective_range += randomSpeedIncrement;	
		
		//System.out.println("Effective Range: "+ effective_range);
		
		float startSpeed = 300 + randomSpeedIncrement;
		
		
		
		gun.getWorld().getRandom().setSeed(gun.getSeed());
		gun.setSeed(gun.getSeed()+3410);
		float randomRangeIncrement = world.getRandom().nextFloat()*100;
		
		float range = 200 + randomRangeIncrement; // doesn't really matter, only affects the lifetime
		
		speedX = xLaunchSpeed + (float) Math.cos(angle) * startSpeed;
		speedY = yLaunchSpeed + (float) Math.sin(angle) * startSpeed;

		
		float launchSpeed = startSpeed;//(float)Math.sqrt(Math.pow(speedX, 2) + Math.pow(speedY, 2));
		lifeTimeSeconds = range / launchSpeed+18;
		
		this.startX = newX;
		this.startY = newY;
		
		
		this.x = newX + (float) Math.cos(angle) * length;
		this.y = newY + (float) Math.sin(angle) * length;
		backX = newX;
		backY = newY;
		oldBackX = backX;
		oldBackY = backY;
	}

	@Override
	public void render(ViewPane viewPane) {
		Graphics2D g = viewPane.getBackImageGraphics2D();
		Color orig = g.getColor();
		
		Random r= new Random();
		if(r.nextInt(400)==9) { colorNum= r.nextInt(FlameBall.colors.size()); }
		
		g.setColor(colors.get(colorNum));
		g.fill(new java.awt.geom.Ellipse2D.Float(getX() - radius, getY() - radius, radius * 2f, radius * 2f));
		
		g.setColor(orig);
	}
	
	@Override
	public void hitObstacle(Obstacle hitObstacle, double timeOfHit){
		// most napalm sticks to walls
		if(world.getRandom().nextInt(10)<5)
		{
			Point2D.Float midpoint= new Point2D.Float((this.oldBackX+ this.getX())/2, (this.oldBackY+ this.getY())/2);
			this.setSpeedX(0); this.setSpeedY(0);
			this.setX(midpoint.x); this.setY(midpoint.y);
			world.incrementAndReSeedRandom();
		}
		else dead= true;

	}
	@Override
	protected void doBulletMove(double seconds, double timeAtStartOfMoveSeconds) {
		if(Math.abs(Point2D.distance(x, y, startX, startY) - effective_range) <=10)
		{
			speedX= 0; speedY = 0;
		}
		
		super.doBulletMove(seconds, timeAtStartOfMoveSeconds);

	}

	@Override
	public float getDamage(){
		return damage;
	}

	@Override
	public double getLifeTimeSeconds(){
		return lifeTimeSeconds;
	}
	@Override
	public float getLength(){
		return length;
	}
	@Override
	public float getCanNotHitOwnPlayerTimeSeconds(){
		return canNotHitOwnPlayerTimeSeconds;
	}
	//added by KT 12/20/2014
	@Override
	public float getAccelX(){
		//if(Math.abs(speedX)>10)
		accelX = -speedX * .6f; //else {accelX=0; speedX=0;}
			
		return accelX;
	}
	@Override
	public float getAccelY(){
		
		//if(Math.abs(speedY)>10)
		accelY = -speedY * .6f; //else { accelY=0; speedY=0;}
		return accelY;
	}
	
	
}
