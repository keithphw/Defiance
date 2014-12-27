package kEffects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Random;

public class ParticleEmitter
    {
        public ArrayList<Particle> particles = new ArrayList<Particle>();
        protected Random r= new Random();
        /// <summary>
        ///Initialize particle emitter
        /// </summary>
        public ParticleEmitter(Point2D.Float loc, Color colorA, Color colorB, int population)
        {
            if (population > 0)
            {
            	//used to be min of 10
                int n = Math.max(10, (int)Math.floor(Math.sqrt(population)));
                int scatter = (int)Math.floor(Math.sqrt(population) / 4) + 5;
                for (int i = 0; i < n; i++)
                {
                    //alternate colors
                    Color c = colorA;
                    //if even
                    if (i%2 == 0) c = colorB;

                    //slightly random centering and angle and size
                    Point2D.Float pt_new= 
                    		new Point2D.Float(loc.x+ randInt(-scatter, scatter),
                    		loc.y+ randInt(-scatter, scatter));
                    particles.add(new Particle(pt_new,
                        randInt(0, 360), c, 
                        (float)Math.max(1, Math.sqrt(population) / 10) * 1f, randInt(2, 6)));


                }
            }
        }

        public ParticleEmitter() {
			// TODO Auto-generated constructor stub
		}

		/// <summary>
        ///draws all the particles from this particle emitter
        /// </summary>
        public void DrawParticles(Graphics g)
        {
        	//basically, a foreach loop
            for(Particle p : particles)
            {
                p.Draw(g);
            }
               
        }
        /// <summary>
        ///steps particles one frame forward
        /// </summary>
        public void MoveParticles()
        {
        	//basically, a foreach loop
        	for(Particle p : particles)
            {
                p.Move();
            }
                for (int i = 0; i < particles.size(); i++)
                {
                    if (particles.get(i).shouldRemove)
                        particles.remove(i);
                }
        }
        // a helper method to generate random number in a range
        public int randInt(int min, int max) 
        {
        	return r.nextInt((max - min) + 1) + min;
        }
    }