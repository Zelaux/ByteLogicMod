package bytelogic.world.blocks.logic;

import arc.Graphics.*;
import arc.Graphics.Cursor.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.io.*;
import bytelogic.gen.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mma.graphics.*;

import java.util.*;

public abstract class BinaryLogicBlock extends LogicBlock{
    protected static final int leftSideIndex = 0;
    protected static final int rightSideIndex = 1;
    public boolean canFlip;
    public String operatorName;
    protected /*@NonNull*/ BinaryProcessor processor;

    public BinaryLogicBlock(String name){
        super(name);
        config(Boolean.class, (BinaryLogicBuild build, Boolean value) -> {
            build.flipped = value;
        });
    }

    @Override
    public void init(){
        super.init();
        consumesTap = canFlip;
        if(processor == null){
            throw new IllegalArgumentException("processor of " + name + " is null");
        }
        if(operatorName == null){
            throw new IllegalArgumentException("processor of " + operatorName + " is null");
        }
    }

    @Override
    public void flipRotation(BuildPlan req, boolean x){
//        super.flipRotation(req, x);
        if(req.config instanceof Boolean bool && canFlip){

            if((x == (req.rotation % 2 == 0)) != invertFlip){
                req.rotation = Mathf.mod(req.rotation + 2, 4);
            }else{
                req.config = !bool;
            }
        }else{
            super.flipRotation(req, x);
        }
    }

    public interface BinaryProcessor{
        int process(int left, int right);
    }

    public class BinaryLogicBuild extends LogicBuild{
        final int[] sides = {0, 0};
        boolean flipped = false;

        @Override
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, int signal){
            if(right() == otherBuilding){
                sides[rightSideIndex] = signal;
                return true;
            }
            if(left() == otherBuilding){
                sides[leftSideIndex] = signal;
                return true;
            }
            return false;
//            return super.acceptSignal(otherBuilding, signal);
        }

        @Override
        public void updateSignalState(){

            lastSignal = getNextSignal();
            sides[0] = 0;
            sides[1] = 0;
            nextSignal = 0;

        }

        @Override
        public void beforeUpdateSignalState(){
            if(doOutput && output(rotation)){
                front().<LogicBuild>as().acceptSignal(this, lastSignal);
            }
        }

        //        @Override
        public int getNextSignal(){
            int left, right;
            if(!flipped){
                left = sides[leftSideIndex];
                right = sides[rightSideIndex];
            }else{
                left = sides[rightSideIndex];
                right = sides[leftSideIndex];
            }

            return processor.process(left, right);
        }

        @Override
        public void drawSelect(){
            super.drawSelect();

            Tile left = tile.nearby((rotation + 1) % 4);
            Tile right = tile.nearby((rotation + 4 - 1) % 4);
            Tile front = tile.nearby((rotation) % 4);
            Tile back = tile.nearby((rotation + 2) % 4);
            float textSize = 0.15f;
            Color color = Pal.accent;
            ADrawf.drawText(left.worldx(), left.worldy(), textSize, color, "a");
            ADrawf.drawText(front.worldx(), front.worldy(), textSize, color, !flipped ? "a " + operatorName + " b" : "b " + operatorName + " a");
            ADrawf.drawText(right.worldx(), right.worldy(), textSize, color, "b");
        }

        @Override
        public void draw(){


            Draw.rect(base, tile.drawx(), tile.drawy());

            Draw.color(currentSignal() > 0 ? Pal.accent : Color.white);
//            super.draw(tile);
            Draw.rect(this.block.region,
            this.x,
            this.y,
            region.width * Draw.scl * Draw.xscl,
            Draw.scl * Draw.yscl * region.height * Mathf.sign(!flipped),
            this.drawrot());

            this.drawTeamTop();
            Draw.color();
        }

        @Override
        public Cursor getCursor(){
            return canFlip ? SystemCursor.hand : SystemCursor.arrow;
        }

        @Override
        public byte version(){
            return (byte)(2 + 0x10*super.version());
        }


        @Override
        public void tapped(){
            super.tapped();
            if(canFlip){
                Sounds.click.at(this);
                configure(!flipped);
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, (byte)(revision/0x10));
            revision = (byte)(revision &0xF);
            if(revision > 0) flipped = read.bool();
            if(revision > 1){
                sides[leftSideIndex] = read.i();
                sides[rightSideIndex] = read.i();
            }
        }

        @Override
        public Object config(){
            return flipped;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.bool(flipped);
            write.i(sides[leftSideIndex]);
            write.i(sides[rightSideIndex]);
        }
    }
}
