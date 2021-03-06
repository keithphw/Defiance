/*
 * PlayerMouseEvent.java
 *
 * Created on 13 November 2007, 00:22
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine.shooter;

import java.io.IOException;

import sydneyengine.superserializable.SSObjectInputStream;
import sydneyengine.superserializable.SSObjectOutputStream;
/**
 *
 * @author CommanderKeith
 */
public class PlayerMouseWheelEvent extends PlayerEvent{
	
	
	protected int wheelRotation;
	
	/** Creates a new instance of PlayerMouseEvent */
	public PlayerMouseWheelEvent() {
	}
	public PlayerMouseWheelEvent(Player player, int wheelRotation){
		super(player);
		this.wheelRotation = wheelRotation;
	}
	
	@Override
	public void applyNow(GameWorld world){
		super.applyNow(world);
		player.cycleGunsForwardBy(wheelRotation, getTimeStamp());
		
	}
	
	public int getWeelRotation() {
		return wheelRotation;
	}
	
	@Override
	public void writeSS(SSObjectOutputStream out) throws IOException{		// this is the method that you over-ride if you want custom serialization
		super.writeSS(out);
		out.writeFields(this);
	}
	
	@Override
	public void readSS(SSObjectInputStream in) throws java.io.IOException{	// this is the method that you over-ride if you want custom serialization
		super.readSS(in);
		in.readFields(this);
	}
	
	
}
