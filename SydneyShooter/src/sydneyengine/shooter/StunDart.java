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
public class StunDart extends Bullet {

	static float canNotHitOwnPlayerTimeSeconds = 1.0f;
	static float playerSpeedIncrease = .2f;
	static double stunTimeSeconds = 5;
	static float playerReloadSpeedMultiplier = 2f;
	protected float maxRange;
	float length;
	float maxSpeed;
	float damage;
	float angle;


	public StunDart() {
		super();
		player = null;
	}

	public StunDart(TranquilizerGun gun, Player player, float newX, float newY, float angle, double spawnTimeSeconds, float xLaunchSpeed, float yLaunchSpeed) {
		super(player, newX, newY, angle, spawnTimeSeconds, xLaunchSpeed, yLaunchSpeed);
		assert Point2D.distance(player.getX(), player.getY(), newX, newY) < player.getR() : Point2D.distance(player.getX(), player.getY(), newX, newY);
		length = 5;
		this.angle = angle;
		
		maxSpeed = 1000;
		maxRange = 1000;
		speedX = xLaunchSpeed + (float) Math.cos(angle) * maxSpeed;
		speedY = yLaunchSpeed + (float) Math.sin(angle) * maxSpeed;
		
		damage = 2.0f;

		this.x = newX + (float) Math.cos(angle) * length;
		this.y = newY + (float) Math.sin(angle) * length;
		backX = newX;
		backY = newY;
		oldBackX = backX;
		oldBackY = backY;
	}
	
	public void hitPlayer(Player hitPlayer, double timeOfHit){
		hitPlayer.newPersonalMessage("***STUNNED***", timeOfHit);
		hitPlayer.takeDamage(this, timeOfHit);
		float playerSpeedIncreaseAfterHit = hitPlayer.getSpeedMultiplier()*this.playerSpeedIncrease;
		if (playerSpeedIncreaseAfterHit < this.playerSpeedIncrease){
			playerSpeedIncreaseAfterHit = this.playerSpeedIncrease;
		}
		
		float playerReloadSpeedIncreaseAfterHit = hitPlayer.getReloadSpeedMultiplier()*(1+this.playerSpeedIncrease);
		if (playerReloadSpeedIncreaseAfterHit > this.playerReloadSpeedMultiplier){
			playerReloadSpeedIncreaseAfterHit = playerReloadSpeedMultiplier;
		}
		
		double playerStunTimeSeconds = hitPlayer.getSpeedMultiplierLengthOfTimeSeconds() + hitPlayer.getSpeedMultiplierStartTimeSeconds() - timeOfHit;
		if (playerStunTimeSeconds < 0){
			playerStunTimeSeconds = stunTimeSeconds;
		}else{
			playerStunTimeSeconds += this.stunTimeSeconds;
			if (playerStunTimeSeconds > this.stunTimeSeconds){
				playerStunTimeSeconds = this.stunTimeSeconds;
			}
		}
		hitPlayer.setSpeedMultiplier(playerSpeedIncreaseAfterHit, timeOfHit, playerStunTimeSeconds);
		hitPlayer.setReloadSpeedMultiplier(playerReloadSpeedIncreaseAfterHit, timeOfHit, playerStunTimeSeconds);
		
		//stuns the player, so he cannot fire... < and more?>
		hitPlayer.setStunned(timeOfHit, stunTimeSeconds);
		this.playerThatWasHit = hitPlayer;
		dead = true;
		
	}

	transient Stroke stroke = new BasicStroke(4f);
	public void render(ViewPane viewPane) {
		Graphics2D g = viewPane.getBackImageGraphics2D();
		g.setColor(Color.white);
		Stroke oldStroke = g.getStroke();
		g.setStroke(stroke);
		g.draw(new Line2D.Float(x, y, backX, backY));
		g.setStroke(oldStroke);
	}

	public float getDamage(){
		return damage;
	}
	public float getLength(){
		return length;
	}
	public float getCanNotHitOwnPlayerTimeSeconds(){
		return canNotHitOwnPlayerTimeSeconds;
	}
	public double getLifeTimeSeconds(){
		return maxRange / maxSpeed;
	}
	
}
