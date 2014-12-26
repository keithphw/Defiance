/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.shooter;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import sydneyengine.shooter.Gun.FlameThrower;
import sydneyengine.shooter.Gun.Gun;
import sydneyengine.shooter.Gun.HomingGun;
import sydneyengine.shooter.Gun.MachineGun;
import sydneyengine.shooter.Gun.NailGun;
import sydneyengine.shooter.Gun.Pistol;
import sydneyengine.shooter.Gun.RocketLauncher;
import sydneyengine.shooter.Gun.ShotGun;
import sydneyengine.shooter.Gun.SniperRifle;
import sydneyengine.shooter.Gun.TranquilizerGun;

/**
 *
 * @author Phillip
 */
public class Bot extends Player{
	final static int NORTH_TEAM = 1;
	final static int SOUTH_TEAM = 2;
	int pathNum;
	int pathPointNum;
	
	boolean strafeLeft; 
	double secondsSinceLastStrafeChange = 0;
	double strafeShiftFreq = .7;
	
	static ArrayList<ArrayList<Point2D.Float>> northTeamPaths = new ArrayList<ArrayList<Point2D.Float>>();
	static ArrayList<ArrayList<Point2D.Float>> southTeamPaths = new ArrayList<ArrayList<Point2D.Float>>();
	static{
		// this is bullsh*t, should use a pathfinding library instead
		ArrayList<Point2D.Float> northTeamPathToSecurableFlag = new ArrayList<Point2D.Float>();
		northTeamPathToSecurableFlag.add(new Point2D.Float(700,80));
		northTeamPathToSecurableFlag.add(new Point2D.Float(720,300));
		northTeamPathToSecurableFlag.add(new Point2D.Float(775,600));
		northTeamPathToSecurableFlag.add(new Point2D.Float(930,1120));
		northTeamPathToSecurableFlag.add(new Point2D.Float(497,1197));
		ArrayList<Point2D.Float> northTeamPathToSecurableFlag2 = new ArrayList<Point2D.Float>();
		northTeamPathToSecurableFlag2.add(new Point2D.Float(600,40));
		northTeamPathToSecurableFlag2.add(new Point2D.Float(250,60));
		northTeamPathToSecurableFlag2.add(new Point2D.Float(100,450));
		northTeamPathToSecurableFlag2.add(new Point2D.Float(120,600));
		northTeamPathToSecurableFlag2.add(new Point2D.Float(270,720));
		northTeamPathToSecurableFlag2.add(new Point2D.Float(345,780));
		northTeamPathToSecurableFlag2.add(new Point2D.Float(320,840));
		northTeamPathToSecurableFlag2.add(new Point2D.Float(70,850));
		northTeamPathToSecurableFlag2.add(new Point2D.Float(100,1120));
		northTeamPathToSecurableFlag2.add(new Point2D.Float(100,1120));
		northTeamPathToSecurableFlag2.add(new Point2D.Float(230,1250));
		northTeamPathToSecurableFlag2.add(new Point2D.Float(503,1203));
		
		ArrayList<Point2D.Float> northTeamPathToCapturableFlag = new ArrayList<Point2D.Float>();
		northTeamPathToCapturableFlag.add(new Point2D.Float(510,1190));
		northTeamPathToCapturableFlag.add(new Point2D.Float(600,1150));
		northTeamPathToCapturableFlag.add(new Point2D.Float(800,1160));
		northTeamPathToCapturableFlag.add(new Point2D.Float(930,1270));
		northTeamPathToCapturableFlag.add(new Point2D.Float(920,1520));
		northTeamPathToCapturableFlag.add(new Point2D.Float(720,1530));
		northTeamPathToCapturableFlag.add(new Point2D.Float(650,1600));
		northTeamPathToCapturableFlag.add(new Point2D.Float(670,1620));
		northTeamPathToCapturableFlag.add(new Point2D.Float(830,1850));
		northTeamPathToCapturableFlag.add(new Point2D.Float(700,1920));
		northTeamPathToCapturableFlag.add(new Point2D.Float(810,2080));
		northTeamPathToCapturableFlag.add(new Point2D.Float(800,2200));
		ArrayList<Point2D.Float> northTeamPathToCapturableFlag2 = new ArrayList<Point2D.Float>();
		northTeamPathToCapturableFlag2.add(new Point2D.Float(490,1210));
		northTeamPathToCapturableFlag2.add(new Point2D.Float(180,1250));
		northTeamPathToCapturableFlag2.add(new Point2D.Float(70,1350));
		northTeamPathToCapturableFlag2.add(new Point2D.Float(400,1670));
		northTeamPathToCapturableFlag2.add(new Point2D.Float(500,2000));
		northTeamPathToCapturableFlag2.add(new Point2D.Float(630,2200));
		northTeamPathToCapturableFlag2.add(new Point2D.Float(800,2200));
		
		ArrayList<Point2D.Float> northTeamPathToCapturableFlagReversed = new ArrayList<Point2D.Float>();
		for (int i = northTeamPathToCapturableFlag.size()-1; i >= 0; i--){
			northTeamPathToCapturableFlagReversed.add(northTeamPathToCapturableFlag.get(i));
		}
		
		ArrayList<Point2D.Float> northTeamPathToCapturableFlag2Reversed = new ArrayList<Point2D.Float>();
		for (int i = northTeamPathToCapturableFlag2.size()-1; i >= 0; i--){
			northTeamPathToCapturableFlag2Reversed.add(northTeamPathToCapturableFlag2.get(i));
		}
		
		northTeamPaths.add(northTeamPathToSecurableFlag);
		northTeamPaths.add(northTeamPathToSecurableFlag2);
		northTeamPaths.add(northTeamPathToCapturableFlag);
		northTeamPaths.add(northTeamPathToCapturableFlag2);
		northTeamPaths.add(northTeamPathToCapturableFlagReversed);
		northTeamPaths.add(northTeamPathToCapturableFlag2Reversed);
		
		for (int i = 0; i < northTeamPaths.size(); i++){
			ArrayList<Point2D.Float> newSouthTeamPath = new ArrayList<Point2D.Float>();
			for (int j = 0; j < northTeamPaths.get(i).size(); j++){
				newSouthTeamPath.add(new Point2D.Float(GameWorld.getWidth()-northTeamPaths.get(i).get(j).x, GameWorld.getHeight()-northTeamPaths.get(i).get(j).y));
			}
			southTeamPaths.add(newSouthTeamPath);
		}
	}
	
	public Bot(){
		super();
		numTimeStops = 0;
		timeStopMultiple = 0.05;
		
		if(world!=null)
		this.strafeLeft = world.getRandom().nextBoolean();
	}
	
	@Override
	public void addKill(){
		super.addKill();
		world.incrementAndReSeedRandom();
		double rand= world.getRandom().nextDouble();
		String s="";
		if(rand<.02) s = "I am the oxygen thief!";		
		else if(rand<.4) s = "That was easy.";
		else if(rand<.06) s= ":) Take that!";
		else if(rand<.08) s= "Hee-hee";
		else if(rand<.1) s= "Gotcha!";
		else if(rand<.12) s= "I love the " + this.getGun().getName();
		else if(rand<.14) s= "Has anyone gotten the " + this.getGun().getName() + " yet?";
		else if(rand<.16) s= "Lo and Behold!";
		else if(rand<.18) s= "Dethroned!";
		else if(rand<.20) s= "Resistance is futile";
		else if(rand<.22) s= "Bring it on";
		else if(rand<.24) s= ":P";
		else if(rand<.26) s= "Terminator unleashed";
		
		world.incrementAndReSeedRandom();
		if(world.getRandom().nextDouble()<.7) s.toLowerCase();
		
		if(!s.equals(""))
		world.addChatText(new ChatText(this, s, false, world.getTotalElapsedSeconds()));
	}
	@Override
	public void addDeath(){
		super.addDeath();
		world.incrementAndReSeedRandom();
		double rand= world.getRandom().nextDouble();
		String s="";
		if(rand<.02) s = "no no no!";		
		else if(rand<.04) s = "So close!";
		else if(rand<.06) s= "Next time";
		else if(rand<.08) s= "Challenge accepted";
		else if(rand<.10) s= "That wasn't how I thought I would get killed.";
		else if(rand<.12) s= "Revenge time!";
		else if(rand<.14) s= "AAAARRRRRGGGGGHHHH!";
		else if(rand<.16) s= "Someone chat!";
		else if(rand<.18) s= "reincarnation takes 7 seconds";
		else if(rand<.20) s= "Gonna do better";
		else if(rand<.22) s= "Aw, common, I just got started!";
		else if(rand<.24) s= "Wait, what just happened?";
		else if(rand<.26) s= "That's too bad.";
		
		world.incrementAndReSeedRandom();
		if(world.getRandom().nextDouble()<.7) s.toLowerCase();
		
		if(!s.equals(""))
		world.addChatText(new ChatText(this, s, false, world.getTotalElapsedSeconds()));
	}
	
	@Override
	public void respawn() {
		super.respawn();
		world.incrementAndReSeedRandom();
		this.strafeLeft = world.getRandom().nextBoolean();
		
		if (getTeam().getSpawnFlags().size() > 1){
			world.incrementAndReSeedRandom();
			// the middle base is captured, so no need to run over to it and can instead go straight for the capturable flag.
			pathNum = world.getRandom().nextInt(2) + 2;
		}else{
			world.incrementAndReSeedRandom();
			pathNum = world.getRandom().nextInt(2);
		}
		pathPointNum = 0;
		Gun botGun = null;
		int numGuns = 8;
		world.incrementAndReSeedRandom();
		int randomInt = world.getRandom().nextInt(numGuns);
		// give bots starting weapon
		if (randomInt == 0){
			botGun = new FlameThrower(world);
		}else if (randomInt == 1){
			botGun = new ShotGun(world);
		}else if (randomInt == 2){
			botGun = new MachineGun(world);
		}else if (randomInt == 3){
			botGun = new SniperRifle(world);
		}else if (randomInt == 4){
			botGun = new TranquilizerGun(world);
		}else if (randomInt == 5){
			botGun = new RocketLauncher(world);
		}else if (randomInt == 6){
			botGun = new HomingGun(world);
		}else if (randomInt == 7) {
			botGun = new NailGun(world);
		}
		addAndUseGun(new Pistol(world)); //all bots have secondary armanent
		
		if(botGun!=null)
		addAndUseGun(botGun); //primary weapon
		
		
		//this.setSpeedMultiplier(2, world.getTotalElapsedSeconds(), Double.MAX_VALUE);
	}
	
	public Player getClosestEnemy(){
		if (this.getGun().getTotalAmmo() == 0){
			return null;
		}
		Player closestPlayer = null;
		double closestDist = Double.MAX_VALUE;
		for (Player player : world.getPlayers()){
			if (player.isDead() // don't kill already dead players
					|| (player.isInvisible() && player.getGun().isFiring() == false) // can't see = can't hit
					|| player.getTeam() == getTeam() // no intentional friendly fire
					|| player.isArmored()) // bots won't fire at invincible person - waste of bullets
			{
				continue;
			}
			if (Point2D.distance(player.getX(), player.getY(), getX(), getY()) < closestDist){
				if (isPlayerShootable(player)){
					closestDist = Point2D.distance(player.getX(), player.getY(), getX(), getY());
					closestPlayer = player;
				}
			}
		}
		if(closestDist<=120 && this.getGun().getName().equals("RocketLauncher") && this.getGuns().size()>1)
		{
			// can't use rockets at close range b/c will hurt yourself
			this.cycleGunsForwardBy(1, world.getEventTimeStampNowSeconds());
		}
		return closestPlayer;
	}
	
	protected boolean isPlayerShootable(Player p){
		if (Point2D.distance(p.getX(), p.getY(), getX(), getY()) > getGun().getRangeForBotAiming()){
			return false;
		}
		ArrayList<Obstacle> obstacles = getWorld().getObstacles();
		for (int i = 0; i < obstacles.size(); i++) {
			Obstacle obstacle = obstacles.get(i);
			KPolygon shape = obstacle.getShape();
//			if (Point2D.Float.distance(x, y, shape.getCentre().x, shape.getCentre().y) > shape.getCircularBound() + radius) {
//				continue;
//			}
			Point2D.Float[] points = shape.getPoints();
			for (int j = 0; j < points.length; j++) {
				int jPlus = (j + 1 == points.length ? 0 : j + 1);
				if (Line2D.linesIntersect(getX(), getY(), p.getX(), p.getY(), points[j].x, points[j].y, points[jPlus].x, points[jPlus].y)) {
					return false;
				}
			}
		}
		return true;
	}
	
	
	double timeStopMultiple;
	int numTimeStops;
	
	@Override
	public void doMove(double seconds, double timeAtStartOfMoveSeconds) {
		assert seconds >= 0 : seconds;
		double nextTimeStop = timeStopMultiple * numTimeStops;
		double timeAtEndOfMoveSeconds = timeAtStartOfMoveSeconds + seconds;
		//System.out.println(this.getClass().getSimpleName() + ": timeAtStartOfMoveSeconds == "+timeAtStartOfMoveSeconds+", timeAtEndOfMoveSeconds == "+timeAtEndOfMoveSeconds+", seconds == "+seconds+", nextTimeStop == "+nextTimeStop);
		while (nextTimeStop <= timeAtEndOfMoveSeconds) {
			double reducedSeconds = nextTimeStop - timeAtStartOfMoveSeconds;
			if (reducedSeconds < 0){
				reducedSeconds = 0;
			}
			if (reducedSeconds != 0){
				//System.out.println(this.getClass().getSimpleName() + ": doMove2(reducedSeconds, timeAtStartOfMoveSeconds), reducedSeconds == "+reducedSeconds+", timeAtStartOfMoveSeconds == "+timeAtStartOfMoveSeconds);
				doMove2(reducedSeconds, timeAtStartOfMoveSeconds);
			}
			nowAtTimeStop(nextTimeStop);
			timeAtStartOfMoveSeconds = nextTimeStop;
			if (timeAtEndOfMoveSeconds == nextTimeStop){
				seconds = 0;
			}else{
				seconds -= reducedSeconds;
			}
			numTimeStops++;
			nextTimeStop = timeStopMultiple * numTimeStops;
		}
		if (seconds > 0){
			//System.out.println(this.getClass().getSimpleName() + ": doMove2(seconds, timeAtStartOfMoveSeconds), seconds == "+seconds+", timeAtStartOfMoveSeconds == "+timeAtStartOfMoveSeconds);
			doMove2(seconds, timeAtStartOfMoveSeconds);
		}
	}
	
	static int minDist = 20;
	public boolean hasBetterWeaponThanPistol()
	{
		for(int i=0; i<this.getGuns().size(); i++)
		{
			if(!guns.get(i).getName().equals("Pistol") && guns.get(i).getTotalAmmo()>0)
				return true;
		}
		return false;
	}
	protected void nowAtTimeStop(double timeAtStartOfMoveSeconds){
		// only act if humans are watching!!
		if (isHumanPlayerInGame() == true){
			//always switch from pistol --> might cause eternal shifting
			if (this.getGun().getTotalAmmo() == 0 
					|| this.getGun().getName().equals("Pistol") && this.hasBetterWeaponThanPistol()) {
				this.cycleGunsForwardBy(1, timeAtStartOfMoveSeconds);
			}
			Player closestEnemy = getClosestEnemy();
			if (closestEnemy != null 
					//&& this.getGun().ammoInCurrentClip > 0
				)
			{ //don't engage if no ammo
				float targetForMoveX = closestEnemy.getX() - getX();
				float targetForMoveY = closestEnemy.getY() - getY();
				// even bots know how to move a mouse
				this.setMouseTargetX(targetForMoveX);
				this.setMouseTargetY(targetForMoveY);
				this.getGun().startFiring(timeAtStartOfMoveSeconds);
				this.setUp(false);
				
				/*
				if(this.secondsSinceLastStrafeChange + this.strafeShiftFreq < timeAtStartOfMoveSeconds
						//&& this.getGun().isFiring()
					)
				{
					secondsSinceLastStrafeChange= timeAtStartOfMoveSeconds;
					strafeLeft = !strafeLeft;
					
				}
				if(strafeLeft) { this.setLeft(true); this.setRight(false); this.setUp(false); this.setDown(false);}
				else { this.setLeft(false); this.setRight(true); this.setUp(false); this.setDown(false);}
				*/
			}
			else
			{
				ArrayList<Point2D.Float> currentPath = getTeamPaths().get(pathNum);
				Point2D.Float targetPointForMove = currentPath.get(pathPointNum);
				boolean doNothingUntilFlagSecured = false;
				while (targetPointForMove.distance(getX(), getY()) < minDist)
				{
					pathPointNum++;
					if (pathPointNum >= currentPath.size()){
						pathPointNum = 0;
						if (pathNum == 0 || pathNum == 1 || pathNum == 4 || pathNum == 5){
							world.incrementAndReSeedRandom();
							pathNum = world.getRandom().nextInt(2) + 2;
							currentPath = getTeamPaths().get(pathNum);
						}
						else if (pathNum == 2 || pathNum == 3)
						{
							world.incrementAndReSeedRandom();
							pathNum = world.getRandom().nextInt(2)+4;
							currentPath = getTeamPaths().get(pathNum);
						}
					}
					targetPointForMove = currentPath.get(pathPointNum);
				}
				if (getWorld().getSecurableFlag().getTeam() != this.getTeam() && 
						Point2D.distance(getWorld().getSecurableFlag().getX(), getWorld().getSecurableFlag().getY(), getX(), getY()) <
						getWorld().getSecurableFlag().radius)
				{
					// the bot is at the middle capturable base, but its team hasn't captured it yet, so bot should wait until the base is secured.
					this.setUp(false);
					this.getGun().stopFiring();
				}
				else
				{
					float targetForMoveX = targetPointForMove.x - getX();
					float targetForMoveY = targetPointForMove.y - getY();
					this.setMouseTargetX(targetForMoveX);
					this.setMouseTargetY(targetForMoveY);
					this.setUp(true);
					this.getGun().stopFiring();
				}
			}
		}
	}
	
	public void doMove2(double seconds, double timeAtStartOfMoveSeconds) {
		assert seconds >= 0 : seconds;
		double timeAtEndOfMoveSeconds = timeAtStartOfMoveSeconds + seconds;
		super.doMove(seconds, timeAtStartOfMoveSeconds);
		//System.out.println(this.getClass().getSimpleName() + ": doMove2(seconds, timeAtStartOfMoveSeconds), seconds == "+seconds+", timeAtStartOfMoveSeconds == "+timeAtStartOfMoveSeconds);
	}
	
	
	// bots special move method based on direction facing, overriding the player move methods
	@Override
	public void doMoveOfTurretAndBody(double seconds, double timeAtStartOfMoveSeconds) {
		assert Double.isNaN(x) == false;
		assert seconds >= 0 : seconds;

		super.PowerupCountdown(timeAtStartOfMoveSeconds);
		super.updateTurning(seconds);
		
		//getGun().rotateTurret(seconds, timeAtStartOfMoveSeconds);

		oldX = x;
		oldY = y;
		float xCoordToWorkOutAngle = 0;
		float yCoordToWorkOutAngle = 0;
		
		
		
		
		if (!dead) {
			if (left) {
				xCoordToWorkOutAngle -= 1;
			}
			if (right) {
				xCoordToWorkOutAngle += 1;
			}
			if (up) {
				yCoordToWorkOutAngle -= 1;
			}
			if (down) {
				yCoordToWorkOutAngle += 1;
			}
		}
		speedX = 0f;
		speedY = 0f;
		if (xCoordToWorkOutAngle != 0 || yCoordToWorkOutAngle != 0) {
			float dirAngle = calcAngle(xCoordToWorkOutAngle, yCoordToWorkOutAngle);

			dirAngle += Math.PI / 2; // 90 degrees is added on since we want an angle of zero to face up, not to the right (which it is without adding 90 degrees)
			
			//---- very wacky movement original based on facing angle, but very intuitive/ easy to develop AI
			speedX = (float) Math.cos(dirAngle + getAngle()) * getCurrentSpeed();
			speedY = (float) Math.sin(dirAngle + getAngle()) * getCurrentSpeed();

		}
		

		super.checkCollisions(seconds);

	}
	
	
	
	public boolean isHumanPlayerInGame(){
		for (Player player : world.getPlayers()){
			if (player instanceof Bot == false){
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<ArrayList<Point2D.Float>> getTeamPaths(){
		if (getTeam().getName().equals("BLUE")){
			return northTeamPaths;
		}else{
			return southTeamPaths;
		}
	}
}
