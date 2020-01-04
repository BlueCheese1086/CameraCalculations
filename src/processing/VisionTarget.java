package processing;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * Defines a real-world target that cameras can search for.
 * 
 * <br/>
 * (Optionally) Can have filters added which are applied to any sightings
 * identified as this target to weed out invalid sightings.
 * 
 * @authors Jack, Jeff
 */
public class VisionTarget {
	// the vertical real-world height of the target off the ground, in arbitrary
	// units
	private double height;
	// The ratio of the target's width to its height (width/height) in arbitrary
	// units
	private double aspectRatio;
	// The name of this target. Used for debugging.
	private String name;
	// Used to filter the sightings of this target before and after processing has
	// been completed
	private SightingFilterFunction preProcessingFilter = null, postProcessingFilter = null;

	/**
	 * Instantiates the vision target with a given debugging name and set of
	 * real-world properties
	 * 
	 * @param name        the name of the vision target. Used for Debugging.
	 * @param height      the vertical real-world height of the target off the
	 *                    ground, in arbitrary units
	 * @param aspectRatio The ratio of the target's width to its height
	 *                    (width/height) in arbitrary units
	 */
	public VisionTarget(String name, double height, double aspectRatio) {
		this.name = name;
		this.height = height;
		this.aspectRatio = aspectRatio;
	}

	/**
	 * Sets the pre-processing filter for this target. This filter takes in a list
	 * of unprocessed (Raw) sightings and checks them, returning a set of
	 * known-valid sightings. <br/>
	 * This filter can additionally be used to combine sightings (see
	 * Sighting.addSighting()) which belong to the same real-world target instance
	 * (for example, a vision target consisting of multiple discrete pieces of
	 * reflective tape). <br/>
	 * <br/>
	 * Note: Raw sightings DO NOT have values for non-pixel-based properties such as
	 * distance or angle. This filter should not utilize those values. If filtering
	 * based on such values is required, use setPostProcessingFilter()
	 * 
	 * @param filter a method which takes an ArrayList of sightings and returns only
	 *               the ones deemed valid
	 */
	public void setPreProcessingFilter(SightingFilterFunction filter) {
		this.preProcessingFilter = filter;
	}

	/**
	 * Sets the post-processing filter for this target. This filter takes in a list
	 * of processed sightings and checks them, returning a set of known-valid
	 * sightings. <br/>
	 * This filter should NOT be used to combine sightings, as doing so will cause
	 * problems with calculated values for the combined sightings. <br/>
	 * <br/>
	 * Note: This method is run after processing. Depending on camera and pipeline
	 * setup, using setPreProcessingFilter() except where processed values are
	 * explicitly needed can reduce processing load.
	 * 
	 * @param filter a method which takes an ArrayList of sightings and returns only
	 *               the ones deemed valid
	 */

	public void setPostProcessingFilter(SightingFilterFunction filter) {
		this.postProcessingFilter = filter;
	}

	/**
	 * Takes a list of possible sightings and uses user-provided logic to determine
	 * whether or not each one is a valid sighting of the vision target.
	 * Additionally can combine sightings that belong to the same instance of a
	 * target. Should not use OptionalDouble components (such as distance or angles)
	 * as raw sightings do not have them set yet. For filtering based on distance,
	 * angle, etc..., see validateProcessedSightings()
	 * 
	 * @param polys the sightings to evaluate.
	 * @return only the valid sightings
	 */
	protected ArrayList<Sighting> validateRawSightings(ArrayList<Sighting> polys) {
		if (preProcessingFilter == null) {
			return polys;
		} else {
			return preProcessingFilter.filter(polys);
		}
	}

	/**
	 * Takes a list of possible sightings and uses user-provided logic to determine
	 * whether or not each one is a valid sighting of the vision target. May use
	 * OptionalDouble components of Sighting (such as distance or angles). For
	 * filtering that does not require OptionalDouble components of sighting, see
	 * validateRawSightings().
	 * 
	 * @param polys the sightings to evaluate.
	 * @return only the valid sightings
	 */
	protected ArrayList<Sighting> validateProcessedSightings(ArrayList<Sighting> polys) {
		if (postProcessingFilter == null) {
			return polys;
		} else {
			return postProcessingFilter.filter(polys);
		}
	}

	/**
	 * Gets the height off the ground of the object, in arbitrary units
	 * 
	 * @return the height, in whatever units you set it to.
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * The ratio of the target's real-world width to its real-world height (as in distance from top to
	 * bottom, not distance off the ground)
	 * 
	 * @return the ratio, (targetWidth/targetHeight
	 */
	public double getAspectRatio() {
		return aspectRatio;
	}

	/**
	 * Returns the name of this Target. Used for debugging.
	 * @return the name, for debugging purposes
	 */
	public String getName() {
		return name;
	}
}