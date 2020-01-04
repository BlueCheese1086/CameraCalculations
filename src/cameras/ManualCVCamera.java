package cameras;

import static java.lang.Thread.interrupted;

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
 * A camera that processes a frame each time the updateSightings() method is
 * called AFTER the initializeCamera() method has been called. <br/>
 * Processing a frame entails running it through any pipelines that have been
 * added to this camera and storing pipeline outputs as sightings of their
 * supported targets. <br/>After a frame has been processed, the getSightings()
 * method will return any sightings from that frame for a particular vision
 * target.
 * 
 * @author Jeff
 *
 */
public class ManualCVCamera extends CVCamera {
	ArrayList<Pipeline> pipelines = new ArrayList<>();
	boolean initialized = false; // whether or not the camera has been initialized
	CvSink sink; //Set when camera initialized
	/**
	 * Instantiates the ManualCVCamera object
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
	public ManualCVCamera(int refreshRate, double vFOV, double hFOV, double xPixels, double yPixels,
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
	 * updateSightings() method may be called to grab a frame from the video source,
	 * run it through any pipelines that have been added to this camera, and save
	 * the resulting sightings of any targets. Those sightings can be accessed
	 * afterwards by getSightings()
	 * 
	 * @param source the source for frames of the CVCamera
	 * @param name   the name of the CVCamera stream
	 */
	public void initializeCamera(VideoSource source, String name) {
		sink= new CvSink(name);
		sink.setSource(source);
		sink.setEnabled(true);
		CameraServer.getInstance().startAutomaticCapture(source);
	}

	/**
	 * MUST BE CALLED PERIODICALLY IN ORDER TO FIND SIGHTINGS<br/>
	 * Grabs a frame from the video source, runs it through any pipelines that have
	 * been added to this camera, and saves the resulting sightings of any targets.
	 * Those sightings can be accessed afterwards by getSightings()
	 */
	public void updateSightings() {
		try {
			Mat sourceMat = new Mat();
			while (!interrupted()) {
				sink.grabFrame(sourceMat);
				process(sourceMat);
			}
		} catch (Exception e) {
			System.err.println("Error processing ManualCVCamera frame");
			e.printStackTrace();
		}

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