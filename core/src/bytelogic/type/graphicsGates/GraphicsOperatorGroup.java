package bytelogic.type.graphicsGates;

import bytelogic.type.*;
import org.jetbrains.annotations.*;

public class GraphicsOperatorGroup implements GroupOfTiledStructure{
    public final static GraphicsOperatorGroup def = new GraphicsOperatorGroup("def");
    private static int total = 0;
    public final String name;
    public final int id;

    public GraphicsOperatorGroup(String name){
        this.name = name;
        id = total++;
    }

    @Override
    public String localized(){
        return null;
    }

    @Override
    public int compareTo(@NotNull GroupOfTiledStructure o){

        if(o instanceof GraphicsOperatorGroup group){
            return Integer.compare(id, group.id);
        }
        return 0;
    }
}
