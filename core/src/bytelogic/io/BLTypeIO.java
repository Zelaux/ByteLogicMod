package bytelogic.io;

import arc.graphics.*;
import arc.util.io.*;
import bytelogic.schematics.*;
import bytelogic.type.*;
import mindustry.io.*;
import mma.annotations.*;
import mma.io.*;
import mma.ui.tiledStructures.*;

import java.io.*;

@ModAnnotations.TypeIOHandler
public class BLTypeIO extends ModTypeIO{
    public static void writeByteLogicSchematic(Writes write, ByteLogicSchematic schematic){
        try{
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ByteLogicSchematics.write(schematic, stream);
            byte[] array = stream.toByteArray();
            write.i(array.length);
            write.b(array);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public static ByteLogicSchematic readByteLogicSchematic(Reads read){
        int size = read.i();
        byte[] bytes = read.b(size);
        try{
            return ByteLogicSchematics.read(new ByteArrayInputStream(bytes));
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public static void writeSignal(Writes write, Signal signal){
        signal.write(write);
    }
    public static final ByteWrites  tmpWrites = new ByteWrites();
    public static final ByteReads tmpReads = new ByteReads();
    private static final Signal tmpSignal=new Signal();
    public static Signal readSignal(Reads read, Signal signal){
        signal.read(read);
        return signal;
    }
    public static Signal readSignal(Reads read){
        tmpSignal.read(read);
        return tmpSignal;
    }

    public static void writeTiledStructures(Writes write, TiledStructures executor){
        String str = JsonIO.write(executor);
        System.out.println(str);
        write.str(str);
    }
    public static void writeByteLogicTiledStructures(Writes write, ByteLogicTiledStructures executor){
        executor.write(write);
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
    public static TiledStructures readByteLogicTiledStructures(Reads read, ByteLogicTiledStructures target){
       target.read(read);
        return target;
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
