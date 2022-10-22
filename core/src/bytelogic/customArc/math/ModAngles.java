package bytelogic.customArc.math;

import arc.math.Angles;
import arc.math.Mathf;

public class ModAngles extends Angles {
    public static float moveLerpToward(float angle, float to, float speed) {
        if (Math.abs(angleDist(angle, to)) < speed || angle==to) {
            return to;
        } else {
            angle = Mathf.mod(angle, 360.0F);
            to = Mathf.mod(to, 360.0F);
            if (angle==to)return to;
            if (angle > to == backwardDistance(angle, to) > forwardDistance(angle, to)) {
                angle -= speed;
            } else {
                angle += speed;
            }

            return angle;
        }
    }
}
