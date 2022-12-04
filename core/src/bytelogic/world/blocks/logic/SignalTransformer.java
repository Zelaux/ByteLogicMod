package bytelogic.world.blocks.logic;

import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import bytelogic.gen.*;
import bytelogic.type.*;
import bytelogic.ui.guide.*;
import bytelogic.world.blocks.logic.SignalBlock.*;
import mindustry.game.*;
import mindustry.ui.*;
import mindustry.world.*;
import mma.io.*;
import org.jetbrains.annotations.*;

public class SignalTransformer extends UnaryLogicBlock{
    protected static final Signal tmpSignal = new Signal();
    protected static final ByteWrites tmpWrites = new ByteWrites();
    protected static final ByteReads tmpRead = new ByteReads();

    public SignalTransformer(String name){
        super(name);
        /*this.<byte[], SignalTransformerBuild>config(byte[].class, (build, bytes) -> {
            build.selectedTypeSignal.fromBytes(bytes);
        });*/
     /*   this.<Integer, SignalTransformerBuild>config(Integer.class, (build, id) -> {
            SignalType type = SignalType.all[id];
            if(type == SignalTypes.nilType)
                type = SignalTypes.numberType;
            build.selectedType = type;
        });*/
        this.<byte[], SignalTransformerBuild>config(byte[].class, (build, bytes) -> {
            Container.set(bytes);
            build.selectedType = Container.selectedType;
            build.inputType = Container.inputType;
        });
        this.<String, SignalTransformerBuild>config(String.class, (build, typeName) -> {
            SignalType type = SignalType.findByName(typeName);
            if(type == SignalTypes.nilType)
                type = SignalTypes.numberType;
            build.selectedType = type;
        });
        processor = it -> it;
    }

    protected static byte[] stateAsBytes(SignalType selectedType, int inputType){
        tmpWrites.reset();
        tmpWrites.str(selectedType.getName());
        tmpWrites.i(inputType);
        return tmpWrites.getBytes();
    }

    @Override
    public void init(){
        if(blockShowcase == null){
            blockShowcase = new BlockShowcase(this, 5, 5, (world, isSwitch) -> {

                world.tile(0, 1).setBlock(byteLogicBlocks.signalBlock, Team.sharded, 0);

                world.tile(1, 1).setBlock(this, Team.sharded, 0);
                world.tile(2, 1).setBlock(byteLogicBlocks.relay, Team.sharded, 0);
//        world.tile(2, 1).build.<BinaryLogicBuild>as().inputType = 1;

                world.tile(2, 2).setBlock(byteLogicBlocks.signalBlock, Team.sharded, 0);
                world.tile(2, 2).build.<SignalLogicBuild>as().nextSignal.setNumber(-1);
                world.tile(3, 1).setBlock(byteLogicBlocks.displayBlock, Team.sharded);
                return new Point2[]{Tmp.p1.set(1, 1)};
            }){
                @Override
                public boolean shouldBuildConfiguration(@NotNull Block block){
                    return super.shouldBuildConfiguration(block) || block instanceof SignalTransformer;
                }
            };
            blockShowcase.hasNoSwitchMirror(false);
        }
        super.init();
    }


    static class Container{
        static SignalType selectedType;
        static int inputType;

        static void set(byte[] bytes){
            tmpRead.setBytes(bytes);
            selectedType = SignalType.findByName(tmpRead.str());
            if(selectedType == SignalTypes.nilType){
                selectedType = SignalTypes.numberType;
            }
            inputType = tmpRead.i();
        }

        public static byte[] bytes(){
            return stateAsBytes(selectedType, inputType);
        }
    }

    public class SignalTransformerBuild extends UnaryLogicBuild{
        SignalType selectedType = SignalTypes.numberType;

        @Override
        public void buildConfiguration(Table table){
            table.table(t -> {
                int i = 0;
                ButtonGroup<Button> group = new ButtonGroup<>();
                for(SignalType type : SignalType.all){
                    if(type == SignalTypes.nilType) continue;
                    t.button(type.getIcon(), Styles.squareTogglei, () -> {
                        configure(stateAsBytes(type, inputType));
                    }).size(48f).checked(selectedType == type).group(group);
                    i++;
                    if(i % 4 == 0){
                        t.row();
                    }
                }
            });
            table.row();
            super.buildConfiguration(table);
        }

        @Override
        public void beforeUpdateSignalState(){
            if(doOutput && canOutputSignal((byte)rotation)){
                lastSignal.type = selectedType;
                front().<ByteLogicBuildingc>as().acceptSignal(this, lastSignal);
            }
        }

        @Override
        public void updateSignalState(){
            lastSignal.set(nextSignal);
            lastSignal.type = selectedType;
            nextSignal.setZero();

        }

        @Override
        public Object config(){
            return stateAsBytes(selectedType, inputType);
        }

        @Override
        public void customWrite(Writes write){
            write.str(selectedType.getName());
        }

        @Override
        public void customRead(Reads read){
            selectedType = SignalType.findByName(read.str());
            if(selectedType == SignalTypes.nilType){
                selectedType = SignalTypes.numberType;
            }
        }

        @Override
        public short customVersion(){
            return 1;
        }
    }
}
