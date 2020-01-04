package processing;


import java.util.ArrayList;

/**
 * A single-method interface meant to filter/combine an ArrayList of Sightings, returning
 * only the ones which are valid according to a given VisionTarget.
 * 
 * @author Jeff
 */
public interface SightingFilterFunction {
	/**
	 * Takes in a list of sightings and returns the ones which are valid.
	 * Can additionally be used to combine sightings using the Sighting.addSighting() method if 
	 * user-supplied logic determines that a single instance of a target has been split into two
	 * sightings.
	 * @param rawSightings the unfiltered list of Sightings
	 * @return the Sightings from rawSightings that are valid according to user-defined logic.
	 */
	public ArrayList<Sighting> filter(ArrayList<Sighting> rawSightings);
}
