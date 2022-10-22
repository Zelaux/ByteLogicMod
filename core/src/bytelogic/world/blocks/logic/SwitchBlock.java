package bytelogic.world.blocks.logic;

import arc.Graphics.*;
import arc.Graphics.Cursor.*;
import arc.audio.*;
import bytelogic.gen.*;
import mindustry.gen.*;

import static mindustry.Vars.player;

public class SwitchBlock extends LogicBlock{
    public Sound clickSound = Sounds.buttonClick;

    public SwitchBlock(String name){
        super(name);
        consumesTap = true;
        this.<Integer, SwitchBuild>config(Integer.class, (build, value) -> {
            build.nextSignal = value;
        });
    }


    public class SwitchBuild extends LogicBuild{
        @Override
        public Cursor getCursor(){
            return interactable(player.team()) ? SystemCursor.hand : SystemCursor.arrow;
        }

        @Override
        protected int currentSignal(){
            return nextSignal;
        }

        @Override
        public void tapped(){
            nextSignal ^= 1;
            clickSound.at(tile);
        }

        @Override
        public Integer config(){
            return nextSignal;
        }

        @Override
        public void updateSignalState(){
            lastSignal = nextSignal;
        }

        @Override
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, int signal){
            return  false;
        }

        @Override
        public void beforeUpdateSignalState(){
            if (doOutput && output(rotation)){
                front().<LogicBuild>as().acceptSignal(this,lastSignal);
            }
        }
        /*
        @Override
        public int signal(){
            return nextSignal;
        }*/
    }
}
