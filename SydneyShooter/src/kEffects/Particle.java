package kEffects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.Random;
    public class Particle
    {
        final int lifetime = 30; Color color; double age = 0; int radius = 1;
        Random r = new Random();
        Point2D.Float  movement= new Point2D.Float(0,0);
		public Particle(Point2D.Float  loc, double angle_deg, Color color, float speed, int radius)
		{

            this.loc = loc; this.radius=radius; this.color = color;
			
            // in java, trig functions work in RADIANS!!!
            movement.x = speed * (float)Math.cos(Math.PI * angle_deg / 180);
            movement.y = speed * (float)Math.sin(Math.PI * angle_deg / 180);
		}
		
		Point2D.Float loc;

		public boolean shouldRemove = false;
		
		public void Move()
		{
            loc.x += movement.x; loc.y += movement.y;
			age ++;
			if (age >= lifetime) {
                shouldRemove = true;
			}
		}
		
		
		
		public void Draw(Graphics g)
		{
			Color col = new Color(color.getRed(), color.getGreen(), color.getBlue(),
					(int)Math.max(255 - (age / lifetime * 255), 0));
            
			g.setColor(col);			
			g.fillOval((int)(loc.x - 1), (int)(loc.y - 1),radius, radius);
		}
    }