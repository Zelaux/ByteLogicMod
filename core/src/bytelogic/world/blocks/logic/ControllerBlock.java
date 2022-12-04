package bytelogic.world.blocks.logic;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import bytelogic.type.*;
import bytelogic.ui.guide.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;

import static mindustry.Vars.tilesize;

public class ControllerBlock extends UnaryLogicBlock{

    public ControllerBlock(String name){
        super(name);
        processor = in -> in;
    }

    @Override
    public void init(){
        if(blockPreview == null){
            Schematic schematic = Schematics.readBase64("bXNjaAF4nG2PUUvEMBCEJ03bq54IJ4Lgk38gD/4e8SHXrmcgzZZ076D+ejetKBzuw8wwXxaysOgs6uRHwqHnJJljpOyEZnGv2A809zlMEjgBaKM/UpxRvb13eDwuQi7yKfRu5MFlin7B81Ur2af5g/NIGU9XLLG4kxfCyxUIaTqLm6Lv6ZPj8M+qTz4uXwr2QWh0M59zT+j0ggstnHGz1hcOg377gL8xsEatuld5wF05mZI4WSYq1FaqjVkflmlhbYm3BrV6tbZVqZqfqt2qBrrZaLKtJgNTTMNOxW64/sXNhtsN74BvCO5SGQ==");
            blockPreview = new SchematicBlockShowcase(
                this,
                schematic,
                false, schematic.width + 2, schematic.height + 2,
                new Point2(1, 1)
            );
            blockPreview.hasNoSwitchMirror(false);
        }
        super.init();
    }

    public class ControllerBuild extends UnaryLogicBuild{
        @Override
        public void update(){
            super.update();

            Building facing = front();
            if(facing != null){
                Signal signal = currentSignal();
                if(signal.type != SignalTypes.numberType && !facing.enabled){
                    facing.control(LAccess.enabled, 1, 0, 0, 0);
                }
                signal.applyControl(facing);
//                if ()
//                facing.control(LAccess.enabled, signal.compareWithZero(), 0, 0, 0);
            }
        }

        @Override
        public void drawSelect(){
            super.drawSelect();
            Building facing = front();
            if(facing != null){
                float sin = Mathf.absin(Time.time + 48, 14f, 0.25f);
//                Draw.color(Pal.accent, Color.white,sin);
//                Tmp.c1.set(0.30980393f, 0.8901961f, 1.0f, 1.0f);
//                Tmp.c2.set(0.16078432f, 0.6156863f, 1.0f, 1.0f);
                Draw.color(Pal.accent, Pal.accentBack, sin);
                Drawf.selected(facing, Draw.getColor());
            }else{
                Draw.color(Pal.remove);
                Draw.rect(Icon.cancelSmall.getRegion(), tile.drawx() + Geometry.d4(rotation).x * tilesize, tile.drawy() + Geometry.d4(rotation).y * tilesize);
                Draw.color();
            }
        }
    }
}
