package processing;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

/**
 * Represents a sighting of a target (i.e. a time a pipeline found something of
 * interest). Contains two types of information. <br/>
 * Pixel-based information is available at any time. It is stored in
 * non-optional datatypes. Optional information requires outside processing
 * before it is available. It is stored in OptionalDoubles.
 * 
 * @author Jack, Jeff
 *
 */
public class Sighting {
	// Pixel-based values. No additional processing necessary.
	protected int topLeftX, topLeftY; // top left x and y coordinates of this sighting.
	protected double centerX, centerY; // center x and y coordinates of this sighting
	protected double height, width;// the height and width of this sighting in pixels
	protected double area;// The number of pixels contained by this sighting
	protected double aspectRatio; // The ratio of the sighting's width to its height
	protected Rectangle boundingRect; // A rectangle with the same center coordinates, width and height as the
										// sighting
	protected double solidity; // The ratio of the sighting's area to its bounding rectangle's area
	protected int rawSightingCount = 1; // The number of sightings that have been added to this one (+1 for the
										// original)
	protected Area contours; // A representation of the shape of this sighting
	protected List<Point> rawPoints; // The raw OpenCV points passed into the constructor

	// Optional values. Must be calculated using external trigonometry. See getters
	// for in-depth descriptions.
	protected OptionalDouble robotBasedYaw, cameraBasedDistance, robotBasedDistance, robotBasedRotation,
			cameraBasedPitch, relativeAspectRatio,
			cameraBasedYaw = cameraBasedPitch = relativeAspectRatio = robotBasedRotation = cameraBasedDistance = robotBasedYaw = OptionalDouble
					.empty();

	/**
	 * Creates a sighting object from a contour/MatOfPoint
	 * 
	 * @param inputMat the contour to create it from
	 */
	public Sighting(MatOfPoint inputMat) {
		rawPoints = new ArrayList<Point>();
		Point[] points = inputMat.toArray();
		this.rawPoints.addAll(inputMat.toList());
		Path2D.Double poly = new Path2D.Double();
		for (int i = 1; i < points.length; i++) {
			poly.moveTo(points[i - 1].x, points[i - 1].y);
			poly.lineTo(points[i].x % points.length, points[i].y % points.length);
		}
		Rect processingRect = Imgproc.boundingRect(inputMat);
		contours = (new Area(poly));
		height = processingRect.height;
		width = processingRect.width;
		topLeftX = processingRect.x;
		topLeftY = processingRect.y;
		boundingRect = new Rectangle((int) topLeftX, (int) topLeftY, (int) width, (int) height);

		area = Imgproc.contourArea(inputMat);
		solidity = area / (width * height);
		aspectRatio = width / height;
		centerX = topLeftX + width / 2.0;
		centerY = topLeftY + height / 2.0;
	}
	/**
	 * Initializes an empty sighting
	 */
	public Sighting() {
		
	}
	
	/**
	 * Combines this sighting with another sighting
	 * 
	 * @param sighting the sighting to combine with this one
	 */
	public void addSighting(Sighting sighting) {
		rawSightingCount += sighting.rawSightingCount;
		rawPoints.addAll(sighting.getRawPoints());
		contours.add(sighting.getContours());
		boundingRect.add(sighting.boundingRect);
		this.topLeftX = Math.min(sighting.topLeftX, this.topLeftX);
		this.topLeftY = Math.min(sighting.topLeftY, this.topLeftY);
		double bottomRightX = Math.max(sighting.topLeftX + sighting.width, this.topLeftX + this.width);
		double bottomRightY = Math.max(sighting.topLeftY + sighting.height, this.topLeftY + this.height);
		width = bottomRightX - topLeftX;
		height = bottomRightY - topLeftY;
		centerX = topLeftX + width / 2.0;
		centerY = topLeftY + height / 2.0;
		area += sighting.area;
		solidity = area / (width * height);
		aspectRatio = width / height;
		cameraBasedYaw = cameraBasedPitch = robotBasedRotation = cameraBasedDistance = robotBasedYaw = OptionalDouble
				.empty();
	}

	/**
	 * Calculates the pixel distance to another sighting
	 * 
	 * @param sighting the sighting to find the distance to
	 * @return the distance to the given sighting, in pixels
	 */
	public double distanceTo(Sighting sighting) {
		double minDistance = Double.MAX_VALUE; // Assume the furthest possible distance
		for (int i = 0; i < rawPoints.size(); i++) {

			// grab two adjacent points of this sighting and draw a line between them
			Point basePoint1 = rawPoints.get(i);
			Point basePoint2 = rawPoints.get((i + 1) % rawPoints.size());
			Line2D baseLine = new Line2D.Double(basePoint1.x, basePoint1.y, basePoint2.x, basePoint2.y);

			for (int j = 0; j < sighting.rawPoints.size(); j++) {

				// Grab two adjacent points of the other sighting and draw a line between them
				Point goalPoint1 = sighting.rawPoints.get(j);
				Point goalPoint2 = sighting.rawPoints.get((j + 1) % sighting.rawPoints.size());
				Line2D goalLine = new Line2D.Double(goalPoint1.x, goalPoint1.y, goalPoint2.x, goalPoint2.y);

				// If it is the smallest we've found so far, set minDistance to the square of
				// the distance from the first goal point
				// to the base line
				minDistance = Math.min(minDistance, baseLine.ptSegDistSq(goalPoint1.x, goalPoint1.y));
				// If it is the smallest we've found so far, set minDistance to the square of
				// the distance from the first base point
				// to the goal line
				minDistance = Math.min(minDistance, goalLine.ptSegDistSq(basePoint1.x, basePoint1.y));
			}
		}
		return Math.sqrt(minDistance);
	}

	/**
	 * Returns a string representation of the sighting for debugging
	 * 
	 * @return the debug values associated with this sighting
	 */
	@Override
	public String toString() {
		return "(" + topLeftX + ", " + topLeftY + ") to (" + (topLeftX + width) + ", " + (topLeftY + height) + ")";
	}

	/**
	 * Gives the ratio of this sighting's aspect ratio to the aspect ratio of the
	 * target in the field. <br/>
	 * That is: (sighting.width/sighting.height)/(target.width/target.height) <br/>
	 * <b>Warning: value will be set to OptionalDouble.empty() if post-pipeline
	 * processing has not been completed on this sighting</b>
	 * 
	 * @return the relative aspect ratio of this sighting
	 */
	public OptionalDouble getRelativeAspectRatio() {
		return relativeAspectRatio;
	}

	/**
	 * Sets a value for the ratio of this sighting's aspect ratio to the aspect
	 * ratio of the target in the field. <br/>
	 * That is: (sighting.width/sighting.height)/(target.width/target.height)
	 * 
	 * @param relativeAspectRatio the ratio of aspect ratios to save
	 */
	protected void setRelativeAspectRatio(double relativeAspectRatio) {
		this.relativeAspectRatio = OptionalDouble.of(relativeAspectRatio);
	}

	/**
	 * Returns the yaw (left-to-right) angle of this Sighting, in radians relative
	 * to the horizontal center of the robot (at whatever distance from the front of
	 * the robot the camera is placed, since depth offset has not been implemented).
	 * <br/>
	 * <b>Warning: value will be set to OptionalDouble.empty() if post-pipeline
	 * processing has not been completed on this sighting</b>
	 * 
	 * @return the yaw value, in radians relative to the horizontal center of the
	 *         robot at the camera's depth
	 */
	public OptionalDouble getRobotBasedYaw() {
		return robotBasedYaw;
	}

	/**
	 * Sets a value for the yaw (left-to-right) angle of this Sighting, in radians
	 * relative to the horizontal center of the robot (at whatever distance from the
	 * front of the robot the camera is placed, since depth offset has not been
	 * implemented).
	 * 
	 * @param robotBasedYaw the angle to save
	 */
	public void setRobotBasedYaw(double robotBasedYaw) {
		this.robotBasedYaw = OptionalDouble.of(robotBasedYaw);
	}

	/**
	 * Returns the distance from the horizontal center of the robot (at the depth of
	 * the camera, since depth offset has not been implemented) to the center of the
	 * sighting, in whatever units the height of the target was set in. <br/>
	 * <b>Warning: value will be set to OptionalDouble.empty() if post-pipeline
	 * processing has not been completed on this sighting</b>
	 * 
	 * @return the distance in the target height's units
	 */
	public OptionalDouble getRobotBasedDistance() {
		return robotBasedDistance;
	}

	/**
	 * Sets a value for the distance from the horizontal center of the robot (at the
	 * depth of the camera, since depth offset has not been implemented) to the
	 * center of the sighting, in arbitrary units
	 * 
	 * @param distance the distance to save
	 */
	public void setRobotBasedDistance(double distance) {
		robotBasedDistance = OptionalDouble.of(distance);
	}

	/**
	 * Returns the distance from the center of the lens to the center of the
	 * sighting, in arbitrary units. <br/>
	 * <b>Warning: value will be set to OptionalDouble.empty() if post-pipeline
	 * processing has not been completed on this sighting</b>
	 * 
	 * @return the distance in the target height's units
	 */
	public OptionalDouble getCameraBasedDistance() {
		return cameraBasedDistance;
	}

	/**
	 * Sets a value for the distance from the horizontal center of the lens to the
	 * center of the sighting, in arbitrary units
	 * 
	 * @param distance the distance to save
	 */
	public void setCameraBasedDistance(double distance) {
		this.cameraBasedDistance = OptionalDouble.of(distance);
	}

	/**
	 * Returns the yaw (left-to-right) angle of the target on the field relative to
	 * the robot. In other words, it returns the angle the target would need to
	 * rotate to face directly towards the camera. <b>Warning: known to be
	 * inaccurate with current algorithm</b>
	 * 
	 * <br/>
	 * <b>Warning: value will be set to OptionalDouble.empty() if post-pipeline
	 * processing has not been completed on this sighting</b>
	 * 
	 * @return the angle of the target relative to the robot, in radians
	 */
	public OptionalDouble getRobotBasedRotation() {
		return robotBasedRotation;
	}

	/**
	 * Sets the value of the yaw (left-to-right) angle of the target on the field
	 * relative to the robot, in radians
	 * 
	 * @param robotBasedRotation the angle to save
	 */
	public void setRobotBasedRotation(double robotBasedRotation) {
		this.robotBasedRotation = OptionalDouble.of(robotBasedRotation);
	}

	/**
	 * Returns the pitch (vertical angle) of the sighting, relative to the camera,
	 * in radians. <br/>
	 * <b>Warning: value will be set to OptionalDouble.empty() if post-pipeline
	 * processing has not been completed on this sighting</b>
	 * 
	 * @return the pitch relative to the camera, in radians
	 */
	public OptionalDouble getCameraBasedPitch() {
		return cameraBasedPitch;
	}

	/**
	 * Sets a value for the pitch (vertical angle) of the sighting, relative to the
	 * camera, in radians
	 * 
	 * @param cameraBasedPitch the camera-based pitch to save
	 */
	public void setCameraBasedPitch(double cameraBasedPitch) {
		this.cameraBasedPitch = OptionalDouble.of(cameraBasedPitch);
	}

	/**
	 * Returns the yaw (left-to-right angle) of the sighting, relative to the
	 * camera, in radians. <br/>
	 * <b>Warning: value will be set to OptionalDouble.empty() if post-pipeline
	 * processing has not been completed on this sighting</b>
	 * 
	 * @return the yaw relative to the camera, in radians
	 */
	public OptionalDouble getCameraBasedYaw() {
		return cameraBasedYaw;
	}

	/**
	 * Sets the yaw (left-to-right angle) of the sighting, relative to the camera,
	 * in radians.
	 * 
	 * @param cameraBasedYaw the yaw value to save
	 */
	public void setCameraBasedYaw(double cameraBasedYaw) {
		this.cameraBasedYaw = OptionalDouble.of(cameraBasedYaw);
	}

	/**
	 * Returns the x coordinate of the top left corner of this sighting
	 * 
	 * @return the x coordinate, i.e. pixels from the left side of the image
	 */
	public int getTopLeftX() {
		return topLeftX;
	}

	/**
	 * Returns the y coordinate of the top left corner of this sighting
	 * 
	 * @return the y coordinate, i.e. pixels from the top of the image
	 */
	public int getTopLeftY() {
		return topLeftY;
	}

	/**
	 * Returns the x coordinate of the center of this sighting
	 * 
	 * @return the x coordinate, i.e. pixels from the left side of the image
	 */
	public double getCenterX() {
		return centerX;
	}

	/**
	 * Returns the y coordinate of the center of this sighting
	 * 
	 * @return the y coordinate, i.e. pixels from the top of the image
	 */
	public double getCenterY() {
		return centerY;
	}

	/**
	 * Returns the height of this sighting in pixels
	 * 
	 * @return the height, i.e. the difference between the y coordinates of the
	 *         bottommost and topmost pixels
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * Returns the width of this sighting in pixels
	 * 
	 * @return the width, i.e. the difference between the x coordinates of the
	 *         rightmost and leftmost pixels
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * Returns the area of this sighting, in pixels
	 * 
	 * @return the area encompassed by the sighting (not its bounding rectangle)
	 */
	public double getArea() {
		return area;
	}

	/**
	 * Returns the solidity of this sighting, i.e. the ratio between its area and
	 * the area of its bounding rectangle.
	 * 
	 * @return 1 for a full rectangle, smaller numbers for things that are less
	 *         solid, 0 for an empty sighting
	 */
	public double getSolidity() {
		return solidity;
	}

	/**
	 * Returns the aspect ratio of this sighting, i.e. the ratio between its width
	 * and height
	 * 
	 * @return the aspect ratio of this sighting
	 */
	public double getAspectRatio() {
		return aspectRatio;
	}

	/**
	 * Returns the number of original unprocessed sightings this sighting is
	 * composed of
	 * 
	 * @return 1 for a vanilla sighting, 2 for a sighting made from one call of
	 *         addSighting(), and so on.
	 */
	public int getRawSightingCount() {
		return rawSightingCount;
	}

	/**
	 * Returns a Java.awt.geom.Area object that represents the contours of this
	 * sighting. Used for advanced processing.
	 * 
	 * @return the Area representing this sighting's contours.
	 */
	public Area getContours() {
		return contours;
	}

	/**
	 * Returns the raw points that were used to make this Sighting
	 * 
	 * @return the Points that were passed into the constructor of this Sighting
	 */
	public List<Point> getRawPoints() {
		return rawPoints;
	}

	/**
	 * Returns the smallest rectangle that encompasses the entirety of this sighting
	 * 
	 * @return a rectangle with the same center x, center y, width and height as
	 *         this sighting.
	 */
	public Rectangle getBoundingRect() {
		return boundingRect;
	}

}