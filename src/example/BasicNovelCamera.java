package example;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import cameras.Camera;
import math.CameraMath;
import processing.Sighting;
import processing.VisionTarget;

public class BasicNovelCamera {
	public static void main(String[] args) {
		VisionTarget target = new VisionTarget("Rocket", 28.75, 1);
		VisionTarget target2 = new VisionTarget("Cargo", 20.01, 1);
		RandomCam c = new RandomCam();
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
class RandomCam implements Camera {
	private ArrayList<Sighting> sightings = new ArrayList<Sighting>();
	double horizontalOffset, depthOffset;

	public RandomCam() {
		horizontalOffset = Math.random();
		depthOffset = Math.random();
	}

	/**
	 * This method should return the number of sightings your camera sees at a given
	 * time. See Camera.java for more details
	 */
	public int sightingCount(VisionTarget vt) {
		return sightings.size();
	}

	/**
	 * This method should return all the sightings your camera sees at a given time.
	 * See Camera.java for more details
	 */
	public ArrayList<Sighting> getSightings(VisionTarget vt) {
		if (vt.getName().equals("Rocket")) {
			return sightings;
		} else {
			ArrayList<Sighting> ret = new ArrayList<Sighting>();
			ret.add(sightings.get(0)); // Do something weird, idk
			return ret;
		}
	}

	public void updateSightings() {
		sightings = new ArrayList<Sighting>();
		sightings.add(new Sighting());
		while (Math.random() < .95) {
			sightings.add(new Sighting());
			// You can use CameraMath functions here!!!!! (Please do, about 99% of the work
			// you'd want to do is there).
			Point2D sightingCoords = CameraMath.calcSightingCoords(Math.random(), Math.random(), Math.random(),
					Math.random(), Math.random());
			sightings.get(sightings.size() - 1).setRobotBasedDistance(sightingCoords.distance(0, 0));
		}
	}
}