/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.shooter;

import sydneyengine.superserializable.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
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
	
	public void applyNow(GameWorld world){
		super.applyNow(world);
		ChatText chatText = new ChatText(getPlayer(), text, alliesOnly, getTimeStamp());
		world.addChatText(chatText);
	}
	
	public void writeSS(SSObjectOutputStream out) throws IOException{		// this is the method that you over-ride if you want custom serialization
		super.writeSS(out);
		out.writeFields(this);
	}
	
	public void readSS(SSObjectInputStream in) throws java.io.IOException{	// this is the method that you over-ride if you want custom serialization
		super.readSS(in);
		in.readFields(this);
	}

	
}
