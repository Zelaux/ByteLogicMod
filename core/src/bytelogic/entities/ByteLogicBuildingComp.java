package bytelogic.entities;

import arc.struct.*;
import bytelogic.gen.*;
import bytelogic.type.*;
import mindustry.annotations.Annotations.*;
import mindustry.gen.*;

@Component
abstract class ByteLogicBuildingComp implements Buildingc, ByteLogicBuildingc, IndexableEntity__byteLogicBuild{
    public abstract void updateSignalState();

    @Override
    public abstract void nextBuildings(IntSeq positions);

    public void beforeUpdateSignalState(){

    }
    public abstract int tickAmount();
   public abstract boolean canOutputSignal(int dir);
   public abstract Signal currentSignal();
    public abstract boolean acceptSignal(ByteLogicBuildingc otherBuilding, Signal signal);
}
