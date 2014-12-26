/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sydneyengine.shooter.Bullet;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;

import sydneyengine.shooter.GameWorld;
import sydneyengine.shooter.KPolygon;
import sydneyengine.shooter.Obstacle;
import sydneyengine.shooter.Player;
import sydneyengine.shooter.ViewPane;
import sydneyengine.superserializable.SSAdapter;
import sydneyengine.superserializable.SSObjectInputStream;
import sydneyengine.superserializable.SSObjectOutputStream;


/**
 *
 * @author CommanderKeith
 */
public abstract class Bullet extends SSAdapter {

	// Note that world.players won't always contain this bullet.player since the player 
	// may have been removed from the world's list.
	protected Player player;
	protected GameWorld world = null;
	protected float x;
	protected float y;
	protected float speedX;
	protected float speedY;
	protected double spawnTimeSeconds;
	protected boolean dead;
	protected Player playerThatWasHit = null;
	float backX;
	float backY;
	float oldBackX;
	float oldBackY;
	
	public Bullet() {
	//System.err.println("getSSCode() == "+getSSCode()+", dead == "+dead);
	}

	public Bullet(Player player, float x, float y, float angle, double spawnTimeSeconds, float xLaunchSpeed, float yLaunchSpeed) {
		this.player = player;
		world = player.getWorld();
		this.spawnTimeSeconds = spawnTimeSeconds;
		dead = false;
	}
	
	public abstract void render(ViewPane viewPane);
	//public abstract void doMove(double seconds, double timeAtStartOfMoveSeconds);
	public abstract float getDamage();

	public abstract double getLifeTimeSeconds();
	public abstract float getLength();
	public abstract float getCanNotHitOwnPlayerTimeSeconds();
	
	public float getAccelX(){
		return 0;
	}
	public float getAccelY(){
		return 0;
	}
	
	public void doMove(double seconds, double timeAtStartOfMoveSeconds) {
		double lifeTimeSeconds = getLifeTimeSeconds();
		assert dead == false : "dead == " + dead;
		assert seconds >= 0 : seconds;
		assert spawnTimeSeconds <= timeAtStartOfMoveSeconds + seconds : "this bullet was spawned in the future! getSSCode() == " + getSSCode() + ", spawnTimeSeconds == " + (spawnTimeSeconds) + ", timeAtStartOfMoveSeconds + seconds == " + (timeAtStartOfMoveSeconds + seconds) + ", " + spawnTimeSeconds + ", " + timeAtStartOfMoveSeconds + ", " + seconds;
		assert spawnTimeSeconds + lifeTimeSeconds >= timeAtStartOfMoveSeconds : "getSSCode() == " + getSSCode() + ", spawnTimeSeconds + lifeTimeSeconds == " + (spawnTimeSeconds + lifeTimeSeconds) + ", timeAtStartOfMoveSeconds + seconds == " + (timeAtStartOfMoveSeconds + seconds) + ", " + spawnTimeSeconds + ", " + lifeTimeSeconds + ", " + timeAtStartOfMoveSeconds + ", " + seconds;
		if (spawnTimeSeconds + lifeTimeSeconds < timeAtStartOfMoveSeconds + seconds) {
			seconds = spawnTimeSeconds + lifeTimeSeconds - timeAtStartOfMoveSeconds;
			doBulletMove(seconds, timeAtStartOfMoveSeconds);
			dead = true;
		} else {
			doBulletMove(seconds, timeAtStartOfMoveSeconds);
			assert spawnTimeSeconds + lifeTimeSeconds >= timeAtStartOfMoveSeconds : "getSSCode() == " + getSSCode() + ", spawnTimeSeconds + lifeTimeSeconds == " + (spawnTimeSeconds + lifeTimeSeconds) + ", timeAtStartOfMoveSeconds + seconds == " + (timeAtStartOfMoveSeconds + seconds) + ", " + spawnTimeSeconds + ", " + lifeTimeSeconds + ", " + timeAtStartOfMoveSeconds + ", " + seconds;
		}
		oldBackX = backX;
		oldBackY = backY;
	}

	protected void doBulletMove(double seconds, double timeAtStartOfMoveSeconds) {
		assert Double.isNaN(x) == false;
		assert seconds >= 0 : seconds;
		float newSpeedX = (float) (speedX + getAccelX() * seconds);
		float newSpeedY = (float) (speedY + getAccelY() * seconds);
		float xIncrement = (float) (((newSpeedX + speedX) / 2f) * seconds);
		float yIncrement = (float) (((newSpeedY + speedY) / 2f) * seconds);
		x += xIncrement;
		y += yIncrement;
		backX += xIncrement;
		backY += yIncrement;

		speedX = newSpeedX;
		speedY = newSpeedY;

		
		//world.getParticleEmitters().add(new ParticleTrail(new Point2D.Float(x, y), this.player.getTeam().getColor(), 1, 2, false));
		
		
		boolean touch = false;

		ArrayList<Obstacle> obstacles = getWorld().getObstacles();
		Obstacle hitObstacle = null;
		Point2D.Double obstacleIntersection = new Point2D.Double();
		float distToClosestHitObstacle = 1000000f; //temporary storage
		Point2D.Double testIntersection = new Point2D.Double();
		//float distCovered = (float) Math.pow(Math.pow(xIncrement, 2) + Math.pow(yIncrement, 2), 0.5f);
		float distCovered = Math.abs(xIncrement) + Math.abs(yIncrement);
		for (int i = 0; i < obstacles.size(); i++) {
			Obstacle obstacle = obstacles.get(i);
			KPolygon shape = obstacle.getShape();
			float error = 0.1f;
			if(shape.centre==null) System.out.println("The obstacle is lacking a center.");
			if (Point2D.distance(x, y, shape.getCentre().x, shape.getCentre().y) > shape.getCircularBound() + getLength() + distCovered + error) {
				// if within range to collide
				continue;
			}
			Point2D.Float[] points = shape.getPoints();
			for (int j = 0; j < points.length; j++) {
				int jPlus = (j + 1 == points.length ? 0 : j + 1);
					boolean intersects = Player.getLineLineIntersection(oldBackX, oldBackY, x, y, points[j].x, points[j].y, points[jPlus].x, points[jPlus].y, testIntersection);
					if (intersects == false) {
						continue;
					}
					//assert Float.isNaN((float) testIntersection.x) == false && Float.isNaN((float) testIntersection.y) == false : testIntersection.x + ", " + testIntersection.y;
					//assert Float.isInfinite((float) testIntersection.x) == false && Float.isInfinite((float) testIntersection.y) == false : testIntersection.x + ", " + testIntersection.y;
					float dist = (float) Point2D.distance(oldBackX, oldBackY, testIntersection.x, testIntersection.y);
					if (dist < distToClosestHitObstacle) {
						// do this if intersects
						
						distToClosestHitObstacle = dist;
						touch = true;
						hitObstacle = obstacle;
						obstacleIntersection.setLocation(testIntersection);
					}
			}
		}

		ArrayList<Player> players = getWorld().getPlayers();
		// World.players won't always contain bullet.player since the player 
		// may have been removed from the world's list, therefore the following assert will 
		// not always be true.
		// assert players.get(players.indexOf(player)) == player : player+", "+players;
		Player hitPlayer = null;
		//Point2D.Double playerIntersection = null;
		float distToClosestHitPlayer = 1000000f; //temporary storage

		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			if (p == player && timeAtStartOfMoveSeconds < spawnTimeSeconds + getCanNotHitOwnPlayerTimeSeconds()) {
				continue;
			}
			if (p.isDead()){
				continue;
			}
			if (Line2D.linesIntersect(oldBackX, oldBackY, x, y, p.getOldX(), p.getOldY(), p.getX(), p.getY())) {
				// if player movement crossed bullet movement
				float dist = (float) Point2D.distance(oldBackX, oldBackY, p.getX(), p.getY());
				if (dist < distToClosestHitPlayer) {
					distToClosestHitPlayer = dist;
					touch = true;
					hitPlayer = p;
				}
			} else if (Line2D.ptSegDist(x, y, oldBackX, oldBackY, p.getX(), p.getY()) < p.getR()) {
				// if bullet inside player body circle
				float dist = (float) Point2D.distance(oldBackX, oldBackY, p.getX(), p.getY());
				if (dist < distToClosestHitPlayer) {
					distToClosestHitPlayer = dist;
					touch = true;
					hitPlayer = p;
				}
			}
		}

		if (touch) {
			if (hitPlayer == this.getPlayer()) {
				//System.out.println(this.getClass().getSimpleName() + ": this player was hit by his own bullet, getPlayer() == " + getPlayer() + ", " + timeAtStartOfMoveSeconds + ", " + spawnTimeSeconds + ", " + getCanNotHitOwnPlayerTimeSeconds());
			}
			if (hitPlayer != null && distToClosestHitPlayer < distToClosestHitObstacle) {
				hitPlayer(hitPlayer, timeAtStartOfMoveSeconds);
			} else if (hitObstacle != null) {
				hitObstacle(hitObstacle, timeAtStartOfMoveSeconds);
			}
		}
	}
	
	public void hitObstacle(Obstacle hitObstacle, double timeOfHit){

		assert (!dead);
			
		dead= true;
		
		tryRocketEffect(timeOfHit);
		
	}
	public void tryRocketEffect(double timeOfHit)
	{
		if(getClass().getSimpleName().equals("BallisticRocket"))
		{
			// "explosion" for rockets
			for(int n=0; n<180; n++)
			{
				world.getBullets().add(new RocketShrapnel(
					//getPlayer(),
					getPlayer(),
					oldBackX, 
					oldBackY,
					(float)(Math.PI*2*n/180),
					timeOfHit,
					0,
					0
					));
					
			}
		}
	}
	public void hitPlayer(Player hitPlayer, double timeOfHit){
		hitPlayer.takeDamage(this, timeOfHit);
		this.playerThatWasHit = hitPlayer;
				
		tryRocketEffect(timeOfHit);
		dead = true;
		
	}
	
	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getSpeedX() {
		return speedX;
	}
	public float getSpeedY() {
		return speedY;
	}

	public void setSpeedX(float speedX) {
		this.speedX = speedX;
	}
	
	public void setSpeedY(float speedY) {
		this.speedY = speedY;
	}
	
	public float getSpeed(){
		return (float)Math.sqrt((Math.pow(speedX, 2) + Math.pow(speedY, 2)));
	}

	public double getSpawnTimeSeconds() {
		return spawnTimeSeconds;
	}

	public void setSpawnTimeSeconds(double spawnTimeSeconds) {
		this.spawnTimeSeconds = spawnTimeSeconds;
	}

	public boolean isDead() {
		return dead;
	}

	@Override
	public void writeSS(SSObjectOutputStream out) throws IOException {		// this is the method that you over-ride if you want custom serialization
		super.writeSS(out);
	}

	@Override
	public void readSS(SSObjectInputStream in) throws java.io.IOException {	// this is the method that you over-ride if you want custom serialization
		super.readSS(in);
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
	public void setDead(boolean dead) {
		this.dead = dead;
	}
	public GameWorld getWorld(){
		return world;
	}
}