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
public abstract class Gun extends SSAdapter implements Item {

	transient Color readyToFireColor = new Color(0f, 1f, 0f, 0.8f);
	transient Color emptyColor = new Color(0.1f, 0.1f, 0.1f, 0.5f);//new Color(0.8f, 0.8f, 0.8f, 0.5f);
	
	protected float gunAngleRelativeToPlayer;
	protected float maxGunRotationSpeed;
	protected boolean firing;
	protected double lastTimeFiredSeconds;
	//protected double triggerPressSeconds;
	protected float gunLength;
	protected GameWorld world;
	// total ammo is ammo in ammoInCurrentClip + ammo.
	int ammo;
	int maxAmmoInClip;
	int ammoInCurrentClip;
	protected float reloadSeconds;
	protected float reloadClipSeconds = 1;
	protected double timeUntilReloaded = 0;
	boolean reloadClip = false;
	protected Player player;
	protected long seed = -Long.MAX_VALUE; // seed for random number generation.

	// need to over-ride the below methods.
	abstract public Bullet createBullet(Player player, float xPosWhenFired, float yPosWhenFired, float gunAngle, float mouseX, float mouseY, double fireTimeSeconds, float xLaunchSpeed, float yLaunchSpeed);
	public abstract int getPlayerGunNum();
	public abstract Item createNewItem(GameWorld world);
	// this should also be over-rided.
	public float getRangeForBotAiming(){
		return 300;
	}
	
	public int getTotalAmmo() {
		return ammo + ammoInCurrentClip;
	}

	public void render(ViewPane viewPane) {
		if (viewPane.getPlayer() != this.getPlayer()){
			return;
		}
		// This draws the reload bar and ammo bar in the bottom right of the viewPane.
		Graphics2D g = viewPane.getBackImageGraphics2D();
		assert getLastTimeFiredSeconds() <= world.getPureTotalElapsedNanos() : world.getPureTotalElapsedNanos() + ", " + getLastTimeFiredSeconds();
		//double timeSinceLastTimeFired = world.getTotalElapsedSeconds() - getLastTimeFiredSeconds();
		float proportionOfReloadTimeElapsed;
		if (reloadClip == true || getTotalAmmo() == 0 || player.isDead() == true) {
			proportionOfReloadTimeElapsed = 0;
		} else {
			assert reloadClip == false : reloadClip;
			proportionOfReloadTimeElapsed = (float)(1f - timeUntilReloaded / getReloadSeconds());
			proportionOfReloadTimeElapsed = Math.min(proportionOfReloadTimeElapsed, 1);
		}
		float proportionOfAmmoInClip;
		if (reloadClip == false){
			proportionOfAmmoInClip = (float)ammoInCurrentClip/maxAmmoInClip;
		}else{
			float proportionOfClipLeftToLoad = (float)(timeUntilReloaded / this.getReloadClipSeconds());
			int ammoToPutIn = maxAmmoInClip - ammoInCurrentClip;
			if (ammoToPutIn > ammo){
				ammoToPutIn = ammo;
			}
			float clipFullnessAfterReload = ((float)ammoToPutIn + ammoInCurrentClip)/(float)maxAmmoInClip;
			proportionOfAmmoInClip = clipFullnessAfterReload - proportionOfClipLeftToLoad;
			assert proportionOfAmmoInClip >= 0 && proportionOfAmmoInClip <= 1: proportionOfAmmoInClip+", "+clipFullnessAfterReload+", "+proportionOfClipLeftToLoad;
		}
		assert proportionOfReloadTimeElapsed <= 1 && proportionOfReloadTimeElapsed >= 0 : proportionOfReloadTimeElapsed;
		assert proportionOfAmmoInClip <= 1 && proportionOfAmmoInClip >= 0 : proportionOfAmmoInClip;
		
		AffineTransform oldAT = g.getTransform();
		g.setTransform(viewPane.getOriginalTransform());
		float xPos = viewPane.getWidth() - 60;
		float yPos = viewPane.getHeight() - 60;

		// draw the reload bar
		float healthBarWidth = 16;
		float healthBarHeight = 50;
		float spaceBetweenBars = 5;

		if (proportionOfReloadTimeElapsed != 0) {
			g.setColor(readyToFireColor);
			float xHeightOfGreen = healthBarHeight * proportionOfReloadTimeElapsed;
			g.fill(new Rectangle2D.Float(xPos + healthBarWidth/2, yPos + healthBarHeight - xHeightOfGreen, 4, xHeightOfGreen));
		}
		if (proportionOfReloadTimeElapsed != 1f) {
			float xHeightOfRed = healthBarHeight * (1f - proportionOfReloadTimeElapsed);
			g.setColor(emptyColor);//(reloadClip == true ? new Color(0,proportionOfClipReLoaded,0) : Color.BLACK));
			g.fill(new Rectangle2D.Float(xPos + healthBarWidth/2, yPos, 4, xHeightOfRed));
		}
		
		// draw the ammo in clip
		xPos += healthBarWidth + spaceBetweenBars;
		if (proportionOfAmmoInClip != 0) {
			g.setColor(readyToFireColor);
			float xHeightOfGreen = healthBarHeight * proportionOfAmmoInClip;
			g.fill(new Rectangle2D.Float(xPos, yPos + healthBarHeight - xHeightOfGreen, healthBarWidth, xHeightOfGreen));
		}
		if (proportionOfAmmoInClip != 1f) {
			float xHeightOfRed = healthBarHeight * (1f - proportionOfAmmoInClip);
			g.setColor(emptyColor);//(reloadClip == true ? new Color(0,1.0f,0) : Color.BLACK));
			g.fill(new Rectangle2D.Float(xPos, yPos, healthBarWidth, xHeightOfRed));
		}
		// draw the name
		FontMetrics fm = viewPane.getFontMetrics(g.getFont());
		float halfStringWidth = fm.stringWidth(this.getName()) / 2f;
		//float stringHeight = fm.getHeight();
		int nameYCoord = (int) yPos - 3;
		g.setColor(Color.black);
		float xCoordBetweenBars = xPos - spaceBetweenBars/2f;
		g.drawString(this.getName(), xCoordBetweenBars - halfStringWidth, nameYCoord);
		// draw the number of clips
		String numClipsString = String.valueOf((int)Math.ceil((float)ammo/maxAmmoInClip));
		halfStringWidth = fm.stringWidth(numClipsString) / 2f;
		g.setColor(Color.black);
		g.drawString(numClipsString, xPos + healthBarWidth/2f - halfStringWidth, yPos + healthBarHeight/2f + 3);
		g.setTransform(oldAT);
	}
	public void respawn() {
		gunAngleRelativeToPlayer = 0;
		//shouldFire = false;
		firing = false;
		lastTimeFiredSeconds = 0;
		//triggerPressSeconds = 0;
	}
	
	public void doMoveAndBulletFire(double seconds, double timeAtStartOfMoveSeconds) {
		assert seconds >= 0 && timeUntilReloaded >= 0 : seconds+", "+timeUntilReloaded;
		assert player != null;
		double timeAtEndOfMoveSeconds = timeAtStartOfMoveSeconds + seconds;

		if (timeUntilReloaded > 0){
			if (timeUntilReloaded > seconds){
				timeUntilReloaded -= seconds;
				player.doMoveBetweenGunFires(seconds, timeAtStartOfMoveSeconds);
				seconds = 0;
				timeAtStartOfMoveSeconds += seconds;
				return;
			}else{
				player.doMoveBetweenGunFires(timeUntilReloaded, timeAtStartOfMoveSeconds);
				seconds -= timeUntilReloaded;
				timeAtStartOfMoveSeconds += timeUntilReloaded;
				timeUntilReloaded = 0;
			}
		}
		assert seconds >= 0 && timeUntilReloaded >= 0 : seconds+", "+timeUntilReloaded;
		while (player.isDead() == false && firing && getTotalAmmo() > 0 && timeUntilReloaded == 0){
			assert getTotalAmmo() >= 0 && timeUntilReloaded == 0: getTotalAmmo()+", "+timeUntilReloaded;
			firing = true;
			if (reloadClip == true) {
				assert timeUntilReloaded == 0 : timeUntilReloaded;
				doClipReload();
			}
			assert ammoInCurrentClip >= 0 : ammoInCurrentClip;
			assert seconds >= 0 : seconds;
			assert timeAtStartOfMoveSeconds <= timeAtEndOfMoveSeconds : timeAtStartOfMoveSeconds + ", " + timeAtEndOfMoveSeconds;
			
			

			if(!player.isStunned()) //stunned players can't fire
			{
				float xPosWhenFired = player.getX();
				float yPosWhenFired = player.getY();
				xPosWhenFired += (float) (getGunLength() * Math.cos(gunAngleRelativeToPlayer + player.getAngle()));
				yPosWhenFired += (float) (getGunLength() * Math.sin(gunAngleRelativeToPlayer + player.getAngle()));
				fire(seconds, timeAtStartOfMoveSeconds, player, xPosWhenFired, yPosWhenFired, gunAngleRelativeToPlayer + player.getAngle(), player.getSpeedX(), player.getSpeedY());
				setLastTimeFiredSeconds(timeAtStartOfMoveSeconds);
				
				ammoInCurrentClip--;
				assert ammoInCurrentClip >= 0 : ammoInCurrentClip;
				if (ammoInCurrentClip <= 0) {
					this.reloadClip();//timeAtStartOfMoveSeconds);
				}
			
			}
			if(this.getTotalAmmo()<=0)// out of ammo
				player.newPersonalMessage(this.getName() + " is out of ammo.", timeAtStartOfMoveSeconds + seconds);
			
			timeUntilReloaded = this.getCurrentReloadSeconds();
			
				if (timeUntilReloaded > 0){
					if (timeUntilReloaded > seconds){
						timeUntilReloaded -= seconds;
						player.doMoveBetweenGunFires(seconds, timeAtStartOfMoveSeconds);
						seconds = 0;
						timeAtStartOfMoveSeconds += seconds;
						return;
					}else{
						player.doMoveBetweenGunFires(timeUntilReloaded, timeAtStartOfMoveSeconds);
						seconds -= timeUntilReloaded;
						timeAtStartOfMoveSeconds += timeUntilReloaded;
						timeUntilReloaded = 0;
				}
			}
			
		}
		if (reloadClip == true && timeUntilReloaded == 0){
			doClipReload();
		}
		player.doMoveBetweenGunFires(seconds, timeAtStartOfMoveSeconds);
	}
	public void fire(double secondsLeft, double timeAtStartOfMoveSeconds, Player player, float xPosWhenFired, float yPosWhenFired, float angle, 
			//, float mouseX, float mouseY,
			float playerSpeedX, float playerSpeedY) {
		
		
			Bullet bullet = createBullet(player, xPosWhenFired, yPosWhenFired, angle, player.getMouseTargetX(), player.getMouseTargetY(), timeAtStartOfMoveSeconds, playerSpeedX, playerSpeedY);
			// Move the bullet to where it should be at the end of this move. Note that each bullet in world.bullets has already had doMove called on it so there won't be any doubling up.
			bullet.doMove(secondsLeft, timeAtStartOfMoveSeconds);
			player.getWorld().getBullets().add(bullet);
	}

	public void rotateTurret(double seconds, double timeAtStartOfMoveSeconds) {
	// The below is commented out since without vehicles and just using soldiers, there's no use having the gun turn independently of the soldier.
		/*float oldGunAngleRelativeToPlayer = this.gunAngleRelativeToPlayer;
	if (player.isDead() == false) {
	//float targetGunAngle = (float) getAngle(mouseTargetX - getX(), mouseTargetY - getY());
	float targetGunAngle = (float) player.calcAngle(player.getMouseTargetX(), player.getMouseTargetY());
	float angleToTurn = (float) (targetGunAngle - oldGunAngleRelativeToPlayer - player.getAngle());
	// Here we make sure angleToTurn is between -Math.PI and +Math.PI so 
	// that it's easy to know which way the gun should turn.
	// The maximum that angleToTurn could be now is + or - 2 * 2*Math.PI.
	if (angleToTurn < -Math.PI) {
	angleToTurn += (float) (2 * Math.PI);
	if (angleToTurn < -Math.PI) {
	angleToTurn += (float) (2 * Math.PI);
	}
	}
	if (angleToTurn > Math.PI) {
	angleToTurn -= (float) (2 * Math.PI);
	if (angleToTurn > Math.PI) {
	angleToTurn -= (float) (2 * Math.PI);
	}
	}
	assert angleToTurn >= -Math.PI && angleToTurn <= Math.PI : angleToTurn;
	assert angleToTurn >= -Math.PI && angleToTurn <= Math.PI : angleToTurn;
	float maxGunAngleChange = (float) (maxGunRotationSpeed * seconds);
	if (angleToTurn > 0) {
	if (angleToTurn > maxGunAngleChange) {
	gunAngleRelativeToPlayer = oldGunAngleRelativeToPlayer + maxGunAngleChange;
	} else {
	gunAngleRelativeToPlayer = oldGunAngleRelativeToPlayer + angleToTurn;
	}
	} else {
	if (angleToTurn < -maxGunAngleChange) {
	gunAngleRelativeToPlayer = oldGunAngleRelativeToPlayer - maxGunAngleChange;
	} else {
	gunAngleRelativeToPlayer = oldGunAngleRelativeToPlayer + angleToTurn;
	}
	}
	if (gunAngleRelativeToPlayer < 0) {
	gunAngleRelativeToPlayer += (float) (2 * Math.PI);
	}
	if (gunAngleRelativeToPlayer >= 2 * Math.PI) {
	gunAngleRelativeToPlayer -= (float) (2 * Math.PI);
	}
	assert targetGunAngle >= 0 : targetGunAngle;
	assert gunAngleRelativeToPlayer >= 0 : gunAngleRelativeToPlayer;
	}*/
	}

	public void startFiring(double triggerPressSeconds) {
		firing = true;
	}

	public void stopFiring() {
		//shouldFire = false;
		firing = false;
	}

	public float getGunAngle() {
		return gunAngleRelativeToPlayer;
	}

	public void setGunAngle(float gunAngle) {
		this.gunAngleRelativeToPlayer = gunAngle;
	}

	public float getMaxGunRotationSpeed() {
		return maxGunRotationSpeed;
	}

	public void setMaxGunRotationSpeed(float maxGunRotationSpeed) {
		this.maxGunRotationSpeed = maxGunRotationSpeed;
	}

	public float getGunLength() {
		return gunLength;
	}

	public float getCurrentReloadSeconds() {
		if (reloadClip) {
			int ammoToPutIn = maxAmmoInClip - ammoInCurrentClip;
			if (ammoToPutIn > ammo){
				ammoToPutIn = ammo;
			}
			float proportionOfClipToReload = ((float)ammoToPutIn/maxAmmoInClip);
			assert proportionOfClipToReload > 0 && proportionOfClipToReload <= 1: proportionOfClipToReload+", note that proportionOfClipToReload should not be zero.";
			return getReloadClipSeconds()*proportionOfClipToReload;
		} else {
			return getReloadSeconds();
		}
	}

	public boolean isFiring() {
		return firing;
	}

	public double getLastTimeFiredSeconds() {
		return lastTimeFiredSeconds;
	}

	protected void setLastTimeFiredSeconds(double lastTimeFiredSeconds) {
		this.lastTimeFiredSeconds = lastTimeFiredSeconds;
	}
/*
	public double getTriggerPressSeconds() {
		return triggerPressSeconds;
	}

	public void setTriggerPressSeconds(double triggerPressSeconds) {
		this.triggerPressSeconds = triggerPressSeconds;
	}
*/
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public String getName() {
		return this.getClass().getSimpleName();
	}

	public void assignToPlayer(Player player, double assignTimeSeconds) {
		player.assignGun(this, assignTimeSeconds);
	}

	public void addAmmo(int extraAmmo) {
		assert ammoInCurrentClip >= 0 : ammoInCurrentClip;
		if (getTotalAmmo() <= 0) {
			ammo += extraAmmo;
			doClipReload();
		}else{
			ammo += extraAmmo;
		}
	}

	public void reloadClip(){
		// checks for clip reload, and sets a request, doesn't actually act
		if (ammo == 0){
			reloadClip = false;		
			return;
		}
		if (ammoInCurrentClip == maxAmmoInClip || reloadClip == true) {
			return;
		}
		assert ammo >= 0 : ammo;
		int ammoToPutIn = maxAmmoInClip - ammoInCurrentClip;
		if (ammoToPutIn > ammo){
			ammoToPutIn = ammo;
		}
		float proportionOfClipToReload = ((float)ammoToPutIn/maxAmmoInClip);
		assert proportionOfClipToReload > 0 && proportionOfClipToReload <= 1: proportionOfClipToReload+", note that proportionOfClipToReload should not be zero.";
		//System.out.println("proportionOfClipToReload == "+proportionOfClipToReload);
		this.timeUntilReloaded = getReloadClipSeconds()*proportionOfClipToReload;
		reloadClip = true;
		// then doClipReload is done in method doMoveAndBulletFire
	}
	protected void doClipReload(){
		if (ammoInCurrentClip == maxAmmoInClip) {
			return;
		} else if (ammo == 0) {
			return;
		} else {
			int ammoToPutIn = maxAmmoInClip - ammoInCurrentClip;
			if (ammoToPutIn > ammo) {
				ammoToPutIn = ammo;
			}
			assert ammoToPutIn > 0 : ammoToPutIn;
			ammo -= ammoToPutIn;
			ammoInCurrentClip += ammoToPutIn;
			assert ammo >= 0 : ammo;
			reloadClip = false;
			timeUntilReloaded = 0;
		}
	}

	public GameWorld getWorld() {
		return world;
	}

	public void setWorld(GameWorld world) {
		this.world = world;
	}

	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	public float getReloadSeconds() {
		if (player != null){
			return reloadSeconds*player.getReloadSpeedMultiplier();
		}
		return reloadSeconds;
	}

	public float getReloadClipSeconds() {
		if (player != null){
			return reloadClipSeconds*player.getReloadSpeedMultiplier();
		}
		return reloadClipSeconds;
	}
}
