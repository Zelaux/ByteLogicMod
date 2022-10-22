package bytelogic.io;

import arc.graphics.Color;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mma.annotations.ModAnnotations;

@ModAnnotations.TypeIOHandler
public class TMTypeIO {
    public static void writeColor(Writes write, Color color) {
        write.f(color.r);
        write.f(color.g);
        write.f(color.b);
        write.f(color.a);
    }
    public static Color readColor(Reads read) {
        Color color = new Color();
        color.r=read.f();
        color.g=read.f();
        color.b=read.f();
        color.a=read.f();
        return color;
    }
}
