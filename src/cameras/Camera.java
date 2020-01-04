package cameras;

import java.util.ArrayList;

import processing.Sighting;
import processing.VisionTarget;

public interface Camera {
	/**
	 * Gives the number of sightings of a target the Camera has identified for the
	 * last frame
	 * 
	 * @param vt the vision target to check
	 * @return the number of sightings identified
	 */
	public int sightingCount(VisionTarget vt);
	/**
	 * Returns a list of all validated sightings of a given target for the last
	 * frame
	 * 
	 * @param vt the specified vision target
	 * @return the list of sightings
	 */
	public ArrayList<Sighting> getSightings(VisionTarget vt);

	
}
