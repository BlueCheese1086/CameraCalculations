package example;

import java.util.ArrayList;

import cameras.ManualCVCamera;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.CameraServer;
import processing.Sighting;
import processing.VisionTarget;

public class Advanced {
	public static void main(String[] args) {
		VisionTarget rocketTarget = new VisionTarget("Rocket", 28.75, 1);
		rocketTarget.setPreProcessingFilter((ArrayList<Sighting> in) -> {
			ArrayList<Sighting> out = new ArrayList<>();
			for (Sighting sighting : in) {
				if (sighting.getArea() > 100 && sighting.getSolidity() > .2) {
					out.add(sighting);
				}
			}
			return out;
		});
		rocketTarget.setPostProcessingFilter((ArrayList<Sighting> in) -> {
			ArrayList<Sighting> out = new ArrayList<>();
			for (Sighting sighting : in) {
				if (sighting.getRobotBasedDistance().getAsDouble() > 20
						&& sighting.getCameraBasedYaw().getAsDouble() < Math.PI) {
					out.add(sighting);
				}
			}
			return out;
		});
		VisionTarget cargoTarget=new VisionTarget("Cargo", 20.01,1);
		RocketPipeline pipeline = new RocketPipeline();
		ManualCVCamera c = new ManualCVCamera(60, 53.13 * Math.PI / 180.0, 54.0 * Math.PI / 180.0, 320, 240, 12,
				7.28125, 0, 0, 0);

		c.initializeCamera(CameraServer.getInstance().addAxisCamera("10.10.86.22"), "TestCamera");
		pipeline.addSupportedTarget(rocketTarget);
		pipeline.addSupportedTarget(cargoTarget);
		c.addPipeline(pipeline);//If desired, any combination of cameras, pipelines, and targets can be set up this way.
		while (true) {
			c.updateSightings();
			System.out.println(c.getSightings(rocketTarget));
			System.out.println(c.getSightings(cargoTarget));
			Thread.sleep(100);
		}
	}

}