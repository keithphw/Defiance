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
public class PlayerMouseEvent extends PlayerEvent{
	
	public static final int MOUSE_PRESS = 100;
	public static final int MOUSE_RELEASE = 101;
	public static final int MOUSE_DRAG = 102;
	public static final int MOUSE_MOVE = 103;
	
	protected int mouseEventType;
	protected float x;
	protected float y;
	protected int button;
	
	/** Creates a new instance of PlayerMouseEvent */
	public PlayerMouseEvent() {
	}
	public PlayerMouseEvent(Player player, int mouseEventType, float x, float y, int button){
		super(player);
		this.mouseEventType = mouseEventType;
		this.setX(x);
		this.setY(y);
		this.button = button;
	}
	
	@Override
	public void applyNow(GameWorld world){
		super.applyNow(world);
		
		player.setMouseTargetX(x);
		player.setMouseTargetY(y);
		
		if (mouseEventType == MOUSE_PRESS){
			player.getGun().startFiring(world.getTotalElapsedSeconds());
		}else if (mouseEventType == MOUSE_RELEASE){
			player.getGun().stopFiring();
		}
		
	}
	
	public int getMouseEventType() {
		return mouseEventType;
	}
	
	public void setMouseEventType(int mouseEventType) {
		this.mouseEventType = mouseEventType;
	}
	
	public int getButton() {
		return button;
	}
	
	public void setButton(int button) {
		this.button = button;
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
