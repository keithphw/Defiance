/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sydneyengine.shooter.Bullet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import sydneyengine.shooter.Player;
import sydneyengine.shooter.ViewPane;

/**
 *
 * @author CommanderKeith
 */
public class BallisticRocket extends Bullet {

	static float canNotHitOwnPlayerTimeSeconds = 1.0f;
	public float maxRange;
	float length;
	float maxSpeed;
	float damage;
	float angle;
	float accelX;
	float accelY;
	double effective_range;
	float startX; float startY;

	public BallisticRocket() {
		super();
	}

	public BallisticRocket(Player player, float newX, float newY, float angle, float mouseX, float mouseY, double spawnTimeSeconds, float xLaunchSpeed, float yLaunchSpeed) {
		super(player, newX, newY, angle, spawnTimeSeconds, xLaunchSpeed, yLaunchSpeed);
		assert Point2D.distance(player.getX(), player.getY(), newX, newY) < player.getR() 
		: Point2D.distance(player.getX(), player.getY(), newX, newY);
		length = 12f;
		this.angle = angle;
		
		float startSpeed = 20;
		float accel = 600;
		maxSpeed = 600;
		damage = 200; // and even more damage is caused by explosion (rocket shrapnel)
		
		effective_range = Point2D.distance(newX, newY, mouseX, mouseY);
		this.startX= newX; this.startY = newY;
		
		
		maxRange = 3500;
		speedX = xLaunchSpeed + (float) Math.cos(angle) * startSpeed;
		speedY = yLaunchSpeed + (float) Math.sin(angle) * startSpeed;
		accelX = (float) Math.cos(angle) * accel;
		accelY = (float) Math.sin(angle) * accel;

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
		g.setColor(Color.green.darker());
		//g.fill(new java.awt.geom.Ellipse2D.Float(getX() - radius, getY() - radius, radius * 2f, radius * 2f));
		//System.out.println(this.getClass().getSimpleName()+": render!!!");
		Stroke oldStroke = g.getStroke();
		g.setStroke(new BasicStroke(6));
		g.draw(new Line2D.Float(x, y, backX, backY));//,(float)(x+length*Math.cos(angle)), (float)(y+length*Math.sin(angle))));
		
		//exhaust
		g.setStroke(new BasicStroke(4));
		g.setColor(Color.yellow);
		g.draw(new Line2D.Float(backX,backY, Math.round(oldBackX - speedX*.01), Math.round(oldBackY - speedY*.01)));
		g.setStroke(oldStroke);
	}
	@Override
	protected void doBulletMove(double seconds, double timeAtStartOfMoveSeconds) {
		if(Math.abs(Point2D.distance(x, y, startX, startY) * .75 - effective_range) <=15) // overshoots the target a little
		{
			super.tryRocketEffect(timeAtStartOfMoveSeconds);
			dead = true;
		}
		else
		{
			super.doBulletMove(seconds, timeAtStartOfMoveSeconds);
		}
		if(this.getSpeed()>this.maxSpeed)
		{
			this.speedX = (float) (maxSpeed * Math.cos(this.angle));
			this.speedY = (float) (maxSpeed * Math.sin(this.angle));
		}
		
	}
	
	@Override
	public float getAccelX(){
		return accelX;
	}
	@Override
	public float getAccelY(){
		return accelY;
	}
	@Override
	public float getDamage(){
		return damage;
	}

	@Override
	public double getLifeTimeSeconds(){
		return maxRange / maxSpeed;
	}
	@Override
	public float getLength(){
		return length;
	}
	@Override
	public float getCanNotHitOwnPlayerTimeSeconds(){
		return canNotHitOwnPlayerTimeSeconds;
	}
}

