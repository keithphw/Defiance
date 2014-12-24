/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.lobby;

import sydneyengine.superserializable.*;
import java.io.*;
import java.util.*;


/**
 *
 * @author CommanderKeith
 */
public class LobbyInfo extends SSAdapter{
	protected int numPlayersConnected = 0;
	protected int mostPlayersOnline = 0;
	protected int totalNumPlayersConnected = 0;
	protected String dateOfServerBirth = "";
	
	protected ArrayListSS<HostedGame> hostedGames = new ArrayListSS<HostedGame>();
	
	public LobbyInfo(){
		
	}

	public void setNumPlayersConnected(int numPlayersConnected) {
		this.numPlayersConnected = numPlayersConnected;
	}

	public ArrayListSS<HostedGame> getHostedGames() {
		return hostedGames;
	}

	public int getMostPlayersOnline() {
		return mostPlayersOnline;
	}

	public int getNumPlayersConnected() {
		return numPlayersConnected;
	}

	public int getTotalNumPlayersConnected() {
		return totalNumPlayersConnected;
	}

	public String getDateOfServerBirth() {
		return dateOfServerBirth;
	}
	

	public void setDateOfServerBirth(String dateOfServerBirth) {
		this.dateOfServerBirth = dateOfServerBirth;
	}

	public void setMostPlayersOnline(int mostPlayersOnline) {
		this.mostPlayersOnline = mostPlayersOnline;
	}

	public void setTotalNumPlayersConnected(int totalNumPlayersConnected) {
		this.totalNumPlayersConnected = totalNumPlayersConnected;
	}
	
	
}
