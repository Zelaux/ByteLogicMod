package bytelogic.ui.elements;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import bytelogic.schematics.*;
import bytelogic.type.byteGates.*;
import bytelogic.ui.dialogs.*;
import mindustry.graphics.*;
import mma.ui.tiledStructures.*;
import mma.ui.tiledStructures.TiledStructuresCanvas.StructureTilemap.StructureTile.*;

import static bytelogic.BLVars.byteLogicSchematics;
import static mma.ui.tiledStructures.TiledStructuresCanvas.unitSize;

public class SmallByteLogicSchematicPreview extends Table{
    private final ByteLogicSchematic schematic;
    private final SchematicGate schematicGate;
    public float scaling = 16f;
    public float thickness = 4f;
    public Color borderColor = Pal.gray;
    TiledStructuresCanvas canvas;

    public SmallByteLogicSchematicPreview(ByteLogicSchematic schematic){

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
        ByteLogicSchematic schematic_ = ByteLogicSchematics.readBase64(byteLogicSchematics.writeBase64(schematic));
        canvas.tilemap.createTile(0, 0, schematicGate = new SchematicGate(schematic_));

        ByteLogicSchematicEditDialog.clearTiles(canvas.tilemap.getChildren().as());


        schemGateWidth = schematicGate.objWidth() * unitSize;
        schemGateHeight = schematicGate.objHeight() * unitSize;
        add(new Table(it -> it.add(canvas).size(schemGateWidth, schemGateHeight)){


            @Override
            public Element hit(float x, float y, boolean touchable){
                setClip(true);
                Element hit = super.hit(x, y, touchable);
                if(!(hit instanceof Connector) && hit != null) return canvas;
                return hit;
            }
        }).grow();
    }
    float schemGateWidth;
    float schemGateHeight;

    @Override
    public void act(float delta){
        super.act(delta);

        float scaleX = Math.min(1, getWidth() / (schemGateWidth / Scl.scl()));
        float scaleY = Math.min(1, getHeight() / (schemGateHeight / Scl.scl()));
        canvas.setTransform(true);
        canvas.originX = canvas.getWidth() / 2f;
        canvas.originY = canvas.getHeight() / 2f;
        canvas.setScale(Math.min(scaleX, scaleY));
        canvas.tilemap.setPosition(0,0);
//        canvas.tilemap.setPosition(-schemGateWidth - getWidth() / 2f, -schemGateWidth - getHeight() / 2f);
    }

    @Override
    public void draw(){
        super.draw();
//        canvas.localToAscendantCoordinates(this, Tmp.v1.setZero());
//        canvas.localToAscendantCoordinates(this, Tmp.v2.set(canvas.getWidth(), canvas.getHeight()));
        /*if(clipBegin(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x - Tmp.v1.x, Tmp.v2.y - Tmp.v1.y)){

            clipEnd();
        }*/
    }
}
