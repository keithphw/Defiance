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
public class Water extends SSAdapter{
	GameWorld world;
	KPolygon shape;
	
	public Water(){
	}
	public Water(GameWorld world, ArrayList<Point2D.Float> points){
		this.world = world;
		shape = new KPolygon(points.toArray(new Point2D.Float[0]));
	}
	public Water(GameWorld world, KPolygon polygon){
		this.world = world;
		shape = polygon;
	}
	static Color color = new Color(140, 140, 255);
	public void render(ViewPane viewPane){
		//System.out.println(this.getClass().getSimpleName()+": "+getShape().getPoints());
		Graphics2D g = viewPane.getBackImageGraphics2D();
		//g.setColor(Color.GRAY.brighter());
		
		g.setColor(Color.cyan);
		g.draw(shape);
		
		g.setColor(color);		
		g.fill(shape);
	}
	

	public void doMove(double seconds, double timeAtStartOfMoveSeconds) {
		assert seconds >= 0 : seconds;
		double timeAtEndOfMoveSeconds = timeAtStartOfMoveSeconds + seconds;
	}
	public KPolygon getShape(){
		return shape;
	}
}