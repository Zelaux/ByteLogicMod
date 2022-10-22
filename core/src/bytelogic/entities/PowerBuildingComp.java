package bytelogic.entities;

import bytelogic.gen.*;
import mindustry.annotations.Annotations.*;
import mindustry.gen.*;

@Component
abstract class ByteLogicBuildingComp implements Buildingc, ByteLogicBuildingc{
    public abstract void updateSignalState();

    public void beforeUpdateSignalState(){

    }
   public abstract boolean output(int dir);
    public abstract boolean acceptSignal(ByteLogicBuildingc otherBuilding, int signal);
}
