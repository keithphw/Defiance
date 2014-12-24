/*
 * GameDesktopPane.java
 *
 * Created on 12 October 2007, 00:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine.ui;

import java.util.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.image.*;
import sydneyengine.*;
import sydneyengine.network.*;
import sydneyengine.superserializable.*;

/**
 *
 * @author Leeder
 */
public class GameDesktopPane extends JDesktopPane{
	protected Component mainComponent;
	/** Creates a new instance of GameDesktopPane */
	public GameDesktopPane() {
		init();
	}
	public GameDesktopPane(Component mainComponent) {
		this.mainComponent = mainComponent;
		init();
	}
	protected void init(){
		/* If the we do setOpaque(false), then for some reason you see wierd 
		 artifacts drawn on the screen which look like cut out bits of the menus.  
		 At least if it's setOpaque(true) it's just a solid colour flash.
		*/
		this.setOpaque(true);
		this.setFocusable(false);
	}
	
	public void setSize(int width, int height){
		super.setSize(width,height);
		if (mainComponent != null){
			mainComponent.setSize(width, height);
		}
	}
	public void setSize(Dimension d){
		super.setSize(d);
		if (mainComponent != null){
			mainComponent.setSize(d);
		}
	}
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x,y,width,height);
		if (mainComponent != null){
			mainComponent.setSize(width, height);
		}
	}
	public void update(Graphics g){
		if (getMainComponent() == null){
			return;
		}
		super.update(g);
	}
	public void paint(Graphics g){
		if (getMainComponent() == null){
			return;
		}
		super.paint(g);
	}
	public Component getMainComponent() {
		return mainComponent;
	}
	public void setMainComponent(Component newMainComponent) {
		if (mainComponent != null){
			this.remove(mainComponent);
		}
		if (newMainComponent != null){
			this.add(newMainComponent, JDesktopPane.DEFAULT_LAYER);
			newMainComponent.setBounds(0,0,this.getWidth(),this.getHeight());
		}
		this.mainComponent = newMainComponent;
	}
	/*public void remove(Component c){
		if (c instanceof JInternalFrame){
			((JInternalFrame)c).setVisible(false);
		}
		super.remove(c);
	}*/
	public void removeAll(){
		this.mainComponent = null;
		super.removeAll();
	}
	/**
	 * Equivalent to Component.getComponents(), except this method guarantees that if mainComponent is non-null, it is first in the array.
	 */	
	public Component[] getComponentsWithMainFirst(){
		Component[] allComponents = super.getComponents();
		if (allComponents.length > 1 && mainComponent != null){
			int mainComponentIndex = -1;
			if (allComponents[0] != mainComponent){
				for (int i = 0; i < allComponents.length; i++){
					if (allComponents[i] == mainComponent){
						mainComponentIndex = i;
					}
				}
				allComponents[mainComponentIndex] = allComponents[0];
				allComponents[0] = mainComponent;
			}
		}
		return allComponents;
	}
	public void removeAllNonMainComponents(){
		Component[] components = getComponentsWithMainFirst();
		for (int i = components.length-1; i >= 1; i--){
			remove(components[i]);
		}
	}
}
