/*
 * World.java
 *
 * Created on 12 November 2007, 18:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package sydneyengine.shooter;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.swing.SwingUtilities;


//added by KT 12/20/2014
import kEffects.Explosion;
import kEffects.ParticleEmitter;
import sydneyengine.RewindableWorld;
import sydneyengine.shooter.Bullet.Bullet;
import sydneyengine.shooter.Gun.FlameThrower;
import sydneyengine.shooter.Gun.HomingGun;
import sydneyengine.shooter.Gun.MachineGun;
import sydneyengine.shooter.Gun.NailGun;
import sydneyengine.shooter.Gun.Pistol;
import sydneyengine.shooter.Gun.RocketLauncher;
import sydneyengine.shooter.Gun.ShotGun;
import sydneyengine.shooter.Gun.SniperRifle;
import sydneyengine.shooter.Gun.TranquilizerGun;
import sydneyengine.shooter.Item.GodlyArmor;
import sydneyengine.shooter.Item.HealthPack;
import sydneyengine.shooter.Item.InvisibilityShroud;
import sydneyengine.shooter.Item.Item;
import sydneyengine.shooter.Item.SpeedShoes;
import sydneyengine.superserializable.ArrayListSS;
import sydneyengine.superserializable.SSObjectInputStream;
import sydneyengine.superserializable.SSObjectOutputStream;


/**
 *
 * @author CommanderKeith
 */
public class GameWorld extends RewindableWorld {

	//added by KT 12/20/2014
	protected ArrayListSS<Explosion> explosions= new ArrayListSS<Explosion>();
	protected ArrayListSS<ParticleEmitter> peS= new ArrayListSS<ParticleEmitter>();	
	
	protected ArrayListSS<ItemSpawner> itemSpawners = new ArrayListSS<ItemSpawner>();
	protected ArrayListSS<ItemHolder> items = new ArrayListSS<ItemHolder>();
	protected ArrayListSS<Player> players = new ArrayListSS<Player>();
	protected ArrayListSS<Bullet> bullets = new ArrayListSS<Bullet>();
	protected ArrayListSS<Team> teams = new ArrayListSS<Team>();
	transient protected ArrayListSS<Obstacle> obstacles = new ArrayListSS<Obstacle>();
	transient protected ArrayListSS<Water> waters = new ArrayListSS<Water>();
	protected ArrayListSS<ChatText> chatTexts = new ArrayListSS<ChatText>();
	float maxTimeToKeepChatTextEventsSeconds = 20f;
	int maxChatLinesToDisplay = 7;
	// For generating random numbers in the Controller thread. 
	//=====NOTE: that to be deterministic, 
	// random should be seeded with a pre-set number, otherwise server and client VM's will do different things.
	protected transient Random random = new Random();
	protected long seed = -Long.MAX_VALUE;
	transient BufferedImage backImage = null;

	public static final float WIDTH = 1000;
	public static final float HEIGHT = 2400;
	
	protected SpawnFlag securableFlag;

	public GameWorld() {
		// create obstacles:
		ArrayList<Point2D.Float> points = new ArrayList<Point2D.Float>();
		points.add(new Point2D.Float(-50, 0));
		points.add(new Point2D.Float(WIDTH + 50, 0));
		points.add(new Point2D.Float(WIDTH + 50, -50));
		points.add(new Point2D.Float(0, -50));
		obstacles.add(new Obstacle(this, points));
		points.clear();
		points.add(new Point2D.Float(-50, -50));
		points.add(new Point2D.Float(0, -50));
		points.add(new Point2D.Float(0, HEIGHT + 50));
		points.add(new Point2D.Float(-50, HEIGHT + 50));
		obstacles.add(new Obstacle(this, points));
		points.clear();
		points.add(new Point2D.Float(WIDTH+20, 1000));
		points.add(new Point2D.Float(820, 600));
		points.add(new Point2D.Float(740, 150));
		points.add(new Point2D.Float(WIDTH+20, 200));
		obstacles.add(new Obstacle(this, points));
		points.clear();
		points.add(new Point2D.Float(600, 100));
		points.add(new Point2D.Float(600, 150));
		points.add(new Point2D.Float(560, 200));
		points.add(new Point2D.Float(660, 180));
		points.add(new Point2D.Float(630, 160));
		obstacles.add(new Obstacle(this, points));
		points.clear();
		points.add(new Point2D.Float(-20, 700));
		points.add(new Point2D.Float(-20, 500));
		points.add(new Point2D.Float(300, 800));
		obstacles.add(new Obstacle(this, points));
		points.clear();
		points.add(new Point2D.Float(130, 900));
		points.add(new Point2D.Float(230, 900));
		points.add(new Point2D.Float(230, 1100));
		points.add(new Point2D.Float(130, 1100));
		obstacles.add(new Obstacle(this, points));
		points.clear();
		points.add(new Point2D.Float(260, 980));
		points.add(new Point2D.Float(325, 1025));
		points.add(new Point2D.Float(325, 1100));
		points.add(new Point2D.Float(260, 1100));
		obstacles.add(new Obstacle(this, points));
		points.clear();
		points.add(new Point2D.Float(230, 1130));
		points.add(new Point2D.Float(325, 1130));
		points.add(new Point2D.Float(325, 1200));
		points.add(new Point2D.Float(230, 1200));
		obstacles.add(new Obstacle(this, points));

		// barricades
		points.clear();
		points.add(new Point2D.Float(300, 950));
		points.add(new Point2D.Float(500, 1000));
		points.add(new Point2D.Float(490, 1010));
		points.add(new Point2D.Float(280, 960));
		obstacles.add(new Obstacle(this, points));
		// water in between the above and below barricades
		points.clear();
		points.add(new Point2D.Float(472, 1000));
		points.add(new Point2D.Float(500, 1000));
		points.add(new Point2D.Float(510, 998));
		points.add(new Point2D.Float(520, 1000));
		points.add(new Point2D.Float(537, 1000));
		points.add(new Point2D.Float(528, 992));
		points.add(new Point2D.Float(516, 989));
		points.add(new Point2D.Float(500, 988));
		waters.add(new Water(this, points));
		points.clear();
		points.add(new Point2D.Float(520, 1000));
		points.add(new Point2D.Float(695, 980));
		points.add(new Point2D.Float(690, 990));
		points.add(new Point2D.Float(527, 1010));
		obstacles.add(new Obstacle(this, points));
		// water in between the above and below barricades
		points.clear();
		points.add(new Point2D.Float(670, 985));
		points.add(new Point2D.Float(695, 980));
		points.add(new Point2D.Float(700, 975));
		points.add(new Point2D.Float(710, 975));
		points.add(new Point2D.Float(717, 980));
		points.add(new Point2D.Float(745, 1000));
		points.add(new Point2D.Float(730, 980));
		points.add(new Point2D.Float(720, 970));
		points.add(new Point2D.Float(705, 965));
		points.add(new Point2D.Float(690, 970));
		waters.add(new Water(this, points));
		points.clear();
		points.add(new Point2D.Float(717, 980));
		points.add(new Point2D.Float(850, 1040));
		points.add(new Point2D.Float(830, 1050));
		points.add(new Point2D.Float(722, 992));
		obstacles.add(new Obstacle(this, points));

		// random obstacles to hide behind
		points.clear();
		points.add(new Point2D.Float(0, 0));
		points.add(new Point2D.Float(25, 0));
		points.add(new Point2D.Float(19, 32));
		KPolygon poly = new KPolygon(points.toArray(new Point2D.Float[0]));
		poly.rotate((float) Math.PI/6);
		poly.translateTo(350, 400);
		obstacles.add(new Obstacle(this, new KPolygon(poly)));
		poly.rotate((float) Math.PI/3);
		poly.translateTo(270, 590);
		obstacles.add(new Obstacle(this, new KPolygon(poly)));
		poly.rotate((float) Math.PI/3);
		poly.translateTo(200, 450);
		obstacles.add(new Obstacle(this, new KPolygon(poly)));
		poly.rotate((float) Math.PI/6);
		poly.translateTo(600, 600);
		obstacles.add(new Obstacle(this, new KPolygon(poly)));
		poly.rotate((float) Math.PI/6);
		poly.translateTo(700, 400);
		obstacles.add(new Obstacle(this, new KPolygon(poly)));
		poly.rotate((float) Math.PI/4);
		poly.translateTo(540, 340);
		obstacles.add(new Obstacle(this, new KPolygon(poly)));
		/*poly.rotate(1);
		poly.translateTo(450, 770);
		obstacles.add(new Obstacle(this, new KPolygon(poly)));
		poly.rotate(1);
		poly.translateTo(740, 780);
		obstacles.add(new Obstacle(this, new KPolygon(poly)));*/
		poly.scale(1.5f);
		poly.rotate(1);
		poly.translateTo(280, 130);
		obstacles.add(new Obstacle(this, new KPolygon(poly)));
		poly.scale(1.5f);
		poly.rotate(1);
		poly.translateTo(300, 300);
		obstacles.add(new Obstacle(this, new KPolygon(poly)));



		// obstacles around the securable flag in the middle
		points.clear();
		points.add(new Point2D.Float(0, 0));
		points.add(new Point2D.Float(40, 0));
		points.add(new Point2D.Float(40, 100));
		points.add(new Point2D.Float(0, 100));
		poly = new KPolygon(points.toArray(new Point2D.Float[0]));
		poly.rotate((float) Math.PI / 4f);
		float xOffset = -50;
		float yOffset = -80;
		poly.translateTo(WIDTH / 2f - xOffset, HEIGHT / 2f - yOffset);
		obstacles.add(new Obstacle(this, new KPolygon(poly)));
		poly.rotate((float) Math.PI / 2f);
		poly.translateTo(WIDTH / 2f - xOffset, HEIGHT / 2f - yOffset);
		obstacles.add(new Obstacle(this, new KPolygon(poly)));



		// copy and mirror all obstacles
		ArrayListSS<Obstacle> newObstacles = new ArrayListSS<Obstacle>();
		for (Obstacle obstacle : obstacles) {
			KPolygon mirrorPoly = new KPolygon(obstacle.getShape());
			mirrorPoly.rotate((float) Math.PI, new Point2D.Float(WIDTH / 2f, HEIGHT / 2f));
			//mirrorPoly.translate(0, 10);
			newObstacles.add(new Obstacle(this, mirrorPoly));
		}
		obstacles.addAll(newObstacles);
		// copy and mirror all waters
		ArrayListSS<Water> newWaters = new ArrayListSS<Water>();
		for (Water water : waters) {
			KPolygon mirrorPoly = new KPolygon(water.getShape());
			mirrorPoly.rotate((float) Math.PI, new Point2D.Float(WIDTH / 2f, HEIGHT / 2f));
			//mirrorPoly.translate(0, 10);
			newWaters.add(new Water(this, mirrorPoly));
		}
		waters.addAll(newWaters);

		// setup the teams:
		Team team1 = new Team(this, "BLUE", Color.BLUE);
		Team team2 = new Team(this, "RED", Color.RED);
		Team natureTeam = new Team(this, Team.NATURE_TEAM_NAME, Color.WHITE);
		getTeams().add(team1);
		getTeams().add(team2);
		getTeams().add(natureTeam);

		SpawnFlag spawnFlag = new SpawnFlag(this, WIDTH * 9 / 10f, 100f, false, team1);
		SpawnFlag spawnFlag2 = new SpawnFlag(this, WIDTH / 10f, HEIGHT - 100, false, team2);
		securableFlag = new SpawnFlag(this, WIDTH / 2f, HEIGHT / 2, true, natureTeam);

		
		ArrayListSS<Item> itemsToSpawn = new ArrayListSS<Item>();
		
		ArrayListSS<Point2D.Float> weaponSpawnPoints= new ArrayListSS<Point2D.Float>();
		
		weaponSpawnPoints.add(new Point2D.Float( 825, 125));
		weaponSpawnPoints.add(new Point2D.Float( WIDTH - 825, HEIGHT - 125));
		
		weaponSpawnPoints.add(new Point2D.Float( 125, 125));
		weaponSpawnPoints.add(new Point2D.Float( WIDTH - 125, HEIGHT - 125));

		weaponSpawnPoints.add(new Point2D.Float( 370, 870));
		weaponSpawnPoints.add(new Point2D.Float( WIDTH - 370, HEIGHT - 870));
		
		weaponSpawnPoints.add(new Point2D.Float( 175, 1150));
		weaponSpawnPoints.add(new Point2D.Float( WIDTH - 175, HEIGHT - 1150));
		
		for(Point2D.Float pt : weaponSpawnPoints)
		{
			itemsToSpawn = new ArrayListSS<Item>();
			
			itemsToSpawn.add(new ShotGun(this));
			itemsToSpawn.add(new FlameThrower(this));
			itemsToSpawn.add(new MachineGun(this));
			itemsToSpawn.add(new TranquilizerGun(this));
			itemsToSpawn.add(new RocketLauncher(this));
			itemsToSpawn.add(new SniperRifle(this));
			itemsToSpawn.add(new HomingGun(this));
			itemsToSpawn.add(new NailGun(this));	
			
			itemSpawners.add(new ItemSpawner(this, itemsToSpawn, pt.x, pt.y, 10));
		}

			

		itemsToSpawn = new ArrayListSS<Item>();
		
		itemsToSpawn.add(new GodlyArmor(this));
		itemsToSpawn.add(new HealthPack(this));
		itemsToSpawn.add(new HealthPack(this));
		itemsToSpawn.add(new InvisibilityShroud(this));
		itemSpawners.add(new ItemSpawner(this, itemsToSpawn, 650, 350, 15));
		itemsToSpawn = new ArrayListSS<Item>();
		itemsToSpawn.add(new GodlyArmor(this));
		itemsToSpawn.add(new HealthPack(this));
		itemsToSpawn.add(new HealthPack(this));
		itemsToSpawn.add(new InvisibilityShroud(this));
		itemSpawners.add(new ItemSpawner(this, itemsToSpawn, WIDTH - 650, HEIGHT - 350, 15));

		itemsToSpawn = new ArrayListSS<Item>();
		itemsToSpawn.add(new HealthPack(this));
		itemsToSpawn.add(new InvisibilityShroud(this));
		itemsToSpawn.add(new SpeedShoes(this));
		itemsToSpawn.add(new GodlyArmor(this));
		itemSpawners.add(new ItemSpawner(this, itemsToSpawn, 850, 950, 10));
		itemsToSpawn = new ArrayListSS<Item>();
		itemsToSpawn.add(new HealthPack(this));
		itemsToSpawn.add(new InvisibilityShroud(this));
		itemsToSpawn.add(new SpeedShoes(this));
		itemsToSpawn.add(new GodlyArmor(this));
		itemSpawners.add(new ItemSpawner(this, itemsToSpawn, WIDTH - 850, HEIGHT - 950, 10));

		team1.addSpawnFlag(spawnFlag);
		float flagCoord = 200;
		team1.setCapturableFlag(new CapturableFlag(this, team1, flagCoord, flagCoord));
		team2.addSpawnFlag(spawnFlag2);
		team2.setCapturableFlag(new CapturableFlag(this, team2, WIDTH - flagCoord, HEIGHT - flagCoord));

		natureTeam.addSpawnFlag(securableFlag);

		// add some bots:
		for (int i = 0; i < 4; i++){
			Player botPlayer = new Bot();
			botPlayer.setWorld(this);
			// add a random time to the bot's lastDeathTimeSeconds so that it spawns at a randomly
			this.incrementAndReSeedRandom();
			botPlayer.setLastDeathTimeSeconds(this.getTotalElapsedSeconds() + this.getRandom().nextFloat()*2);
			botPlayer.setName("bot_"+i);
			addPlayer(botPlayer);
			botPlayer.setDead(true);
		}
	}

	
	public void addPlayer(Player p, Team team) {
		players.add(p);
		p.setWorld(this);
		p.setTeam(team);
		team.getPlayers().add(p);
		//System.out.println(this.getClass().getSimpleName() + ": getTeams().get(0).getPlayers().size() == "+getTeams().get(0).getPlayers().size()+", getTeams().get(1).getPlayers().size() == "+getTeams().get(1).getPlayers().size());
		p.respawn();
	}

	public void addPlayer(Player p) {
		Team playersTeam = null;
		//System.out.println(this.getClass().getSimpleName() + ": getTeams().get(0).getPlayers().size() == "+getTeams().get(0).getPlayers().size()+", getTeams().get(1).getPlayers().size() == "+getTeams().get(1).getPlayers().size());
		if (getTeams().get(0).getPlayers().size() > getTeams().get(1).getPlayers().size()) {
			playersTeam = getTeams().get(1);
		} else {
			playersTeam = getTeams().get(0);
		}
		addPlayer(p, playersTeam);
	}

	// Note that this method can be called even when the player is already removed.
	public void removePlayer(Player p, double timeAtStartOfMoveSeconds) {
		//assert players.contains(p) : "p == " + p + ", players == " + players;
		players.remove(p);
		if (p.getCapturableFlag() != null) {
			p.getCapturableFlag().drop(timeAtStartOfMoveSeconds);
		}
		p.getTeam().getPlayers().remove(p);
	}

	public void addItem(ItemHolder item) {
		items.add(item);
	}

	@Override
	protected void doMaxTimeMove(double seconds, double timeAtStartOfMoveSeconds) {
		numDoMaxTimeMoves++;
		double timeAtEndOfMoveSeconds = timeAtStartOfMoveSeconds + seconds;
		for (int j = 0; j < getBullets().size(); j++) {
			getBullets().get(j).doMove(seconds, timeAtStartOfMoveSeconds);
		}
		for (int j = 0; j < getItems().size(); j++) {
			getItems().get(j).doMove(seconds, timeAtStartOfMoveSeconds);
		}
		for (int j = 0; j < getItemSpawners().size(); j++) {
			getItemSpawners().get(j).doMove(seconds, timeAtStartOfMoveSeconds);
		}
		for (int j = 0; j < getPlayers().size(); j++) {
			Player p= getPlayers().get(j);
			p.doMove(seconds, timeAtStartOfMoveSeconds);
			for(int k=0; k < getPlayers().get(j).messages.size(); k++)
			{
				// Note that for most players the viewPane will be null, only the player controlled by this VM will have a non-null viewPane.
				if(p.getViewPane()==null){ // System.out.println("VIEWPANE IS NULL");
				
				}
				else
				if(p.messages.get(k).getTimeStamp()+ p.getViewPane().maxTimeToKeepMessageTextEventsSeconds  // <-- Null here possible 12/23/2014. 9:53 P.M
						< getTotalElapsedSeconds() + getElapsedSeconds())
				{
					p.messages.remove(k);
					//k--;
				}	
			}
			assert getPlayers().get(j).getWorld() == this : getPlayers().get(j).getWorld() + ", " + this;
		}
		
		for (int j = 0; j < getTeams().size(); j++) {
			getTeams().get(j).doMove(seconds, timeAtStartOfMoveSeconds);
		}
		for(Explosion e : getExplosions())
		{
			e.Move((int)(seconds*1000));
			//hurt everybody, even friendly players
		}	
		for(ParticleEmitter p: getParticleEmitters())
		{
			p.MoveParticles();
		}
		
		
		// Note that dead bullets need to be eliminated after player.doMove method 
		// since when a bullet is fired by the player, bullet.doMove is called.
		for (int j = 0; j < getBullets().size(); j++) {
			if (getBullets().get(j).isDead()) {
				getBullets().remove(j);
				j--;
			}
		}
		for (int j = 0; j < getItems().size(); j++) {
			if (getItems().get(j).isDead()) {
				getItems().remove(j);
				j--;
			}
		}
		for (int i = 0; i < chatTexts.size(); i++) {
			if (chatTexts.get(i).getTimeStamp() + maxTimeToKeepChatTextEventsSeconds < this.getTotalElapsedSeconds() + this.getElapsedSeconds()) {
				chatTexts.remove(i);
				i--;
			}
		}
		assert playersSumToPlayersInTeams();
		assert teams.size() <= 3 : teams;
	 
	}

	public boolean playersSumToPlayersInTeams() {
		int numPlayersInTeams = 0;
		for (int i = 0; i < getTeams().size(); i++) {
			numPlayersInTeams += getTeams().get(i).getPlayers().size();
		}
		if (numPlayersInTeams != getPlayers().size()) {
			System.err.println(this.getClass().getSimpleName() + ": numPlayersInTeams == " + numPlayersInTeams + ", getPlayers().size() == " + getPlayers().size());
			return false;
		}
		return true;
	}

	public void render(ViewPane viewPane) {
		Graphics2D g = viewPane.getBackImageGraphics2D();

		AffineTransform oldTranform = g.getTransform();
		g.setTransform(viewPane.getOriginalTransform());
		if (backImage == null || viewPane.getWidth() != backImage.getWidth() || viewPane.getHeight() != backImage.getHeight()) {
			backImage = viewPane.getGraphicsConfiguration().createCompatibleImage(viewPane.getWidth(), viewPane.getHeight(), Transparency.OPAQUE);//new BufferedImage(viewPane.getWidth(), viewPane.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics backImageGraphics = backImage.getGraphics();
			backImageGraphics.setColor(Color.LIGHT_GRAY);
			backImageGraphics.fillRect(0, 0, backImage.getWidth(), backImage.getHeight());
			System.out.println(this.getClass().getSimpleName() + ": initialising backImage for quick background painting");
		}
		g.drawImage(backImage, 0, 0, null);
		g.setTransform(oldTranform);
		/*
		if (backImage == null) {
		backImage = viewPane.getGraphicsConfiguration().createCompatibleImage(2*bufferedImage.getWidth(), 2*bufferedImage.getHeight(), Transparency.OPAQUE);//new BufferedImage(viewPane.getWidth(), viewPane.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics backImageGraphics = backImage.getGraphics();
		backImageGraphics.drawImage(bufferedImage, 0, 0, null);
		backImageGraphics.drawImage(bufferedImage, bufferedImage.getWidth(), bufferedImage.getHeight(), null);
		backImageGraphics.drawImage(bufferedImage, bufferedImage.getWidth(), 0, null);
		backImageGraphics.drawImage(bufferedImage, 0, bufferedImage.getHeight(), null);
		System.out.println(this.getClass().getSimpleName() + ": initializing backImage for quick background painting");
		}
		g.drawImage(backImage, -backImage.getWidth()/2, -backImage.getHeight()/2, null);
		 */
		for (int i = 0; i < getWaters().size(); i++) {
			getWaters().get(i).render(viewPane);
		}
		for (int i = 0; i < getObstacles().size(); i++) {
			getObstacles().get(i).render(viewPane);
		}
		for (int i = 0; i < getTeams().size(); i++) {
			getTeams().get(i).render(viewPane);
		}
		for (int i = 0; i < getItems().size(); i++) {
			getItems().get(i).render(viewPane);
		}
		for (int i = 0; i < getPlayers().size(); i++) {
			getPlayers().get(i).render(viewPane);
		}
		for (int i = 0; i < getBullets().size(); i++) {
			getBullets().get(i).render(viewPane);
		}
		for (int i = 0; i < getParticleEmitters().size(); i++) {
			getParticleEmitters().get(i).DrawParticles(g);
		}
		for (int i = 0; i < getExplosions().size(); i++) {
			getExplosions().get(i).Draw(g);
		}

		oldTranform = g.getTransform();
		g.setTransform(viewPane.getOriginalTransform());
		int textX = 20;
		int lineHeight = 15;
		int textY = viewPane.getHeight() - lineHeight;
		
		int numMessageDisplayed = 0;
		// Only display the last 'maxChatLinesToDisplay' lines.
		for (int i = chatTexts.size() - 1; i >= 0; i--) {
			if (chatTexts.get(i).isAlliesOnly() && chatTexts.get(i).getPlayer().getTeam() != viewPane.getPlayer().getTeam()) {
				// skip this line because the viewPane's player isn't meant to see this message.
				i--;
				continue;
			}
			
			if (chatTexts.get(i).isAlliesOnly()){
				g.setFont(g.getFont().deriveFont(Font.ITALIC));
			}
			
			//draws player's name in color
			g.setColor(chatTexts.get(i).getPlayer().getTeam().getColor());
			g.drawString(chatTexts.get(i).getPlayer().getName() + ": ", textX, textY);
			
			//super awesome way to compute pixel length of string
			int shift= SwingUtilities.computeStringWidth(g.getFontMetrics(), chatTexts.get(i).getPlayer().getName() + ": ");

			
			String message= chatTexts.get(i).getText();
			// a problem if one name is part of another name, ex. BOB and BOB1
			// ---> solution: sort player names by short to long, display long names last?
			
			
			ArrayList<Player> playersNames= new ArrayList<Player>();
			
			for(int p=0; p<getPlayers().size(); p++)
			{
				String name= getPlayers().get(p).getName();
				int endmark= message.length()-name.length()+1;
				if(endmark>0)
				{					
					for (int j = 0; j < endmark; j++) 
					{						
						if(message.substring(j, j+name.length()).equals(name))
							playersNames.add(getPlayers().get(p));
					}				
				}
			}
			
			
			g.setColor(Color.black);
			g.drawString(message, textX + shift, textY);
			
			for(Player k : playersNames)
			{
				g.setColor(k.getTeam().getColor());
				int m= message.indexOf(k.getName());
				int locShift= SwingUtilities.computeStringWidth(g.getFontMetrics(), message.substring(0, m));
				g.drawString(k.getName(), textX+ locShift+ shift, textY);
			}
			
			g.setFont(g.getFont().deriveFont(Font.PLAIN));
			textY -= lineHeight;
			numMessageDisplayed++;
			if (numMessageDisplayed >= this.maxChatLinesToDisplay) {
				break;
			}
			
		}
		g.setTransform(oldTranform);
	}
	
	public ArrayListSS<Player> getPlayers() {
		return players;
	}

	public boolean checkPlayersHaveDiffSSCodes() {
		boolean ordered = true;
		for (int i = 0; i < players.size(); i++) {
			Player player = players.get(i);
			if (players.indexOf(player) != players.lastIndexOf(player)) {
				System.err.println(this.getClass().getSimpleName() + ": players.indexOf(player) == " + players.indexOf(player) + ", list.lastIndexOf(e) == " + players.lastIndexOf(player) + ", players.get(" + i + ") == " + players.get(i) + " players.size() == " + players.size());
				ordered = false;
			}
		}
		return ordered;
	}

	public void addChatText(ChatText e) {//Player player, double timeStamp, String text){
		
		//=========================== [ cheats ]
		
		if (e.getText().toLowerCase().equals("give me speed")) { // speed
			e.getPlayer().setSpeedMultiplier(2.5f, e.getTimeStamp(), 90);
		}
		if(e.getText().toLowerCase().equals("give me allguns")) { // all guns
			e.getPlayer().assignGun(new NailGun(this), e.getTimeStamp());
			e.getPlayer().assignGun(new MachineGun(this), e.getTimeStamp());
			e.getPlayer().assignGun(new ShotGun(this), e.getTimeStamp());
			e.getPlayer().assignGun(new TranquilizerGun(this), e.getTimeStamp());
			e.getPlayer().assignGun(new RocketLauncher(this), e.getTimeStamp());
			e.getPlayer().assignGun(new SniperRifle(this), e.getTimeStamp());
			e.getPlayer().assignGun(new FlameThrower(this), e.getTimeStamp());
			e.getPlayer().assignGun(new HomingGun(this), e.getTimeStamp());
			e.getPlayer().assignGun(new Pistol(this), e.getTimeStamp());
		}
		if(e.getText().toLowerCase().equals("give me armor")) { // invincibility
			e.getPlayer().setArmored(e.getTimeStamp(), 90);
		}
		if(e.getText().equals("give me stealth")) { // invisibility
			e.getPlayer().setInvisible(e.getTimeStamp(), 90);
		}
		
		//=================================================================
		
		
		
		if (e.getText().toLowerCase().startsWith("numbots")) {
			// only grabs the last digit, so there's a max of nine bots that can be requested.
			String numBotsString = e.getText().substring(e.getText().length()-1, e.getText().length());
			int numBots = -1;
			try{
				numBots = Integer.parseInt(numBotsString);
			}catch(NumberFormatException ex){
			}
			if (numBots != -1){
				for (int i = 0; i < getPlayers().size(); i++){
					if (getPlayers().get(i) instanceof Bot){
						this.removePlayer(getPlayers().get(i), e.getTimeStamp());
						i--;
					}
				}
				for (int i = 0; i < numBots; i++){
					Player botPlayer = new Bot();
					botPlayer.setWorld(this);
					botPlayer.setName("bot_"+i);
					addPlayer(botPlayer);
				}
			}
			
			
		}
		chatTexts.add(e);
		Collections.sort(chatTexts);
	}
	

	public ArrayListSS<Bullet> getBullets() {
		return bullets;
	}

	public void setBullets(ArrayListSS<Bullet> bullets) {
		this.bullets = bullets;
	}

	public ArrayListSS<ItemHolder> getItems() {
		return items;
	}

	public void setItems(ArrayListSS<ItemHolder> items) {
		this.items = items;
	}

	public ArrayListSS<Team> getTeams() {
		return teams;
	}

	public void setTeams(ArrayListSS<Team> teams) {
		this.teams = teams;
	}

	public ArrayListSS<Obstacle> getObstacles() {
		return obstacles;
	}

	public void setObstacles(ArrayListSS<Obstacle> obstacles) {
		this.obstacles = obstacles;
	}

	public Random getRandom() {
		return random;
	}
	public ArrayListSS<ParticleEmitter> getParticleEmitters()
	{
		return peS;
	}
	public ArrayListSS<Explosion> getExplosions()
	{
		return explosions;
	}
	public ArrayListSS<Water> getWaters() {
		return waters;
	}

	public ArrayListSS<ItemSpawner> getItemSpawners() {
		return itemSpawners;
	}

	public void setItemSpawners(ArrayListSS<ItemSpawner> itemSpawners) {
		this.itemSpawners = itemSpawners;
	}

	public void incrementAndReSeedRandom() {
		seed += 1513;
		getRandom().setSeed(seed);
	}
	
	@Override
	public void writeSS(SSObjectOutputStream out) throws IOException {		// this is the method that you over-ride if you want custom serialization
		super.writeSS(out);
		assert checkPlayersHaveDiffSSCodes() : "players == " + getPlayers();
	}

	@Override
	public void readSS(SSObjectInputStream in) throws java.io.IOException {	// this is the method that you over-ride if you want custom serialization
		super.readSS(in);
		assert checkPlayersHaveDiffSSCodes() : "players == " + getPlayers();
	}

	public static float getWidth() {
		return WIDTH;
	}

	public static float getHeight() {
		return HEIGHT;
	}

	public

	SpawnFlag getSecurableFlag() {
		return securableFlag;
	}
}
