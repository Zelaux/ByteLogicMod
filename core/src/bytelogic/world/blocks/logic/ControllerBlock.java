package bytelogic.world.blocks.logic;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.math.geom.*;
import arc.util.Time;
import arc.util.Tmp;
import bytelogic.type.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.world.*;

import static mindustry.Vars.tilesize;

public class ControllerBlock extends UnaryLogicBlock {

    public ControllerBlock(String name) {
        super(name);
        processor = in -> in;
    }

    public class ControllerBuild extends UnaryLogicBuild {
        @Override
        public void update() {
            super.update();

            Building facing = front();
            if (facing != null) {
                Signal signal = currentSignal();
                signal.applyControl(facing);
//                if ()
//                facing.control(LAccess.enabled, signal.compareWithZero(), 0, 0, 0);
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            Building facing = front();
            if (facing != null) {
                float sin = Mathf.absin(Time.time + 48, 14f, 0.25f);
//                Draw.color(Pal.accent, Color.white,sin);
//                Tmp.c1.set(0.30980393f, 0.8901961f, 1.0f, 1.0f);
//                Tmp.c2.set(0.16078432f, 0.6156863f, 1.0f, 1.0f);
                Draw.color(Pal.accent, Pal.accentBack, sin);
                Drawf.selected(facing, Draw.getColor());
            } else {
                Draw.color(Pal.remove);
                Draw.rect(Icon.cancelSmall.getRegion(), tile.drawx() + Geometry.d4(rotation).x * tilesize, tile.drawy() + Geometry.d4(rotation).y * tilesize);
                Draw.color();
            }
        }
    }
}
