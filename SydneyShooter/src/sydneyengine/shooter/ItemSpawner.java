/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.shooter;
import java.io.IOException;

import sydneyengine.shooter.Item.Item;
import sydneyengine.superserializable.ArrayListSS;
import sydneyengine.superserializable.SSAdapter;
import sydneyengine.superserializable.SSObjectInputStream;
import sydneyengine.superserializable.SSObjectOutputStream;

/**
 * Spawns a new itemHolder respawnIntervalSeconds after the old itemHolder died.
 * 
 * @author CommanderKeith
 */
public class ItemSpawner extends SSAdapter {

	GameWorld world;
	ArrayListSS<Item> itemsToSpawn;
	protected float x;
	protected float y;
	protected double respawnIntervalAfterPickUpSeconds;
	static protected double respawnIntervalAfterNoPickUpSeconds = 0;
	ItemHolder itemHolder = null;
	long seed = 0;
	
	public ItemSpawner() {
	}

	public ItemSpawner(GameWorld world, ArrayListSS<Item> itemsToSpawn, float x, float y, double respawnIntervalAfterPickUpSeconds) {
		this.world = world;
		this.itemsToSpawn = itemsToSpawn;
		this.x = x;
		this.y = y;
		this.respawnIntervalAfterPickUpSeconds = respawnIntervalAfterPickUpSeconds;
		itemHolder = createNewItemHolder(world, x, y, 0);
		world.addItem(itemHolder);
	}
	
	public ItemHolder createNewItemHolder(GameWorld world, float x, float y, double spawnTimeSeconds){
		world.incrementAndReSeedRandom();
		int randomInt = (world.getRandom().nextInt(itemsToSpawn.size()));
		ItemHolder newItemHolder = new ItemHolder(world, itemsToSpawn.get(randomInt).createNewItem(world), x, y, spawnTimeSeconds);
		world.incrementAndReSeedRandom();
		randomInt = (world.getRandom().nextInt(5));
		newItemHolder.setLifeTimeSeconds(20+randomInt);
		return newItemHolder;
	}
	
	public void doMove(double seconds, double timeAtStartOfMoveSeconds) {
		if (itemHolder.isDead()){
			double respawnIntervalSeconds = (itemHolder.wasPickedUp() ? respawnIntervalAfterPickUpSeconds : respawnIntervalAfterNoPickUpSeconds);
			if (respawnIntervalSeconds + itemHolder.getDeathTimeSeconds() < timeAtStartOfMoveSeconds + seconds) {
				ItemHolder oldItemHolder = itemHolder;
				itemHolder = createNewItemHolder(world, x, y, oldItemHolder.getDeathTimeSeconds() + respawnIntervalSeconds);
				world.addItem(itemHolder);
				// Need to do itemHolder.doMove method for the remaining time left.
				double remainingTimeToMove = timeAtStartOfMoveSeconds + seconds - (oldItemHolder.getDeathTimeSeconds() + respawnIntervalSeconds);
				assert remainingTimeToMove >= 0 : remainingTimeToMove;
				itemHolder.doMove(remainingTimeToMove, timeAtStartOfMoveSeconds + seconds - remainingTimeToMove);
				//System.out.println(this.getClass().getSimpleName() + ": respawning, respawnIntervalSeconds == "+respawnIntervalSeconds+", oldItemHolder.getDeathTimeSeconds() == "+oldItemHolder.getDeathTimeSeconds()+", "+(respawnIntervalSeconds + oldItemHolder.getDeathTimeSeconds())+", timeAtStartOfMoveSeconds + seconds == "+(timeAtStartOfMoveSeconds + seconds));
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

	@Override
	public void writeSS(SSObjectOutputStream out) throws IOException {		// this is the method that you over-ride if you want custom serialization
		super.writeSS(out);
	}

	@Override
	public void readSS(SSObjectInputStream in) throws java.io.IOException {	// this is the method that you over-ride if you want custom serialization
		super.readSS(in);
	}

	public double getRespawnIntervalAfterPickUpSeconds() {
		return respawnIntervalAfterPickUpSeconds;
	}

}