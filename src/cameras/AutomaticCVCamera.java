package cameras;

import static java.lang.Thread.interrupted;
import static java.lang.Thread.sleep;

import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.VideoSource;
import edu.wpi.first.wpilibj.CameraServer;
import processing.Pipeline;
import processing.Sighting;
import processing.SightingContainer;

/**
 * A camera that automatically records video and processes frames at a speed
 * dictated by its refresh rate. Once the initializeCamera() method is run, this
 * processing will be run automatically in a separate thread. <br/>
 * Processing a frame entails running it through any pipelines that have been
 * added to this camera and storing pipeline outputs as sightings of their
 * supported targets. After a frame has been processed, the getSightings()
 * method will return any sightings from that frame for a particular vision
 * target.
 * 
 * @author Jeff
 *
 */

public class AutomaticCVCamera extends CVCamera {
	ArrayList<Pipeline> pipelines = new ArrayList<>();

	/**
	 * Instantiates the AutomaticCVCamera object
	 * 
	 * @param refreshRate      the number of frames to process per second
	 * @param vFOV             the vertical FOV on the Camera in radians
	 * @param hFOV             the horizontal FOV on the Camera in radians
	 * @param xPixels          the number of pixels in the x direction (ex: 320)
	 * @param yPixels          the number of pixels in the y direction (ex: 240)
	 * @param horizontalOffset the distance from the center of the robot to the
	 *                         center of the lens of the Camera (in the horizontal
	 *                         direction) in arbitrary units. Considers right to be
	 *                         the positive direction.
	 * @param verticalOffset   the distance (in arbitrary units) from the ground to
	 *                         the center of the camera lens
	 * @param depthOffset      The distance of the camera from the front of the
	 *                         robot, in arbitrary units. Considers forward to be
	 *                         the positive direction, so a camera 10 units inside
	 *                         the robot would have a depth offset of -10
	 * @param hAngle           the horizontal placement angle of the camera in
	 *                         radians. Considers right (clockwise) to be positive.
	 * @param vAngle           the vertical placement angle of the Camera in
	 *                         radians. Considers upward angles positive.
	 */
	public AutomaticCVCamera(int refreshRate, double vFOV, double hFOV, double xPixels, double yPixels,
			double horizontalOffset, double verticalOffset, double depthOffset, double hAngle, double vAngle) {
		super(refreshRate, vFOV, hFOV, xPixels, yPixels, horizontalOffset, verticalOffset, depthOffset, hAngle, vAngle);
	}

	/**
	 * Adds a pipeline that processes the images the CVCamera takes
	 * 
	 * @param p the pipeline to add.
	 */
	public void addPipeline(Pipeline p) {
		pipelines.add(p);
	}

	/**
	 * Initializes the CVCamera with a given Source. After this method is run, the
	 * CVCamera will initialize a new Thread, then, depending on the refresh rate,
	 * periodically grab a frame from the source, run it through any pipelines that
	 * have been added to this camera, and save the resulting sightings of any
	 * targets. Those sightings can be accessed at any time by getSightings()
	 * 
	 * @param source the source for frames of the CVCamera
	 * @param name   the name of the CVCamera stream
	 */
	public void initializeCamera(VideoSource source, String name) {
		CvSink sink = new CvSink(name);
		sink.setSource(source);
		sink.setEnabled(true);
		new Thread(() -> {
			try {
				CameraServer.getInstance().startAutomaticCapture(source);
				Mat sourceMat = new Mat();
				while (!interrupted()) {
					try {
						System.out.println("We're at least trying!");
						sink.grabFrame(sourceMat);
						process(sourceMat);
						sleep((int) (1000.0 / REFRESH_RATE));
					} catch (Exception e) {
						System.err.println("Problem processing AutomaticCVCamera frame");
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				System.err.println("Error setting up Automatic CV Camera");
				e.printStackTrace();
			}
		}).start();
	}

	/**
	 * Takes a frame that was grabbed by the Camera and runs the camera's
	 * CVPipelines on that frame, finding and storing sightings to the pipelines'
	 * supported targets
	 * 
	 * @param Source the captured frame
	 */
	private void process(Mat source) {
		for (Pipeline pipeline : pipelines) {
			ArrayList<Sighting> sightings = new ArrayList<Sighting>();
			ArrayList<MatOfPoint> mats = pipeline.process(source);
			mats.forEach(mop -> sightings.add(new Sighting(mop)));

			pipeline.getSupportedTargets().forEach(target -> {
				activeTargets.putIfAbsent(target, new SightingContainer(this, target));
				activeTargets.get(target).updateObjects((ArrayList<Sighting>) sightings.clone());
			});
		}
	}
}