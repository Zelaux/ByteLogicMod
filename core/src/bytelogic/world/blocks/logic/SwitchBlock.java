package bytelogic.world.blocks.logic;

import arc.Graphics.*;
import arc.Graphics.Cursor.*;
import arc.audio.*;
import bytelogic.gen.*;
import bytelogic.type.*;
import mindustry.gen.*;

import static mindustry.Vars.player;

public class SwitchBlock extends LogicBlock{
    public Sound clickSound = Sounds.buttonClick;

    public SwitchBlock(String name){
        super(name);
        consumesTap = true;
        this.<Long, SwitchBuild>config(Long.class, (build, value) -> {
            build.nextSignal.setNumber(value);
        });
    }
protected static final Signal oneSignal=Signal.valueOf(1);

    public class SwitchBuild extends LogicBuild{
        @Override
        public Cursor getCursor(){
            return interactable(player.team()) ? SystemCursor.hand : SystemCursor.arrow;
        }

        @Override
        public Signal currentSignal(){
            return nextSignal;
        }

        @Override
        public void tapped(){
            nextSignal.xor(oneSignal);
            configure(nextSignal.number());
            clickSound.at(tile);
        }

        @Override
        public Long config(){
            return nextSignal.number();
        }

        @Override
        public void updateSignalState(){
            lastSignal.set(nextSignal);
        }

        @Override
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, Signal signal){
            return  false;
        }

        @Override
        public void beforeUpdateSignalState(){
            if (doOutput && canOutputSignal(rotation)){
                front().<ByteLogicBuildingc>as().acceptSignal(this,lastSignal);
            }
        }

        /*
        @Override
        public int signal(){
            return nextSignal;
        }*/
    }
}
