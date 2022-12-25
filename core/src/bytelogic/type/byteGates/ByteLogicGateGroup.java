package bytelogic.type.byteGates;

import arc.*;
import arc.util.*;
import bytelogic.type.*;
import org.jetbrains.annotations.*;

public class ByteLogicGateGroup implements GroupOfTiledStructure{
    public static final ByteLogicGateGroup signalProviders = new ByteLogicGateGroup("signal-providers");
    public static final ByteLogicGateGroup inputOutput = new ByteLogicGateGroup("input-output");
    public static final ByteLogicGateGroup bitOperators = new ByteLogicGateGroup("bit-operators");
    public static final ByteLogicGateGroup unaryOperators = new ByteLogicGateGroup("unary-operators");
    public static final ByteLogicGateGroup binaryOperators = new ByteLogicGateGroup("binary-operators");
    public static ByteLogicGateGroup draw=new ByteLogicGateGroup("draw");
    private static int total = 0;
    public final String name;
    public final int id;

    public ByteLogicGateGroup(String name){
        this.name = name;
        id = total++;
    }

    @Override
    public int compareTo(@NotNull GroupOfTiledStructure o){
        if(o instanceof ByteLogicGateGroup group){
            return Integer.compare(id, group.id);
        }
        return 0;
    }

    public String localized(){
        return Core.bundle.get("byte-logic-gate-group." + Strings.camelToKebab(name), name);
    }
}
