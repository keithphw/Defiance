package sydneyengine.shooter;

import java.awt.geom.Point2D;

public class Vector {
	public Vector() {
		
	}
	public Vector(double x, double y) {
		this.x= x; this.y= y;
	}
	double x; double y;
	public static Vector unitNormal(double x1, double y1, double x2, double y2, Point2D.Double facing)
	{
		double dy = y2 - y1;
		double dx = x2 - x1;
		double dist = Point2D.distance(x1, y1, x2, y2);
		
		Point2D.Double midpoint = new Point2D.Double((x1 + x2)/2, (y1 + y2)/2);
		
		// fix later
		Vector one = new Vector(-dy / dist, dx / dist);
		Vector two = new Vector(dy / dist, -dx / dist);
		
		// find which normal based on the side the test point is on
		if(Point2D.distance(one.x + midpoint.x, one.y + midpoint.y, facing.x, facing.y) <
			Point2D.distance(two.x + midpoint.x, two.y + midpoint.y, facing.x, facing.y))
			return one;
		else 
			return two;

	}
	public Vector subtract(Vector other)
	{
		return new Vector(this.x - other.x, this.y - other.y);
	}
	public Vector add(Vector other)
	{
		return new Vector(this.x + other.x, this.y + other.y);
	}
	public Vector multiplyScalar(double scalar)
	{
		return new Vector(this.x * scalar, this.y * scalar);
	}
	public static double dotProduct(Vector a, Vector b)
	{
		return a.x * b.x + a.y * b.y;
	}
	public double getX() {
		return this.x;
	}
	public double getY() {
		return this.y;
	}
	

}
