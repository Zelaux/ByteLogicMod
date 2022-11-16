package bytelogic.world.blocks.logic;

import arc.graphics.g2d.*;
import arc.math.geom.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.world.*;

import static mindustry.Vars.tilesize;

public class ControllerBlock extends UnaryLogicBlock{

    public ControllerBlock(String name){
        super(name);
        processor = in -> in;
    }

    public class ControllerBuild extends UnaryLogicBuild{
        @Override
        public void update(){
            super.update();

            Building facing = front();
            if(facing != null ){
                facing.control(LAccess.enabled, currentSignal(), 0, 0, 0);
            }
        }

        @Override
        public void drawSelect(){
            super.drawSelect();
            Building facing = front();
            if(facing != null && facing.block().logicConfigurable){
                Drawf.selected(facing, Pal.accent);
            }else{
                Draw.color(Pal.remove);
                Draw.rect(Icon.cancelSmall.getRegion(), tile.drawx() + Geometry.d4(rotation).x * tilesize, tile.drawy() + Geometry.d4(rotation).y * tilesize);
                Draw.color();
            }
        }
    }
}
