package bytelogic.world.blocks.logic;

import arc.struct.*;
import bytelogic.gen.*;
import mindustry.world.*;

public class AcceptorLogicBlock extends LogicBlock{

    public AcceptorLogicBlock(String name){
        super(name);
        doOutput = false;
    }

    public abstract class AcceptorLogicBuild extends LogicBuild{
        @Override
        public void update(){
            super.update();
        }

        @Override
        public void nextBuildings(IntSeq positions){
            for(int i = 0; i < 4; i++){
                if(canOutputSignal(i)){
                    Tile nearby = tile.nearby(i);
                    if(nearby != null) positions.add(nearby.array());
                }
            }
        }

        @Override
        public void beforeUpdateSignalState(){

            for(int i = 0; i < 4; i++){
                if(canOutputSignal((byte)i)){
                    nearby(i).<ByteLogicBuildingc>as().acceptSignal(this, lastSignal);
                }
            }
        }

    }
}
