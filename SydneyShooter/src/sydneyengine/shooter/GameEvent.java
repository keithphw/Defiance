/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.shooter;

import sydneyengine.*;

/**
 *
 * @author CommanderKeith
 */
public abstract class GameEvent extends AbstractEvent{
	public void apply(RewindableWorld world){
		applyNow((GameWorld)world);
	}
	public abstract void applyNow(GameWorld world);
	
}
