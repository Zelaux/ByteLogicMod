package bytelogic.world.blocks.logic;

import bytelogic.gen.ByteLogicBuildingc;

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
                if (canOutputSignal((byte)i)){
                    nearby(i).<ByteLogicBuildingc>as().acceptSignal(this,lastSignal);
                }
            }
        }

    }
}
