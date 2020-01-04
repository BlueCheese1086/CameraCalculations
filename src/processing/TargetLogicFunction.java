package processing;


import java.util.ArrayList;
/**
 * <b>ADVANCED APPLICATIONS ONLY</b>
 * A single-method interface made for defining lambdas that produce an ArrayList of VisionTargets
 * given the current state of the robot. Used to make Computer Vision Pipelines output different
 * types of sightings dynamically for each frame.
 * @author Jeff
 *
 */
public interface TargetLogicFunction {
	/**
	 * Determines based on user-provided logic which VisionTargets a pipeline should support on any given tick
	 * @return the targets to support
	 */
	public ArrayList<VisionTarget> getTargets();
}
