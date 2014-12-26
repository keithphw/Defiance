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
public class SniperRifleBullet extends Bullet {

	static float canNotHitOwnPlayerTimeSeconds = 1.0f;
	public float maxRange;
	float length;
	float maxSpeed;
	float damage;
	float angle;

	public SniperRifleBullet() {
		super();
	}

	public SniperRifleBullet(Player player, float newX, float newY, float angle, double spawnTimeSeconds, float xLaunchSpeed, float yLaunchSpeed) {
		super(player, newX, newY, angle, spawnTimeSeconds, xLaunchSpeed, yLaunchSpeed);
		assert Point2D.distance(player.getX(), player.getY(), newX, newY) < player.getR() : Point2D.distance(player.getX(), player.getY(), newX, newY);
		length = 15f;
		this.angle = angle;
		maxSpeed = 1500;
		damage = 100;
		maxRange = 2000;
		speedX = xLaunchSpeed + (float) Math.cos(angle) * maxSpeed;
		speedY = yLaunchSpeed + (float) Math.sin(angle) * maxSpeed;

		this.x = newX + (float) Math.cos(angle) * length;
		this.y = newY + (float) Math.sin(angle) * length;
		backX = newX;
		backY = newY;
		oldBackX = backX;
		oldBackY = backY;
	}
	transient BasicStroke stroke = new BasicStroke(3);

	@Override
	public void render(ViewPane viewPane) {
		Graphics2D g = viewPane.getBackImageGraphics2D();
		g.setColor(Color.BLACK);
		//g.fill(new java.awt.geom.Ellipse2D.Float(getX() - radius, getY() - radius, radius * 2f, radius * 2f));
		//System.out.println(this.getClass().getSimpleName()+": render!!!");
		Stroke oldStroke = g.getStroke();
		g.setStroke(stroke);
		g.draw(new Line2D.Float(x, y, backX, backY));//,(float)(x+length*Math.cos(angle)), (float)(y+length*Math.sin(angle))));
		//g.setColor(Color.PINK);
		//g.draw(new Line2D.Float(backX,backY, oldBackX, oldBackY));
		g.setStroke(oldStroke);
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
