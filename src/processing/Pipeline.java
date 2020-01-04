package processing;


import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * A Computer Vision pipeline that can take a frame (image) from a camera and
 * produce a set of contours that can be turned into sightings of supported
 * VisionTargets. <br/>
 * 
 * 
 * The recommended way to use Pipelines is to add supported VisionTargets via
 * the addSupportedTargets() method. In that case, each frame will be searched
 * for sightings of each Target passed in. <br/>
 *
 * 
 * Additionally, if it is required, it is possible to pass in a custom
 * TargetLogicFunction, a lambda which returns a list of targets. In this case,
 * the pipeline will output to whatever targets the function returns, as opposed
 * to a fixed set of Targets. An example use for this is a moving camera that should only
 * find targets of a certain type when it is in a certain position.
 * 
 * @author Jeff
 *
 */
public abstract class Pipeline {
	//Fixed list of supported targets
	private ArrayList<VisionTarget> supportedTargets=new ArrayList<>();
	//Function used for dynamic target logic
	private TargetLogicFunction targetDeterminer = null;
	// true if the getSupportedTargets method returns supportedTargets every time,
	// false if they have implemented their own logic
	private boolean fixedTargets = true;
	
	/**
	 * Processes the frame into a set of contours (sighting boundaries) to be made
	 * into sightings
	 * 
	 * @param source the frame (image) to process
	 * @return a set of contours, stored in a Mat to be turned into sightings
	 */
	protected abstract ArrayList<MatOfPoint> process(Mat source);

	/**
	 * Returns the targets that this pipeline can see. For example, if there are
	 * targets of two colors, two pipelines could be used to catch the different
	 * colors, and they could have separate (or the same, if desired) supported
	 * targets.
	 * 
	 * @return the targets that this pipeline supports
	 */
	public ArrayList<VisionTarget> getSupportedTargets() {
		if (fixedTargets) {
			return supportedTargets;
		} else if (targetDeterminer != null) {
			return targetDeterminer.getTargets();
		} else {
			System.err.println(
					"Dynamic targetting was attempted on a vision pipeline, but that pipeline has not been assigned a TargetLogicFunction!");
			return supportedTargets;
		}
	}

	/**
	 * Adds a supported VisionTarget to this pipeline, meaning outputs of this
	 * pipeline will be stored as sightings for that target.
	 * 
	 * @param target the target to add
	 */
	public void addSupportedTarget(VisionTarget target) {
		supportedTargets.add(target);
	}

	/**
	 * Adds supported VisionTargets to this pipeline, meaning outputs of this
	 * pipeline will be stored as sightings for those targets.
	 * 
	 * @param targets the targets to add
	 */
	public void addSupportedTargets(VisionTarget... targets) {
		for (VisionTarget target : targets) {
			addSupportedTarget(target);
		}
	}

	/**
	 * Removes a supported VisionTarget from this pipeline, meaning outputs of this
	 * pipeline will be no longer stored as sightings for that target.
	 * 
	 * @param target the target to remove
	 */
	public void removeSupportedTarget(VisionTarget target) {
		supportedTargets.remove(target);
	}

	/**
	 * Removes supported VisionTargets from this pipeline, meaning outputs of this
	 * pipeline will be no longer stored as sightings for those targets.
	 * 
	 * @param targets the targets to remove
	 */
	public void removeSupportedTargets(VisionTarget... targets) {
		for (VisionTarget target : targets) {
			removeSupportedTarget(target);
		}
	}

	/**
	 * <b>ADVANCED APPLICATIONS ONLY</b><br/>
	 * Sets a custom target logic function for this pipeline. After this method is
	 * called, the objects seen by this pipeline will be stored as instances of the
	 * VisionTargets that the TargetLogicFunction returns each tick. This
	 * automatically overrides the default behavior, which stores sightings to all
	 * VisionTargets that have been added via the addSupportedTargets() method. This
	 * method is for advanced applications only, as most pipelines will only need to
	 * output to a fixed set of Targets each tick.
	 * 
	 * @param function a lambda that returns an ArrayList of VisionTargets, run each
	 *                 tick to determine what targets the pipeline is currently
	 *                 looking for.
	 */
	public void setTargetLogicFunction(TargetLogicFunction function) {
		targetDeterminer = function;
		enableCustomTargetLogic();
	}

	/**
	 * <b>ADVANCED APPLICATIONS ONLY</b><br/>
	 * Enables custom target logic, that is, enables the use of a
	 * TargetLogicFunction to determine what targets this pipeline is looking
	 * for/returning dynamically each tick. This overrides the default behavior,
	 * which stores sightings to all VisionTargets that have been added via the
	 * addSupportedTargets() method.
	 */
	public void enableCustomTargetLogic() {
		fixedTargets = false;
	}

	/**
	 * Restores default behavior, causing this pipeline to output only to the set of
	 * Targets that have been fed into the addSupportedTarget methods.
	 */
	public void disableCustomTargetLogic() {
		fixedTargets = true;
	}
}
