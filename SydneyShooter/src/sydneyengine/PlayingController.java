/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sydneyengine;

import sydneyengine.shooter.GameFrame;
import sydneyengine.shooter.Player;
import sydneyengine.shooter.ViewPane;

/**
 *
 * @author Joanne Woodward
 */
public interface PlayingController {
	
	public GameFrame getGameFrame();

	public void setGameFrame(GameFrame gameFrame);
	
	public Player getPlayer();

	public void setPlayer(Player player);

	public ViewPane getViewPane();

	public void setViewPane(ViewPane viewPane);
}
