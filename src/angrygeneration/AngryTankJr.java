package angrygeneration;

import robocode.*;
import java.awt.Color;

public class AngryTankJr extends Robot {

	 public void run() {
		 setBodyColor(new Color(255, 0, 0));
		 while (true) {
	            ahead(100);
	            turnGunRight(360);
	            back(100);
	            turnGunRight(360);
		 }
     }
 
    public void onScannedRobot(ScannedRobotEvent e) {
        fire(1);
    }

}