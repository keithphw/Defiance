 /*
 * Player.java
 *
 * Created on 12 November 2007, 18:19
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package sydneyengine.shooter;

import sydneyengine.*;
import sydneyengine.superserializable.*;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;

/**
 *
 * @author CommanderKeith
 */
public class Player extends SSAdapter{

	protected ArrayListSS<PersonalMessage> messages= new ArrayListSS<PersonalMessage>();
	protected GameWorld world;
	protected float x;
	protected float y;
	// oldX and oldY are used for bullet intersection tests
	protected float oldX;
	protected float oldY;
	protected float radius;
	// Note that for most players the viewPane will be null, only the player controlled by this VM will have a non-null viewPane.
	protected transient ViewPane viewPane = null;
	protected String name = "player";
	// mouseTargetX and Y are coordinates which are relative to the this.x and y.
	protected float mouseTargetX;
	protected float mouseTargetY;
	protected boolean left;
	protected boolean right;
	protected boolean up;
	protected boolean down;
	protected float angle;
	protected float maxSpeed;
	protected float speedX;
	protected float speedY;
	protected float speedMultiplier;
	protected double speedMultiplierStartTimeSeconds;
	protected double speedMultiplierLengthOfTimeSeconds;
	protected float reloadSpeedMultiplier;
	protected double reloadSpeedMultiplierStartTimeSeconds;
	protected double reloadSpeedMultiplierLengthOfTimeSeconds;
	protected Team team;
	protected double lastDeathTimeSeconds;
	float respawnSeconds;
	protected boolean dead;
	protected float hitPoints;
	protected float maxHitPoints;
	float maxFrictAccel;
	int gunIndex = 0;
	protected ArrayListSS<Gun> guns = new ArrayListSS<Gun>(10);
	protected int kills = 0;
	protected int deaths = 0;
	protected int flagCaptures = 0;
	protected int flagReturns = 0;
	protected CapturableFlag capturableFlag = null;
	
	protected boolean invisible = false;
	double invisibilityStartTime = 0;
	double invisibilityLengthTime = 0;
	
	protected boolean armored = false;
	double armoredStartTime = 0;
	double armoredLengthTime = 0;
	
	protected boolean stunned = false;
	double stunnedStartTime=0;
	double stunnedLengthTime=0;
	
	// A copy of the value of latency or lag to the server is stored here as well 
	// as in Nexus.latencyCalculator.latencyInfo. It's only stored here so that all 
	// clients can know all other client's latency, (since they don't have other clients' Nexus).
	// This variable is made transient so that when the value isn't reset to the 
	// tailWorld's old value it won't revert to that old latency. This object's writeSS and 
	// readSS methods are over-ridden to actually write the value of latencyToServerNanos.
	transient protected long latencyToServerNanos = -1;
	
	/** Creates a new instance of Player */
	public Player() {		
		// the constructor should not initiate stats - that should be done in respawn.  See respawn docs for why.
		lastDeathTimeSeconds = 0;//-Double.MAX_VALUE;		
	}

	public Point2D.Float getViewCentreOnMap() {
		return new Point2D.Float(x, y);
	}

	protected void resetGuns() {
		guns.clear();
	}

	/** IMPORTANT!! This method must reset all important player fields because when the world 
	goes back in time, the player's stats are not necessarily reset if the tail 
	world does not contain the player.  Because the PlayerJoinEvent keeps a 
	reference to this player, its current stats live on and when it is re-added
	to the world it will have all of its stats from the future!
	 */
	public void respawn() {
		//is this really random?
		r= new Random();
		resetGuns();
		addAndUseGun(new Pistol(world));
		
		angle = (float) 0;

		maxSpeed = 100;
		maxFrictAccel = -25;
		x = (float) (this.getTeam().getSpawnX());
		y = (float) (this.getTeam().getSpawnY());
		oldX = x;
		oldY = y;
		radius = 7;
		mouseTargetX = 0;
		mouseTargetY = 0;
		left = false;
		right = false;
		up = false;
		down = false;
		speedX = 0;
		speedY = 0;
		speedMultiplier = 1;
		speedMultiplierStartTimeSeconds = 0;
		speedMultiplierLengthOfTimeSeconds = 0;
		reloadSpeedMultiplier = 1;
		reloadSpeedMultiplierStartTimeSeconds = 0;
		reloadSpeedMultiplierLengthOfTimeSeconds = 0;
		// do not re-initiate lastDeathTimeSeconds since it should be left as whatever it was when this player died.
		dead = false;
		maxHitPoints = 100;
		hitPoints = maxHitPoints;
		
		respawnSeconds = 7.0f;
		invisible = false;
		invisibilityStartTime = 0;
		invisibilityLengthTime = 0;
		
		armored = false;
		armoredStartTime = 0;
		armoredLengthTime = 0;
		
		stunned = false;
		stunnedStartTime=0;
		stunnedLengthTime=0;
	}

	public void newPersonalMessage(String message, double timeStamp)
	{
		if(this.getViewPane()!=null)

		this.messages.add(new PersonalMessage(this, message, timeStamp));
	}
	public void doMove(double seconds, double timeAtStartOfMoveSeconds) {
		assert getWorld() != null;
		assert guns.size() > 0 && gunIndex < guns.size() && guns.get(gunIndex) != null : guns.size() + ", " + gunIndex + ", guns == " + guns;
		oldX = x;
		oldY = y;
		if (dead) {
			double timeUntilRespawn= (lastDeathTimeSeconds + respawnSeconds) - (timeAtStartOfMoveSeconds + seconds);
			//if (lastDeathTimeSeconds + respawnSeconds < timeAtStartOfMoveSeconds + seconds) {
			if(timeUntilRespawn <= 0)	
			{
				double spawnTimeSeconds = lastDeathTimeSeconds + respawnSeconds;
				double timeToMove = spawnTimeSeconds - timeAtStartOfMoveSeconds;
				getGun().doMoveAndBulletFire(timeToMove, timeAtStartOfMoveSeconds);
				seconds -= timeToMove;
				timeAtStartOfMoveSeconds += timeToMove;
				
				this.newPersonalMessage("Respawned.", world.getEventTimeStampNowSeconds());
				
				this.respawn();
			}			
			else if((int)timeUntilRespawn>0)
				this.newPersonalMessage("Respawning in "+ (int)timeUntilRespawn, world.getEventTimeStampNowSeconds());

		}
		getGun().doMoveAndBulletFire(seconds, timeAtStartOfMoveSeconds);
	}

	public void doMoveBetweenGunFires(double seconds, double timeAtStartOfMoveSeconds) {
		assert seconds >= 0 : seconds;
		if (this.speedMultiplier != 1){
			if (this.speedMultiplierStartTimeSeconds + this.speedMultiplierLengthOfTimeSeconds < timeAtStartOfMoveSeconds + seconds){
				double timeLeftToApplySpeedIncreaseFactor = speedMultiplierStartTimeSeconds + speedMultiplierLengthOfTimeSeconds - timeAtStartOfMoveSeconds;
				assert timeLeftToApplySpeedIncreaseFactor >= 0 : timeLeftToApplySpeedIncreaseFactor;	// delete this
				assert timeLeftToApplySpeedIncreaseFactor <= seconds : timeLeftToApplySpeedIncreaseFactor+", "+seconds;	// delete this
				// make sure that timeUntilSpeedDecreased is not slightly less than zero due to the small maths inaccuracies problem.
				timeLeftToApplySpeedIncreaseFactor = (timeLeftToApplySpeedIncreaseFactor < 0 ? 0 : timeLeftToApplySpeedIncreaseFactor);
				doMoveForReloadSpeedMultiplier(timeLeftToApplySpeedIncreaseFactor, timeAtStartOfMoveSeconds);
				seconds -= timeLeftToApplySpeedIncreaseFactor;
				timeAtStartOfMoveSeconds += timeLeftToApplySpeedIncreaseFactor;
				assert seconds >= 0 : seconds;	// delete this
				// make sure that timeUntilSpeedDecreased is not slightly less than zero due to the small maths inaccuracies problem.
				seconds = (seconds < 0 ? 0 : seconds);
				speedMultiplier = 1;
			}
		}
		doMoveForReloadSpeedMultiplier(seconds, timeAtStartOfMoveSeconds);
	}
	public void doMoveForReloadSpeedMultiplier(double seconds, double timeAtStartOfMoveSeconds) {
		assert seconds >= 0 : seconds;
		if (this.reloadSpeedMultiplier != 1){
			if (this.reloadSpeedMultiplierStartTimeSeconds + this.reloadSpeedMultiplierLengthOfTimeSeconds < timeAtStartOfMoveSeconds + seconds){
				double timeLeftToApplySpeedIncreaseFactor = reloadSpeedMultiplierStartTimeSeconds + reloadSpeedMultiplierLengthOfTimeSeconds - timeAtStartOfMoveSeconds;
				assert timeLeftToApplySpeedIncreaseFactor >= 0 : timeLeftToApplySpeedIncreaseFactor;	// delete this
				assert timeLeftToApplySpeedIncreaseFactor <= seconds : timeLeftToApplySpeedIncreaseFactor+", "+seconds;	// delete this
				// make sure that timeUntilSpeedDecreased is not slightly less than zero due to the small maths inaccuracies problem.
				timeLeftToApplySpeedIncreaseFactor = (timeLeftToApplySpeedIncreaseFactor < 0 ? 0 : timeLeftToApplySpeedIncreaseFactor);
				doMoveOfTurretAndBody(timeLeftToApplySpeedIncreaseFactor, timeAtStartOfMoveSeconds);
				seconds -= timeLeftToApplySpeedIncreaseFactor;
				timeAtStartOfMoveSeconds += timeLeftToApplySpeedIncreaseFactor;
				assert seconds >= 0 : seconds;	// delete this
				// make sure that timeUntilSpeedDecreased is not slightly less than zero due to the small maths inaccuracies problem.
				seconds = (seconds < 0 ? 0 : seconds);
				reloadSpeedMultiplier = 1;
			}
		}
		doMoveOfTurretAndBody(seconds, timeAtStartOfMoveSeconds);
	}
	public void PowerupCountdown(double timeAtStartOfMoveSeconds) {
		if (invisible){
			if (this.invisibilityStartTime + this.invisibilityLengthTime < timeAtStartOfMoveSeconds){
				invisible = false;
			}
		}
		if (armored){
			if(this.armoredStartTime + this.armoredLengthTime < timeAtStartOfMoveSeconds){
				armored= false;
			}
		}
		if (stunned){
			if(this.stunnedStartTime + this.stunnedLengthTime < timeAtStartOfMoveSeconds){
				stunned= false;
			}
		}
	}
	public void updateTurning(double seconds) {
	
		float oldAngle = this.getAngle();
		if (dead == false) {
			//float targetGunAngle = (float) getAngle(mouseTargetX - getX(), mouseTargetY - getY());
			float targetAngle = (float) calcAngle(mouseTargetX, mouseTargetY);

			float angleToTurn = (float) (targetAngle - oldAngle);
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
			assert angleToTurn >= -(float)Math.PI && angleToTurn <= (float)Math.PI : angleToTurn + ", " + Math.PI;
			float maxAngleChange = (float) (getMaxRotationSpeed() * seconds);
			if (angleToTurn > 0) {
				if (angleToTurn > maxAngleChange) {
					angle = oldAngle + maxAngleChange;
				} else {
					angle = oldAngle + angleToTurn;
				}
			} else {
				if (angleToTurn < -maxAngleChange) {
					angle = oldAngle - maxAngleChange;
				} else {
					angle = oldAngle + angleToTurn;
				}
			}
			if (getAngle() < 0) {
				angle += (float) (2 * Math.PI);
			}
			if (getAngle() >= 2 * Math.PI) {
				angle -= (float) (2 * Math.PI);
			}
			assert targetAngle >= 0 : targetAngle;
			assert getAngle() >= 0 : getAngle();
		}
	}
	
	public void doMoveOfTurretAndBody(double seconds, double timeAtStartOfMoveSeconds) {
		assert Double.isNaN(x) == false;
		assert seconds >= 0 : seconds;

		// extracted & refactored method, bot also uses this
		this.PowerupCountdown(timeAtStartOfMoveSeconds);
		
		this.updateTurning(seconds);
		
		getGun().rotateTurret(seconds, timeAtStartOfMoveSeconds);

		oldX = x;
		oldY = y;
		float xCoordToWorkOutAngle = 0;
		float yCoordToWorkOutAngle = 0;
		
		
		
		
		if (dead == false) {
			if (left == true) {
				xCoordToWorkOutAngle -= 1;
			}
			if (right == true) {
				xCoordToWorkOutAngle += 1;
			}
			if (up == true) {
				yCoordToWorkOutAngle -= 1;
			}
			if (down == true) {
				yCoordToWorkOutAngle += 1;
			}
		}
		speedX = 0f;
		speedY = 0f;
		if (xCoordToWorkOutAngle != 0 || yCoordToWorkOutAngle != 0) {
			float dirAngle = (float) calcAngle(xCoordToWorkOutAngle, yCoordToWorkOutAngle);//(float)Math.atan(yCoordToWorkOutAngle/xCoordToWorkOutAngle);
			dirAngle += Math.PI / 2; // 90 degrees is added on since we want an angle of zero to face up, not to the right (which it is without adding 90 degrees)
			
			//---- very wacky movement original based on facing angle --- but is NEEDED by BOTS
			//speedX = (float) Math.cos(dirAngle + getAngle()) * getCurrentSpeed();
			//speedY = (float) Math.sin(dirAngle + getAngle()) * getCurrentSpeed();
			
			
			//---- new added by KT 12/20/2014 ---- familiar moving system, but will mess up bots if they don't override it
			speedX = (float) Math.cos(dirAngle - Math.PI/2) * getCurrentSpeed();
			speedY = (float) Math.sin(dirAngle - Math.PI/2) * getCurrentSpeed();
		}
		
		checkCollisions(seconds);
		
	}
	public void checkCollisions(double seconds) {
		//s = t(u + v)/2
				x = (float) (oldX + seconds * speedX);
				y = (float) (oldY + seconds * speedY);

				boolean touch = false;
				ArrayList<Obstacle> obstacles = getWorld().getObstacles();
				Obstacle hitObstacle = null;
				for (int i = 0; i < obstacles.size(); i++) {
					Obstacle obstacle = obstacles.get(i);
					KPolygon shape = obstacle.getShape();
					if (Point2D.Float.distance(x, y, shape.getCentre().x, shape.getCentre().y) > shape.getCircularBound() + radius) {
						continue;
					}
					Point2D.Float[] points = shape.getPoints();
					for (int j = 0; j < points.length; j++) {
						int jPlus = (j + 1 == points.length ? 0 : j + 1);
						if (Line2D.Float.linesIntersect(oldX, oldY, x, y, points[j].x, points[j].y, points[jPlus].x, points[jPlus].y)) {
							touch = true;
							hitObstacle = obstacle;
						}
						if (Line2D.ptSegDist(points[j].x, points[j].y, points[jPlus].x, points[jPlus].y, getX(), getY()) < radius) {
							touch = true;
							hitObstacle = obstacle;
						}
					}
				}
				
				
				
				ArrayList<Water> waters = getWorld().getWaters();
				Water hitWater = null;
				for (int i = 0; i < waters.size(); i++) {
					Water water = waters.get(i);
					KPolygon shape = water.getShape();
					if (Point2D.Float.distance(x, y, shape.getCentre().x, shape.getCentre().y) > shape.getCircularBound() + radius) {
						continue;
					}
					Point2D.Float[] points = shape.getPoints();
					for (int j = 0; j < points.length; j++) {
						int jPlus = (j + 1 == points.length ? 0 : j + 1);
						if (Line2D.Float.linesIntersect(oldX, oldY, x, y, points[j].x, points[j].y, points[jPlus].x, points[jPlus].y)) {
							touch = true;
							hitWater = water;
						}
						if (Line2D.ptSegDist(points[j].x, points[j].y, points[jPlus].x, points[jPlus].y, getX(), getY()) < radius) {
							touch = true;
							hitWater = water;
						}
					}
				}
				
				
				if (touch) {
					x = oldX;
					y = oldY;
					speedX = 0;
					speedY = 0;
				}
	}

	public void takeDamage(Bullet b, double deathTimeSeconds) {
		
		if (dead == false && !armored) {
			setHitPoints(this.getHitPoints() - b.getDamage());
			if (getHitPoints() <= 0) {
				die(deathTimeSeconds, b);
			}
			// someone assisted in killing you, but not quite
			else b.getPlayer().addAssist(this);
		}
	}
	// must use arraylistSS and not arraylist
	protected ArrayListSS<Player> assistedToKill = new ArrayListSS<Player>();
	protected int assists;
	public ArrayListSS<Player> getAssistList()
	{
		return assistedToKill;
	}
	public int getAssists()
	{
		return assists;
	}
	public void addAssist(Player p)
	{
		// can only contribute to each person's death only ONCE!
		if(!assistedToKill.contains(p))
		{
			assistedToKill.add(p); assists++;
		}
	}
	public void die(double deathTimeSeconds, Bullet b) {
		dead = true;
		lastDeathTimeSeconds = deathTimeSeconds;
		for (Gun gun : guns) {
			if (gun == null){// || gun instance of Pistol) {
				continue;
			}
			gun.stopFiring();
			// when person dies, drops current gun
			world.addItem(new ItemHolder(world, gun, x, y, deathTimeSeconds));
		}
		resetGuns();
		Gun defaultGun = new Pistol(world);
		addAndUseGun(defaultGun);

		String bonus="";
		
		
		if (getCapturableFlag() != null) {
			getCapturableFlag().drop(deathTimeSeconds);
			bonus += " and drops the flag. ";
		}
		addDeath();
		b.getPlayer().addKill();
		
		if(this.getTeam()==b.getPlayer().team) 
		{
			if(this==b.getPlayer()) bonus+=" (Suicide!)";
			else bonus += " (Friendly Fire!)";
		}
		
		
		String assistance= "";
		
		ArrayList<String> temp= new ArrayList<String>();
		for(Player p: this.getWorld().getPlayers())
		{
			if(p.getAssistList().contains(this))
			{
				// you can't assist yourself
				if(!p.equals(b.getPlayer())) temp.add(p.getName());				
				//must clear assists that reference dead people
				p.getAssistList().remove(this);
			}
		}

		if(temp.size()>0)
		{
			assistance= ", assisted by ";	
			if(temp.size()==1) assistance+=temp.get(0);
			else
			{
				for(int k=0; k<temp.size()-1; k++)
				{
					assistance+=temp.get(k)+ ", ";
				}
				if(temp.size()>1)
					assistance+= "and "+ temp.get(temp.size()-1);
			}
		}
		
		//added by KT 12/20/2014
		this.getWorld().addChatText(new ChatText(this, " was "+ getCauseOfDeath(b)+ " by "+ b.getPlayer().getName() 
				+ bonus + assistance, false, this.getLastDeathTimeSeconds()));
		
		//kill streak announced
		if(b.getPlayer().getKillStreak()>=3)
		this.getWorld().addChatText(new ChatText(b.getPlayer(), "now has a kill streak of "+ b.getPlayer().getKillStreak(), false, this.getLastDeathTimeSeconds()));
		
	}
	Random r;
	public String getCauseOfDeath(Bullet b)
	{
		
		int n= (int)Math.floor(r.nextInt(90)/30); // 0,1,2, or 3
		String s= b.getClass().getSimpleName();
		String result= "killed by a mysterious cause.";
		if(s.equals("MachineGunBullet")) 
			{
			if(n==0) result= "mowed down";
			else if(n==1) result= "slower than the machine gun wielded";
			else if(n==2) result= "rained on";
			else result= "peppered to death";
			}
		else if(s.equals("StunDart")) 
			{
			if(n==0) result= "given an overdose of Tylenol";
			else if(n==1) result= "tranquilized";
			else if(n==2) result= "put to sleep";
				else result= "stunned by his sudden lifelessness";
			}
		else if(s.equals("PistolBullet")) 
			{
			if(n==0) result= "plonked with a plain pistol";
			else if(n==1) result= "pistoled (is that a word?)";
			else if(n==2) result= "given a heroic death by pistol";
			else result= "suffered death by pistol";
			}
		else if(s.equals("FlameBall")) 
			{
			if(n==0) result= "incinerated";
			else if(n==1) result= "burned to a crisp";
			else if(n==2) result= "surrounded with napalm";
			else result = "over-toasted";			
			}
		else if(s.equals("ShotGunPellet")) 
			{
			if(n==0) result= "fed led pellets";
			else if(n==1) result= "perfectly pelleted";
			else result= "thoroughly sprayed";
			}
		else if(s.equals("SniperRifleBullet")) 
			{
			if(n==0) result = "picked off";
			else if(n==1) result= "socked from two miles away";
			else if(n==2) result= "snazzily sniped";
			else result= "superbly sniped";
			}
		else if(s.equals("BallisticRocket") || s.equals("RocketShrapnel")) 
			{
			if(n==0) result= "unexplainably exploded";
			else if(n==1) result= "given a concussion due to a rocket";
			else if(n==2) result= "stuffed with rocket shrapnel";
			else result= "blown to pieces";
			}
		return result;
	}
	public float calcAngle(float xCoord, float yCoord) {
		float angle = 0;
		if (xCoord != 0) {
			angle = (float) Math.atan(yCoord / xCoord);
			if (xCoord < 0) {
				angle += (float) Math.PI;
			}
		} else if (yCoord > 0) {
			angle = (float) (Math.PI / 2);
		} else if (yCoord < 0) {
			angle = (float) (3 * Math.PI / 2);
		}
		if (angle < 0) {
			angle += (float) (2f * Math.PI);
		}
		assert angle >= 0 : "angle == " + angle + ", xCoord == " + xCoord + ", yCoord == " + yCoord;
		return angle;
	}

	//determinant
	static double det(double a, double b, double c, double d) {
		return a * d - b * c;
	}

	static boolean getLineLineIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, Point2D.Double intersection) {
		if (!Line2D.linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4)) {
			return false;
		}
		intersection.x = det(det(x1, y1, x2, y2), x1 - x2,
				det(x3, y3, x4, y4), x3 - x4) /
				det(x1 - x2, y1 - y2, x3 - x4, y3 - y4);
		intersection.y = det(det(x1, y1, x2, y2), y1 - y2,
				det(x3, y3, x4, y4), y3 - y4) /
				det(x1 - x2, y1 - y2, x3 - x4, y3 - y4);
		if (Double.isNaN(intersection.x) || Double.isNaN(intersection.y)){
			return false;
		}
		return true;
	}
	
	public boolean containsPoint(float px, float py) {
		if (Point2D.distance(x, y, px, py) < radius) {
			return true;
		}
		return false;
	}

	public void render(ViewPane viewPane) {
		//System.out.println(this.getClass().getSimpleName()+": getWorld() == "+getWorld()+", this == "+this);
		assert getWorld() != null : this;
		Graphics2D g = viewPane.getBackImageGraphics2D();
		if (isDead()) {
			Color deadColor = Color.GRAY;
			g.setColor((new Color(deadColor.getRed(), deadColor.getGreen(), deadColor.getBlue(), 150)));
		} else {
			g.setColor(getTeam().getColor());
		}
		if (invisible && !this.getGun().isFiring() && !isDead()){
			Color c = g.getColor();
			// a semi-transparent color:
			g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 40));
			if (viewPane.getPlayer().getTeam() != this.getTeam()){
				// to players of opposing team, you will appear as a miniscule pixel dot
				// to players of same team, you will be semi-transparent
				float invRadius = 1f;
				g.fill(new java.awt.geom.Ellipse2D.Float(getX() - invRadius, getY() - invRadius, invRadius * 2f, invRadius * 2f));
				return;
				
			}
		}
		if (armored && !isDead()){
			Color orig= g.getColor();
			g.setColor(Color.BLACK);
			float rad= radius+3;
			g.drawOval((int)(getX() - rad), (int)(getY() - rad), (int)rad * 2, (int)rad * 2);
			g.setColor(orig);
		}	

		g.fill(new java.awt.geom.Ellipse2D.Float(getX() - radius, getY() - radius, radius * 2f, radius * 2f));
		if (viewPane.isShowMapDescriptions() || containsPoint(viewPane.getWorldMouseX(), viewPane.getWorldMouseY())) {
			// draw the health bar
			float healthBarWidth = 30;
			float healthBarHeight = 5;
			float healthBarYCoord = getY() - radius - 3 - healthBarHeight;
			
			//==== fancy color healthbar
			if(getHitPoints()/getMaxHitPoints()>.6) 
				g.setColor(Color.GREEN);
			else if(getHitPoints()/getMaxHitPoints()>.25) g.setColor(Color.YELLOW);
			else g.setColor(Color.RED);
			
			
			float xWidthOfGreen = healthBarWidth * getHitPoints() / getMaxHitPoints();
			g.fill(new Rectangle2D.Float(getX() - healthBarWidth / 2, healthBarYCoord, xWidthOfGreen, healthBarHeight));
			if (this.getHitPoints() != this.getMaxHitPoints()) {
				float xWidthOfRed = healthBarWidth * (getMaxHitPoints() - getHitPoints()) / getMaxHitPoints();
				g.setColor(Color.DARK_GRAY);
				g.fill(new Rectangle2D.Float(getX() - healthBarWidth / 2 + xWidthOfGreen, healthBarYCoord, xWidthOfRed, healthBarHeight));
			}
			// draw the name
			FontMetrics fm = viewPane.getFontMetrics(g.getFont());
			float halfStringWidth = Math.round(fm.stringWidth(this.getName()) / 2f);
			//int stringHeight = fm.getHeight();
			int nameYCoord = (int) healthBarYCoord - 3;
			if (isDead()) {
				g.setColor(getTeam().getColor().darker().darker());
			} else {
				g.setColor(getTeam().getColor());
			}
			g.drawString(this.getName(), getX() - halfStringWidth, nameYCoord);

		}
		getGun().render(viewPane);


	/*g.setColor(Color.WHITE);
	Stroke oldStroke = g.getStroke();
	g.setStroke(new BasicStroke(1));
	g.draw(new Line2D.Float(getX(), getY(), (float) (getX() + 6 * Math.cos(getAngle())), (float) (getY() + 6 * Math.sin(getAngle()))));
	g.setStroke(oldStroke);*/
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team newTeam) {
		this.team = newTeam;
	}

	public float getOldX() {
		return oldX;
	}

	public void setOldX(float oldX) {
		this.oldX = oldX;
	}

	public float getOldY() {
		return oldY;
	}

	public void setOldY(float oldY) {
		this.oldY = oldY;
	}

	public double getLastDeathTimeSeconds() {
		return lastDeathTimeSeconds;
	}

	public void setLastDeathTimeSeconds(double lastDeathTimeSeconds) {
		this.lastDeathTimeSeconds = lastDeathTimeSeconds;
	}

	public boolean isDead() {
		return dead;
	}

	public void setDead(boolean dead) {
		this.dead = dead;
	}

	public float getHitPoints() {
		return hitPoints;
	}

	public void setHitPoints(float hitPoints) {
		this.hitPoints = hitPoints;
	}

	public GameWorld getWorld() {
		return world;
	}

	public void setWorld(GameWorld world) {
		this.world = world;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public void setPosition(float newX, float newY) {
		this.x = newX;
		this.y = newY;
		this.oldX = newX;
		this.oldY = newY;
	}

	public ViewPane getViewPane() {
		return viewPane;
	}

	public void setViewPane(ViewPane viewPane) {
		this.viewPane = viewPane;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getR() {
		return radius;
	}

	public String toString() {
		return "Player_" + getName() + "_" + getSSCode();
	}

	public boolean isLeft() {
		return left;
	}

	public void setLeft(boolean left) {
		this.left = left;
	}

	public boolean isRight() {
		return right;
	}

	public void setRight(boolean right) {
		this.right = right;
	}

	public boolean isUp() {
		return up;
	}

	public void setUp(boolean up) {
		this.up = up;
	}

	public boolean isDown() {
		return down;
	}

	public void setDown(boolean down) {
		this.down = down;
	}

	public //protected float targetGunAngle;
			float getMouseTargetX() {
		return mouseTargetX;
	}

	public void setMouseTargetX(float mouseTargetX) {
		this.mouseTargetX = mouseTargetX;
	}

	public float getMouseTargetY() {
		return mouseTargetY;
	}

	public void setMouseTargetY(float mouseTargetY) {
		this.mouseTargetY = mouseTargetY;
	}

	public void writeSS(SSObjectOutputStream out) throws IOException {		// this is the method that you over-ride if you want custom serialization
		super.writeSS(out);
		out.writeInt((int)this.getLatencyToServerNanos());
	}

	public void readSS(SSObjectInputStream in) throws java.io.IOException {	// this is the method that you over-ride if you want custom serialization
		super.readSS(in);
		this.setLatencyToServerNanos(in.readInt());
	}

	public ArrayListSS<Gun> getGuns() {
		return guns;
	}

	public Gun getGun() {
		return guns.get(gunIndex);
	}

	public void addGun(Gun gun) {
		assert gun != null;
		assert guns.contains(gun) == false;
		guns.add(gun);
		gun.setPlayer(this);
	}

	public void addAndUseGun(Gun gun) {
		assert gun != null;
		assert guns.contains(gun) == false;
		guns.add(gun);
		gun.setPlayer(this);
		gunIndex = guns.size()-1;
	}

	public boolean setGunFromIndexIfAvailable(int newGunNum, double startTimeSeconds) {
		for (int i = 0; i < guns.size(); i++){
			if (guns.get(i).getPlayerGunNum() == newGunNum){
				setGunToUse(i, startTimeSeconds);
				return true;
			}
		}
		return false;
	}
	protected void setGunToUse(int i, double startTimeSeconds){
		int oldGunIndex = gunIndex;
				if (getGuns().get(oldGunIndex).isFiring()){
					getGuns().get(oldGunIndex).stopFiring();
					getGuns().get(i).startFiring(startTimeSeconds);
				}
				gunIndex = i;
	}
	public void cycleGunsForwardBy(int num, double startTimeSeconds){
		int newGunIndex = this.gunIndex + num;
		while (newGunIndex >= guns.size()){
			newGunIndex -= guns.size();
		}
		while (newGunIndex < 0){
			newGunIndex += guns.size();
		}
		
		setGunToUse(newGunIndex, startTimeSeconds);
	}

	public float getSpeedX() {
		return speedX;
	}

	public void setSpeedX(float speedX) {
		this.speedX = speedX;
	}

	public float getSpeedY() {
		return speedY;
	}

	public void setSpeedY(float speedY) {
		this.speedY = speedY;
	}

	public float getAngle() {
		return angle;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public float getMaxRotationSpeed() {
		return getGun().getMaxGunRotationSpeed();
	}

	/*public void setMaxRotationSpeed(float maxRotationSpeed) {
	this.maxRotationSpeed = maxRotationSpeed;
	}*/
	public CapturableFlag getCapturableFlag() {
		return capturableFlag;
	}

	public void setCapturableFlag(CapturableFlag capturableFlag) {
		this.capturableFlag = capturableFlag;
	}

	public float getMaxHitPoints() {
		return maxHitPoints;
	}

	public void setMaxHitPoints(float maxHitPoints) {
		this.maxHitPoints = maxHitPoints;
	}

	public boolean acceptsItem(Item item) {
		return true;
	}
	public void assignGun(Gun newGun, double assignTimeSeconds){
		boolean addNewGun = true;
		for (int i = 0; i < guns.size(); i++){
			if (guns.get(i).getPlayerGunNum() == newGun.getPlayerGunNum()){
				// already have this gun, so take the ammo only
				guns.get(i).addAmmo(newGun.getTotalAmmo());
				addNewGun = false;
				this.newPersonalMessage("Acquired more "+newGun.getName() + " ammo.", assignTimeSeconds);
				
				break;
			}
		}
		if (addNewGun) {
			newGun.setPlayer(this);
			this.addGun(newGun);
			this.newPersonalMessage("Picked up "+newGun.getName(), assignTimeSeconds);
		}

		
	}
	/**
	 * Note that this method is basically unnecessary if this Controller thread is a ClientController, 
	 * because if there is a nexus connection problem then the whole game ends so there's no point removing this player.
	 * 
	 * However, if the Controller thread is a ServingController then this method will send an event to all 
	 * clients which will remove this player from the world.players list at the same time on all clients and this server.
	 * The client that was disconnected will not have the same timeStamped event since it 
	 * cannot communicate with the server and that client will quit anyway.
	 */
	boolean hasSentRemovePlayerEvent = false;
	transient Object hasSentRemovePlayerEventMutex = new Object();
	public void nexusClosing(){
		synchronized (hasSentRemovePlayerEventMutex){
			if (hasSentRemovePlayerEvent == true){
				System.out.println(this.getClass().getSimpleName()+": hasSentRemovePlayerEvent already == "+hasSentRemovePlayerEvent+", so returning.");
				return;
			}
			hasSentRemovePlayerEvent = true;
		}
		final RemovePlayerEvent removePlayerEvent = new RemovePlayerEvent(this);
		removePlayerEvent.setTimeStamp(getWorld().getEventTimeStampNowSeconds());
		removePlayerEvent.getEventWrapper().setId(getSSCode());// the id is just used for error-checking.
		removePlayerEvent.getEventWrapper().setCount(EventWrapper.COUNT_TO_IGNORE);
		final Player thisPlayer = this;
		
		if (javax.swing.SwingUtilities.isEventDispatchThread() == true){
			getWorld().getEventStore().addEventFromViewPane(removePlayerEvent.getEventWrapper());
		}else{
			try {
				javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						assert thisPlayer.getWorld() != null : thisPlayer;
						assert getWorld().getController() != null;
						getWorld().getEventStore().addEventFromViewPane(removePlayerEvent.getEventWrapper());
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (java.lang.reflect.InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	public float getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(float maxSpeed) {
		this.maxSpeed = maxSpeed;
	}
	
	public float getCurrentSpeed(){
		return maxSpeed*getSpeedMultiplier();
	}

	public int getKills() {
		return kills;
	}

	public int getDeaths() {
		return deaths;
	}

	public int getFlagCaptures() {
		return flagCaptures;
	}

	public int getFlagReturns() {
		return flagReturns;
	}
	public void addKill() {
		kills++;
		killStreak++;
	}
	protected int killStreak;
	public void addDeath() {
		deaths++;
		killStreak=0;
	}

	public void addFlagCapture() {
		flagCaptures++;
	}

	public void addFlagReturn() {
		flagReturns++;
	}

	public void setSpeedMultiplier(float speedMultiplier, double timeNowSeconds, double lengthOfTimeSeconds){
		this.speedMultiplier = speedMultiplier;
		speedMultiplierStartTimeSeconds = timeNowSeconds;
		speedMultiplierLengthOfTimeSeconds = lengthOfTimeSeconds;
	}
	public float getSpeedMultiplier() {
		return speedMultiplier;
	}
	public double getSpeedMultiplierLengthOfTimeSeconds() {
		return speedMultiplierLengthOfTimeSeconds;
	}
	public double getSpeedMultiplierStartTimeSeconds() {
		return speedMultiplierStartTimeSeconds;
	}
	
	public void setReloadSpeedMultiplier(float reloadSpeedMultiplier, double timeNowSeconds, double lengthOfTimeSeconds){
		this.reloadSpeedMultiplier = reloadSpeedMultiplier;
		reloadSpeedMultiplierStartTimeSeconds = timeNowSeconds;
		reloadSpeedMultiplierLengthOfTimeSeconds = lengthOfTimeSeconds;
	}
	public float getReloadSpeedMultiplier() {
		return reloadSpeedMultiplier;
	}
	public double getReloadSpeedMultiplierLengthOfTimeSeconds() {
		return reloadSpeedMultiplierLengthOfTimeSeconds;
	}
	public double getReloadSpeedMultiplierStartTimeSeconds() {
		return reloadSpeedMultiplierStartTimeSeconds;
	}
	public int getKillStreak()
	{
		return killStreak;
	}
	public long getLatencyToServerNanos() {
		return latencyToServerNanos;
	}

	public void setLatencyToServerNanos(long latencyToServerNanos) {
		this.latencyToServerNanos = latencyToServerNanos;
	}
	
	public boolean isInvisible() {
		return invisible;
	}
	public boolean isArmored() {
		return armored;
	}
	public boolean isStunned() {
		return stunned;
	}
	
	public void setStunned(double stunnedStartTime, double stunnedLengthTime)
	{
		stunned= true;
		this.stunnedStartTime = stunnedStartTime;
		this.stunnedLengthTime = stunnedLengthTime;
	}
	
	// =============== POWERUPS: can only get one powerup at one time
	public void setInvisible(double invisibilityStartTime, double invisibilityLengthTime){
		invisible = true; armored= false;
		this.invisibilityStartTime = invisibilityStartTime;
		this.invisibilityLengthTime = invisibilityLengthTime;
	}
	public void setArmored(double armoredStartTime, double armoredLengthTime){
		armored= true; invisible= false;
		this.armoredStartTime= armoredStartTime;
		this.armoredLengthTime= armoredLengthTime;
	}
	
	
}
