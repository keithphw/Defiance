
package sydneyengine.shooter;

import java.io.IOException;

import sydneyengine.superserializable.SSObjectInputStream;
import sydneyengine.superserializable.SSObjectOutputStream;
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
	
	@Override
	public void applyNow(GameWorld world){
		super.applyNow(world);
		world.removePlayer(player, getTimeStamp());
		System.out.println(this.getClass().getSimpleName()+": player removed, player == "+player);
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
