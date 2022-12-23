package bytelogic.world.blocks;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.io.*;
import bytelogic.gen.*;
import bytelogic.io.*;
import bytelogic.type.ByteLogicOperators.*;
import bytelogic.type.ByteLogicOperators.LinkedGate.*;
import bytelogic.type.*;
import bytelogic.ui.dialogs.*;
import bytelogic.world.blocks.logic.*;
import mindustry.annotations.Annotations.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.ui.*;
import mma.ui.tiledStructures.*;
import mma.ui.tiledStructures.TiledStructures.*;

import java.util.*;

import static mindustry.Vars.state;

public class ByteLogicProcessor extends LogicBlock{
    public static final TiledStructures tmpStructures = new TiledStructures(ByteLogicDialog.allByteLogicGates.as());
    @Load("@realName()-input-wire")
    public TextureRegion inputWire;
    @Load("@realName()-output-wire")
    public TextureRegion outputWire;

    public ByteLogicProcessor(String name){
        super(name);
        update = true;
        destructible = true;
        configurable = true;
        rotate = false;

        this.<String, ByteLogicProcessorBuild>config(String.class, (build, string) -> {
            build.setStructures(string);
//            System.out.println(JsonIO.print(string));
        });
    }

    public class ByteLogicProcessorBuild extends LogicBuild{
        public final TiledStructures structures = new TiledStructures(ByteLogicDialog.allByteLogicGates.as());
        private final Signal[] signalOutputCache;
        private final Signal[] signalInputCache;
        private final Signal[] nextSignalInputCache;
        public boolean buildingUpdate = false;
        private byte[] sideStates = new byte[size * 4];

        public ByteLogicProcessorBuild(){
            signalOutputCache = new Signal[4 * size];
            signalInputCache = new Signal[4 * size];
            nextSignalInputCache = new Signal[4 * size];
            for(int i = 0; i < signalOutputCache.length; i++){
                signalOutputCache[i] = new Signal();
                signalInputCache[i] = new Signal();
                nextSignalInputCache[i] = new Signal();
            }
        }

        @Override
        public Object config(){
            return JsonIO.write(structures);
        }

        public boolean accessible(){
            return !privileged || state.rules.editor;
        }

        @Override
        public void buildConfiguration(Table table){
            if(!accessible()){
                deselect();
                return;
            }

            table.button(Icon.pencil, Styles.cleari, () -> {
                ByteLogicDialog dialog = new ByteLogicDialog();
                dialog.show(() -> structures.all, it -> {
                    tmpStructures.all.set(it);
                    configure(JsonIO.write(tmpStructures));
                });
                deselect();
            }).size(40f);
        }

        @Override
        public boolean canOutputSignal(int dir){
            return true;
        }

        @Override
        public void beforeUpdateSignalState(){
            for(int i = 0; i < signalOutputCache.length; i++){
                Building nearby = nearby(i / size);
                if(nearby instanceof ByteLogicBuildingc buildingc){
                    buildingc.acceptSignal(this, signalOutputCache[i]);
                }
                signalOutputCache[i].setZero();
            }

            buildingUpdate = true;
            //initializing inputs
            structures.update();
            //calculating signals
            structures.update();
            buildingUpdate = false;
        }

        @Override
        public void updateSignalState(){
            for(int i = 0; i < nextSignalInputCache.length; i++){
                signalInputCache[i].set(nextSignalInputCache[i]);
                nextSignalInputCache[i].setZero();
            }
        }

        @Override
        public void updateTile(){
        }

        @Override
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, Signal signal){
            if(otherBuilding != null){
                byte sideId = relativeTo(otherBuilding.<Building>as());
                nextSignalInputCache[sideId * size].set(signal);
            }
            return super.acceptSignal(otherBuilding, signal);
        }

        @Override
        public void customWrite(Writes write){
            BLTypeIO.writeTiledStructures(write, structures);

            writeSignalArray(write, signalOutputCache);
            writeSignalArray(write, signalInputCache);
            writeSignalArray(write, nextSignalInputCache);
        }

        private void writeSignalArray(Writes write, Signal[] array){
            write.i(array.length);
            for(Signal value : array){
                value.write(write);
            }
        }

        @Override
        public void customRead(Reads read){
            BLTypeIO.readTiledStructures(read, structures);

            readSignalArray(read, signalOutputCache);
            readSignalArray(read, signalInputCache);
            readSignalArray(read, nextSignalInputCache);
            prepareStructures();
        }

        @Override
        public void draw(){
            Draw.rect(base, tile.drawx(), tile.drawy());

            for(int i = 0; i < 4; i++){
                if(sideStates[i] == 1){
                    Draw.color(signalInputCache[i].color());
                    Draw.rect(inputWire, x, y, i * 90);
                }else if(sideStates[i] == 2){
                    Draw.color(signalOutputCache[i].color());
                    Draw.rect(outputWire, x, y, i * 90);
                }
            }
            Draw.color();
        }

        private void readSignalArray(Reads read, Signal[] array){
            int size = read.i();
            for(int i = 0; i < size; i++){
                if(i < array.length){
                    array[i].read(read);
                }else{
                    SignalType type = array[0].type;
                    long value = array[0].number();
                    array[0].read(read);
                    array[0].type = type;
                    array[0].setNumber(value);
                }
            }
        }

        @Override
        public short customVersion(){
            return 0;
        }

        public void setStructures(String string){
            JsonIO.read(TiledStructures.class, structures, string);
            prepareStructures();
        }

        private void prepareStructures(){
            Seq<InputGate> inputGates = new Seq<>();
            Seq<OutputGate> outputGates = new Seq<>();
            for(TiledStructure<?> tiledStructure : structures.all){
                if(tiledStructure instanceof LinkedGate linkedGate){
                    linkedGate.link = this;
                    if(linkedGate instanceof InputGate inputGate){
                        inputGates.add(inputGate);
                    }else if(linkedGate instanceof OutputGate outputGate){
                        outputGates.add(outputGate);
                    }
                }
            }
            Arrays.fill(sideStates, (byte)0);

            for(InputGate inputGate : inputGates){
                sideStates[inputGate.clockWisePosition % sideStates.length] = 1;
            }
            for(OutputGate outputGate : outputGates){
                sideStates[outputGate.clockWisePosition % sideStates.length] = 2;
            }
        }

        public void transferSignal(int clockWisePosition, Signal signal){
            signalOutputCache[clockWisePosition].set(signal);
        }

        public Signal inputSignal(int clockWisePosition){
            return signalInputCache[clockWisePosition];
        }
    }
}
