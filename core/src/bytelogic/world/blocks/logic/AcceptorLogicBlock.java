package bytelogic.world.blocks.logic;

import bytelogic.gen.*;
import mindustry.gen.*;

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
        public void beforeUpdateSignalState(){

            for(int i = 0; i < 4; i++){
                if (output((byte)i)){
                    nearby(i).<LogicBuild>as().acceptSignal(this,lastSignal);
                }
            }
        }

    }
}
