package bytelogic.entities;

import bytelogic.gen.*;
import bytelogic.type.*;
import mindustry.annotations.Annotations.*;
import mindustry.gen.*;

@Component
abstract class ByteLogicBuildingComp implements Buildingc, ByteLogicBuildingc{
    public abstract void updateSignalState();

    public void beforeUpdateSignalState(){

    }
   public abstract boolean canOutputSignal(int dir);
   public abstract Signal currentSignal();
    public abstract boolean acceptSignal(ByteLogicBuildingc otherBuilding, Signal signal);
}
