package bytelogic.world.blocks.logic;


import bytelogic.gen.*;

public abstract class UnaryLogicBlock extends LogicBlock{
    protected /*@NonNull*/ UnaryProcessor processor;

    public UnaryLogicBlock(String name){
        super(name);
    }


    public interface UnaryProcessor{
        int process(int signal);
    }

    public class UnaryLogicBuild extends LogicBuild{
        @Override
        public void updateTile(){
            super.updateTile();

        }

        @Override
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, int signal){
            if(back() == otherBuilding) return super.acceptSignal(otherBuilding, signal);
            return false;
        }

        @Override
        public void updateSignalState(){
            lastSignal = processor.process(nextSignal);
            nextSignal = 0;

        }

        @Override
        public void beforeUpdateSignalState(){
            if(doOutput && output((byte)rotation)){
                front().<LogicBuild>as().acceptSignal(this, lastSignal);
            }
        }
        /*
        @Override
        public int signal(){
            return processor.process(sback());
        }*/
    }
}
