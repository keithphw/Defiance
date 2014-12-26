/*
 * To change this template, choose Tools | Templates
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
public class ChatTextEvent extends PlayerEvent{
	
	protected String text;
	boolean alliesOnly;
	
	/** Creates a new instance of PlayerMouseEvent */
	public ChatTextEvent() {
	}
	public ChatTextEvent(Player player, String text, boolean alliesOnly){
		super(player);
		this.text = text;
		this.alliesOnly = alliesOnly;
	}
	
	@Override
	public void applyNow(GameWorld world){
		super.applyNow(world);
		ChatText chatText = new ChatText(getPlayer(), text, alliesOnly, getTimeStamp());
		world.addChatText(chatText);
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
