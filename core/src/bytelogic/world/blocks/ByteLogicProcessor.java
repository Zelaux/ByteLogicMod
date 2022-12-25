package bytelogic.world.blocks;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import bytelogic.gen.*;
import bytelogic.io.*;
import bytelogic.type.*;
import bytelogic.type.byteGates.ByteLogicOperators.*;
import bytelogic.type.byteGates.ByteLogicOperators.LinkedGate.*;
import bytelogic.ui.dialogs.*;
import bytelogic.world.blocks.logic.*;
import mindustry.annotations.Annotations.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.ui.*;
import mindustry.world.*;
import mma.ui.tiledStructures.*;
import mma.ui.tiledStructures.TiledStructures.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static mindustry.Vars.state;

public class ByteLogicProcessor extends LogicBlock{
    public static final TiledStructures tmpStructures = new TiledStructures(ByteLogicDialog.allByteLogicGates.as());
    @Load("@realName()-input-wire")
    public TextureRegion inputWire;
    @Load("@realName()-output-wire")
    public TextureRegion outputWire;

    public Point2[] edges;
    public Point2[] innerEdges;

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

    @Override
    public void init(){
        innerEdges = new Point2[size * 4];
        edges = new Point2[size * 4];
        int leftX = -(size - 1) / 2 - 1,
            bottomY = -(size - 1) / 2 - 1,
            rightX = Mathf.ceil((size - 1) / 2f) + 1,
            topY = Mathf.ceil((size - 1) / 2f) + 1;
        int innerLeftX = -(size - 1) / 2,
            innerBottomY = -(size - 1) / 2,
            innerRightX = Mathf.ceil((size - 1) / 2f),
            innerTopY = Mathf.ceil((size - 1) / 2f);
        for(int i = 0; i < size; i++){

            edges[i] = new Point2(rightX, bottomY + i + 1);
            edges[i + size] = new Point2(rightX - i - 1, topY);
            edges[i + size * 2] = new Point2(leftX, topY - i - 1);
            edges[i + size * 3] = new Point2(leftX + 1 + i, bottomY);

            innerEdges[i] = new Point2(innerRightX, innerBottomY + i);
            innerEdges[i + size] = new Point2(innerRightX - i, innerTopY);
            innerEdges[i + size * 2] = new Point2(innerLeftX, innerTopY - i);
            innerEdges[i + size * 3] = new Point2(innerLeftX + i, innerBottomY);
        }
        super.init();
    }

    public class ByteLogicProcessorBuild extends LogicBuild{
        public final TiledStructures structures = initStructures();
        protected final Signal[] signalOutputCache;
        protected final Signal[] signalInputCache;
        protected final Signal[] nextSignalInputCache;
        public boolean buildingUpdate = false;
        protected byte[] sideStates = new byte[size * 4];

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

        @NotNull
        protected TiledStructures initStructures(){
            return new TiledStructures(ByteLogicDialog.allByteLogicGates.as());
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
            for(int i = 0; i < edges.length; i++){
                Tile nearby = tile.nearby(edges[i]);
                if(nearby != null && nearby.build instanceof ByteLogicBuildingc buildingc){
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
                int edge = Structs.indexOf(edges, it -> {
                    Tile nearby = tile.nearby(it);
                    return nearby != null && nearby.build == otherBuilding;
                });
//                int trns = size / 2 + 1;
//                int i = Structs.indexOf(edges, it -> nearby(it.x * trns, it.y * trns) == otherBuilding);
//                if (i)
//                byte sideId = relativeTo(otherBuilding.<Building>as());
                if(edge != -1){
                    nextSignalInputCache[edge].set(signal);
                }
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
            for(int i = 0; i < edges.length; i++){
                if(sideStates[i] == 0) continue;
                int rotationId = i / size;
                Tile nearby = tile.nearby(innerEdges[i]);
                float drawx = nearby.worldx();
                float drawy = nearby.worldy();
                if(sideStates[i] == 1){
                    Draw.color(signalInputCache[i].color());
                    Draw.rect(inputWire, drawx, drawy, rotationId * 90);
                }else if(sideStates[i] == 2){
                    Draw.color(signalOutputCache[i].color());
                    Draw.rect(outputWire, drawx, drawy, rotationId * 90);
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

        protected void prepareStructures(){
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
