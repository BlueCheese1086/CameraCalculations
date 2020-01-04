package math;

import processing.Sighting;
import processing.VisionTarget;

/**
 * A class containing some static methods to assist in camera setup.
 * 
 * @author Jeff
 *
 */
public class CameraConfig {
	/**
	 * Configures the horizontal and vertical angles of the CVCamera
	 * 
	 * @param target           the target used to configure the robot. The center of
	 *                         the robot must be perfectly aligned with the given
	 *                         target
	 * @param distance         the distance between the robot and the target
	 * @param horizontalOffset the horizontal offset of the camera from the center
	 *                         of the robot (left/right, in arbitrary units)
	 * @param verticalOffset   the vertical offset of the camera from the ground (in
	 *                         arbitrary units)
	 * @param s                the sighting representing the target
	 * @throws java.lang.Exception To configure, the CVCamera must only be able to
	 *         see one valid sighting.
	 * @return a double array of size 2, the {horizontalAngle, verticalAngle} of the
	 *         camera setup in radians.
	 */
	public static double[] configure(VisionTarget target, double distance, double horizontalOffset,
			double verticalOffset, Sighting s) throws Exception {
		if (!s.getCameraBasedYaw().isPresent() || !s.getCameraBasedPitch().isPresent())
			throw new Exception("Raw vertical angle or raw horizontal angle is not available!");

		double horizontalAngle = CameraConfig.getXPlacementAngle(s.getCameraBasedYaw().getAsDouble(), distance,
				horizontalOffset);
		double verticalAngle = CameraConfig.getYPlacementAngle(s.getCameraBasedPitch().getAsDouble(),
				target.getHeight() - verticalOffset, distance);
		return new double[] { horizontalAngle, verticalAngle };
	}

	/**
	 * Finds the horizontal placement angle of the camera based on a sighting with a
	 * known location. Requires the target to be placed directly in front of the
	 * center of the robot. Proof of concept:
	 * https://www.desmos.com/calculator/nqyjtmea0p
	 * 
	 * @param sightingAngle  the horizontal angle to the sighting from the camera
	 *                       (direct camera output) in radians
	 * @param cameraDistance the distance to the sighting from the camera, in
	 *                       arbitrary units
	 * @param cameraOffset   the horizontal offset from the center of the robot of
	 *                       the camera, in arbitrary units
	 * @return The horizontal placement angle of the camera, in radians, with 0
	 *         being facing forward and clockwise being positive.
	 */
	public static double getXPlacementAngle(double sightingAngle, double cameraDistance, double cameraOffset) {
		return -Math.asin(cameraOffset / cameraDistance) - sightingAngle;
	}

	/**
	 * Finds the vertical placement angle of the camera using a target with a known
	 * location.
	 * 
	 * @param sightingAngle  The vertical angle from the camera to the sighting
	 *                       (direct camera output), in radians
	 * @param heightDiff     The difference in height between the camera and the
	 *                       target, in arbitrary units
	 * @param cameraDistance The distance from the camera to the sighting
	 * @return The vertical placement angle of the camera, in radians, with 0 being
	 *         flat with the ground and up being positive.
	 */
	public static double getYPlacementAngle(double sightingAngle, double heightDiff, double cameraDistance) {
		return Math.atan2(heightDiff, cameraDistance) - sightingAngle;
	}

	/**
	 * Uses a known {angle<->pixel coordinate} correspondence to calculate the Field
	 * of View in a given direction.
	 * 
	 * @param coord         The coordinate of the center of the sighting. Use x
	 *                      coordinate for calculating horizontal FOV and y
	 *                      coordinate for vertical FOV
	 * @param dimensionSize The number of pixels in the given dimension. For a
	 *                      320x240 camera, use 320 for calculating horizontal FOV
	 *                      and 240 for calculating vertical FOV
	 * @param angle         The known angle to the given sighting, in radians. Must
	 *                      be measured accurately in order to provide meaningful
	 *                      data.
	 * @return The FOV in the given direction, horizontal or vertical.
	 */
	public static double calcFOV(double coord, double dimensionSize, double angle) {
		double center = (dimensionSize / 2.0) - 0.5;// -.5 accounts for 0 being lowest pixel value and
		// (dimensionSize-1) being the highest
		// uses pinhole model of camera to find FOV
		double focalLength = (coord - center) / Math.tan(angle);
		return 2.0 * Math.atan((dimensionSize / 2.0) / focalLength);
	}

	/**
	 * Uses a known {target position<->pixel coordinate} correspondence to calculate
	 * the Field of View in a given direction.
	 * 
	 * @param coord           The coordinate of the center of the sighting. Use x
	 *                        coordinate for calculating horizontal FOV and y
	 *                        coordinate for vertical FOV
	 * @param dimensionSize   The number of pixels in the given dimension. For a
	 *                        320x240 camera, use 320 for calculating horizontal FOV
	 *                        and 240 for calculating vertical FOV
	 * @param forwardDistance How far forward the target is from the camera, in arbitrary units.
	 * @param orthDistance    How far along the orthogonal axis of interest the
	 *                        target is from the camera. For horizontal FOV, that is
	 *                        how far left/right the target is (right being
	 *                        positive), for vertical, it's how much higher the
	 *                        target is than the camera, in the same units as above.
	 * @return The FOV in the given direction, horizontal or vertical.
	 */
	public static double calcFOV(double coord, double dimensionSize, double forwardDistance, double orthDistance) {
		double center = (dimensionSize / 2.0) - 0.5;// -.5 accounts for 0 being lowest pixel value and
		// (dimensionSize-1) being the highest
		// uses pinhole model of camera to find FOV
		double focalLength = (coord - center) / (orthDistance / forwardDistance);
		return 2.0 * Math.atan((dimensionSize / 2.0) / focalLength);
	}
}
