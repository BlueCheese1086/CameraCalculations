package example;

import java.util.ArrayList;
import java.util.OptionalDouble;

import cameras.AutomaticCVCamera;
import cameras.Camera;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.CameraServer;
import processing.Sighting;
import processing.VisionTarget;

public class Basic {
	public static void main(String[] args) {
		VisionTarget rocketTarget = new VisionTarget("Rocket", 28.75, 1);
		RocketPipeline pipeline = new RocketPipeline();
		AutomaticCVCamera c = new AutomaticCVCamera(60, 53.13 * Math.PI / 180.0, 54.0 * Math.PI / 180.0, 320, 240, 12,
				7.28125, 0, 0, 0);
		
		c.initializeCamera(CameraServer.getInstance().addAxisCamera("10.10.86.22"), "TestCamera");
		pipeline.addSupportedTarget(rocketTarget);
		c.addPipeline(pipeline);
	}

}