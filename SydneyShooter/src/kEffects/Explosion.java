package kEffects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
//ported from Kwuang's explosion class
public class Explosion
{
    /// <summary>
    ///Initialize explosion
    /// </summary>
    public Explosion(Point2D.Float center, double durationMillisecs, float radius)
    {
        this.loc = center;
        this.duration = durationMillisecs;
        this.radius = radius;
    }

    private Point2D.Float loc;
    public boolean shouldRemove = false;

    private double duration;

    private float radius;

    private float progress;

    private double age;

    /// <summary>
    ///steps the explosion one frame forward
    /// </summary>
    public void Move(int millisecs)
    {
        age+=millisecs; progress = (float)(age / duration);
        if (age >= duration)
        {
            shouldRemove = true;
        }
    }

    /// <summary>
    ///draw explosion
    /// </summary>
    public void Draw(Graphics g)
    {   	
        float currentRadius = 1.5f + (radius - 1.5f) * progress;
            	
        int in255 = Math.max(0, Math.min(255, (Math.round(255 * progress))));
        
        Color c= Color.orange;
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 255- in255));
    	g.fillOval((int)(loc.x - currentRadius), (int)(loc.y - currentRadius), 
        		(int)(currentRadius * 2), (int)(currentRadius * 2));
    	//yellow
        g.setColor(new Color(255, 255, in255, 255 - in255));
        g.fillOval((int)(loc.x - currentRadius), (int)(loc.y - currentRadius), 
        		(int)(currentRadius * 2), (int)(currentRadius * 2));
        
    }
}
