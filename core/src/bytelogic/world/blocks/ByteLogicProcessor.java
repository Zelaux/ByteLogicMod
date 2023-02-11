package bytelogic.world.blocks;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import bytelogic.*;
import bytelogic.gen.*;
import bytelogic.io.*;
import bytelogic.schematics.*;
import bytelogic.type.*;
import bytelogic.type.byteGates.ByteLogicOperators.*;
import bytelogic.ui.dialogs.*;
import bytelogic.world.blocks.logic.*;
import mindustry.annotations.Annotations.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.ui.*;
import mindustry.world.*;
import mma.*;
import mma.io.*;
import mma.ui.tiledStructures.TiledStructures.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static mindustry.Vars.state;

public class ByteLogicProcessor extends LogicBlock{
    public static final ByteLogicTiledStructures tmpStructures = BLVars.nullOnPack(() -> new ByteLogicTiledStructures(ByteLogicDialog.allByteLogicGates.as()));
    private static final ByteWrites byteWrites = new ByteWrites();
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

        this.<byte[], ByteLogicProcessorBuild>config(byte[].class, (build, bytes) -> {

            build.setStructures(bytes);
            // System.out.println(JsonIO.print(string));
        });
    }

    @Override
    public TextureRegion[] icons(){
        return !ModVars.packSprites ? new TextureRegion[]{fullIcon} : new TextureRegion[]{base};
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
        public final ByteLogicTiledStructures structures = initStructures();
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
        protected ByteLogicGateProvider provider(){
            return ByteLogicGateProvider.defaultProvider;
        }

        @NotNull
        protected ByteLogicTiledStructures initStructures(){
            return new ByteLogicTiledStructures(provider().providers.as());
        }

        @Override
        public Object config(){
            byteWrites.reset();
            structures.write(byteWrites);
            return byteWrites.getBytes();
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
                    BLTypeIO.tmpWrites.reset();
                    BLTypeIO.writeByteLogicTiledStructures(BLTypeIO.tmpWrites, tmpStructures);
                    configure(BLTypeIO.tmpWrites.getBytes());
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


        public void setStructures(String string){
            JsonIO.read(ByteLogicTiledStructures.class, structures, string);
            prepareStructures();
        }

        public void setStructures(byte[] bytes){
            BLTypeIO.tmpReads.setBytes(bytes);
            BLTypeIO.readByteLogicTiledStructures(BLTypeIO.tmpReads, structures);
            prepareStructures();
        }

        protected void prepareStructures(){
            byte[] inputs = new byte[size * 4];
            byte[] outputs = new byte[size * 4];
            for(TiledStructure<?> tiledStructure : structures.all){
                if(tiledStructure instanceof ByteLogicGate byteLogicGate){
                    byteLogicGate.setLink(this);

                    for(var side : byteLogicGate.inputSides()){
                        inputs[side % inputs.length] = 1;
                    }
                    for(var side : byteLogicGate.outputSides()){
                        outputs[side % outputs.length] = 1;
                    }
                }
            }
            Arrays.fill(sideStates, (byte)0);
            for(int i = 0; i < inputs.length; i++){
                sideStates[i] = (byte)Math.max(inputs[i], outputs[i] * 2);
            }
        }

        public void transferSignal(int clockWisePosition, Signal signal){
            signalOutputCache[clockWisePosition].set(signal);
        }

        public Signal inputSignal(int clockWisePosition){
            return signalInputCache[clockWisePosition];
        }

        @Override
        public void customWrite(Writes write){
            BLTypeIO.writeByteLogicTiledStructures(write, structures);

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
            BLTypeIO.readByteLogicTiledStructures(read, structures);

            readSignalArray(read, signalOutputCache);
            readSignalArray(read, signalInputCache);
            readSignalArray(read, nextSignalInputCache);
            prepareStructures();
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
            return 1;
        }
    }
}
