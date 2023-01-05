package bytelogic.ui.elements;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import bytelogic.schematics.*;
import bytelogic.type.ConnectionSettings.*;
import bytelogic.type.ConnectionSettings.WireDescriptor.*;
import bytelogic.ui.dialogs.*;
import kotlin.jvm.internal.Ref.*;
import mindustry.graphics.*;
import mma.ui.tiledStructures.TiledStructures.*;
import mma.ui.tiledStructures.*;
import mma.ui.tiledStructures.TiledStructuresCanvas.StructureTilemap.*;
import mma.ui.tiledStructures.TiledStructuresCanvas.StructureTilemap.StructureTile.*;
import org.jetbrains.annotations.*;
import zelaux.arclib.ui.tooltips.*;

import static mma.ui.tiledStructures.TiledStructuresCanvas.unitSize;

public class ByteLogicSchematicPreview extends Table{
    private final Seq<MockConnector> mockInputs = new Seq<>();
    private final Seq<MockConnector> mockOutputs = new Seq<>();
    private final ByteLogicSchematic schematic;
    public float scaling = 16f;
    public float thickness = 4f;
    public Color borderColor = Pal.gray;
    TiledStructuresCanvas canvas;

    public ByteLogicSchematicPreview(ByteLogicSchematic schematic){

        this.schematic = schematic;
        canvas = new TiledStructuresCanvas(BaseTiledStructuresDialog.tmpDialog){

            @Override
            public void draw(){
                boolean checked = parent.parent instanceof Button button && button.isOver();
                clipBegin(x, y, width, height);
                super.draw();
                clipEnd();


                Draw.color(checked ? Pal.accent : borderColor);
                Draw.alpha(parentAlpha);
                Lines.stroke(Scl.scl(thickness));
                Lines.rect(x, y, width, height);
                Draw.reset();
            }
        };

        TiledStructureGroup group = new TiledStructureGroup();
        for(TiledStructure structure : schematic.structures){
            group.list().add(structure);
        }
        group.calculateSize();
        for(TiledStructure structure : schematic.structures){
            canvas.tilemap.createTile(structure);
        }




        ByteLogicSchematicEditDialog.clearTiles(canvas.tilemap.getChildren().as());


        table(inputConnections -> {
            buildConnections(inputConnections, schematic.connectionSettings.inputWires, mockInputs, true);
        }).growY();
        add(new Table(it -> it.add(canvas).grow()){


            @Override
            public Element hit(float x, float y, boolean touchable){
                setClip(true);
                Element hit = super.hit(x, y, touchable);
                if(!(hit instanceof Connector) && hit != null) return canvas;
                return hit;
            }
        }).grow();

        table(outputConnections -> {
            buildConnections(outputConnections, schematic.connectionSettings.outputWires, mockOutputs, false);
        }).growY();
    }

    @Override
    public void act(float delta){
        super.act(delta);
        float scaleX = Math.min(1, getWidth() / (schematic.width * unitSize / Scl.scl()));
        float scaleY = Math.min(1, getHeight() / (schematic.height * unitSize / Scl.scl()));
        canvas.setTransform(true);
        canvas.originX=canvas.getWidth()/2f;
        canvas.originY=canvas.getHeight()/2f;
        canvas.setScale(Math.min(scaleX, scaleY));
        canvas.tilemap.setPosition(-schematic.width * unitSize-getWidth()/2f, -schematic.height * unitSize -getHeight()/2f);
    }

    @Override
    public void draw(){
        super.draw();
        canvas.localToAscendantCoordinates(this, Tmp.v1.setZero());
        canvas.localToAscendantCoordinates(this, Tmp.v2.set(canvas.getWidth(), canvas.getHeight()));
        drawWires();
        /*if(clipBegin(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x - Tmp.v1.x, Tmp.v2.y - Tmp.v1.y)){

            clipEnd();
        }*/
    }

    @NotNull
    private void buildConnections(Table table, Seq<WireDescriptor> wires, Seq<MockConnector> collectors, boolean isInput){
        table.defaults().growY();
        table.clearChildren();
        collectors.clear();
        for(int i = 0; i < wires.size; i++){
            WireDescriptor wireDescriptor = wires.get(i);

            MockConnector element = new MockConnector(!isInput, i, ConnectorStyle.defaultStyle());
            collectors.add(element);
            ObjectRef<String> tooltipText = new ObjectRef<>();
            SideTooltips.mutableSideTooltip(table, Align.topRight, element, tooltipText);
            element.update(() -> {
                tooltipText.element = wireDescriptor.name;
            });
            table.row();
        }
    }

    private void drawWires(){
        Seq<StructureTile> tiles = canvas.tilemap.getChildren().as();
        for(int i = 0; i < mockInputs.size; i++){
            MockConnector conFrom = mockInputs.get(i);
            WireDescriptor wireDescriptor = schematic.connectionSettings.inputWires.get(conFrom.id);
            for(StructureDescriptor structureDescriptor : wireDescriptor.connectedStructures){
                StructureTile structureTile = tiles.find(it -> it.tx == structureDescriptor.x && it.ty == structureDescriptor.y);
                Connector conTo = structureTile.conParent[structureDescriptor.connectionIndex];
                Vec2
                    from = conFrom.localToAscendantCoordinates(this, Tmp.v1.set(conFrom.getWidth() / 2f, conFrom.getHeight() / 2f)),
                    to = conTo.localToAscendantCoordinates(this, Tmp.v2.set(conTo.getWidth() / 2f, conTo.getHeight() / 2f));
                drawCurve(Color.white, from.x, from.y, to.x, to.y, false);
            }
        }
        for(int i = 0; i < mockOutputs.size; i++){
            MockConnector conTo = mockOutputs.get(i);
            WireDescriptor wireDescriptor = schematic.connectionSettings.outputWires.get(conTo.id);
            for(StructureDescriptor structureDescriptor : wireDescriptor.connectedStructures){
                StructureTile structureTile = tiles.find(it -> it.tx == structureDescriptor.x && it.ty == structureDescriptor.y);

                Connector conFrom = structureTile.conChildren[structureDescriptor.connectionIndex];
                Vec2
                    from = conFrom.localToAscendantCoordinates(this, Tmp.v1.set(conFrom.getWidth() / 2f, conFrom.getHeight() / 2f)),
                    to = conTo.localToAscendantCoordinates(this, Tmp.v2.set(conTo.getWidth() / 2f, conTo.getHeight() / 2f));
                drawCurve(Color.white, from.x, from.y, to.x, to.y, true);
            }
        }
    }

    protected void drawCurve(Color color, float x1, float y1, float x2, float y2, boolean scale1){
        float scale = canvas.scaleX;

        Lines.stroke(4f * scale);
        Draw.color(color, parentAlpha);

        if(scale1){
            Fill.square(x1, y1, 8f * scale, 45f);
            Fill.square(x2, y2, 8f, 45f);
        }else{
            Fill.square(x1, y1, 8f, 45f);
            Fill.square(x2, y2, 8f * scale, 45f);
        }

        float dist = Math.abs(x1 - x2) / 2f;
        float lerpProgress = Interp.pow5Out.apply(Mathf.clamp(Math.abs(y1 - y2) * 2 / unitSize));
//            float disty = lerpProgress;
        float disty = 0;
        /*if(!selfConnecting){
            disty = 0;
        }else{
            float value = (y2 - y1) * 2;
            if(value == 0) value = unitSize / 2f;
            disty = Math.min(dist, Math.abs(value)) * Mathf.sign(value);
        }*/
        float cx1 = x1 + dist * (lerpProgress);
        float cx2 = x2 - dist * (lerpProgress);

        float cy1 = y1 + disty;
        float cy2 = y2 + disty;
        Lines.curve(x1, y1, cx1, cy1, cx2, cy2, x2, y2, Math.max(4, (int)(Mathf.dst(x1, y1, x2, y2) / 4f)));

        float progress = (Time.time % (60 * 4)) / (60 * 4);

        float t2 = progress * progress;
        float t3 = progress * t2;
        float t1 = 1 - progress;
        float t13 = t1 * t1 * t1;
        float kx1 = t13 * x1 + 3 * progress * t1 * t1 * cx1 + 3 * t2 * t1 * cx2 + t3 * x2;
        float ky1 = t13 * y1 + 3 * progress * t1 * t1 * cy1 + 3 * t2 * t1 * cy2 + t3 * y2;

        if(scale1){
            Fill.circle(kx1, ky1, 6f * Mathf.lerp(scale, 1f, progress));
        }else{
            Fill.circle(kx1, ky1, 6f * Mathf.lerp(scale, 1f, 1f - progress));
        }

        Draw.reset();
    }

    public class MockConnector extends Button{
        public final boolean isInput;
        public final int id;

        public MockConnector(boolean isInput, int id, ConnectorStyle style){
            super(new ButtonStyle(isInput ? style.inputStyle : style.outputStyle));
            this.isInput = isInput;
            this.id = id;

            clearChildren();
        }

        public boolean canConnectTo(StructureTile.Connector other){
            return
                isInput != other.findParent && !other.isDisabled()/* &&
                            tile() != other.tile()*/;
        }

        @Override
        public void draw(){
            super.draw();
            float cx = x + width / 2f;
            float cy = y + height / 2f;

            // these are all magic numbers tweaked until they looked good in-game, don't mind them.
            Lines.stroke(3f, Pal.accent);
            Draw.alpha(parentAlpha);
            if(isInput){
                Lines.line(cx, cy + 9f, cx + 9f, cy);
                Lines.line(cx + 9f, cy, cx, cy - 9f);
            }else{
                Lines.square(cx, cy, 9f, 45f);
            }
        }
    }
}
