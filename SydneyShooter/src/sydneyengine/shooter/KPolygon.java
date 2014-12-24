// author: Keith Woodward, keithphw@hotmail.com

package sydneyengine.shooter;

import sydneyengine.superserializable.*;
import java.awt.geom.*;
import java.util.*;
import java.io.*;
import java.awt.*;

public final class KPolygon  extends SSAdapter implements Serializable, Shape{
	
	public Point2D.Float[] points;
	public Point2D.Float centre = null;
	public float area = -1;
	public float circularBound = -1;
	public transient static float minDistance = 0.0001f;	// a KPolygon's points can't be less than this distance away from each other.'

	
	// The following methods are needed to implement java.awt.geom.Shape.
	// Note that they are not implemented efficiently.
	public boolean intersects(double x, double y, double w, double h){
		Point2D.Float[] somePoints = {new Point2D.Float((float)x,(float)y),
		new Point2D.Float((float)(x+w),(float)y),
		new Point2D.Float((float)(x+w),(float)(y+h)),
		new Point2D.Float((float)(x),(float)(y+h))};
		KPolygon rect = new KPolygon(somePoints);
		return rect.intersects(this);
	}
	public boolean intersects(Rectangle2D r){
		return this.intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}
	public boolean contains(double x, double y, double w, double h){
		Point2D.Float[] somePoints = {new Point2D.Float((float)x,(float)y),
		new Point2D.Float((float)(x+w),(float)y),
		new Point2D.Float((float)(x+w),(float)(y+h)),
		new Point2D.Float((float)(x),(float)(y+h))};
		KPolygon rect = new KPolygon(somePoints);
		return rect.contains(this);
	}
	public boolean contains(Rectangle2D r){
		return this.contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
	}
	public PathIterator getPathIterator(AffineTransform at){
		return new KPolygonIterator(this, at);
	}
	public PathIterator getPathIterator(AffineTransform at, double flatness){
		return new KPolygonIterator(this, at);
	}
	public class KPolygonIterator implements PathIterator {
		int type = PathIterator.SEG_MOVETO;
		int pointNum = 0;
		KPolygon kPolygon;
		AffineTransform affine;
		
		double[] singlePointSetDouble = new double[2];
		
		KPolygonIterator(KPolygon kPolygon) {
			this(kPolygon, null);
		}
		
		KPolygonIterator(KPolygon kPolygon, AffineTransform at) {
			this.kPolygon = kPolygon;
			this.affine = at;
		}
		
		public int getWindingRule() {
			return GeneralPath.WIND_EVEN_ODD;
		}
		
		public boolean isDone() {
			return (pointNum > kPolygon.points.length);
			// done when pointNum == points.length since all points are given in
			// currentSegment() then SEG_CLOSE must be given.
		}
		
		public void next() {
			// curentSegment() is called first then next(), etc
			if (pointNum == kPolygon.points.length-1){
				type = PathIterator.SEG_CLOSE;
			} else{
				type = PathIterator.SEG_LINETO;
			}
			pointNum++;
		}
		
		public int currentSegment(float[] coords){
			if (type != PathIterator.SEG_CLOSE){
				if (affine != null){
					float[] singlePointSetFloat = new float[2];
					singlePointSetFloat[0] = kPolygon.getPoint(pointNum).x;
					singlePointSetFloat[1] = kPolygon.getPoint(pointNum).y;
					affine.transform(singlePointSetFloat, 0, coords, 0, 1);
				} else{
					coords[0] = kPolygon.getPoint(pointNum).x;
					coords[1] = kPolygon.getPoint(pointNum).y;
				}
			}
			return type;
		}
		
		public int currentSegment(double[] coords){
			if (type != PathIterator.SEG_CLOSE){
				if (affine != null){
					singlePointSetDouble[0] = kPolygon.getPoint(pointNum).x;
					singlePointSetDouble[1] = kPolygon.getPoint(pointNum).y;
					affine.transform(singlePointSetDouble, 0, coords, 0, 1);
				} else{
					coords[0] = kPolygon.getPoint(pointNum).x;
					coords[1] = kPolygon.getPoint(pointNum).y;
				}
			}
			return type;
		}
	}
	
	/***************************************************************************************/
	// the following is needed to implement Serializable & SSObject/SSAdapter
	public KPolygon(){
		points = null;
	}
	
	/***************************************************************************************/
	
// a KPolygon can have any shape as long as it has a single closed path,
// and its sides do not intersect.
	public KPolygon(KPolygon kPolygon) {
		points = new Point2D.Float[kPolygon.points.length];
		for (int i = 0; i < points.length; i++) {
			points[i] = new Point2D.Float(kPolygon.points[i].x, kPolygon.points[i].y);
		}
		centre = new Point2D.Float(kPolygon.centre.x, kPolygon.centre.y);
		area = kPolygon.area;
		circularBound = kPolygon.circularBound;
	}
	
	/***************************************************************************************/
	
	public KPolygon(Point2D.Float[] originalPoints) {
		int counter = 0;
		for (int i = 0; i < originalPoints.length-1; i++) {
			if (originalPoints[i].distance(originalPoints[i+1]) < minDistance) {
				System.out.println("one is smaller than minDist.");
				originalPoints[i] = null;
				counter++;
			}
		}
		for (int i = 0; i < originalPoints.length-1; i++) {
			if (originalPoints[i] == null) {
				continue;
			}
			if (originalPoints[originalPoints.length-1].distance(originalPoints[i]) < minDistance) {
				System.out.println("one is smaller than minDist.");
				originalPoints[i] = null;
				counter++;
			}
		}
		Point2D.Float[] newPoints = new Point2D.Float[originalPoints.length - counter];
		counter = 0;
		for (int i = 0; i < originalPoints.length; i++) {
			if (originalPoints[i] != null) {
				newPoints[counter] = originalPoints[i];
				counter++;
			}
		}
		points = newPoints;
		if (points.length >= 3) {
			//ensureAntiClockwisePath();
			area = findCentreAndArea();		// to initialize centre, and ensureAntiClockwisePath;
			circularBound = findCircularBound();
			//revalidate();
			
		} else {
			centre = null;
		}
	}
	
	/***************************************************************************************/
	
	public boolean isValid() {
		Line2D.Float currentLine;
		int a;
		int b;
		int c;
		int y;
		for (int i = 0; i < points.length; i++) {
			a = (i + 1 > points.length-1 ? 0 : i+1);
			currentLine = new Line2D.Float(points[i], points[a]);
			b = (a + 1 > points.length-1 ? 0 : a+1);
			a = (i - 1 < 0 ? points.length-1 : i-1);
			for (int j = 0; j < points.length; j++) {
				y = (j + 1 > points.length-1 ? 0 : j+1);
				if (j != a && j != i && j != b && y != a &&
					currentLine.intersectsLine(points[j].x, points[j].y, points[y].x, points[y].y)) {
					return false;
				}
			}
		}
		return true;
	}
	
	/***************************************************************************************/
	
	public Rectangle2D.Float getBounds2D()	// returns upside down rectangle (reversed y-axis), (corner is in bottom left)
	{
		Point2D.Float[] bounds = new Point2D.Float[2];
		bounds[0] = new Point2D.Float(points[0].x, points[0].y);
		bounds[1] = new Point2D.Float(points[0].x, points[0].y);
		for (int i = 0; i < points.length; i++) {
			if (points[i].x < bounds[0].x) {
				bounds[0].x = points[i].x;
			} else if (points[i].x > bounds[1].x) {
				bounds[1].x = points[i].x;
			}
			if (points[i].y < bounds[0].y) {
				bounds[0].y = points[i].y;
			} else if (points[i].y > bounds[1].y) {
				bounds[1].y = points[i].y;
			}
		}
		return new Rectangle2D.Float(bounds[0].x, bounds[0].y,
			bounds[1].x - bounds[0].x,
			bounds[1].y - bounds[0].y);
	}
	
	public Rectangle getBounds(){
		Point[] bounds = new Point[2];
		bounds[0] = new Point(Math.round(points[0].x), Math.round(points[0].y));
		bounds[1] = new Point(Math.round(points[0].x), Math.round(points[0].y));
		for (int i = 0; i < points.length; i++){
			if (points[i].x < bounds[0].x){
				bounds[0].x = Math.round(points[i].x);
			} else if (points[i].x > bounds[1].x){
				bounds[1].x = Math.round(points[i].x);
			}
			if (points[i].y < bounds[0].y){
				bounds[0].y = Math.round(points[i].y);
			} else if (points[i].y > bounds[1].y){
				bounds[1].y = Math.round(points[i].y);
			}
		}
		return new Rectangle(bounds[0].x, bounds[0].y,bounds[1].x - bounds[0].x,bounds[1].y - bounds[0].y);
	}
	
	/***************************************************************************************/
	
	public static boolean boundsIntersect(Rectangle2D.Float rect1, Rectangle2D.Float rect2) {
		return ((rect2.x + rect2.width > rect1.x &&
			rect2.y + rect2.height > rect1.y &&
			rect2.x < rect1.x + rect1.width &&
			rect2.y < rect1.y + rect1.height));
		   /*||
		  (rect1.x + rect1.width > rect2.x &&
		   rect1.y + rect1.height > rect2.y &&
		   rect1.x < rect2.x + rect2.width &&
		   rect1.y < rect2.y + rect2.height));
			*/
	}
	
	/***************************************************************************************/
	
	public boolean boundsIntersect(KPolygon foreign){
		if (centre.distance(foreign.centre) > getCircularBound() + foreign.getCircularBound()){
			return false;
		}
		return true;
	}
	
	public boolean contains(Point2D.Float point){
		return contains(point.x, point.y);
	}
	public boolean contains(float x, float y){
		//construct an interval starting at x,y and pointing away from this.centre, & give it a length of circularBound
		float angle = findAngle(this.getCentre().x, this.getCentre().y, x, y);
		
		float endX = x + this.getCircularBound()*(float)Math.cos(angle);
		float endY = y + this.getCircularBound()*(float)Math.sin(angle);
		int crossings = 0;
		for (int i = 0; i < this.getPoints().length; i++){
			int nextI = (i + 1 == this.getPoints().length ? 0 : i+1);
			if (linesIntersect(x, y, endX, endY, this.getPoint(i).x, this.getPoint(i).y, this.getPoint(nextI).x, this.getPoint(nextI).y)){
				crossings++;
			}
		}
		if (crossings%2 == 0){
			return false;
		}
		return true;
	}
	public boolean contains(double x, double y){
		//int cross = pointCrossings(x, y);	//Curve.crossingsForPath(getPathIterator(null), x, y);
		//return ((cross & 1) != 0);
		return contains((float)x, (float)y);//getGeneralPath().contains(x, y);
	}
	public boolean contains(Point2D point){
		return contains(point.getX(), point.getY());
	}
	
	/***************************************************************************************/
	
	public boolean contains(KPolygon foreign) {
  /*GeneralPath generalPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
  generalPath.moveTo(points[0].x, points[0].y);
  for (int i = 1; i < points.length; i++)
  {
   generalPath.lineTo(points[i].x, points[i].y);
  }
  generalPath.closePath();*/
		for (int i = 0 ; i < foreign.points.length; i++){
			if (contains(foreign.points[i])){
				return true;
			}
		}
		return false;
	}
	
	
	/***************************************************************************************/
	
	public boolean perimeterIntersects(KPolygon foreign){
		if (centre.distance(foreign.centre) > circularBound + foreign.circularBound){
			return false;
		}
		for (int i = 0; i < points.length; i++){
			int nextI = (i+1 >= points.length ? 0 : i+1);
			for (int j = 0; j < foreign.points.length; j++){
				int nextJ = (j+1 >= foreign.points.length ? 0 : j+1);
				if (Line2D.Float.linesIntersect(points[i].x, points[i].y, points[nextI].x, points[nextI].y, foreign.points[j].x, foreign.points[j].y, foreign.points[nextJ].x, foreign.points[nextJ].y)){
					return true;
				}
			}
		}
		return false;
	}
	
	/***************************************************************************************/
	
	public boolean intersects(KPolygon foreign){
		if (perimeterIntersects(foreign)){
			return true;
		}
		if (contains(foreign.getPoint(0)) || foreign.contains(getPoint(0))){
			return true;
		}
		return false;
	}
	/***************************************************************************************/
	
	public static boolean linesIntersect(float X1, float Y1, float X2, float Y2, float X3, float Y3, float X4, float Y4){
		return ((relativeCCW(X1, Y1, X2, Y2, X3, Y3)*relativeCCW(X1, Y1, X2, Y2, X4, Y4) <= 0)
		&& (relativeCCW(X3, Y3, X4, Y4, X1, Y1)*relativeCCW(X3, Y3, X4, Y4, X2, Y2) <= 0));
	}
	// tells you which side P is relative to the line. 1 is anti-clockwise??, 1 is clockwise??, 0 (rare) is on the line.
	public static int relativeCCW(float X1, float Y1, float X2, float Y2, float PX, float PY){
		X2 -= X1;
		Y2 -= Y1;
		PX -= X1;
		PY -= Y1;
		float ccw = PX * Y2 - PY * X2;
		if (ccw == 0.0f){
			ccw = PX * X2 + PY * Y2;
			if (ccw > 0.0){
				PX -= X2;
				PY -= Y2;
				ccw = PX * X2 + PY * Y2;
				if (ccw < 0.0f){
					ccw = 0.0f;
				}
			}
		}
		return (ccw < 0.0) ? -1 : ((ccw > 0.0) ? 1 : 0);
	}
	
	/***************************************************************************************/
	
	public boolean intersectsLine(float x1, float y1, float x2, float y2){
		if (Point2D.Float.distance(x2,y2,centre.x,centre.y) > circularBound){
			return false;
		}
		for (int i = 0; i < points.length-1; i++){
			if (linesIntersect(x1,y1,x2,y2,points[i].x,points[i].y,points[i+1].x,points[i+1].y)){
				return true;
			}
		}
		if (linesIntersect(x1,y1,x2,y2,points[points.length-1].x,points[points.length-1].y,points[0].x,points[0].y)){
			return true;
		}
		return false;
	}
	public boolean intersectsLine(Point2D.Float p1, Point2D.Float p2){
		return intersectsLine(p1.x, p1.y, p2.x, p2.y);
	}
	public boolean intersectsLine(Line2D.Float line){
		return intersectsLine(line.x1, line.y1, line.x2, line.y2);
	}
	
	/***************************************************************************************/
	
// gives point of intersection with line specified, where intersectoin point returned is the one closest to (x1, y1), so the leading point is (x2, y2)
// null is returned if there is no intersection.
// if there is, float[] returned has length 4,
// float[0] is x coord, float[1] is y coord and
// float[2] is the closest side of the KPolygon that the line intersects with.
// float[3] is the distance the impact point to x1, y1
	public float[] closestIntersectionWithLine(float x1, float y1, float x2, float y2){
		//if (Line2D.Float.ptLineDist(x2,y2,x1,y1,centre.x,centre.y) > circularBound){
	/*if (Point2D.Float.distance(x1,y1,centre.x,centre.y) > circularBound){
		return null;
	}*/
		Point2D.Float closestIntersectionPoint = null;
		int sideThatHasClosestIntersection = -1;
		float closestIntersectionDistance = Float.MAX_VALUE;
		int nextI;
		for (int i = 0; i < points.length; i++){
			nextI = (i+1==points.length?0:i+1);
			if (linesIntersect(x1,y1,x2,y2,points[i].x,points[i].y,points[nextI].x,points[nextI].y)){
				Point2D.Float currentIntersectionPoint = getIntersection(new Line2D.Float(x1,y1,x2,y2), new Line2D.Float(points[i].x,points[i].y,points[nextI].x,points[nextI].y));
				if (currentIntersectionPoint == null){
					continue;
				}
				float currentIntersectionDistance = (float)currentIntersectionPoint.distance(x1, y1);
				if (currentIntersectionDistance < closestIntersectionDistance){
					closestIntersectionPoint = currentIntersectionPoint;
					closestIntersectionDistance = currentIntersectionDistance;
					sideThatHasClosestIntersection = i;
				}
			}
		}
		if (sideThatHasClosestIntersection == -1){
			return null;
		}
		float[] coordsAndSide = new float[4];
		coordsAndSide[0] = closestIntersectionPoint.x;
		coordsAndSide[1] = closestIntersectionPoint.y;
		coordsAndSide[2] = sideThatHasClosestIntersection;
		coordsAndSide[3] = closestIntersectionDistance;
		return coordsAndSide;
	}
	public float[] closestIntersectionWithLine(Point2D.Float p1, Point2D.Float p2){
		return closestIntersectionWithLine(p1.x, p1.y, p2.x, p2.y);
	}
	public float[] closestIntersectionWithLine(Line2D.Float line){
		return closestIntersectionWithLine(line.x1, line.y1, line.x2, line.y2);
	}
	/***************************************************************************************/
	
	
	public float findAngle(int i, Point2D.Float[] decreasingPoints) {
		// this only works where the polygon has an anti-clockwise path.
		// returns angle in radians, not degrees.
		
		float currentAngle;
		float nextPointAngle;
		float prevPointAngle;
		
		prevPointAngle = findAngle(i, (i - 1 < 0 ? decreasingPoints.length-1 : i-1), decreasingPoints);
		nextPointAngle = findAngle(i, (i + 1 > decreasingPoints.length-1 ? 0 : i+1), decreasingPoints);
		
		currentAngle = prevPointAngle - nextPointAngle;
		if (currentAngle < 0) {
			currentAngle += (float)(2*Math.PI);
		}
		return currentAngle;
	}
	
	/***************************************************************************************/
	
	public float findAngle(int a, int i, int b, Point2D.Float[] decreasingPoints) // a is prev, b is next
	{
		// this only works where the polygon has an anti-clockwise path.
		// returns angle in radians, not degrees.
		
		float currentAngle;
		float nextPointAngle;
		float prevPointAngle;
		
		prevPointAngle = findAngle(i, a, decreasingPoints);
		nextPointAngle = findAngle(i, b, decreasingPoints);
		
		currentAngle = prevPointAngle - nextPointAngle;
		if (currentAngle < 0) {
			currentAngle += (float)(2*Math.PI);
		}
		return currentAngle;
	}
	
	/***************************************************************************************/
	
	public float findAngle(int start, int dest, Point2D.Float[] decreasingPoints) {
		// returns angle that dest is relative to start, measured anti-clockwise from the x-axis
		// returns angle in radians, not degrees.
		float angle;
		float x;
		float y;
		
		x = decreasingPoints[dest].x - decreasingPoints[start].x;
		y = decreasingPoints[dest].y - decreasingPoints[start].y;
		
		if (x == 0.0f) {
			return (y > 0 ? (float)(Math.PI/2) : (float)(Math.PI*3/2));
		}
		
		angle = (float)(Math.atan(y/x) + (x < 0 ? Math.PI : 0));
		
		if (angle < 0) {
			angle += (float)2*Math.PI;
		}
		return angle;
	}
	
	/***************************************************************************************/
	
	public static float findAngle(Point2D.Float p1, Point2D.Float p2)	// p1 == start, p2 == dest
	{
		return findAngle(p1.x, p1.y, p2.x, p2.y);
  /*// returns angle that dest is relative to start, measured anti-clockwise from the x-axis
  // returns angle in radians, not degrees.
  float angle;
  float x = p2.x - p1.x;
  float y = p2.y - p1.y;
   
  if (x == 0.0f)
  {
   return (y > 0 ? (float)(Math.PI/2) : (float)(Math.PI*3/2));
  }
   
  angle = (float)(Math.atan(y/x) + (x < 0 ? Math.PI : 0));
   
  if (angle < 0)
  {
   angle += (float)2*Math.PI;
  }
  return angle;*/
	}
	
	/***************************************************************************************/
	
	public static float findAngle(float x1, float y1, float x2, float y2){//Point2D.Float p1, Point2D.Float p2)	// p1 == start, p2 == dest
		// returns angle that dest is relative to start, measured anti-clockwise from the x-axis
		// returns angle in radians, not degrees.
		float angle;
		float x = x2 - x1;
		float y = y2 - y1;
		
		if (x == 0.0f) {
			return (y > 0 ? (float)(Math.PI/2) : (float)(Math.PI*3/2));
		}
		
		angle = (float)(Math.atan(y/x) + (x < 0 ? Math.PI : 0));
		
		if (angle < 0) {
			angle += (float)2*Math.PI;
		}
		return angle;
	}
	
	/***************************************************************************************/
	// similar to relativeCCW, maybe should use that if only want to know what side of the line a point is on since its more efficient.
	public static float findAngleRelativeToLine(float subjectPointX, float subjectPointY, float x1, float y1, float x2, float y2){
		// gives angle subjectPoint, p1, p2, or the angle that subjectPoint would be if p1, p2 was the horizontal.
		float relativeAngle = findAngle(x1, y1, subjectPointX, subjectPointY) - findAngle(x1, y1, x2, y2);
		if (relativeAngle < 0){
			relativeAngle += (float)(Math.PI*2);
		}
		return relativeAngle;
	}
	/***************************************************************************************/
	// gives angle subjectPoint, p1, p2, or the angle that subjectPoint would be if p1, p2 was the horizontal.
	public static float findAngleRelativeToLine(Point2D.Float subjectPoint, Point2D.Float p1, Point2D.Float p2){
		return findAngleRelativeToLine(subjectPoint.x, subjectPoint.y, p1.x, p1.y, p2.x, p2.y);
	}
	
	/***************************************************************************************/
	// gives angle subjectPoint, p1, p2, or the angle that subjectPoint would be if p1, p2 was the horizontal.
	// it only returns an angle between Math.PI and -Math.PI.
	public static float findSignedAngleRelativeToLine(float sx, float sy, float p1x, float p1y, float p2x, float p2y){
		float relativeAngle = findAngleRelativeToLine(sx, sy, p1x, p1y, p2x, p2y);
		if (relativeAngle > Math.PI){
			relativeAngle = (float)(-1*(2*Math.PI - relativeAngle));
		}
		return relativeAngle;
	}
	public static float findSignedAngleRelativeToLine(Point2D.Float subjectPoint, Point2D.Float p1, Point2D.Float p2){
		return findSignedAngleRelativeToLine(subjectPoint.x, subjectPoint.y, p1.x, p1.y, p2.x, p2.y);
	/*float relativeAngle = findAngleRelativeToLine(subjectPoint, p1, p2);
	if (relativeAngle > Math.PI){
		relativeAngle = (float)(-1*(2*Math.PI - relativeAngle));
	}
	return relativeAngle;*/
	}
	
	/***************************************************************************************/
	
	public void ensureAntiClockwisePath() {
		// to establish whether or not the polygon path is anticlockwise or not.
		// proper angle sum
		float properAngleSum = (float)((points.length - 2)*Math.PI);
		
		// angle sum assuming the points are listed in anti-clockwise direction
		float angleSum = 0;
		Point2D.Float currentPoint;
		
		for (int i = 0; i < points.length; i++) {
			angleSum += findAngle(i, points);
		}
		
		// reverses the order of the polygon so that the points go anti-clockwise
		// if they weren't doing that already.
		
		// System.out.println("properAngleSum = " + properAngleSum);
		// System.out.println("angleSum = " + angleSum);
		
		if (!(angleSum > (properAngleSum - 0.001f) && angleSum < (properAngleSum + 0.001f))) {
			// System.out.println("reversing");
			Point2D.Float[] reversed = new Point2D.Float[points.length];
			int countDown = points.length-1;
			for (int i = 0; i < points.length; i++) {
				reversed[countDown] = points[i];
				countDown--;
			}
			points = reversed;
		}
	}
	
	/***************************************************************************************/
	
	public Point2D.Float findTriangleCentre(int i, Point2D.Float[] decreasingPoints) {
		int h = (i + 1 > decreasingPoints.length-1 ? 0 : i+1);
		int j = (i - 1 < 0 ? decreasingPoints.length-1 : i-1);
		float x = (float)((decreasingPoints[h].x + decreasingPoints[i].x + decreasingPoints[j].x)/3);
		float y = (float)((decreasingPoints[h].y + decreasingPoints[i].y + decreasingPoints[j].y)/3);
		return new Point2D.Float(x, y);
	}
	
	/***************************************************************************************/
	
	public float findTriangleArea(int i, Point2D.Float[] decreasingPoints) {
		int h = (i + 1 > decreasingPoints.length-1 ? 0 : i+1);
		int j = (i - 1 < 0 ? decreasingPoints.length-1 : i-1);
		float distA = (float)Math.sqrt(Math.pow(decreasingPoints[h].y - decreasingPoints[j].y, 2) + Math.pow(decreasingPoints[h].x - decreasingPoints[j].x, 2));
		float distB = (float)Math.sqrt(Math.pow(decreasingPoints[i].y - decreasingPoints[h].y, 2) + Math.pow(decreasingPoints[i].x - decreasingPoints[h].x, 2));
		float distC = (float)Math.sqrt(Math.pow(decreasingPoints[j].y - decreasingPoints[i].y, 2) + Math.pow(decreasingPoints[j].x - decreasingPoints[i].x, 2));
		float s = (float)((distA + distB + distC)/2);
		return (float)Math.sqrt(Math.abs(s*(s - distA)*(s - distB)*(s - distC)));
	}
	
	/***************************************************************************************/
	
	public static float findTriangleArea(float x1, float y1, float x2, float y2, float x3, float y3) {
		float distA = (float)Math.sqrt(Math.pow(y2 - y3, 2) + Math.pow(x2 - x3, 2));
		float distB = (float)Math.sqrt(Math.pow(y1 - y2, 2) + Math.pow(x1 - x2, 2));
		float distC = (float)Math.sqrt(Math.pow(y3 - y1, 2) + Math.pow(x3 - x1, 2));
		float s = (float)((distA + distB + distC)/2);
		return (float)Math.sqrt(s*(s - distA)*(s - distB)*(s - distC));
	}
	
	/***************************************************************************************/
	
	public static Point2D.Float findTriangleCentre(float x1, float y1, float x2, float y2, float x3, float y3) {
		float x = (float)((x1 + x2 + x3)/3);
		float y = (float)((y1 + y2 + y3)/3);
		return new Point2D.Float(x, y);
	}
	
	/***************************************************************************************/
	
	public static float findAreaBetweenLinesAfterTranslation(Point2D.Float one, Point2D.Float two,
		Point2D.Float three, Point2D.Float four) {
		return (findTriangleArea(one.x, one.y, two.x, two.y, four.x, four.y) +
			findTriangleArea(one.x, one.y, four.x, four.y, three.x, three.y));
	}
	
	/***************************************************************************************/
	
	public static float findAreaBetweenLines(Point2D.Float one, Point2D.Float two,
		Point2D.Float three, Point2D.Float four) // line 1-2 and line 3-4
	{
		float area;
		if (linesIntersect(one.x, one.y, two.x, two.y, three.x, three.y, four.x, four.y)) {
			Point2D.Float intersection = getIntersection(new Line2D.Float(one, two), new Line2D.Float(three, four));
			area = findTriangleArea(intersection.x, intersection.y, one.x, one.y, three.x, three.y) +
				findTriangleArea(intersection.x, intersection.y, two.x, two.y, four.x, four.y);
		} else if (linesIntersect(one.x, one.y, three.x, three.y, two.x, two.y, four.x, four.y)) {
			Point2D.Float intersection = getIntersection(new Line2D.Float(one, three), new Line2D.Float(two, four));
			area = findTriangleArea(intersection.x, intersection.y, one.x, one.y, two.x, two.y) +
				findTriangleArea(intersection.x, intersection.y, three.x, three.y, four.x, four.y);
		} else {
			area = (findTriangleArea(one.x, one.y, two.x, two.y, four.x, four.y) +
				findTriangleArea(one.x, one.y, four.x, four.y, three.x, three.y));
		}
		return area;
	}
	
	/***************************************************************************************/
	
	public float findArea() {
		// don't need to ensureAntiClockwisePath every single area check, so ditch it later on.
		// ensureAntiClockwisePath();
		Point2D.Float[] decreasingPoints = points;
		Point2D.Float[] newDecreasingPoints;
		// loop to find the triangles and area of this polygon.
		float totalArea = 0;
		Line2D.Float currentLine;
		int g;
		int h;
		int i = 0;
		int j;
		int k;
		
		MainLoop:
			while (decreasingPoints.length > 3) {
			j = (i + 1 > decreasingPoints.length-1 ? 0 : i+1);		// one point after 'i'.
			if (findAngle(i, decreasingPoints) >= Math.PI) {
				i = j;
				continue;
			}
			h = (i - 1 < 0 ? decreasingPoints.length-1 : i-1);		// one point before 'i'.
			g = (h - 1 < 0 ? decreasingPoints.length-1 : h-1);		// two points before 'i'.
			// j is above,												   one point after 'i'.
			k = (j + 1 > decreasingPoints.length-1 ? 0 : j+1);		// two points after 'i'.
			
			float ihgAngle = findAngle(h, decreasingPoints);
			float ihjAngle = findAngle(j, h, i, decreasingPoints);
			
			// checks if hg cuts the triangle jih.
			if (ihgAngle < ihjAngle) {
				i = j;
				continue;
			}
			
			float ijkAngle = findAngle(j, decreasingPoints);
			float ijhAngle = findAngle(i, j, h, decreasingPoints);
			
			// checks if hg cuts the triangle jih.
			if (ijkAngle < ijhAngle) {
				i = j;
				continue;
			}
			
			currentLine = new Line2D.Float(decreasingPoints[h],
				decreasingPoints[j]);
			for (int m = 0; m < decreasingPoints.length; m++) {
				if (m == g || m == h || m == i || m == j) {
					continue;
				}
				if (currentLine.intersectsLine(decreasingPoints[m].x,		// if true then the line does not form an uncut triangle.
					decreasingPoints[m].y,
					decreasingPoints[(m + 1 > decreasingPoints.length-1 ? 0 : m+1)].x,
					decreasingPoints[(m + 1 > decreasingPoints.length-1 ? 0 : m+1)].y)) {
					i = j;
					continue MainLoop;
				}
			}
			
			// use Heron's formula to find area of the triangle and add it to the total.
			totalArea += findTriangleArea(i, decreasingPoints);
			
			// make a new array that excludes the point 'i'.
			newDecreasingPoints = new Point2D.Float[decreasingPoints.length-1];
			int counter = 0;
			for (int m = 0; m < decreasingPoints.length; m++) {
				if (m != i) {
					newDecreasingPoints[counter] = decreasingPoints[m];
					counter++;
				}
			}
			decreasingPoints = newDecreasingPoints;
			if (i > decreasingPoints.length-1) {
				i = 0;
			}
			}
		
		// we now have only one triangle left.
		totalArea += findTriangleArea(0, decreasingPoints);
		
		// finished.
		return totalArea;
	}
	
	/***************************************************************************************/
	
	public float findCentreAndArea() {
		ensureAntiClockwisePath();
		Point2D.Float[] decreasingPoints = points;
		Point2D.Float[] newDecreasingPoints;
		// loop to find the triangles and area of this polygon.
		float totalArea = 0;
		float currentTriangleArea;
		Point2D.Float currentTriangleCentre;
		Point2D.Float incompleteCentre = new Point2D.Float(0,0);
		Line2D.Float currentLine;
		int g;
		int h;
		int i = 0;
		int j;
		int k;
		
		int numFailedIterations = 0;
		
		MainLoop:
			while (decreasingPoints.length > 3) {
				if (numFailedIterations > decreasingPoints.length) {
					System.out.println("continual failures\nnumber of points = " + decreasingPoints.length);
					System.out.println("isValid() = " + isValid());
					area = findAreaQuickly();
					getCentreAndArea();
					System.out.println(this);
					//try{ Thread.sleep(5000); } catch (InterruptedException e) {}
					
					return getCentreAndArea();
				}
				j = (i + 1 > decreasingPoints.length-1 ? 0 : i+1);		// one point after 'i'.
				
				if (findAngle(i, decreasingPoints) > (float)Math.PI) {
					i = j;
//    System.out.print("#F1");
					numFailedIterations++;
					continue;
				}
				h = (i - 1 < 0 ? decreasingPoints.length-1 : i-1);		// one point before 'i'.
				g = (h - 1 < 0 ? decreasingPoints.length-1 : h-1);		// two points before 'i'.
				// j is above,												   one point after 'i'.
				k = (j + 1 > decreasingPoints.length-1 ? 0 : j+1);		// two points after 'i'.
				
				float ihgAngle = findAngle(h, decreasingPoints);
				float ihjAngle = findAngle(j, h, i, decreasingPoints);
				
				// checks if hg cuts the triangle jih.
				if (ihgAngle <= ihjAngle) {
					i = j;
//    System.out.print("#F2");
					numFailedIterations++;
					continue;
				}
				
				float ijkAngle = findAngle(j, decreasingPoints);
				float ijhAngle = findAngle(i, j, h, decreasingPoints);
				
				// checks if hg cuts the triangle jih.
				if (ijkAngle <= ijhAngle) {
					i = j;
//    System.out.print("#F3");
					numFailedIterations++;
					continue;
				}
				
				currentLine = new Line2D.Float(decreasingPoints[h], decreasingPoints[j]);
				for (int m = 0; m < decreasingPoints.length; m++) {
//    System.out.print("#B ");
					if (m == g || m == h || m == i || m == j) {
						continue;
					}
					if (currentLine.intersectsLine(decreasingPoints[m].x,		// if true then the line does not form an uncut triangle.
						decreasingPoints[m].y,
						decreasingPoints[(m + 1 > decreasingPoints.length-1 ? 0 : m+1)].x,
						decreasingPoints[(m + 1 > decreasingPoints.length-1 ? 0 : m+1)].y)) {
						i = j;
///       System.out.print("#F4");
						numFailedIterations++;
						continue MainLoop;
					}
				}
				
				numFailedIterations = 0;
				// use Heron's formula to find area of the triangle and add it to the total.
				currentTriangleArea = findTriangleArea(i, decreasingPoints);
				currentTriangleCentre = findTriangleCentre(i, decreasingPoints);
				incompleteCentre.x += (float)(currentTriangleCentre.x*currentTriangleArea);
				incompleteCentre.y += (float)(currentTriangleCentre.y*currentTriangleArea);
				totalArea += currentTriangleArea;
				
				// make a new array that excludes the point 'i'.
				newDecreasingPoints = new Point2D.Float[decreasingPoints.length-1];
				int counter = 0;
				for (int m = 0; m < decreasingPoints.length; m++) {
//    System.out.print("#C ");
					if (m != i) {
						newDecreasingPoints[counter] = decreasingPoints[m];
						counter++;
					}
				}
				decreasingPoints = newDecreasingPoints;
				if (i > decreasingPoints.length-1) {
					i = 0;
				}
			}
			
			// we now have only one triangle left.
			currentTriangleArea = findTriangleArea(0, decreasingPoints);
			currentTriangleCentre = findTriangleCentre(0, decreasingPoints);
			incompleteCentre.x += (float)(currentTriangleCentre.x*currentTriangleArea);
			incompleteCentre.y += (float)(currentTriangleCentre.y*currentTriangleArea);
			totalArea += currentTriangleArea;
			if (totalArea != 0.0f) {
				incompleteCentre.x = (float)(incompleteCentre.x/totalArea);
				incompleteCentre.y = (float)(incompleteCentre.y/totalArea);
				centre = incompleteCentre;
			} else if (decreasingPoints.length == 2) {
				centre = new Point2D.Float((float)((decreasingPoints[0].x + decreasingPoints[1].x)/2),
					(float)((decreasingPoints[0].y + decreasingPoints[1].y)/2));
			} else if (decreasingPoints.length == 1) {
				centre = new Point2D.Float(decreasingPoints[0].x, decreasingPoints[0].y);
			} else {
				centre = new Point2D.Float(0,0);
			}
			
			// finished.
			return totalArea;
	}
	
	/***************************************************************************************/
	
	public float getCentreAndArea() {
		float area1;
		float area2;
		float totalArea = 0;
		float cogx = 0;
		float cogy = 0;
		float x1;
		float y1;
		float x2;
		float y2;
		float y3 = -100;
		float y4 = -100;
		Line2D.Float testLine;
		int h;
		int g;
		
		for (int i = 0; i < points.length; i++) {
			g = (i+1 > points.length-1 ? 0 : i+1);
			if (points[i].x > points[g].x) {
				x1 = points[i].x;
				y1 = points[i].y;
				x2 = points[g].x;
				y2 = points[g].y;
				do
				{
					if (x2 != points[g].x) {
						x1 = x2;
						y1 = y2;
						x2 = points[g].x;
						y2 = points[g].y;
					}
					for (int j = 0; j < points.length; j++) {
						if (j != i &&
							points[j].x < x1 &&
							points[j].x > x2 &&
							(points[j].x < points[(j+1 > points.length-1 ? 0 : j+1)].x ||
							points[j].x > points[(j-1 < 0 ? points.length-1 : j-1)].x)) {
							y2 = ((y2 - y1)/(x2 - x1))*(points[j].x - x1) + y1;
							x2 = points[j].x;
						}
					}
					
					// here the yValues of the lines with negative area regions are found.
					JLoop:
								for (int j = 0; j < points.length; j++) {
						h = (j+1 > points.length-1 ? 0 : j+1);
//	  System.out.print("j = " + j + " which is " + points[j].x + ", " + points[j].y);
//	  System.out.print(" i = " + i + " x1 = " + x1 + " x2 =  " + x2);
						if (j != i &&
							points[j].x < points[h].x &&
							points[j].x <= x2 &&
							points[h].x >= x1) {
//	   System.out.println(" - made it.");
							y3 = ((points[h].y - points[j].y)/(points[h].x - points[j].x))*(x2 - points[j].x) + points[j].y;
							y4 = ((points[h].y - points[j].y)/(points[h].x - points[j].x))*(x1 - points[j].x) + points[j].y;
							if ((y3+y4)/2 > (y2+y1)/2) {
//	    System.out.println("too high");
								continue;
							}
							testLine = new Line2D.Float((float)((x1+x2)/2), (float)((y1+y2)/2), (float)((x1+x2)/2), (float)((y3+y4)/2));
							for (int k = 0; k < points.length; k++) {
								if (k == i ||
									k == j) {
									continue;
								}
								if (testLine.intersectsLine(new Line2D.Float(points[k], points[(k+1 > points.length-1 ? 0 : k+1)]))) {
//	     System.out.println("too low");
									continue JLoop;
								}
							}
							
							area1 = findTriangleArea(x1, y1, x2, y2, x1, y4);
							area2 = findTriangleArea(x1, y4, x2, y3, x2, y2); // can do this more quickly using height formula.
							totalArea += area1 + area2;
							cogx += (float)(((x1 + x2 + x1)/3)*area1);
							cogx += (float)(((x1 + x2 + x2)/3)*area2);
							cogy += (float)(((y1 + y2 + y4)/3)*area1);
							cogy += (float)(((y4 + y3 + y2)/3)*area2);
//	   System.out.println("x1 = " + x1 + " y1 = " + y1 + " x2 = " + x2 + " y2 = " + y2 + " y3 = " + y3 + " y4 = " + y4);
							break JLoop;
						}
/*	  else
	  {
	   System.out.println();
	  }*/
								}
				} while (x2 != points[g].x);
			}
		}
		
		cogx /= totalArea;
		cogy /= totalArea;
		centre = new Point2D.Float(cogx, cogy);
		return totalArea;
	}
	
	/***************************************************************************************/
	
	public float findAreaQuickly() {
		float totalArea = 0;
		for (int i = 0; i < points.length - 1; i++) {
			totalArea += (float)((points[i].x - points[i+1].x)*(points[i+1].y + (points[i].y - points[i+1].y)/2));
		}
		// need to do points[point.length-1] and points[0].
		totalArea += (float)((points[points.length-1].x - points[0].x)*(points[0].y + (points[points.length-1].y - points[0].y)/2));
		return totalArea;
	}
	
	/***************************************************************************************/
	
	// this method should be used only to find the point of intersection after it
	// is established that an intersection exists, to test
	// if an intersection exists, use linesIntersect().
	// a return of null means that there is an interval of intersection, not just a point.
	public static Point2D.Float getIntersection(Line2D.Float lineA, Line2D.Float lineB) {
		// check for a vertical gradient
		if (lineA.x2 - lineA.x1 == 0 && lineB.x2 - lineB.x1 == 0) {
			System.out.println("uh oh 1");
			if (lineA.x1 == lineB.x1 && lineA.y1 == lineB.y1 &&
				((lineA.y2 > lineA.y1 && lineB.y2 < lineB.y1) ||
				(lineA.y2 < lineA.y1 && lineB.y2 > lineB.y1))) {
				return new Point2D.Float(lineA.x1, lineA.y1);
			} else if (lineA.x1 == lineB.x2 && lineA.y1 == lineB.y2 &&
				((lineA.y2 > lineA.y1 && lineB.y1 < lineB.y2) ||
				(lineA.y2 < lineA.y1 && lineB.y1 > lineB.y2))) {
				return new Point2D.Float(lineA.x1, lineA.y1);
			} else if (lineA.x2 == lineB.x1 && lineA.y2 == lineB.y1 &&
				((lineA.y1 > lineA.y2 && lineB.y2 < lineB.y1) ||
				(lineA.y1 < lineA.y2 && lineB.y2 > lineB.y1))) {
				return new Point2D.Float(lineA.x2, lineA.y2);
			} else if (lineA.x2 == lineB.x2 && lineA.y2 == lineB.y2 &&
				((lineA.y1 > lineA.y2 && lineB.y1 < lineB.y2) ||
				(lineA.y1 < lineA.y2 && lineB.y1 > lineB.y2))) {
				return new Point2D.Float(lineA.x2, lineA.y2);
			}
			
			System.out.println("null 2");
			return null;		// there is an interval of intersection
		}
		
		if (Math.abs(lineA.x2 - lineA.x1) <= minDistance) {
			float y = ((lineB.y2 - lineB.y1)/(lineB.x2 - lineB.x1))*(lineA.x1 - lineB.x1) + lineB.y1;
			//System.out.println("lineA.y1 = " + lineA.y1 + " lineB.y2 = " + lineB.y2 + " y = " + y);
			return new Point2D.Float(lineA.x1, y);
		}
		
		if (Math.abs(lineB.x2 - lineB.x1) <= minDistance) {
			float y = ((lineA.y2 - lineA.y1)/(lineA.x2 - lineA.x1))*(lineB.x1 - lineA.x1) + lineA.y1;
			//System.out.println("lineA.y1 = " + lineA.y1 + " lineB.y2 = " + lineB.y2 + " y = " + y);
			return new Point2D.Float(lineB.x1, y);
		}
		
		float m1 = (lineA.y2 - lineA.y1)/(lineA.x2 - lineA.x1);
		float m2 = (lineB.y2 - lineB.y1)/(lineB.x2 - lineB.x1);
		//System.out.println("up to here");
		
		if ((m1 == m2) && (m1*(lineB.x1 - lineA.x1) + lineA.y1 - lineB.y1 == 0)) {
			System.out.println("uh oh 2");
			if (lineA.x1 == lineB.x1 || lineA.x1 == lineB.x2) {
				return new Point2D.Float(lineA.x1, (float)(m1*(lineA.x1 - lineA.x1) + lineA.y1));
			} else if (lineA.x2 == lineB.x1 || lineA.x2 == lineB.x2) {
				return new Point2D.Float(lineA.x2, (float)(m1*(lineA.x2 - lineA.x1) + lineA.y1));
			}
			System.out.println("null 5");
			return null;		// there is an interval of intersection
		}
		
		float x = (float)((m1*lineA.x1 - m2*lineB.x1 - lineA.y1 + lineB.y1)/(m1 - m2));
		float y = (float)(m1*(x - lineA.x1) + lineA.y1);
		//System.out.println("lineA.y1 = " + lineA.y1 + " lineB.y2 = " + lineB.y2 + " y = " + y + " m1 = " + m1 + " m2 = " + m2);
		
		// System.out.println("x = " + x);
		// System.out.println("y = " + y);
		
  /*if (!( ((x >= lineA.x1 && x <= lineA.x2) || (x <= lineA.x1 && x >= lineA.x2) ||
		  (x >= lineB.x1 && x <= lineB.x2) || (x <= lineB.x1 && x >= lineB.x2)) &&
		 ((y >= lineA.y1 && y <= lineA.y2) || (y <= lineA.y1 && y >= lineA.y2) ||
		  (y >= lineB.y1 && y <= lineB.y2) || (y <= lineB.y1 && y >= lineB.y2)) ))
  {
   System.out.println("null 6");
   return null;		// no intersection
  }*/
		
		return new Point2D.Float(x, y);
	}
	
	/***************************************************************************************/
	
	public float getPerimeter() {
		float perimeter = 0;
		for (int i = 0; i < points.length-1; i++) {
			perimeter += (float)points[i].distance(points[i+1]);
		}
		perimeter += (float)points[points.length-1].distance(points[0]);
		return perimeter;
	}
	
	/***************************************************************************************/
	
	public void rotate(float angle, Point2D.Float axle) {
		float currentAngle;
		float distance;
		for (int i = 0; i < points.length; i++) {
			currentAngle = findAngle(axle, points[i]);
			currentAngle += angle;
			distance = (float)axle.distance(points[i]);
			points[i].x = axle.x + (float)(distance*Math.cos(currentAngle));
			points[i].y = axle.y + (float)(distance*Math.sin(currentAngle));
		}
		currentAngle = findAngle(axle, centre);
		currentAngle += angle;
		distance = (float)axle.distance(centre);
		centre.x = axle.x + (float)(distance*Math.cos(currentAngle));
		centre.y = axle.y + (float)(distance*Math.sin(currentAngle));
	}
	
	/***************************************************************************************/
	
	public void rotate(float angle) {
		rotate(angle, centre);
	}
	
	/***************************************************************************************/
	
	public void translate(float x, float y) {
		for (int i = 0; i < points.length; i++) {
			points[i].x += x;
			points[i].y += y;
		}
		centre.x += x;
		centre.y += y;
	}
	
	/***************************************************************************************/
	public void translateTo(float x, float y){
		float xIncrement = x - centre.x;
		float yIncrement = y - centre.y;
		centre = new Point2D.Float(x, y);
		for (int i = 0; i < points.length; i++){
			points[i].x += xIncrement;
			points[i].y += yIncrement;
		}
	}
	public void translateTo(Point2D.Float newCentre){
		translateTo(newCentre.x, newCentre.y);
	}
	public void translateToOrigin(){
		translate(-centre.x, -centre.y);
	}
	
	/***************************************************************************************/
	
	public void scale(float xMultiplier, float yMultiplier, float x, float y){
		float incX;
		float incY;
		for (int i = 0; i < points.length; i++){
			incX = points[i].x - x;
			incY = points[i].y - y;
			incX *= xMultiplier;
			incY *= yMultiplier;
			points[i].x = x + incX;
			points[i].y = y + incY;
		}
		incX = centre.x - x;
		incY = centre.y - y;
		incX *= xMultiplier;
		incY *= yMultiplier;
		centre.x = x + incX;
		centre.y = y + incY;
		area = findAreaQuickly();
		circularBound = findCircularBound();
	}
	public void scale(float multiplierX, float multiplierY){
		scale(multiplierX, multiplierY, getCentre().x, getCentre().y);
	}
	public void scale(float multiplier){
		scale(multiplier, multiplier, getCentre().x, getCentre().y);
	}
	
	/***************************************************************************************/
	
	public float findCircularBound() {
		float currentRadius;
		float maxRadius = 0;
		for (int i = 0; i < points.length; i++) {
			currentRadius = (float)(centre.distance(points[i]));
			if (currentRadius > maxRadius) {
				maxRadius = currentRadius;
			}
		}
		circularBound = maxRadius;
		return maxRadius;
	}
	
	/***************************************************************************************/
	
	// finds the smallest circularBound and the new centre of the given centres and circularBounds.
	// returns a float array, [0] & [1] are the centre X & Y coords and [2] is the circularBound.
	public static float[] findCombinedCentreAndCircularBound(float x1, float y1, float circularBound1, float x2, float y2, float circularBound2){
		float dist = (float)Point2D.Float.distance(x1, y1, x2, y2);
		float newCircularBound = (float)((circularBound1 + dist + circularBound2)/2);
		float x = x2 - x1;
		float y = y2 - y1;
		float distFromC1ToNewC = newCircularBound - circularBound1;
		float[] centreXYAndBound = new float[3];
		centreXYAndBound[0] = (float)(distFromC1ToNewC*x/dist);
		centreXYAndBound[1] = (float)(distFromC1ToNewC*y/dist);
		centreXYAndBound[2] = newCircularBound;
		
		return centreXYAndBound;
	}
	
	public static float[] findCombinedCentreAndCircularBound(KPolygon kPolygon1, KPolygon kPolygon2){
		return findCombinedCentreAndCircularBound(kPolygon1.getCentre().x, kPolygon1.getCentre().y, kPolygon1.getCircularBound(), kPolygon2.getCentre().x, kPolygon2.getCentre().y, kPolygon2.getCircularBound());
	}
	public static float[] findCombinedCentreAndCircularBound(Point2D.Float centre1, float circularBound1, Point2D.Float centre2, float circularBound2){
		return findCombinedCentreAndCircularBound(centre1.x, centre1.y, circularBound1, centre2.x, centre2.y, circularBound2);
	}
	/***************************************************************************************/
	public Point2D.Float[] getPoints() {
		return points;
	}
	
	/***************************************************************************************/
	
	
	
	public float getWidth() {
		float leftX = points[0].x;
		float rightX = points[0].x;
		for (int i = 0; i < points.length; i++) {
			if (points[i].x < leftX){
				leftX = points[i].x;
			} else if (points[i].x > rightX){
				rightX = points[i].x;
			}
		}
		return rightX - leftX;
	}
	
	/***************************************************************************************/
	
	public float getHeight() {
		float topY = points[0].y;
		float botY = points[0].y;
		for (int i = 0; i < points.length; i++) {
			if (points[i].y < botY){
				botY = points[i].y;
			} else if (points[i].y > topY){
				topY = points[i].y;
			}
		}
		return topY - botY;
	}
	
	/***************************************************************************************/
	
	public String toString() {
		/*StringBuffer kPolygonString = new StringBuffer("[KPolygon] number of points = " + points.length + "\n" +
			"area = " + area + "\n" +
			"circularBound = " + circularBound + "\n" +
			"centre = " + "( " + centre.x + ", " + centre.y + " )\n");
		for (int i = 0; i < points.length; i++) {
			kPolygonString.append("( " + points[i].x + ", " + points[i].y + " )\n");
		}
		return new String(kPolygonString);*/
		return super.toString();
	}
	
	/***************************************************************************************/
	
	public GeneralPath getGeneralPath() {
		GeneralPath generalPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		generalPath.moveTo(points[0].x, points[0].y);
		for (int i = 1; i < points.length; i++) {
			generalPath.lineTo(points[i].x, points[i].y);
		}
		generalPath.closePath();
		//generalPath.moveTo(centre.x, centre.y);
		return generalPath;
	}
	
	/***************************************************************************************/
	public Point2D.Float getCentre(){
		// centre is only initialized when you create the KPolygon...
		if(centre==null) this.findCentreAndArea();
		return centre;
	}
	/***************************************************************************************/
	public float getCircularBound(){
		return circularBound;
	}
	
	/***************************************************************************************/
	public Point2D.Float getPoint(int pointNum){
		if (pointNum == -1){
			return centre;
		}
		return points[pointNum];
	}
	/***************************************************************************************/
	
	public static Point2D.Float getMidPoint(Point2D.Float p1, Point2D.Float p2){
		return new Point2D.Float((float)((p1.x + p2.x)/2), (float)((p1.y + p2.y)/2));
	}
	
	/***************************************************************************************/
	/*
	// makes this KPolygon so that it equals(model) but is a different object (ie this != model and also this.points != model.points)
	// creates no unnecessary garbage. eg if same number of points, no new point array is created.
	public void makeEqualTo(KPolygon model){
		area = model.area;
		circularBound = model.circularBound;
		int pointsLength = model.points.length;
		if (points == null || pointsLength != points.length){
			Point2D.Float[] newPoints = new Point2D.Float[pointsLength];
			int i = 0;
			if (points != null){
				// reuse as many old Point2D.Float objects as possible
				for ( ; i < points.length && i < newPoints.length; i++){
					newPoints[i] = points[i];
				}
			}
			// fill the remaining space with new Point2D.Floats
			for ( ; i < newPoints.length; i++){
				newPoints[i] = new Point2D.Float();
			}
			points = newPoints;
		}
		// assign the right values
		for (int i = 0; i < points.length; i++){
			points[i].x = model.points[i].x;
			points[i].y = model.points[i].y;
		}
		if (centre == null){
			centre = new Point2D.Float(model.centre.x, model.centre.y);
		} else{
			centre.x = model.centre.x;
			centre.y = model.centre.y;
		}
	}*/
	private void writeObject(java.io.ObjectOutputStream out) throws IOException{
		out.writeFloat(area);
		out.writeFloat(circularBound);
		out.writeInt(points.length);
		for (int i = 0; i < points.length; i++){
			out.writeFloat(points[i].x);
			out.writeFloat(points[i].y);
		}
		if (centre == null){
			out.writeBoolean(false);
		} else{
			out.writeBoolean(true);
			out.writeFloat(centre.x);
			out.writeFloat(centre.y);
		}
	}
	
	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
		area = in.readFloat();
		circularBound = in.readFloat();
		points = new Point2D.Float[in.readInt()];
		for (int i = 0; i < points.length; i++){
			points[i] = new Point2D.Float(in.readFloat(), in.readFloat());
		}
		if (in.readBoolean()){ //flag to signal that centre is not null and has been sent
			centre = new Point2D.Float(in.readFloat(), in.readFloat());
		}
	}
	
	public void writeSS(SSObjectOutputStream out) throws IOException{
		out.writeFloat(area);
		out.writeFloat(circularBound);
		out.writeInt(points.length);
		for (int i = 0; i < points.length; i++){
			out.writeFloat(points[i].x);
			out.writeFloat(points[i].y);
		}
		if (centre == null){
			out.writeBoolean(false);
		} else{
			out.writeBoolean(true);
			out.writeFloat(centre.x);
			out.writeFloat(centre.y);
		}
	}
	public void readSS(SSObjectInputStream in) throws IOException{
		area = in.readFloat();
		circularBound = in.readFloat();
		int pointsLength = in.readInt();
		if (points == null || pointsLength != points.length){
			Point2D.Float[] newPoints = new Point2D.Float[pointsLength];
			int i = 0;
			if (points != null){
				for ( ; i < points.length && i < newPoints.length; i++){
					newPoints[i] = points[i];
				}
			}
			for ( ; i < newPoints.length; i++){
				newPoints[i] = new Point2D.Float();
			}
			points = newPoints;
		}
		for (int i = 0; i < points.length; i++){
			points[i].x = in.readFloat();
			points[i].y = in.readFloat();
		}
		if (in.readBoolean()){
			if (centre == null){
				centre = new Point2D.Float(in.readFloat(), in.readFloat());
			} else{
				centre.x = in.readFloat();
				centre.y = in.readFloat();
			}
		}
	}
}
