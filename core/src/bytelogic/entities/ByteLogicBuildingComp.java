package bytelogic.entities;

import arc.struct.*;
import bytelogic.gen.*;
import bytelogic.type.*;
import mindustry.annotations.Annotations.*;
import mindustry.gen.*;

@Component
abstract class ByteLogicBuildingComp implements Buildingc, ByteLogicBuildingc, bytelogic.gen.BLIndexableEntity___byteLogicBuild{
    public abstract void swapingSignalState();
    public abstract void updateDisplaySignalState();

    @Override
    public abstract void nextBuildings(IntSeq positions);

    public void transportSignalState(){

    }
    public abstract int tickAmount();
   public abstract boolean canOutputSignal(int dir);
   public abstract Signal displaySignal();
    public abstract boolean acceptSignal(ByteLogicBuildingc otherBuilding, Signal signal);
}
