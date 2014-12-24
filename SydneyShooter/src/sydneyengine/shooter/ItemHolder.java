/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sydneyengine.shooter;

import sydneyengine.superserializable.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;

/**
 *
 * @author CommanderKeith
 */
public class ItemHolder extends SSAdapter {

	Item item;
	GameWorld world;
	protected float x;
	protected float y;
	protected double spawnTimeSeconds;
	protected double lifeTimeSeconds;
	protected boolean dead;
	protected double deathTimeSeconds = -1;
	protected boolean wasPickedUp = false;
	float canNotHitOwnPlayerTimeSeconds = 0.1f;
	protected Player playerThatWasHit = null;
	float radius = 5;

	public ItemHolder() {
		//System.err.println("getSSCode() == "+getSSCode()+", dead == "+dead);
	}

	public ItemHolder(GameWorld world, Item item, float x, float y, double spawnTimeSeconds) {
		this.world = world;
		assert item != null;
		this.item = item;
		this.x = x;
		this.y = y;
		this.spawnTimeSeconds = spawnTimeSeconds;
		dead = false;
		lifeTimeSeconds = 20;
	}

	//transient BasicStroke stroke = new BasicStroke(3);
	public void render(ViewPane viewPane) {
		Graphics2D g = viewPane.getBackImageGraphics2D();
		//g.setColor(Color.GRAY.brighter());
		g.setColor(Color.white);
		g.fill(new java.awt.geom.Rectangle2D.Float(getX() - radius, getY() - radius, radius * 2f, radius * 2f));
		
		g.setColor(Color.cyan);
		g.draw(new java.awt.geom.Rectangle2D.Float(getX() - radius, getY() - radius, radius * 2f, radius * 2f));

		/*
		g.setColor(Color.BLACK);
		// draw the name, centered
		FontMetrics fm = viewPane.getFontMetrics(g.getFont());
		float stringWidth = fm.stringWidth(item.getName()) / 2f;

		g.drawString(item.getName(), getX() - stringWidth, getY() - radius - 2);
		*/

	}

	public void doMove(double seconds, double timeAtStartOfMoveSeconds) {
		assert dead == false : "dead == " + dead;
		assert dead == false || deathTimeSeconds != -1 : dead + ", " + deathTimeSeconds;
		assert seconds >= 0 : seconds;
		assert spawnTimeSeconds <= timeAtStartOfMoveSeconds + seconds : "this bullet was spawned in the future! getSSCode() == " + getSSCode() + ", spawnTimeSeconds == " + (spawnTimeSeconds) + ", timeAtStartOfMoveSeconds + seconds == " + (timeAtStartOfMoveSeconds + seconds) + ", " + spawnTimeSeconds + ", " + timeAtStartOfMoveSeconds + ", " + seconds;
		assert dead == true || spawnTimeSeconds + lifeTimeSeconds + 0.0001 >= timeAtStartOfMoveSeconds : "getSSCode() == " + getSSCode() + ", spawnTimeSeconds + lifeTimeSeconds == " + (spawnTimeSeconds + lifeTimeSeconds) + " should be greater than timeAtStartOfMoveSeconds == "+timeAtStartOfMoveSeconds+".   timeAtStartOfMoveSeconds + seconds == " + (timeAtStartOfMoveSeconds + seconds) + ", " + spawnTimeSeconds + ", " + lifeTimeSeconds + ", " + timeAtStartOfMoveSeconds + ", " + seconds;
		if (spawnTimeSeconds + lifeTimeSeconds < timeAtStartOfMoveSeconds + seconds) {
			seconds = spawnTimeSeconds + lifeTimeSeconds - timeAtStartOfMoveSeconds;
			seconds = (seconds < 0 ? 0 : seconds);	// this line is needed since sometimes seconds can be very slightly negative here.
			doItemHolderMove(seconds, timeAtStartOfMoveSeconds);
			dead = true;
			deathTimeSeconds = spawnTimeSeconds + lifeTimeSeconds;
		} else {
			doItemHolderMove(seconds, timeAtStartOfMoveSeconds);
			assert spawnTimeSeconds + lifeTimeSeconds >= timeAtStartOfMoveSeconds : "getSSCode() == " + getSSCode() + ", spawnTimeSeconds + lifeTimeSeconds == " + (spawnTimeSeconds + lifeTimeSeconds) + ", timeAtStartOfMoveSeconds + seconds == " + (timeAtStartOfMoveSeconds + seconds) + ", " + spawnTimeSeconds + ", " + lifeTimeSeconds + ", " + timeAtStartOfMoveSeconds + ", " + seconds;
		}
		assert item != null || item == null && dead == true : item + ", " + dead;
	}

	protected void doItemHolderMove(double seconds, double timeAtStartOfMoveSeconds) {
		assert Double.isNaN(x) == false;
		assert seconds >= 0 : seconds;
		boolean touch = false;
		ArrayList<Player> players = world.getPlayers();
		Player hitPlayer = null;
		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			if (p.isDead() || p.acceptsItem(item) == false) {
				continue;
			}
			if (Line2D.ptSegDist(p.getX(), p.getY(), p.getOldX(), p.getOldY(), x, y) < this.radius + p.getR()) {
				touch = true;
				hitPlayer = p;
			}
		}
		if (touch) {
			if (hitPlayer != null) {
				item.assignToPlayer(hitPlayer, timeAtStartOfMoveSeconds);
				this.playerThatWasHit = hitPlayer;
				dead = true;
				// the below is not accurate, so the update time (seconds) needs to be small.
				deathTimeSeconds = timeAtStartOfMoveSeconds;
				wasPickedUp = true;
			}
		}

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

	public double getSpawnTimeSeconds() {
		return spawnTimeSeconds;
	}

	public void setSpawnTimeSeconds(double spawnTimeSeconds) {
		this.spawnTimeSeconds = spawnTimeSeconds;
	}

	public boolean isDead() {
		return dead;
	}

	public void writeSS(SSObjectOutputStream out) throws IOException {		// this is the method that you over-ride if you want custom serialization
		super.writeSS(out);
	}

	public void readSS(SSObjectInputStream in) throws java.io.IOException {	// this is the method that you over-ride if you want custom serialization
		super.readSS(in);
	}

	public double getLifeTimeSeconds() {
		return lifeTimeSeconds;
	}

	public void setLifeTimeSeconds(double lifeTimeSeconds) {
		this.lifeTimeSeconds = lifeTimeSeconds;
	}

	public double getDeathTimeSeconds() {
		return deathTimeSeconds;
	}

	public boolean wasPickedUp() {
		return wasPickedUp;
	}
}
