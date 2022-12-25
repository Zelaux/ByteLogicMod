package bytelogic.io;

import arc.graphics.*;
import arc.util.io.*;
import mindustry.io.*;
import mma.annotations.*;
import mma.io.*;
import mma.ui.tiledStructures.*;

@ModAnnotations.TypeIOHandler
public class BLTypeIO extends ModTypeIO{
    public static void writeTiledStructures(Writes write, TiledStructures executor){
        write.str(JsonIO.write(executor));
    }

    /*    public static TiledStructures readTiledStructures(Reads read){
            String string = read.str();
            System.out.println("String: "+string);
            return JsonIO.read(TiledStructures.class, string);
        }*/
    public static TiledStructures readTiledStructures(Reads read, TiledStructures target){
        String string = read.str();
//        System.out.println("String: " + string);
        return JsonIO.read(TiledStructures.class, target, string);
    }

    public static void writeColor(Writes write, Color color){
        write.f(color.r);
        write.f(color.g);
        write.f(color.b);
        write.f(color.a);
    }

    public static Color readColor(Reads read){
        Color color = new Color();
        color.r = read.f();
        color.g = read.f();
        color.b = read.f();
        color.a = read.f();
        return color;
    }
}
