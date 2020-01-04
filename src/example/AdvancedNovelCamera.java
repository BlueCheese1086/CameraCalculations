package example;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import cameras.CVCamera;
import math.CameraMath;
import processing.Sighting;
import processing.SightingContainer;
import processing.VisionTarget;

public class AdvancedNovelCamera {
	public static void main(String[] args) {
		VisionTarget target = new VisionTarget("Rocket", 28.75, 1);
		VisionTarget target2 = new VisionTarget("Cargo", 20.01, 1);
		RandomCam2 c = new RandomCam2(50, Math.PI / 2, Math.PI / 2, 320, 240, 10, 10, 30, Math.PI / 4, 0);
		while (true) {
			c.updateSightings();
			System.out.println(c.getSightings(target));
			System.out.println(c.getSightings(target2));
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}

/**
 * An implementation of an add-on to the camera library. Pretends to see random
 * sightings (bad practice, obviously)
 * 
 * @author Jeff
 *
 */
class RandomCam2 extends CVCamera {

	public RandomCam2(int refreshRate, double vFOV, double hFOV, double xPixels, double yPixels,
			double horizontalOffset, double verticalOffset, double depthOffset, double hAngle, double vAngle) {
		super(refreshRate, vFOV, hFOV, xPixels, yPixels, horizontalOffset, verticalOffset, depthOffset, hAngle, vAngle);
	}

	public void updateSightings() {
		for (SightingContainer container : this.activeTargets.values()) {
			ArrayList<Sighting> sightings = new ArrayList<>();
			while (Math.random() < .95) {
				sightings.add(new RandomSighting(Math.random(), Math.random()));
			}

			// Doing my own math, sometimes using library functions
			for (Sighting sighting : sightings) {
				sighting.setCameraBasedDistance(Math.random());
				Point2D sightingCoords = CameraMath.calcSightingCoords(sighting.getCameraBasedDistance().getAsDouble(),
						sighting.getCameraBasedYaw().getAsDouble(), this.getHorizontalOffset(), this.getDepthOffset(),
						this.getHorizontalAngle());
				sightings.get(sightings.size() - 1).setRobotBasedDistance(sightingCoords.distance(0, 0));
			}
			container.setSightings(sightings);
		}
	}
}

/**
 * If you need ABSOLUTE control over your sightings, you can extend the Sighting
 * class.
 * 
 * @author Jeff
 *
 */
class RandomSighting extends Sighting {
	/**
	 * My own fun little constructor
	 * 
	 * @param a has no meaning
	 * @param b has even less meaning, somehow
	 */
	public RandomSighting(double a, double b) {
		this.centerX = a;
		this.centerY = b - a + a * a;
		this.aspectRatio = Math.pow(a, b);

		this.area = a * (b - a);
		this.width = a / b;
		this.height = a * b;
	}
}