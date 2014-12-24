/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.shooter;

import sydneyengine.*;
import sydneyengine.superserializable.*;
import java.io.*;
import java.util.*;
/**
 *
 * @author CommanderKeith
 */
public class ChangeTeamEvent extends PlayerEvent{

	Team newTeam;
	public ChangeTeamEvent() {
	}
	public ChangeTeamEvent(Player player, Team newTeam){
		super(player);
		this.newTeam = newTeam;
	}
	public void apply(RewindableWorld world){
		super.apply(world);
		if (player.getCapturableFlag() != null){
			player.getCapturableFlag().drop(getTimeStamp());
		}
		player.getTeam().removePlayer(player);
		player.setTeam(newTeam);
		newTeam.addPlayer(player);
		player.respawn();
	}
	public void writeSS(SSObjectOutputStream out) throws IOException{		// this is the method that you over-ride if you want custom serialization
		super.writeSS(out);
		out.writeInt(newTeam.getSSCode());
	}
	
	public void readSS(SSObjectInputStream in) throws java.io.IOException{	// this is the method that you over-ride if you want custom serialization
		super.readSS(in);
		int newTeamSSCode = in.readInt();
		newTeam = (Team)in.getStoredObject(newTeamSSCode);	// I think that the problem here is that the player object is being garbage collected as soon as it is read...
		assert newTeam != null : "newTeamSSCode == "+newTeamSSCode+", teams == "+GameFrame.getStaticGameFrame().getController().getWorld().getTeams();
	}
}
