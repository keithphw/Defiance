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
public class TranquilizerGun extends Gun{
	
	static int playerGunNum = 7;
	float coneAngle;
	
	public TranquilizerGun(){
		respawn();
	}
	public TranquilizerGun(GameWorld world) {
		this();
		this.world = world;
	//System.err.println("getSSCode() == "+getSSCode()+", dead == "+dead);
	}
	
	public void respawn() {
		super.respawn();
		gunLength = 6;
		reloadSeconds = 0.33f;
		reloadClipSeconds = 3;
		ammo = 40;
		maxAmmoInClip = 8;
		ammoInCurrentClip = maxAmmoInClip;
		maxGunRotationSpeed = (float)Math.PI*6f;
		coneAngle = (float)Math.PI/16f;
	}
	public int getPlayerGunNum(){
		return playerGunNum;
	}
	public Item createNewItem(GameWorld world){
		return new TranquilizerGun(world);
	}
	public Bullet createBullet(Player player, float xPosWhenFired, float yPosWhenFired, float gunAngle, float mouseX, float mouseY, double lastTimeFiredSeconds, float xLaunchSpeed, float yLaunchSpeed){
		/*world.getRandom().setSeed(seed);
		seed += 3491;
		float randomAngleIncrement = (float)((world.getRandom().nextFloat()-0.5)*coneAngle);
		*/
		return new StunDart(this, player, xPosWhenFired, yPosWhenFired, gunAngle, lastTimeFiredSeconds, xLaunchSpeed, yLaunchSpeed);
	}
	
	transient BasicStroke stroke = new BasicStroke(3);
	public void render(ViewPane viewPane) {
		super.render(viewPane);
		Graphics2D g = viewPane.getBackImageGraphics2D();
		g.setColor(Color.WHITE);
		Stroke oldStroke = g.getStroke();
		g.setStroke(stroke);
		g.draw(new Line2D.Float(player.getX(), player.getY(), (float) (player.getX() + gunLength * Math.cos(gunAngleRelativeToPlayer+player.getAngle())), (float) (player.getY() + gunLength * Math.sin(gunAngleRelativeToPlayer+player.getAngle()))));
		g.setStroke(oldStroke);
	}
	
	
}
