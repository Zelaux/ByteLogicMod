package bytelogic.world.blocks;

import arc.graphics.g2d.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import bytelogic.gen.*;
import bytelogic.type.*;
import bytelogic.type.byteGates.ByteLogicOperators.*;
import bytelogic.type.byteGates.ByteLogicOperators.LinkedGate.*;
import bytelogic.type.graphicsGates.GraphicsOperators.*;
import bytelogic.type.graphicsGates.GraphicsOperators.GraphicsOperator.*;
import bytelogic.ui.dialogs.*;
import mindustry.annotations.Annotations.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.logic.LogicDisplay.*;
import mma.graphics.*;
import mma.ui.tiledStructures.*;
import mma.ui.tiledStructures.TiledStructures.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static mindustry.Vars.*;

public class GraphicsProcessor extends ByteLogicProcessor{
    public static final TiledStructures tmpStructures = new TiledStructures(GraphicsLogicDialog.allGraphicsGates.as());
    public int updatePerTick = 8;
    @Load("@realName()-input-wire")
    public TextureRegion inputWire;
    @Load("@realName()-output-wire")
    public TextureRegion outputWire;

    public GraphicsProcessor(String name){
        super(name);
        update = true;
        destructible = true;
        configurable = true;
        rotate = false;
        size = 2;

    }

    public class GraphicsProcessorBuild extends ByteLogicProcessor.ByteLogicProcessorBuild implements DrawCommandListener{
//        public final TiledStructures structures = ;

        public boolean buildingUpdate = false;
        public LongQueue commands = new LongQueue(256);
        private byte[] sideStates = new byte[size * 4];

        @NotNull
        @Override
        protected TiledStructures initStructures(){
            return new TiledStructures(GraphicsLogicDialog.allGraphicsGates.as());
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
                GraphicsLogicDialog dialog = new GraphicsLogicDialog();
                dialog.show(() -> structures.all, it -> {
                    tmpStructures.all.set(it);
                    configure(JsonIO.write(tmpStructures));
                });
                deselect();
            }).size(40f);
        }

        @Override
        public boolean canOutputSignal(int dir){
            return false;
        }

        @Override
        public void beforeUpdateSignalState(){
            buildingUpdate = true;
            for(int i = 0; i < updatePerTick; i++){
                //initializing inputs
                structures.update();
                //calculating signals
                structures.update();
            }
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
        public void draw(){
            super.draw();
           /* for(int i = 0; i < edges.length; i++){
                Tile nearby = tile.nearby(edges[i]);
                Tile nearbyInner = tile.nearby(innerEdges[i]);
                ADrawf.drawText(nearby.worldx(), nearby.worldy(), i + "");
                Lines.line(nearbyInner.worldx(), nearbyInner.worldy(), nearby.worldx(), nearby.worldy());
            }*/
//            float offset = tilesize * size / 2f + tilesize / 2f;
            /*for(int i = 0; i < size; i++){

                ADrawf.drawText(x + offset, y -offset+ tilesize * i+tilesize, i + "");
                ADrawf.drawText(x + offset - tilesize * i-tilesize, y + offset, (i + size) + "");
                ADrawf.drawText(x - offset, y +offset-tilesize * i-tilesize, (i + size * 2) + "");
                ADrawf.drawText(x - offset+tilesize+tilesize * (i), y - offset, (i + size * 3) + "");
            }*/
        }

        @Override
        protected void prepareStructures(){
            Seq<InputGate> inputGates = new Seq<>();
            for(TiledStructure<?> tiledStructure : structures.all){
                if(tiledStructure instanceof GraphicsOperator operator){
                    operator.listener = this;
                }
                if(tiledStructure instanceof LinkedGate linkedGate){
                    linkedGate.link = this;
                    if(linkedGate instanceof InputGate inputGate){
                        inputGates.add(inputGate);
                    }
                }
            }
            Arrays.fill(sideStates, (byte)0);

            for(InputGate inputGate : inputGates){
                sideStates[inputGate.clockWisePosition % sideStates.length] = 1;
            }
        }

        public void transferSignal(int clockWisePosition, Signal signal){
            signalOutputCache[clockWisePosition].set(signal);
        }

        public Signal inputSignal(int clockWisePosition){
            return signalInputCache[clockWisePosition];
        }

        @Override
        public void executeDrawCommand(long drawCommand){
            commands.addLast(drawCommand);
        }

        @Override
        public void flushDisplay(){
            ;
            for(Building building : proximity){
                if(building instanceof LogicDisplayBuild display){
                    for(int i = 0; i < commands.size; i++){
                        display.commands.addLast(commands.get(i));
                    }
                }
            }
            commands.clear();
        }


    }
}
