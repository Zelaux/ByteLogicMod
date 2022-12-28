package bytelogic.schematics;

import arc.func.*;
import arc.struct.*;
import mma.ui.tiledStructures.*;
import mma.ui.tiledStructures.TiledStructures.*;

public class ByteLogicTiledStructures extends TiledStructures{
    public ByteLogicTiledStructures(Seq<Prov<? extends TiledStructure>> allObjectiveTypes){
        super(allObjectiveTypes);
    }
    public void set(Seq<Prov<? extends TiledStructure>> allObjectiveTypes){
        this.allObjectiveTypes.set(allObjectiveTypes);
    }
}
