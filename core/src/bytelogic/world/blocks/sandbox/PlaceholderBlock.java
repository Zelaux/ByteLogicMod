package bytelogic.world.blocks.sandbox;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;
import arc.util.io.*;
import bytelogic.gen.BLIcons.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;

public class PlaceholderBlock extends Block{
    public PlaceholderBlock(String name){
        super(name);
        rotate = true;
        destructible = true;
        update = true;
        consumesTap = true;
        this.<Boolean, BlockPlaceholderBuild>config(Boolean.class, (build, bool) -> {
            build.originalBlock = bool;
        });
        requirements(Category.logic, ItemStack.empty);
    }

    @Override
    public void init(){
        super.init();
        buildVisibility = BuildVisibility.sandboxOnly;
    }

    @Override
    public void drawDefaultPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        if(plan.config == Boolean.TRUE){
            Draw.color(Color.brick);
        }
        super.drawDefaultPlanRegion(plan, list);
    }

    @Override
    public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list){
        super.drawPlanConfig(plan, list);
        Draw.color();
        Draw.rect(Regions.collapser0Closed32, plan.drawx(), plan.drawy(), 180 + plan.rotation * 90);
    }

    public class BlockPlaceholderBuild extends Building{
        boolean originalBlock = false;

        @Override
        public void draw(){
            if(originalBlock) Draw.color(Color.brick);
            Draw.rect(block.region, x, y, 0);
            Draw.color();
            Draw.rect(Regions.collapser0Closed32, x, y, 180 + rotdeg());

        }

        @Override
        public void tapped(){
            configure(!originalBlock);
        }

        @Override
        public Object config(){
            return originalBlock;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(0);
            write.bool(originalBlock);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            read.i();
            originalBlock = read.bool();
        }
    }
}
