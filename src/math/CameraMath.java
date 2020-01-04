package math;
import java.awt.geom.Point2D;

/**
 * A static class containing methods for calculating relevant information from
 * camera data.
 * 
 * @author Jeff
 *
 */
public class CameraMath {

	/**
	 * Calculates the distance along the floor from the camera to the sighting,
	 * given the camera and the sighting's height off the ground Math explained
	 * here: https://www.desmos.com/calculator/wenmpiptft
	 * 
	 * @param cameraBasedPitch The vertical angle from the camera to the sighting,
	 *                         not accounting for camera tilt
	 * @param cameraVAngle     The amount that the camera is tilted off the ground,
	 *                         measured in radians with an upward tilt being
	 *                         positive
	 * @param targetHeight     The height of the center of the target off the
	 *                         ground, in arbitrary units
	 * @param cameraHeight     The height of the center of the camera lens off the
	 *                         ground, in arbitrary units.
	 * @return The distance along the floor from the camera to the sighting, in
	 *         whatever units you used for your input.
	 */
	public static double calculateDistance(double cameraBasedPitch, double cameraVAngle, double targetHeight,
			double cameraHeight) {
		// account for the camera being tilted up or down
		double robotBasedPitch = cameraBasedPitch + cameraVAngle;
		// difference in height between the camera and the target
		double changeInHeight = targetHeight - cameraHeight;
		// calculate the distance to the target (length of a straight line in 3D from
		// the lens to the center of target)
		double distanceToSighting = changeInHeight / Math.sin(robotBasedPitch);

		// use that to get the distance along the floor from the camera to the target
		// (so the length of the line in 2D along the floor from the camera to the
		// target)
		return distanceToSighting * Math.cos(robotBasedPitch);

	}

	/**
	 * Calculates the cartesian coordinates of the given sighting if the center of
	 * the robot is (0,0), given the distance and angle from the camera (the point
	 * (horizontalOffset, -depthOffset) to the sighting. Math explained here:
	 * https://www.desmos.com/calculator/gyrqes9eo6
	 * 
	 * @param cameraBasedDistance The distance from the camera to the sighting, in
	 *                            arbitrary units
	 * @param cameraBasedYaw      The horizontal angle from the camera to the
	 *                            sighting, in radians.
	 * @param horizontalOffset    The number of units to the right/left the camera
	 *                            is offset from the center of the robot, with right
	 *                            being positive
	 * @param depthOffset         the number of units the camera is offset from the
	 *                            center of the robot forwards/backwards, with
	 *                            backwards being positive.
	 * @param cameraYawOffset     The angle of tilt of the camera left-to-right,
	 *                            with right being positive, in radians
	 * @return The coordinates of the sighting if the center of the robot is (0,0),
	 *         and coordinates are measured using the units given above
	 */

	public static Point2D calcSightingCoords(double cameraBasedDistance, double cameraBasedYaw, double horizontalOffset,
			double depthOffset, double cameraYawOffset) {
		cameraBasedYaw += cameraYawOffset;
		// If the robot is (0,0), find the x and y coords of the sighting.
		double targetX = cameraBasedDistance * Math.sin(cameraBasedYaw) + horizontalOffset;
		double targetY = cameraBasedDistance * Math.cos(cameraBasedYaw) + depthOffset;
		return new Point2D.Double(targetX, targetY);
	}

	/**
	 * Calculates the angle from the origin to a given point, p, with 0 being along
	 * the y axis.
	 * 
	 * @param p the coordinates of a point in space
	 * @return The angle from the origin to that point, in radians.
	 */
	public static double calcRobotAngle(Point2D p) {
		if (p.getY() >= 0) {
			return Math.atan(p.getX() / p.getY());
		} else if (p.getX() >= 0) {
			return Math.atan(p.getX() / p.getY()) + Math.PI;
		}
		return Math.atan(p.getX() / p.getY()) - Math.PI;
	}

	/**
	 * Given the aspect ratios of the real-life target and what the camera sees,
	 * return their ratio (one divided by the other)
	 * 
	 * @param sightingAR the aspect ratio of the sighting
	 * @param targetAR   the aspect ratio of the target
	 * @return the
	 */
	public static double calcRelativeAspectRatio(double sightingAR, double targetAR) {
		return sightingAR / targetAR;// Consider making changes to account for different targets/conditions
	}

	/**
	 * Finds the horizontal angle to a specific pixel on the CVCamera Math explained
	 * here: https://www.desmos.com/calculator/6ao6tvsriq
	 * 
	 * @param x          the pixel's x coordinate
	 * @param pixelWidth the width of the camera stream in pixels. For a 320x240
	 *                   resolution, that's 320.
	 * @param HFOV       the horizontal field of view of the camera, in radians.
	 * @return the horizontal angle from the center of the camera to that pixel.
	 */
	public static double getXAngle(double x, double pixelWidth, double HFOV) {
		// uses pinhole model of camera to find angle
		double horizontalFocalLength = (pixelWidth / 2.0) / Math.tan(HFOV / 2.0);
		double centerX = (pixelWidth / 2.0) - 0.5;// -.5 accounts for 0 being lowest pixel value and
													// (pixelWidth-1) being the highest
		return Math.atan((x - centerX) / horizontalFocalLength);
	}

	public static double calcRobotBasedRotation(double relAspectRatio) {
		return Math.max(Math.acos(relAspectRatio), 0);
	}

	/**
	 * Finds the vertical angle to a specific pixel on the CVCamera Math explained
	 * here: https://www.desmos.com/calculator/wenmpiptft
	 * 
	 * @param y           the pixel's y coordinate
	 * @param pixelHeight the height of the camera stream in pixels. For a 320x240
	 *                    resolution, that's 240.
	 * @param VFOV        the vertical field of view of the camera, in radians.
	 * @return the vertical angle from the center of the camera to that pixel.
	 */
	public static double getYAngle(double y, double pixelHeight, double VFOV) {
		// uses pinhole model of camera to find angle
		double verticalFocalLength = (pixelHeight / 2.0) / Math.tan(VFOV / 2.0);
		double centerY = (pixelHeight / 2.0) - 0.5;// -.5 accounts for 0 being lowest pixel value and
													// (pixelHeight-1) being the highest
		return Math.atan((centerY - y) / verticalFocalLength);
	}

}
