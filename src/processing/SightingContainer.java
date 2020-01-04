package processing;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import cameras.CVCamera;
import cameras.Camera;
import math.CameraMath;

/**
 * A class meant for storing the sightings of a particular target by a
 * particular camera. Each camera-target pair defines one.
 * 
 * @author Jeff
 *
 */
public class SightingContainer {

	private ArrayList<Sighting> rawSightings = new ArrayList<>();// Raw, unfiltered, unprocessed sightings
	private ArrayList<Sighting> processedSightings = new ArrayList<>();// Filtered, processed, valid sightings
	private CVCamera camera;
	private VisionTarget visionTarget;

	/**
	 * Creates the CVCamera SightingContainer
	 * 
	 * @param c the CVCamera the container uses to get its image data
	 * @param v the vision target it finds
	 */
	public SightingContainer(CVCamera c, VisionTarget v) {
		this.camera = c;
		this.visionTarget = v;
	}

	/**
	 * Returns the most recent target sightings from the given camera
	 * 
	 * @return a list of all the sightings
	 */
	public ArrayList<Sighting> getSightings() {
		return new ArrayList<>(processedSightings);
	}

	/**
	 * Not used directly in the library. May be used in other camera implementations
	 * if traditional processing is not wanted. Overwrites the currently-saved sightings
	 * to the input
	 * 
	 * @param processedSightings a list of processed sightings to save in this SightingContainer
	 */
	public void setSightings(ArrayList<Sighting> processedSightings) {
		this.processedSightings=processedSightings;
	}

	/**
	 * Returns the number of sightings of this target from this camera
	 * 
	 * @return the number of sightings of this target from this camera
	 */
	public int sightingCount() {
		return processedSightings.size();
	}

	/**
	 * Updates the data from the CVCamera
	 * 
	 * @param polys the data from the CVCamera
	 */
	public void updateObjects(ArrayList<Sighting> polys) {
		rawSightings.clear();
		rawSightings.addAll(polys);
		rawSightings = visionTarget.validateRawSightings(rawSightings);
		calculateCameraBasedPitches();
		calculateCameraBasedDistances();
		calculateCameraBasedYaws();
		placementAdjustCartesian();
		calculateRelativeAspectRatios();
		calculateRotations();
		processedSightings = visionTarget.validateProcessedSightings(new ArrayList<>(rawSightings));
	}

	/**
	 * Uses the center y pixel of the sightings to calculate the vertical angles
	 * from the camera to each sighting, then stores them in the sightings.
	 */
	private void calculateCameraBasedPitches() {
		for (Sighting s : rawSightings) {
			s.setCameraBasedPitch(
					CameraMath.getYAngle(s.getCenterY(), camera.getPixelHeight(), camera.getVerticalFOV()));
		}
	}

	/**
	 * Uses the vertical angle from the camera to the sightings to calculate the
	 * distances from the camera to each sighting, then stores the distances in the
	 * sightings.
	 */
	private void calculateCameraBasedDistances() {
		for (Sighting s : rawSightings) {
			s.setCameraBasedDistance(CameraMath.calculateDistance(s.getCameraBasedPitch().getAsDouble(),
					camera.getVerticalAngle(), visionTarget.getHeight(), camera.getVerticalOffset()));
		}
	}

	/**
	 * Uses the center x pixel of the sightings to calculate the horizontal angles
	 * from the camera to the sightings, then stores them in the sightings.
	 */
	private void calculateCameraBasedYaws() {
		for (Sighting s : rawSightings) {
			s.setCameraBasedYaw(
					CameraMath.getXAngle(s.getCenterX(), camera.getPixelWidth(), camera.getHorizontalFOV()));
		}
	}

	/**
	 * Adjusts for the placement of the camera on the robot using cartesian
	 * coordinates. Treats the camera as a point (horizontalOffset, -depthOffset),
	 * then calculates the distance and angle of the sighting relative to the origin
	 * (usually the center of the robot)
	 */
	private void placementAdjustCartesian() {
		for (Sighting s : rawSightings) {
			Point2D p = CameraMath.calcSightingCoords(s.getCameraBasedDistance().getAsDouble(),
					s.getCameraBasedYaw().getAsDouble(), camera.getHorizontalOffset(), camera.getDepthOffset(),
					camera.getHorizontalAngle());
			// From that, find the distance and angle of the sighting from the origin.
			s.setRobotBasedDistance(p.distance(0, 0));
			s.setRobotBasedYaw(CameraMath.calcRobotAngle(p));
		}

	}

	/**
	 * Calculates the relative aspect ratio of the sighting to the target. For
	 * example, if the H:V aspect ratio of the target is 3:4 and the H:V aspect
	 * ratio of the sighting is 1:2, the relative aspect ratio is (1/2)/(3/4), or
	 * 2/3. Stores the result in the sightings.
	 */
	private void calculateRelativeAspectRatios() {
		for (Sighting s : rawSightings) {
			s.setRelativeAspectRatio(
					CameraMath.calcRelativeAspectRatio(s.getAspectRatio(), visionTarget.getAspectRatio()));
		}
	}

	/**
	 * Calculates the horizontal rotation of the vision target relative to the
	 * CVCamera. Usually unreliable. If your sighting has multiple pieces (for
	 * example, two pieces of tape), consider calculating rotation using their
	 * relative distances instead.
	 */
	private void calculateRotations() {
		for (Sighting s : rawSightings) {
			double relAspectRatio = s.getRelativeAspectRatio().getAsDouble();
			s.setRobotBasedRotation(CameraMath.calcRobotBasedRotation(relAspectRatio));
		}
	}

	// DEPRECATED CODE
	/**
	 * DEPRECATED METHOD, may be preferred if cartesian method proves ineffective
	 * Adjusts for camera placement trigonometry
	 */
	// Desmos for proof of concept: https://www.desmos.com/calculator/jwrnkmhvu1
	private void placementAdjustTrig() {
		// This math is based off of representing the sighting, horizontal center of the
		// robot, and camera as 3 points on a triangle.
		// Using the laws of sines and cosines, we can account for the horizontal shift
		// in the camera and find the angle of the sighting
		// relative to the center of the robot.
		// Angle names such as "angleFromSighting" represent the angle of the triangle
		// at the point specified (Sighting in the example)
		// DEPRECATED CODE, math should still work but is gross and inefficient
		for (Sighting p : rawSightings) {
			double angleFromCamera = Math.PI / 2.0
					+ (p.getCameraBasedYaw().getAsDouble() - camera.getHorizontalAngle());
			double cameraBasedDistance = p.getCameraBasedDistance().getAsDouble();
			double robotBasedDistance = Math.sqrt(cameraBasedDistance * cameraBasedDistance
					+ camera.getHorizontalOffset() * camera.getHorizontalOffset()
					- 2.0 * cameraBasedDistance * camera.getHorizontalOffset() * Math.cos(angleFromCamera));
			p.setRobotBasedDistance(robotBasedDistance);

			if (camera.getHorizontalOffset() != 0) {
				// law of sines
				double angleFromSighting = Math
						.asin(camera.getHorizontalOffset() * Math.sin(angleFromCamera) / robotBasedDistance);
				// Due to range restrictions on arcsin, if the triangle is obtuse (ie if the sum
				// of the squares of the legs is less than the square of the hypotenuse, correct
				// to make the angle obtuse).
				if (robotBasedDistance * robotBasedDistance
						+ cameraBasedDistance * cameraBasedDistance < camera.getHorizontalOffset()
								* camera.getHorizontalOffset()) {
					angleFromSighting += 2 * (Math.PI / 2 - angleFromSighting);
				}
				// sum of the angles in a triangle is pi
				double angleFromRobot = Math.PI - angleFromCamera - angleFromSighting;
				// transform the angle so that 0 is in front of the robot, not out of view
				p.setRobotBasedYaw(Math.PI / 2.0 - angleFromRobot);
			} else {
				p.setRobotBasedYaw(angleFromCamera);
			}
		}
	}

}