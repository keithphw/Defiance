/*
 * CustomCursors.java
 *
 * Created on 30 September 2006, 23:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package sydneyengine.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

/**
 *
 * @author Keith
 */
public class CustomCursors {
	
	static Cursor whiteCrosshairCursor;
	static Cursor blackCrosshairCursor;
	static{
		
		// whiteCrosshairCursor:
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		int preferredWidthAndHeight = 18;
		Dimension dimension = toolkit.getBestCursorSize(preferredWidthAndHeight, preferredWidthAndHeight);
		BufferedImage img = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2D = (Graphics2D)img.getGraphics();
		Composite oldComposite = g2D.getComposite();
		g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0));
		g2D.fillRect(0,0,img.getWidth(),img.getHeight());
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2D.setComposite(oldComposite);
		
		int width, height, halfWidth, halfHeight;
		if (img.getWidth() > preferredWidthAndHeight || img.getHeight() > preferredWidthAndHeight){
			width = preferredWidthAndHeight;
			height = preferredWidthAndHeight;
			halfWidth = width/2;
			halfHeight = height/2;
		}else{
			width = img.getWidth();
			height = img.getHeight();
			halfWidth = width/2;
			halfHeight = height/2;
		}
		
		g2D.setColor(Color.white);
		g2D.setStroke(new BasicStroke(1));
		int div = 4;
		g2D.drawOval(width/4, height/4, width-width/2, height-height/2);
		
		g2D.drawLine(0, halfHeight, halfWidth - width/div, halfHeight);
		g2D.drawLine(halfWidth + width/div, halfHeight, width, halfHeight);
		g2D.drawLine(halfWidth, 0, halfWidth, halfHeight - height/div);
		g2D.drawLine(halfWidth, halfHeight + height/div, halfWidth, height);
		//g2D.drawLine(0, halfHeight, width, halfHeight);
		//g2D.drawLine(halfWidth, 0, halfWidth, height);
		whiteCrosshairCursor = toolkit.createCustomCursor(img, new Point(halfWidth,halfHeight), "whiteCrosshair");//(int)(img.getWidth()/2),(int)(img.getHeight()/2)), "crosshair");
		
		
		// blackCrosshairCursor:
		img = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_ARGB);
		g2D = (Graphics2D)img.getGraphics();
		oldComposite = g2D.getComposite();
		g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0));
		g2D.fillRect(0,0,img.getWidth(),img.getHeight());
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2D.setComposite(oldComposite);
		
		System.out.println("CustomCursor: img.getWidth() == "+img.getWidth()+", img.getHeight() == "+img.getHeight());
		if (img.getWidth() > preferredWidthAndHeight || img.getHeight() > preferredWidthAndHeight){
			width = preferredWidthAndHeight;
			height = preferredWidthAndHeight;
			halfWidth = width/2;
			halfHeight = height/2;
		}else{
			width = img.getWidth();
			height = img.getHeight();
			halfWidth = width/2;
			halfHeight = height/2;
		}
		g2D.setColor(Color.BLACK);
		g2D.setStroke(new BasicStroke(1));
		int f = 0;
		div = 3;
		int c = 1;
		g2D.drawOval(width/4, height/4, width-width/2+c, height-height/2+c);
		g2D.drawLine(halfWidth, halfHeight, halfWidth, halfHeight);
		
		g2D.drawLine(0, halfHeight, halfWidth - width/div, halfHeight);
		g2D.drawLine(halfWidth + width/div, halfHeight, width-f, halfHeight);
		g2D.drawLine(halfWidth, 0, halfWidth, halfHeight - height/div);
		g2D.drawLine(halfWidth, halfHeight + height/div, halfWidth, height-f);
		/*
		g2D.setStroke(new BasicStroke(1));
		int n = 3;
		g2D.drawLine(0, 0, n, n);
		g2D.drawLine(0, height, n, height-n);
		g2D.drawLine(width, 0, width-n, n);
		g2D.drawLine(width, height, width-n, height-n);
		*/
		blackCrosshairCursor = toolkit.createCustomCursor(img, new Point(halfWidth,halfHeight), "blackCrosshair");//(int)(img.getWidth()/2),(int)(img.getHeight()/2)), "crosshair");
	}
	
	// prevent an object of this class from being created:
	private CustomCursors() {
	}
	
	public static Cursor getMenuCursor(){
		return Cursor.getDefaultCursor();
	}
	public static Cursor getGameCursor(){
		return blackCrosshairCursor;
		//return whiteCrosshairCursor;
	}
	
}