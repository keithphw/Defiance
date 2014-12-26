/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.shooter;

import java.awt.geom.Point2D;

import sydneyengine.superserializable.SSAdapter;
/**
 *
 * @author Phillip
 */
public class BBox extends SSAdapter{
	float x, y, w, h;
	public BBox(){
		
	}
	public BBox(float x, float y, float w, float h){
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	public static BBox makeBBoxEncompassingCoords(float c1x, float c1y, float c2x, float c2y){
//		Point2D.Float c1 = p1.getBox().getCenter();
//		Point2D.Float c2 = p2.getBox().getCenter();
		float x1 = 0;
		float y1 = 0;
		float x2 = 0;
		float y2 = 0;
		if (c1x < c2x){
			if (c1y < c2y){
				x1 = c1x;
				y1 = c1y;
				x2 = c2x;
				y2 = c2y;
			}else{
				x1 = c1x;
				y1 = c2y;
				x2 = c2x;
				y2 = c1y;
			}
		}else{
			if (c1y < c2y){
				x1 = c2x;
				y1 = c1y;
				x2 = c1x;
				y2 = c2y;
			}else{
				x1 = c2x;
				y1 = c2y;
				x2 = c1x;
				y2 = c1y;
			}
		}
		BBox spaceBetween = new BBox(x1, y1, x2-x1, y2-y1);
		return spaceBetween;
	}

	public float getH() {
		return h;
	}

	public void setH(float h) {
		this.h = h;
	}

	public float getW() {
		return w;
	}

	public void setW(float w) {
		this.w = w;
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
	
	@Override
	public String toString(){
		return "x"+x+", y"+y+", w"+w+", h"+h;
	}
	
	public Point2D.Float getCenter(){
		return new Point2D.Float(getCenterX(), getCenterY());
	}
	public float getCenterX(){
		return x + w/2f;
	}
	public float getCenterY(){
		return y + h/2f;
	}
	public void setCenter(Point2D.Float p){
		setCenterX(p.x);
		setCenterY(p.y);
	}
	public void setCenterX(double newCentreX){
		this.x = (float)newCentreX - w/2f;
	}
	public void setCenterY(double newCentreY){
		this.y = (float)newCentreY - h/2f;
	}
	
	protected static boolean isBetween(double v, double n1, double n2){
		if (v >= n1 && v <= n2){
			return true;
		}
		return false;
	}
	public boolean contains(Point2D.Float p){
		return contains(p.x, p.y);
	}
	public boolean contains(double x, double y) {
		double x0 = getX();
		double y0 = getY();
		return (x >= x0 &&
			y >= y0 &&
			x <= x0 + getW() &&
			y <= y0 + getH());
    }
	public boolean intersects(BBox box) {
		return intersects(box.x, box.y, box.w, box.h);
	}
	// code from java.awt.Rectangle2D:
    public boolean intersects(double x, double y, double w, double h) {
		if (this.w <= 0 || this.h <= 0 || w <= 0 || h <= 0) {
			return false;
		}
		double x0 = getX();
		double y0 = getY();
		return (x + w >= x0 &&
			y + h >= y0 &&
			x <= x0 + getW() &&
			y <= y0 + getH());
    }
	public boolean contains(double x, double y, double w, double h) {
		if (w <= 0 || h <= 0) {
			return false;
		}
		double x0 = getX();
		double y0 = getY();
		return (x >= x0 &&
			y >= y0 &&
			(x + w) <= x0 + getW() &&
			(y + h) <= y0 + getH());
	}
	public static void union(BBox src1,
			     BBox src2,
			     BBox dest) {
		double x1 = Math.min(src1.getX(), src2.getX());
		double y1 = Math.min(src1.getY(), src2.getY());
		double x2 = Math.max(src1.getX()+src1.getW(), src2.getX()+src2.getW());
		double y2 = Math.max(src1.getY()+src1.getH(), src2.getY()+src2.getH());
		dest.setFrameFromDiagonal(x1, y1, x2, y2);
    }
	public void setFrameFromDiagonal(double x1, double y1, double x2, double y2) {
		if (x2 < x1) {
			double t = x1;
			x1 = x2;
			x2 = t;
		}
		if (y2 < y1) {
			double t = y1;
			y1 = y2;
			y2 = t;
		}
		setX((float)x1);
		setY((float)y1);
		setW((float)(x2 - x1));
		setH((float)(y2 - y1));
    }
	

}