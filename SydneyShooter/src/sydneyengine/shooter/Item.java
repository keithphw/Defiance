/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.shooter;

/**
 *
 * @author CommanderKeith
 */
public interface Item {
	public String getName();
	public void assignToPlayer(Player player, double assignTimeSeconds);
	public Item createNewItem(GameWorld world);
}
