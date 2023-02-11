package bytelogic.utils;

import arc.util.serialization.*;
import mindustry.io.*;

import java.io.*;

public class JsonUtils{
    public static void saveWriter(Runnable runnable){

        BaseJsonWriter writer = JsonIO.json.getWriter();
        runnable.run();
        if(writer!=null) JsonIO.json.setWriter(writer);
    }
    public interface ErrorRunnable{
        void run() throws  IOException;
    }
    public static void saveWriterIO(ErrorRunnable runnable) throws IOException{

        BaseJsonWriter writer = JsonIO.json.getWriter();
        runnable.run();
        if(writer!=null) JsonIO.json.setWriter(writer);
    }
}
