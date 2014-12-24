
package sydneyengine.shooter;

import sydneyengine.superserializable.*;
import java.io.*;
import java.util.*;
/**
 *  Note that this event is unusual since it is not sent by the computer which controls the 
 *  player, it is sent by the server VM when it detects that the Nexus's connection to the client VM is lost.
 * 
 * @author CommanderKeith
 */
public class RemovePlayerEvent extends PlayerEvent{
	
	/** Creates a new instance of PlayerMouseEvent */
	public RemovePlayerEvent() {
	}
	public RemovePlayerEvent(Player player){
		super(player);
	}
	
	public void applyNow(GameWorld world){
		super.applyNow(world);
		world.removePlayer(player, getTimeStamp());
		System.out.println(this.getClass().getSimpleName()+": player removed, player == "+player);
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
