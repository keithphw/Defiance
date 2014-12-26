/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.shooter.Item;

import sydneyengine.shooter.GameWorld;
import sydneyengine.shooter.Player;

/**
 *
 * @author CommanderKeith
 */
public interface Item {
	public String getName();
	public void assignToPlayer(Player player, double assignTimeSeconds);
	public Item createNewItem(GameWorld world);
}
