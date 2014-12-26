package sydneyengine.shooter;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;

import kEffects.Particle;
import kEffects.ParticleEmitter;

public class ParticleTrail extends ParticleEmitter{

	public ParticleTrail(Float loc, Color color, int numParticles, int radius, boolean mobile) {
		super();
		
		
		if (numParticles > 0)
        {
            int n = Math.max(1, numParticles);
            int scatter = numParticles;
            for (int i = 0; i < n; i++)
            {

                //slightly random centering and angle and size
                Point2D.Float pt_new= 
                		new Point2D.Float(loc.x+ randInt(-scatter, scatter),
                		loc.y+ randInt(-scatter, scatter));
                
                
                float speed = 0;
                if(mobile) speed = 30;
                particles.add(new Particle(pt_new,
                    //randInt(0, 360), 
                    0,
                    color, speed
                    , radius));


            }
        }
	}

}
