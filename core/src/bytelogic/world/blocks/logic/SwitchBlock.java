package bytelogic.world.blocks.logic;

import arc.Graphics.*;
import arc.Graphics.Cursor.*;
import arc.audio.*;
import arc.math.geom.*;
import arc.util.*;
import bytelogic.gen.*;
import bytelogic.type.*;
import bytelogic.ui.guide.*;
import bytelogic.world.blocks.logic.BinaryLogicBlock.*;
import bytelogic.world.blocks.logic.SignalBlock.*;
import mindustry.game.*;
import mindustry.gen.*;

import static mindustry.Vars.player;

public class SwitchBlock extends LogicBlock{
    protected static final Signal oneSignal = Signal.valueOf(1);
    public Sound clickSound = Sounds.buttonClick;

    public SwitchBlock(String name){
        super(name);
        consumesTap = true;
        this.<Long, SwitchBuild>config(Long.class, (build, value) -> {
            build.nextSignal.setNumber(value);
        });
    }

    @Override
    public void init(){
        if(blockShowcase == null){
            blockShowcase = new BlockShowcase(this, 5, 5, (world, isSwitch) -> {

                world.tile(0, 1).setBlock(this, Team.sharded, 0);

                world.tile(1, 1).setBlock(byteLogicBlocks.relay, Team.sharded, 0);
                world.tile(2, 1).setBlock(byteLogicBlocks.multiplier, Team.sharded, 0);
                world.tile(2, 1).build.<BinaryLogicBuild>as().inputType = 1;

                world.tile(2, 2).setBlock(byteLogicBlocks.signalBlock, Team.sharded, 3);
                world.tile(2, 2).build.<SignalLogicBuild>as().nextSignal.setNumber(-1);
                world.tile(3, 1).setBlock(byteLogicBlocks.displayBlock, Team.sharded);
                return new Point2[]{Tmp.p1.set(0, 1)};
            });
            blockShowcase.hasNoSwitchMirror(false);
        }
        super.init();
    }

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
            return false;
        }

        @Override
        public void beforeUpdateSignalState(){
            if(doOutput && canOutputSignal(rotation)){
                front().<ByteLogicBuildingc>as().acceptSignal(this, lastSignal);
            }
        }

        /*
        @Override
        public int signal(){
            return nextSignal;
        }*/
    }
}
