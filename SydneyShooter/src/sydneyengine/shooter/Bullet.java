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
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;

import math.geom2d.*;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;


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
			if(shape.centre==null) System.out.println("GODDAMN");
			if (Point2D.Float.distance(x, y, shape.getCentre().x, shape.getCentre().y) > shape.getCircularBound() + getLength() + distCovered + error) {
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
					float dist = (float) Point2D.Float.distance(oldBackX, oldBackY, testIntersection.x, testIntersection.y);
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
		// Note that world.players won't always contain this bullet.player since the player 
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
			if (Line2D.Float.linesIntersect(oldBackX, oldBackY, x, y, p.getOldX(), p.getOldY(), p.getX(), p.getY())) {
				// The below is not really the right distance to where the 
				// player was hit, but it is an OK approximation.
				float dist = (float) Point2D.Float.distance(oldBackX, oldBackY, p.getX(), p.getY());
				if (dist < distToClosestHitPlayer) {
					distToClosestHitPlayer = dist;
					touch = true;
					hitPlayer = p;
				}
			} else if (Line2D.ptSegDist(x, y, oldBackX, oldBackY, p.getX(), p.getY()) < p.getR()) {
				// The below is not really the right distance to where the 
				// player was hit, but it is an OK approximation.
				float dist = (float) Point2D.Float.distance(oldBackX, oldBackY, p.getX(), p.getY());
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
		String s= this.getClass().getSimpleName();
		Point2D.Float midpoint= new Point2D.Float((this.oldBackX+ this.getX())/2, (this.oldBackY+ this.getY())/2);

		
		if(s.equals("FlameBall")) 
		{
			// flamethrower napalm sticks to walls
			this.setSpeedX(0); this.setSpeedY(0);
			this.setX(midpoint.x); this.setY(midpoint.y);
		}		
		else
		{
			if(!s.equals("Stun Dart"))
			{
				//most bullets will crater walls (destructable terrain)
					
				float[] intersect_info= hitObstacle.getShape().closestIntersectionWithLine(oldBackX, oldBackY, x, y);
				
				Point2D.Float intersect= new Point2D.Float(intersect_info[0], intersect_info[1]);
				
				// length of bullet, determines size of crater
				float length= this.getLength()/2;
				
				// 1. create a hexagon around bullet collision area --> convert to Simple polygon 2d from javaGeom lib
				//-------- should probably be anti-clockwise, to match KPolygon
				math.geom2d.Point2D[] pts= new math.geom2d.Point2D[6];
				pts[0]= new math.geom2d.Point2D(intersect.x - length, intersect.y);
				pts[1]= new math.geom2d.Point2D(intersect.x - length/2, intersect.y - length);
				pts[2]= new math.geom2d.Point2D(intersect.x + length/2, intersect.y - length);
				pts[3]= new math.geom2d.Point2D(intersect.x + length, intersect.y);
				pts[4]= new math.geom2d.Point2D(intersect.x + length/2, intersect.y + length);
				pts[5]= new math.geom2d.Point2D(intersect.x - length/2, intersect.y + length);
				
				
									
				Polygon2D crater= new SimplePolygon2D();
				for(int i=0; i< pts.length; i++)
				{
					crater.addVertex(pts[i]);
				}
				
				
				// 2. get the obstacle that collided with bullet --> also convert to geometry
				Polygon2D rock =  new SimplePolygon2D();
	
				Point2D.Float[] orig= hitObstacle.getShape().points;
				for(int i=0; i< orig.length; i++)
				{
					rock.addVertex(new math.geom2d.Point2D(orig[i].x, orig[i].y));
				}	
				// 3. get the difference of the two geometries
				Polygon2D obstacle= Polygons2D.difference(rock, crater);
				// 5. update obstacle shape
				KPolygon update= new KPolygon();
	

				// String[] y = x.toArray(new String[0]);
				math.geom2d.Point2D[] vertices= obstacle.vertices().toArray(new math.geom2d.Point2D[obstacle.vertices().size()]);

				update.points= new Point2D.Float[vertices.length];
	
				for(int i=0; i<vertices.length; i++)
				{
					update.points[i] = new Point2D.Float((float)vertices[i].getX(), (float)vertices[i].getY());
				}
	
				//hitObstacle.setShape(update);	
				
			}			
			dead= true;
		}
		
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
	
	/*public float getSpeed(){
		return (float)Math.pow(Math.pow(speedX, 2) + Math.pow(speedY, 2), 0.5f);
	}*/

	public double getSpawnTimeSeconds() {
		return spawnTimeSeconds;
	}

	public void setSpawnTimeSeconds(double spawnTimeSeconds) {
		this.spawnTimeSeconds = spawnTimeSeconds;
	}

	public boolean isDead() {
		return dead;
	}

	public void writeSS(SSObjectOutputStream out) throws IOException {		// this is the method that you over-ride if you want custom serialization
		super.writeSS(out);
	}

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