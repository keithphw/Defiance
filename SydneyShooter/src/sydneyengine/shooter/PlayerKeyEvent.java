/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.shooter;

import java.awt.event.KeyEvent;
import java.io.IOException;

import sydneyengine.superserializable.SSObjectInputStream;
import sydneyengine.superserializable.SSObjectOutputStream;
/**
 *
 * @author CommanderKeith
 */
public class PlayerKeyEvent extends PlayerEvent{
	
	public static final int KEY_PRESS = 200;
	public static final int KEY_RELEASE = 201;
	protected int keyEventType;
	protected int keyCode;
	
	/** Creates a new instance of PlayerMouseEvent */
	public PlayerKeyEvent() {
	}
	public PlayerKeyEvent(Player player, int keyEventType, int keyCode){
		super(player);
		this.keyEventType = keyEventType;
		this.keyCode = keyCode;
	}
	
	@Override
	public void applyNow(GameWorld world){
		super.applyNow(world);
		
		if (keyEventType == KEY_PRESS){
			if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A){
				player.setLeft(true);
			}else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D){
				player.setRight(true);
			}else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W){
				player.setUp(true);
			}else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S){
				player.setDown(true);
			}else if (keyCode == KeyEvent.VK_R){
				player.getGun().reloadClip();
			}else if (keyCode == KeyEvent.VK_1){
				player.setGunFromIndexIfAvailable(1, getTimeStamp());
			}else if (keyCode == KeyEvent.VK_2){
				player.setGunFromIndexIfAvailable(2, getTimeStamp());
			}else if (keyCode == KeyEvent.VK_3){
				player.setGunFromIndexIfAvailable(3, getTimeStamp());
			}else if (keyCode == KeyEvent.VK_4){
				player.setGunFromIndexIfAvailable(4, getTimeStamp());
			}else if (keyCode == KeyEvent.VK_5){
				player.setGunFromIndexIfAvailable(5, getTimeStamp());
			}else if (keyCode == KeyEvent.VK_6){
				player.setGunFromIndexIfAvailable(6, getTimeStamp());
			}else if (keyCode == KeyEvent.VK_7){
				player.setGunFromIndexIfAvailable(7, getTimeStamp());
			}else if (keyCode == KeyEvent.VK_8){
				player.setGunFromIndexIfAvailable(8, getTimeStamp());
			}else if (keyCode == KeyEvent.VK_9){
				player.setGunFromIndexIfAvailable(9, getTimeStamp());
			}
			// added by KT 12/20/2014- support for previous/next guns (Q/E), can also use mousewheel
			
			else if(keyCode==KeyEvent.VK_Q){
				player.cycleGunsForwardBy(-1, getTimeStamp());
			}
			else if(keyCode==KeyEvent.VK_E){
				player.cycleGunsForwardBy(1, getTimeStamp());
			}
		}else if (keyEventType == KEY_RELEASE){
			if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A){
				player.setLeft(false);
			}else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D){
				player.setRight(false);
			}else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W){
				player.setUp(false);
			}else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S){
				player.setDown(false);
			}
		}
		
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

	public int getKeyEventType() {
		return keyEventType;
	}

	public void setKeyEventType(int keyEventType) {
		this.keyEventType = keyEventType;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}
	
	
}
