package sydneyengine.shooter;

import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import sydneyengine.superserializable.SSAdapter;

public class SpawnFlag extends SSAdapter{

	//KPolygon body = null;
	GameWorld world;
	protected Team team = null;
	protected float x;
	protected float y;
	protected float radius = 30;
	protected boolean transferable = false;
	transient Ellipse2D.Float flagAreaCircle = new Ellipse2D.Float(0,0,radius*2f,radius*2f);

	float secondsSecured = 0;
	static float maxSecondsSecured = 5;

	public SpawnFlag(){}

	public SpawnFlag(GameWorld world, float x, float y, boolean transferable, Team team){
		this.world = world;
		this.x = x;
		this.y = y;
		this.transferable = transferable;
		this.team = team;
		flagAreaCircle = new Ellipse2D.Float((x-radius),(y-radius),2f*radius,2f*radius);
		/*Point2D.Float[] points = new Point2D.Float[4];
		points[0] = new Point2D.Float(0,0);
		points[1] = new Point2D.Float(0,80);
		points[2] = new Point2D.Float(80,80);
		points[3] = new Point2D.Float(80,0);
		body = new KPolygon(points);
		body.positionCentreAtOrigin();
		body.translateTo(flagPoint);*/

	}


	public void doMove(double seconds, double timeAtStartOfMoveSeconds) {
		if (transferable){
			Team teamCurrentlyInCircle = null;
			Player playerOfTeamCurrentlyInFlagArea = null;
			boolean multipleTeamsInFlagArea = false;
			for (Player player : world.getPlayers()){
				if (player.isDead() == true) {
					continue;
				}
				if (Point2D.distance(x, y, player.getX(), player.getY()) < player.getR() + radius){
					if (teamCurrentlyInCircle == null){
						teamCurrentlyInCircle = player.getTeam();
						playerOfTeamCurrentlyInFlagArea = player;
					}else if (teamCurrentlyInCircle != player.getTeam()){
						// multiple teams in flag area, so no use doing anything more...
						multipleTeamsInFlagArea = true;
					}
				}
			}
			if (teamCurrentlyInCircle == null || multipleTeamsInFlagArea){
				return;
			}
			
			if (teamCurrentlyInCircle == team){
				secondsSecured = 0;
			}else{
				secondsSecured += seconds;
				if (secondsSecured >= maxSecondsSecured){
					team.removeSpawnFlag(this);
					team = teamCurrentlyInCircle;
					team.addSpawnFlag(this);
					secondsSecured = 0;
					playerOfTeamCurrentlyInFlagArea.addFlagCapture();
				}
			}
		}
	}


	public void render(ViewPane viewPane){
		Graphics2D g = viewPane.getBackImageGraphics2D();
		g.setColor(team.getColor());
		Stroke oldStroke = g.getStroke();
		g.setStroke(new BasicStroke(2));
		flagAreaCircle.x = x-radius;
		flagAreaCircle.y = y-radius;
		g.draw(flagAreaCircle);
		//g2D.draw(new Ellipse2D.Float((getCentre().x-radius),(getCentre().y-radius),2*radius,2*radius));
		g.setStroke(oldStroke);
		
		if (viewPane.isShowMapDescriptions() || containsPoint(viewPane.getWorldMouseX(), viewPane.getWorldMouseY())) {
			// draw the name
			FontMetrics fm = viewPane.getFontMetrics(g.getFont());
			String text;
			if (this.getTeam() == viewPane.getPlayer().getTeam()){
				text = "Your team's base";
			}else{
				 text = "Enemy team's base";
				 if (this.isTransferable()){
					 text += ", capture it for your team!";
				 }else{
					 text += ", it can't be captured";
				 }
			}
			float stringWidth = fm.stringWidth(text) / 2f;
			g.drawString(text, getX() - stringWidth, getY()+radius+15);
		}
	}
	public boolean containsPoint(float px, float py) {
		if (Point2D.distance(getX(), getY(), px, py) < radius) {
			return true;
		}
		return false;
	}

	public GameWorld getWorld(){
		return world;
	}
	public Team getTeam(){
		return team;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public boolean isTransferable() {
		return transferable;
	}

	public float getRadius() {
		return radius;
	}
}