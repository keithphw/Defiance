package sydneyengine.shooter.Bullet;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import sydneyengine.shooter.CustomColor;
import sydneyengine.shooter.KPolygon;
import sydneyengine.shooter.Obstacle;
import sydneyengine.shooter.Player;
import sydneyengine.shooter.ViewPane;
import sydneyengine.shooter.Gun.HomingGun;

public class HomingBullet extends Bullet {

	static float canNotHitOwnPlayerTimeSeconds = 1.5f;
	float radius;
	float length;
	float maxSpeed;
	float damage;
	float angle;
	float maxRotationSpeed = (float) Math.PI;
	float startX; float startY;

	protected float lifeTimeSeconds;
	Point2D.Double target;

	public HomingBullet() {
		super();
		player = null;
	}

	public HomingBullet(HomingGun gun, Player player, float newX, float newY, float angle, double spawnTimeSeconds, float xLaunchSpeed, float yLaunchSpeed) {
		super(player, newX, newY, angle, spawnTimeSeconds, xLaunchSpeed, yLaunchSpeed);
		assert Point2D.distance(player.getX(), player.getY(), newX, newY) < player.getR() : Point2D.distance(player.getX(), player.getY(), newX, newY);
		radius = 3.0f;
		length = 2*radius;
		damage = 25.0f;
		this.angle = angle;
		gun.getWorld().getRandom().setSeed(gun.getSeed());
		gun.setSeed(gun.getSeed()+3041);
		float randomSpeedIncrement = world.getRandom().nextFloat()*30;
		float startSpeed = 60 + randomSpeedIncrement;
		gun.getWorld().getRandom().setSeed(gun.getSeed());
		gun.setSeed(gun.getSeed()+1761);
		float randomRangeIncrement = world.getRandom().nextFloat()*200;
		float range = 600 + randomRangeIncrement;
		speedX = xLaunchSpeed + (float) Math.cos(angle) * startSpeed;
		speedY = yLaunchSpeed + (float) Math.sin(angle) * startSpeed;

		
		float launchSpeed = startSpeed;
		lifeTimeSeconds = range / launchSpeed + 5;
		
		startX = newX; startY = newY;
		
		this.x = newX + (float) Math.cos(angle) * length;
		this.y = newY + (float) Math.sin(angle) * length;
		backX = newX;
		backY = newY;
		oldBackX = backX;
		oldBackY = backY;
	}
	@Override
	public void doBulletMove(double seconds, double timeAtStartOfMoveSeconds) {
	
		super.doBulletMove(seconds, timeAtStartOfMoveSeconds);
		
		if(!dead && getClosestEnemy()!=null && Point2D.distance(x, y, startX, startY) > 40)
		{
			target = new Point2D.Double(getClosestEnemy().getX(), getClosestEnemy().getY());
			updateTurning(seconds);
			this.speedX = (float) (this.getSpeed() * Math.cos(angle));
			this.speedY = (float) (this.getSpeed() * Math.sin(angle));
		}

		//world.getParticleEmitters().add(new ParticleTrail(new Point2D.Float(x, y), CustomColor.PURPLE, 1, 2, false));
		
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
	public void updateTurning(double seconds) {
		
		float oldAngle = this.getAngle();
		if (!dead && getClosestEnemy()!=null) {
			//float targetGunAngle = (float) getAngle(mouseTargetX - getX(), mouseTargetY - getY());
			float targetAngle = calcAngle(getClosestEnemy().getX() - getX(), getClosestEnemy().getY() - getY());

			float angleToTurn = targetAngle - oldAngle;
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
	public Player getClosestEnemy(){
		// modified from the bot method: for certain cases: ie. the 2nd closest enemy is shootable but 1st isn't:
		// this will shoot 2nd closest
		// bot method will not shoot at all.
		Player closestPlayer = null;
		double closestDist = Double.MAX_VALUE;
		ArrayList<Player> isShootable = new ArrayList<Player>();
		for(Player p : world.getPlayers())
		{
			if (p.isDead() || (p.isInvisible() && p.getGun().isFiring() == false) || p.getTeam() == this.getPlayer().getTeam()){
				continue;
			}
			if(isPlayerShootable(player))
				isShootable.add(p);
		}
		for (Player p : isShootable){
			
			if (Point2D.distance(p.getX(), p.getY(), getX(), getY()) < closestDist){
				closestDist = Point2D.distance(p.getX(), p.getY(), getX(), getY());
				closestPlayer = p;
			}
		}
		return closestPlayer;
	}
	protected boolean isPlayerShootable(Player p){
		ArrayList<Obstacle> obstacles = getWorld().getObstacles();
		for (int i = 0; i < obstacles.size(); i++) {
			Obstacle obstacle = obstacles.get(i);
			KPolygon shape = obstacle.getShape();
//			if (Point2D.Float.distance(x, y, shape.getCentre().x, shape.getCentre().y) > shape.getCircularBound() + radius) {
//				continue;
//			}
			Point2D.Float[] points = shape.getPoints();
			for (int j = 0; j < points.length; j++) {
				int jPlus = (j + 1 == points.length ? 0 : j + 1);
				if (Line2D.linesIntersect(getX(), getY(), p.getX(), p.getY(), points[j].x, points[j].y, points[jPlus].x, points[jPlus].y)) {
					return false;
				}
			}
		}
		return true;
	}

	//transient BasicStroke stroke = new BasicStroke(3);
	@Override
	public void render(ViewPane viewPane) {
		Graphics2D g = viewPane.getBackImageGraphics2D();
		
		g.setColor(this.player.getTeam().getColor());
		g.draw(new Ellipse2D.Float(getX() - radius * 1.2f, getY() - radius * 1.2f, radius * 2f * 1.2f, radius * 2f * 1.2f));
				
		g.setColor(CustomColor.PURPLE);		
		g.fill(new Ellipse2D.Float(getX() - radius* .8f, getY() - radius* .8f, radius * 2f* .8f, radius * 2f* .8f));
	}

	@Override
	public float getDamage(){
		return damage;
	}

	@Override
	public double getLifeTimeSeconds(){
		return lifeTimeSeconds;
	}
	@Override
	public float getLength(){
		return length;
	}
	@Override
	public float getCanNotHitOwnPlayerTimeSeconds(){
		return canNotHitOwnPlayerTimeSeconds;
	}
	public float getAngle() {
		return this.angle;
	}
	public float getMaxRotationSpeed() {
		return maxRotationSpeed;
	}
	
	
}
