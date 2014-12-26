/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.shooter.Gun;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;

import sydneyengine.shooter.GameWorld;
import sydneyengine.shooter.Player;
import sydneyengine.shooter.ViewPane;
import sydneyengine.shooter.Bullet.Bullet;
import sydneyengine.shooter.Bullet.ShotGunPellet;
import sydneyengine.shooter.Item.Item;
/**
 *
 * @author CommanderKeith
 */
public class ShotGun extends Gun{
	
	static int playerGunNum = 3;
	float coneAngle;
	
	public ShotGun(){
		respawn();
	}
	public ShotGun(GameWorld world) {
		this();
		this.world = world;
	//System.err.println("getSSCode() == "+getSSCode()+", dead == "+dead);
	}
	
	@Override
	public void respawn() {
		super.respawn();
		gunLength = 5;
		reloadSeconds = 0.2f;
		reloadClipSeconds = 1.5f;
		ammo = 16;
		maxAmmoInClip = 2;
		ammoInCurrentClip = maxAmmoInClip;
		maxGunRotationSpeed = (float)Math.PI*2f;
		coneAngle = (float)Math.PI/12f;
		
	}
	@Override
	public float getRangeForBotAiming(){
		return 500;
	}
	@Override
	public int getPlayerGunNum(){
		return playerGunNum;
	}
	@Override
	public Item createNewItem(GameWorld world){
		return new ShotGun(world);
	}
	@Override
	public void fire(double secondsLeft, double timeAtStartOfMoveSeconds, Player player, float xPosWhenFired, float yPosWhenFired, float angle, 
			//float mouseX, float mouseY, 
			float playerSpeedX, float playerSpeedY){
		for (int i = 0; i < 25; i++){
			Bullet bullet = createBullet(player, xPosWhenFired, yPosWhenFired, angle, player.getMouseTargetX(), player.getMouseTargetY(), timeAtStartOfMoveSeconds, playerSpeedX, playerSpeedY);
			// Move the bullet to where it should be at the end of this move. Note that each bullet in world.bullets has already had doMove called on it so there won't be any doubling up.
			bullet.doMove(secondsLeft, timeAtStartOfMoveSeconds);
			player.getWorld().getBullets().add(bullet);
		}
	}
	@Override
	public Bullet createBullet(Player player, float xPosWhenFired, float yPosWhenFired, float gunAngle, float mouseX, float mouseY, double lastTimeFiredSeconds, float xLaunchSpeed, float yLaunchSpeed){
		world.getRandom().setSeed(seed);
		seed += 3491;
		float randomAngleIncrement = (float)((world.getRandom().nextFloat()-0.5)*coneAngle);
		return new ShotGunPellet(this, player, xPosWhenFired, yPosWhenFired, gunAngle + randomAngleIncrement, lastTimeFiredSeconds, xLaunchSpeed, yLaunchSpeed);
	}
	
	transient BasicStroke stroke = new BasicStroke(4);
	@Override
	public void render(ViewPane viewPane) {
		super.render(viewPane);
		Graphics2D g = viewPane.getBackImageGraphics2D();
		g.setColor(Color.GRAY);
		Stroke oldStroke = g.getStroke();
		g.setStroke(stroke);
		g.draw(new Line2D.Float(player.getX(), player.getY(), (float) (player.getX() + gunLength * Math.cos(gunAngleRelativeToPlayer+player.getAngle())), (float) (player.getY() + gunLength * Math.sin(gunAngleRelativeToPlayer+player.getAngle()))));
		g.setStroke(oldStroke);
	}
	
}
