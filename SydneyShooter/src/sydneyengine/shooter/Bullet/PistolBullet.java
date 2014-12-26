/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sydneyengine.shooter.Bullet;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import sydneyengine.shooter.Obstacle;
import sydneyengine.shooter.Player;
import sydneyengine.shooter.Vector;
import sydneyengine.shooter.ViewPane;

/**
 *
 * @author CommanderKeith
 */
public class PistolBullet extends Bullet {

	static float canNotHitOwnPlayerTimeSeconds = 1.0f;
	protected float maxRange;
	float radius;
	float length;
	float maxSpeed;
	float damage;
	float angle;
	
	boolean hasBounced = false;


	public PistolBullet() {
		super();
		player = null;
	}

	public PistolBullet(Player player, float newX, float newY, float angle, double spawnTimeSeconds, float xLaunchSpeed, float yLaunchSpeed) {
		super(player, newX, newY, angle, spawnTimeSeconds, xLaunchSpeed, yLaunchSpeed);
		assert Point2D.distance(player.getX(), player.getY(), newX, newY) < player.getR() : Point2D.distance(player.getX(), player.getY(), newX, newY);
		radius = 2.0f;
		length = 2*radius;;
		this.angle = angle;
		maxSpeed = 700;
		damage = 7.0f;
		maxRange = 1500;
		speedX = xLaunchSpeed + (float) Math.cos(angle) * maxSpeed;
		speedY = yLaunchSpeed + (float) Math.sin(angle) * maxSpeed;

		this.x = newX + (float) Math.cos(angle) * length;
		this.y = newY + (float) Math.sin(angle) * length;
		backX = newX;
		backY = newY;
		oldBackX = backX;
		oldBackY = backY;
	}
	
	@Override
	public void hitObstacle(Obstacle hitObstacle, double timeOfHit){
		//pistol bullets should bounce of walls
		
		// find what edge it hit
		/*
		if(!hasBounced)
		{
			Point2D.Float[] points = hitObstacle.getShape().getPoints();
			for (int j = 0; j < points.length; j++) {
				int jPlus = (j + 1 == points.length ? 0 : j + 1);
				if (Line2D.Float.linesIntersect(oldBackX, oldBackY, x, y, points[j].x, points[j].y, points[jPlus].x, points[jPlus].y)) {
	
					updateAfterBounce(points[j].x, points[j].y, points[jPlus].x, points[jPlus].y);
					hasBounced= true;
				}
				
				if (Line2D.ptSegDist(points[j].x, points[j].y, points[jPlus].x, points[jPlus].y, getX(), getY()) < radius) {
					updateAfterBounce(points[j].x, points[j].y, points[jPlus].x, points[jPlus].y);
					hasBounced = true;
				}
				
				
			}
		}
		else 
			*/
			dead = true;
	}
	// where the intersection is the result
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
		//determinant
		static double det(double a, double b, double c, double d) {
			return a * d - b * c;
		}


		void updateAfterBounce(double x1, double y1, double x2, double y2)
		{
			// from http://stackoverflow.com/questions/573084/how-to-calculate-bounce-angle
			
			this.x= this.oldBackX;
			this.y= this.oldBackY;
			
			// from http://stackoverflow.com/questions/1243614/how-do-i-calculate-the-normal-vector-of-a-line-segment?rq=1
			Vector normal = Vector.unitNormal(x1, y1, x2, y2, new Point2D.Double(getX(), getY()));
			Vector speed= new Vector(speedX, speedY);
			
			double perpendicular_coef = Vector.dotProduct(normal, speed);			
			Vector perpendicular = normal.multiplyScalar(perpendicular_coef);
			
			// can use this to also slide along walls
			Vector parallel = speed.subtract(perpendicular);
			
			Vector result = parallel.subtract(perpendicular);
			
			speedX = (float)result.getX();
			speedY = (float)result.getY();
			
			
			System.out.println(speedX);
			System.out.println(speedY);
			System.out.println("SPEED:"+ getSpeed()+ "\n\n");
            if (speedY < 0)
                angle = (float) -Math.acos(speedX / getSpeed());
            else
                angle = (float) Math.acos(speedX / getSpeed());
            
            System.out.println("ANGLE: "+ angle);
		}
		

	@Override
	public void render(ViewPane viewPane) {
		Graphics2D g = viewPane.getBackImageGraphics2D();
		g.setColor(Color.gray);
		g.fill(new Ellipse2D.Float(backX - radius, backY - radius, radius * 2f, radius * 2f));
		g.setColor(Color.black);
		g.fill(new Ellipse2D.Float(getX() - radius, getY() - radius, radius * 2f, radius * 2f));
	}

	@Override
	public float getDamage(){
		return damage;
	}

	@Override
	public double getLifeTimeSeconds(){
		return maxRange / maxSpeed;
	}
	@Override
	public float getLength(){
		return length;
	}
	@Override
	public float getCanNotHitOwnPlayerTimeSeconds(){
		return canNotHitOwnPlayerTimeSeconds;
	}
	
	
}
