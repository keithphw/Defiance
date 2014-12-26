/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.shooter;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import sydneyengine.superserializable.SSAdapter;
/**
 *
 * @author CommanderKeith
 */
public class Obstacle extends SSAdapter{
	GameWorld world;
	KPolygon shape;
	transient Color color = new Color(120, 120, 125);
	
	public Obstacle(){
	}
	public Obstacle(GameWorld world, ArrayList<Point2D.Float> points){
		this.world = world;
		shape = new KPolygon(points.toArray(new Point2D.Float[0]));
	}
	public Obstacle(GameWorld world, KPolygon polygon){
		this.world = world;
		shape = polygon;
	}
	
	public void render(ViewPane viewPane){
		//System.out.println(this.getClass().getSimpleName()+": "+Color.GRAY.brighter());//+getShape().getPoints());
		Graphics2D g = viewPane.getBackImageGraphics2D();		
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
	public void setShape(KPolygon newShape){
		
		shape= newShape;
	}
}