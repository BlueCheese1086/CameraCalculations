package cameras;


import java.util.ArrayList;
import java.util.HashMap;

import processing.Sighting;
import processing.SightingContainer;
import processing.VisionTarget;
/**
 * An abstract class that allows for storage of data related to a camera on the robot, including
 * intrinsic properties like FOV and properties of the camera's placement on the robot.
 * <br/>Any object that in some way produces Sightings should extend camera.
 * @author Jeff
 *
 */
public abstract class CVCamera implements Camera {
	// Camera stream parameters. Intrinsic to your camera.
	private double verticalFOV;
	private double horizontalFOV;
	private double pixelWidth;
	private double pixelHeight;
	protected final int REFRESH_RATE; // fps of the camera

	// Placement Parameters. Allow processing to account for differences in
	// placement and give
	// outputs relative to the robot, not the cameras.
	private double horizontalOffset, verticalOffset, depthOffset;
	private double horizontalAngle, verticalAngle;

	// A list of the targets this camera is currently outputting to. Should be
	// set dynamically in order to allow access to
	// A specific target's sightings each frame
	protected HashMap<VisionTarget, SightingContainer> activeTargets = new HashMap<>();

	/**
	 * Instantiates the Camera object
	 * 
	 * @param refreshRate      the number of frames to process per second
	 * @param vFOV             the vertical FOV on the Camera in radians
	 * @param hFOV             the horizontal FOV on the Camera in radians
	 * @param xPixels          the number of pixels in the x direction (ex: 320)
	 * @param yPixels          the number of pixels in the y direction (ex: 240)
	 * @param horizontalOffset the distance from the center of the robot to the
	 *                         center of the lens of the Camera (in the horizontal
	 *                         direction) in arbitrary units
	 * @param verticalOffset   the distance (in arbitrary units) from the ground to
	 *                         the center of the Camera lens
	 * @param depthOffset      the distance of the camera from the front of the
	 *                         robot, in arbitrary units. Currently unused.
	 * @param hAngle           the horizontal placement angle of the Camera in
	 *                         radians (is the camera facing left or right?)
	 * @param vAngle           the vertical placement angle of the Camera in radians
	 *                         (is the camera facing upwards or downwards?)
	 */
	public CVCamera(int refreshRate, double vFOV, double hFOV, double xPixels, double yPixels, double horizontalOffset,
			double verticalOffset, double depthOffset, double hAngle, double vAngle) {
		this.REFRESH_RATE = refreshRate;
		this.verticalFOV = vFOV;
		this.horizontalFOV = hFOV;
		this.pixelWidth = xPixels;
		this.pixelHeight = yPixels;
		this.horizontalOffset = horizontalOffset;
		this.verticalOffset = verticalOffset;
		this.depthOffset = depthOffset;
		this.horizontalAngle = hAngle;
		this.verticalAngle = vAngle;
	}

	/**
	 * Gives the number of sightings of a target the Camera has identified for the
	 * last frame
	 * 
	 * @param vt the vision target to check
	 * @return the number of sightings identified
	 */
	public int sightingCount(VisionTarget vt) {
		if (!activeTargets.containsKey(vt))
			return 0;
		return activeTargets.get(vt).sightingCount();
	}

	/**
	 * Returns a list of all validated sightings of a given target for the last
	 * frame
	 * 
	 * @param vt the specified vision target
	 * @return the list of sightings
	 */
	public ArrayList<Sighting> getSightings(VisionTarget vt) {
		return activeTargets.containsKey(vt) ? activeTargets.get(vt).getSightings() : null;
	}

	/**
	 * Returns the vertical field of view of this camera, in radians
	 * @return the vertical field of view, in radians
	 */
	public double getVerticalFOV() {
		return verticalFOV;
	}

	/**
	 * Returns the horizontal field of view of this camera, in radians
	 * @return the horizontal field of view, in radians
	 */
	public double getHorizontalFOV() {
		return horizontalFOV;
	}

	/**
	 * Returns the number of pixels wide each frame is. For a 320x240 image, this would bee 320
	 * @return the width of the stream in pixels
	 */
	public double getPixelWidth() {
		return pixelWidth;
	}

	/**
	 * Returns the number of pixels tall each frame is. For a 320x240 image, this would bee 240
	 * @return the height of the stream in pixels
	 */
	public double getPixelHeight() {
		return pixelHeight;
	}

	/**
	 * returns the refreshrate, or framerate of this camera
	 * @return the number of frames per second this camera should process
	 */
	public int getRefreshRate() {
		return REFRESH_RATE;
	}

	/**
	 * Returns the number of arbitrary units off from the center of the robot the camera is placed horizontally (left-to-right).
	 * @return the distance from the center of the camera lens to the center of the robot, in user-decided arbitrary units
	 */
	public double getHorizontalOffset() {
		return horizontalOffset;
	}

	/**
	 * Returns the number of arbitrary units off of the ground the camera is placed vertically.
	 * @return the distance from the center of the camera lens to the ground, in user-decided arbitrary units
	 */
	public double getVerticalOffset() {
		return verticalOffset;
	}

	/**
	 * Returns the number of arbitrary units off from the front of the robot the camera is placed (front-to-back).
	 * <br><b>UNUSED VALUE</b>
	 * @return the distance from the front of the robot to the camera lens, in user-decided arbitrary units
	 */
	public double getDepthOffset() {
		return depthOffset;
	}

	/**
	 * Returns the placement yaw angle (left-to-right) of the camera on the robot
	 * @return the yaw angle, in radians.
	 */
	public double getHorizontalAngle() {
		return horizontalAngle;
	}

	/**
	 * Returns the placement pitch angle (up-and-down) of the camera on the robot
	 * @return the pitch angle, in radians.
	 */
	public double getVerticalAngle() {
		return verticalAngle;
	}

}
