/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine.shooter;

import sydneyengine.AbstractEvent;
import sydneyengine.RewindableWorld;

/**
 *
 * @author CommanderKeith
 */
public abstract class GameEvent extends AbstractEvent{
	@Override
	public void apply(RewindableWorld world){
		applyNow((GameWorld)world);
	}
	public abstract void applyNow(GameWorld world);
	
}
