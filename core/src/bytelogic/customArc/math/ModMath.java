package bytelogic.customArc.math;

import arc.math.Mathf;

public class ModMath  {
    public static float round(double value){
        float scale = 0.00000001f;
        return ((float) Math.ceil(value/ scale))*scale;
    }
    public static float atan(float tan){
        return round(Mathf.radiansToDegrees*Math.atan(tan));
    }
}
