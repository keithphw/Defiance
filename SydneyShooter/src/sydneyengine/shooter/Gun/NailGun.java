package sydneyengine.shooter.Gun;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;

import sydneyengine.shooter.CustomColor;
import sydneyengine.shooter.GameWorld;
import sydneyengine.shooter.Player;
import sydneyengine.shooter.ViewPane;
import sydneyengine.shooter.Bullet.Bullet;
import sydneyengine.shooter.Bullet.NailBullet;
import sydneyengine.shooter.Item.Item;

public class NailGun extends Gun{
	
	static int playerGunNum = 9;
	long seed = -Long.MAX_VALUE;	// seed for random number generation.
	float coneAngle;
	
	public NailGun(){
		respawn();
	}
	public NailGun(GameWorld world) {
		this();
		this.world = world;
	//System.err.println("getSSCode() == "+getSSCode()+", dead == "+dead);
	}
	
	@Override
	public void respawn() {
		super.respawn();
		gunLength = 6;
		reloadSeconds = 0.1f;
		reloadClipSeconds = 4;
		ammo = 270;
		maxAmmoInClip = 90;
		ammoInCurrentClip = maxAmmoInClip;
		maxGunRotationSpeed = (float)Math.PI*2f;
		coneAngle = (float)Math.PI/14f;
	}
	@Override
	public float getRangeForBotAiming(){
		return 650;
	}
	@Override
	public int getPlayerGunNum(){
		return playerGunNum;
	}
	@Override
	public Item createNewItem(GameWorld world){
		return new MachineGun(world);
	}
	@Override
	public void fire(double secondsLeft, double timeAtStartOfMoveSeconds, Player player, float xPosWhenFired, float yPosWhenFired, float angle, 
			//float mouseX, float mouseY, 
			float playerSpeedX, float playerSpeedY){
		for (int i = 0; i < 3; i++){
			Bullet bullet = createBullet(player, xPosWhenFired, yPosWhenFired, angle, player.getMouseTargetX(), player.getMouseTargetY(), timeAtStartOfMoveSeconds, playerSpeedX, playerSpeedY);
			// Move the bullet to where it should be at the end of this move. Note that each bullet in world.bullets has already had doMove called on it so there won't be any doubling up.
			bullet.doMove(secondsLeft, timeAtStartOfMoveSeconds);
			player.getWorld().getBullets().add(bullet);
		}
	}
	@Override
	public Bullet createBullet(Player player, float xPosWhenFired, float yPosWhenFired, float gunAngle, float mouseX, float mouseY, double lastTimeFiredSeconds, float xLaunchSpeed, float yLaunchSpeed){
		world.getRandom().setSeed(seed);
		seed += 2876;
		float randomAngleIncrement = (float)((world.getRandom().nextFloat()-0.5)*coneAngle);
		return new NailBullet(this, player, xPosWhenFired, yPosWhenFired, gunAngle + randomAngleIncrement, lastTimeFiredSeconds, xLaunchSpeed, yLaunchSpeed);
	}
	
	transient BasicStroke stroke = new BasicStroke(3);
	@Override
	public void render(ViewPane viewPane) {
		super.render(viewPane);
		Graphics2D g = viewPane.getBackImageGraphics2D();
		g.setColor(CustomColor.MAROON);
		Stroke oldStroke = g.getStroke();
		g.setStroke(stroke);
		g.draw(new Line2D.Float(player.getX(), player.getY(), (float) (player.getX() + gunLength * Math.cos(gunAngleRelativeToPlayer+player.getAngle())), (float) (player.getY() + gunLength * Math.sin(gunAngleRelativeToPlayer+player.getAngle()))));
		g.setStroke(oldStroke);
	}
	
	
}
