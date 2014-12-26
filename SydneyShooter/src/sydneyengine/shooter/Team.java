/*
 * Team.java
 *
 * Created on 10 October 2007, 02:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine.shooter;


import java.awt.Color;

import sydneyengine.superserializable.ArrayListSS;
import sydneyengine.superserializable.SSAdapter;

public class Team extends SSAdapter{
	GameWorld world;
	protected Color color;
	protected String name;
	protected float radius = 20;
	protected ArrayListSS<Player> players = new ArrayListSS<Player>();
	ArrayListSS<SpawnFlag> spawnFlags = new ArrayListSS<SpawnFlag>();
	// spawnFlags.get(0) is the base flag and is non-transferable
	CapturableFlag capturableFlag;
	
	public final static String NATURE_TEAM_NAME = "Nature";

	// no-arg constructor for the SS streams
	public Team(){
	}
	public Team(GameWorld world, String name, Color color){
		this.world = world;
		this.name = name;
		this.color = color;
	}
	public void doMove(double seconds, double timeAtStartOfMoveSeconds) {
		for (int i = 0; i < getSpawnFlags().size(); i++){
			SpawnFlag spawnFlag = getSpawnFlags().get(i);
			spawnFlag.doMove(seconds, timeAtStartOfMoveSeconds);
		}
		if (capturableFlag != null){
			capturableFlag.doMove(seconds, timeAtStartOfMoveSeconds);
		}
	}
	public void render(ViewPane viewPane){
		for (int i = 0; i < getSpawnFlags().size(); i++){
			getSpawnFlags().get(i).render(viewPane);
		}
		if (capturableFlag != null){
			capturableFlag.render(viewPane);
		}
	}
		
	public float getSpawnX() {
		return getSpawnFlags().get(getSpawnFlags().size()-1).getX();
	}
	
	public float getSpawnY() {
		return getSpawnFlags().get(getSpawnFlags().size()-1).getY();
	}
	public float getRadius() {
		return radius;
	}
	
	public void setRadius(float radius) {
		this.radius = radius;
	}
	public void addPlayer(Player p){
		assert players.contains(p) == false;
		players.add(p);
	}
	public void removePlayer(Player p){
		players.remove(p);
	}
	public ArrayListSS<Player> getPlayers() {
		return players;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public void addSpawnFlag(SpawnFlag spawnFlag){
		spawnFlags.add(spawnFlag);
	}
	public void removeSpawnFlag(SpawnFlag spawnFlag){
		spawnFlags.remove(spawnFlag);
	}
	public ArrayListSS<SpawnFlag> getSpawnFlags(){
		return spawnFlags;
	}
	
	public SpawnFlag getSpawnFlagBase(){
		return spawnFlags.get(0);
	}
	public void setCapturableFlag(CapturableFlag capturableFlag){
		this.capturableFlag = capturableFlag;
	}
	public CapturableFlag getCapturableFlag(){
		return capturableFlag;
	}
	
}