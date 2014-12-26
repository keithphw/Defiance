package sydneyengine.shooter;

import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import sydneyengine.shooter.GameWorld;
import sydneyengine.shooter.Player;
import sydneyengine.shooter.SpawnFlag;
import sydneyengine.shooter.Team;
import sydneyengine.shooter.ViewPane;
import sydneyengine.superserializable.SSAdapter;

public class CapturableFlag extends SSAdapter {

	GameWorld world;
	Team team = null;
	float radius = 20;
	//transient Ellipse2D.Float flagAreaCircle = new Ellipse2D.Float(0, 0, radius * 2f, radius * 2f);
	Player enemyThief = null;	// if not null, this enemy player has the flag.
	boolean dropped = false;
	float dropPointX;
	float dropPointY;
	double dropTime = 0;
	static float dropTimeBeforeReplace = 10;	// seconds
	float homeX;
	float homeY;

	public CapturableFlag() {
	}

	public CapturableFlag(GameWorld world, Team team, float homeX, float homeY) {
		this.world = world;
		this.team = team;
		this.homeX = homeX;
		this.homeY = homeY;
		//flagAreaCircle = new Ellipse2D.Float((getCurrentX() - radius), (getCurrentY() - radius), 2f * radius, 2f * radius);
	}
	
	public void drop(double dropTime){
		if (enemyThief != null) {
			dropped = true;
			enemyThief.setCapturableFlag(null);
			dropPointX = enemyThief.getX();
			dropPointY = enemyThief.getY();
			this.dropTime = dropTime;
			enemyThief = null;
			//System.out.println("CapturableFlag: flag dropped by: " + enemyThief.toString());
		}
	}
	public void doMove(double seconds, double timeAtStartOfMoveSeconds) {
		if (enemyThief != null) {
			if (enemyThief.getTeam().getCapturableFlag().isAtBase() == true){
				//SpawnFlag enemyBaseFlag = enemyThief.getTeam().getSpawnFlagBase();
				for (SpawnFlag enemyBaseFlag : enemyThief.getTeam().getSpawnFlags()){
					float distance = (float) Point2D.distance(enemyBaseFlag.getX(), enemyBaseFlag.getY(), enemyThief.getX(), enemyThief.getY());
					if (distance < enemyThief.getR() + enemyBaseFlag.radius) {
						enemyThief.addFlagCapture();
						//System.out.println("CapturableFlag: flag successfully captured by: " + enemyThief.toString()+".... "+enemyBaseFlag.getX()+", "+enemyBaseFlag.getY()+", "+enemyThief.getX()+", "+enemyThief.getY());
						enemyThief.setCapturableFlag(null);
						enemyThief = null;
						break;
					}
				}
			}
			
		} else if (dropped == true) {
			if (seconds + timeAtStartOfMoveSeconds > dropTime + dropTimeBeforeReplace) {
				dropped = false;
				//System.out.println("CapturableFlag: flag returned after being dropped");
			} else {
				for (Player player : world.getPlayers()) {
					if (player.isDead() == true) {
						continue;
					}
					float distance = (float) Point2D.distance(dropPointX, dropPointY, player.getX(), player.getY());
					if (distance < player.getR() + radius) {
						if (player.getTeam() == getTeam()) {
							player.addFlagReturn();
							dropped = false;
							//System.out.println("CapturableFlag: flag secured from after dropped");
						} else {
							enemyThief = player;
							enemyThief.setCapturableFlag(this);
							dropped = false;
							//System.out.println("CapturableFlag: kidnapped by: " + player + ", after being dropped!");
						}
					}
				}
			}
		} else {
			for (Player player : world.getPlayers()) {
				if (player.isDead() == true || player.getTeam() == getTeam()) {
					continue;
				}
				float distance = (float) Point2D.distance(homeX, homeY, player.getX(), player.getY());
				if (distance < player.getR() + radius) {
					enemyThief = player;
					player.setCapturableFlag(this);
					/*String m1 = "James_Bond.mid";
					String m2 = "Jaws.mid";
					BigFrame.playMusic((Math.random() > 0.5 ? m1 : m2));*/
					//System.out.println("CapturableFlag: kidnapped by: " + player);
				}
			}
		}
		assert dropped == true || enemyThief != null || getCurrentX() == homeX : dropped+", "+enemyThief+", "+getCurrentX()+", "+homeX;
	}

	public void render(ViewPane viewPane) {
		Graphics2D g = viewPane.getBackImageGraphics2D();
		g.setColor(team.getColor());
		/*
		if (dropped){
			g.setColor(Color.MAGENTA);
		}
		else if (enemyThief == null){
			g.setColor(Color.GREEN);
		}else{
			enemyThief.render(viewPane);
			g.setColor(Color.YELLOW);
			g.drawString(enemyThief.getSSCode()+", "+getWorld().getPlayers().toString(), getCurrentX(), getCurrentY()+20);
		}*/
		Stroke oldStroke = g.getStroke();
		g.setStroke(new BasicStroke(4));
		//flagAreaCircle.x = getCurrentX() - radius;
		//flagAreaCircle.y = getCurrentY() - radius;
		g.draw(new Ellipse2D.Float((getCurrentX() - radius), (getCurrentY() - radius), 2f * radius, 2f * radius));
		//g2D.draw(new Ellipse2D.Float((getCentre().x-radius),(getCentre().y-radius),2*radius,2*radius));
		g.setStroke(oldStroke);
		//System.out.println(this.getClass().getSimpleName()+": enemyThief == "+enemyThief+", dropped == "+dropped+", getCurrentX() == "+getCurrentX()+", getCurrentY() == "+getCurrentY()+", getTeam().getSpawnFlagBase().getX() == "+getTeam().getSpawnFlagBase().getX()+", getTeam().getSpawnFlagBase().getY() == "+getTeam().getSpawnFlagBase().getY());
		if (viewPane.isShowMapDescriptions() || containsPoint(viewPane.getWorldMouseX(), viewPane.getWorldMouseY())) {
			// draw the name
			FontMetrics fm = viewPane.getFontMetrics(g.getFont());
			String text = (this.getTeam() == viewPane.getPlayer().getTeam() ? "Your team's flag" : "Enemy team's flag");
			float stringWidth = fm.stringWidth(text) / 2f;
			g.drawString(text, getCurrentX() - stringWidth, getCurrentY()+radius+15);
		}
	}
	public boolean containsPoint(float px, float py) {
		if (Point2D.distance(getCurrentX(), getCurrentY(), px, py) < radius) {
			return true;
		}
		return false;
	}
	public boolean isAtBase(){
		if (enemyThief != null) {
			return false;
		} else if (dropped == true) {
			return false;
		}
		return true;
	}
	public float getCurrentX() {
		float xNow = homeX;
		if (enemyThief != null) {
			xNow = enemyThief.getX();
		} else if (dropped == true) {
			xNow = dropPointX;
		}
		return xNow;
	}

	public float getCurrentY() {
		float yNow = homeY;
		if (enemyThief != null) {
			yNow = enemyThief.getY();
		} else if (dropped == true) {
			yNow = dropPointY;
		}
		return yNow;
	}
	
	public GameWorld getWorld() {
		return world;
	}

	public Team getTeam() {
		return team;
	}
}
