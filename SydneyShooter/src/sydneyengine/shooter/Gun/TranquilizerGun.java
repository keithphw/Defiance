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
import sydneyengine.shooter.Bullet.StunDart;
import sydneyengine.shooter.Item.Item;
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
	
	@Override
	public void respawn() {
		super.respawn();
		gunLength = 6;
		reloadSeconds = 0.33f;
		reloadClipSeconds = 3;
		ammo = 24;
		maxAmmoInClip = 6;
		ammoInCurrentClip = maxAmmoInClip;
		maxGunRotationSpeed = (float)Math.PI*6f;
		coneAngle = (float)Math.PI/16f;
	}
	@Override
	public int getPlayerGunNum(){
		return playerGunNum;
	}
	@Override
	public Item createNewItem(GameWorld world){
		return new TranquilizerGun(world);
	}
	@Override
	public Bullet createBullet(Player player, float xPosWhenFired, float yPosWhenFired, float gunAngle, float mouseX, float mouseY, double lastTimeFiredSeconds, float xLaunchSpeed, float yLaunchSpeed){
		/*world.getRandom().setSeed(seed);
		seed += 3491;
		float randomAngleIncrement = (float)((world.getRandom().nextFloat()-0.5)*coneAngle);
		*/
		return new StunDart(this, player, xPosWhenFired, yPosWhenFired, gunAngle, lastTimeFiredSeconds, xLaunchSpeed, yLaunchSpeed);
	}
	
	transient BasicStroke stroke = new BasicStroke(3);
	@Override
	public void render(ViewPane viewPane) {
		super.render(viewPane);
		Graphics2D g = viewPane.getBackImageGraphics2D();
		Color orig = g.getColor();
		g.setColor(Color.WHITE);
		Stroke oldStroke = g.getStroke();
		g.setStroke(stroke);
		g.draw(new Line2D.Float(player.getX(), player.getY(), (float) (player.getX() + gunLength * Math.cos(gunAngleRelativeToPlayer+player.getAngle())), (float) (player.getY() + gunLength * Math.sin(gunAngleRelativeToPlayer+player.getAngle()))));
		g.setStroke(oldStroke);
		g.setColor(orig);
	}
	
	
}
