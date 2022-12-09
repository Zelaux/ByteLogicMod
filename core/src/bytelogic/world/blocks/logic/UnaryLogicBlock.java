package bytelogic.world.blocks.logic;


import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import bytelogic.annotations.BLAnnotations.*;
import bytelogic.gen.*;
import bytelogic.type.*;
import bytelogic.ui.guide.*;
import mindustry.annotations.*;
import mindustry.annotations.Annotations.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.ui.*;

public abstract class UnaryLogicBlock extends LogicBlock{

    protected static final int[] sideMasks = {
        0,
        UnaryInputSides.bitMaskLeft,
        UnaryInputSides.bitMaskBack,
        UnaryInputSides.bitMaskRight
    };
    protected static final int combinedSideMask = UnaryInputSides.bitMaskLeft | UnaryInputSides.bitMaskBack | UnaryInputSides.bitMaskRight;
    protected static final int leftSideMaskIndex = 1;
    protected static final int backSideMaskIndex = 2;
    protected static final int rightSideMaskIndex = 3;
    @Annotations.Load("@realName()-side")
    public TextureRegion sideRegion;
    protected /*@NonNull*/ UnaryProcessor processor;

    public UnaryLogicBlock(String name){
        super(name);
        configurable = true;
        this.<Byte, UnaryLogicBuild>config(Byte.class, (build, value) -> {
            build.inputType = value;
        });
        this.<Integer, UnaryLogicBuild>config(Integer.class, (build, value) -> {
            build.inputType = updateInputType(value);
        });
    }

    protected static byte unarySideState(boolean left, boolean back, boolean right){
        return UnaryInputSides.get(0b11, left, back, right);
    }

    protected static byte updateInputType(int oldInputType){
        final int backInput = 0;
        final int leftInput = 1;
        final int rightInput = 2;
        return unarySideState(
            oldInputType == leftInput, oldInputType == backInput, oldInputType == rightInput
        );
    }

    @Override
    public void init(){
        if(blockPreview == null){
            blockPreview = new SchematicBlockPreview(
                this,
                Schematics.readBase64("bXNjaAF4nF1Ouw7CMAy8Ni0DRUwIqWLhBzLwPYghaa0S4SZRkg79ekiAqWfp/DifZexQCzRWzYTDYlVYZaKY5A3dSHEIxifjLIAdK00cUd8fLa56TSTZTWaQsxulsX5J0rMa6Ol4pIDTZiMQqxX9ZjovnIxnkw2XjRTNZBVLzW544bwRRxN9uZffQsG+UJVzlbkuTQ0BHHPR/iSRoyp9/zV0dpk1BZlWT+8/RIMmS+IDAytMKA==")
            );/*
            blockShowcase = new BlockShowcase(this, 5, 5, (world, isSwitch) -> {
                Schematic schematic = Schematics
                                          .readBase64("bXNjaAF4nH2Ou24CQQxF775IQURPEwnRIbnI9yCK2V1rM8Lz0HhWaL+ezCZQwrFc2Dq+MjrUDVpvHONz9iYtlFkzfWM7sg7JxmyDB7AR07Mo6vOlw6lfMpOEyQ7kwkjKKc4SSO3kjZDebB5+qJcwXPHhWNVMjMOLIzdLtlEsJxzf5/4Hfr2QRqtRzFJ+7bBS40lVCm3ppl63u6dSoSlztc77P3HrZ9dzorxEvj9o2mIVfgFGtFTY");
                int offsetY = 0;
                if(schematic.height < 5){
                    offsetY++;
                }
                Point2[] points = schematic.tiles
                                       .select(it -> it.block == Blocks.message)
                                       .map(it -> new Point2(it.x, it.y))
                                       .toArray(Point2.class);
                for(Stile tile : schematic.tiles){
                    if(tile.block == Blocks.message){
                        tile.block = this;
                    }
                    world.tile(tile.x, tile.y + offsetY).setBlock(tile.block, Team.sharded, tile.rotation);
                    world.tile(tile.x, tile.y + offsetY).build.configured(null, tile.config);
                }*//*

                world.tile(0, 1).setBlock(inputBlock(isSwitch), Team.sharded, 0);

                world.tile(1, 1).setBlock(this, Team.sharded, 0);
                world.tile(2, 1).setBlock(byteLogicBlocks.multiplier, Team.sharded, 0);
                world.tile(2, 1).build.<BinaryLogicBuild>as().inputType = 1;

                world.tile(2, 2).setBlock(byteLogicBlocks.signalBlock, Team.sharded, 3);
                world.tile(2, 2).build.<SignalLogicBuild>as().nextSignal.setNumber(-1);
                world.tile(3, 1).setBlock(byteLogicBlocks.displayBlock, Team.sharded);*//*
                return points;
            });*/
        }
        super.init();
//        if(processor == null) throw new RuntimeException("Processor for " + name + " is null");
    }

    @Override
    public void drawPlanRegion(BuildPlan req, Eachable<BuildPlan> list){

        if(req.config instanceof Integer value){
            req.config = updateInputType(value);
            drawPlanRegion(req, list);
            return;
        }
        if(!(req.config instanceof Byte value)){
            req.config = unarySideState(false, true, false);
            drawPlanRegion(req, list);
            return;
        }
        TextureRegion back = base;
        Draw.rect(back, req.drawx(), req.drawy(),
            back.width * req.animScale * Draw.scl,
            back.height * req.animScale * Draw.scl,
            0);
        for(int i = 1; i < sideMasks.length; i++){
            if((value & sideMasks[i]) == 0) continue;
            Draw.rect(i == backSideMaskIndex ? region : sideRegion, req.drawx(), req.drawy(),
                region.width * req.animScale * Draw.scl,
                region.height * req.animScale * Draw.scl * Mathf.sign(i == leftSideMaskIndex),
                req.rotation * 90);
        }
    }

    @Override
    public void flipRotation(BuildPlan req, boolean x){
        if(req.config instanceof Integer value){
            req.config = updateInputType(value);
            flipRotation(req, x);
            return;
        }
        if(!(req.config instanceof Byte value)){
            super.flipRotation(req, x);
            return;
        }
        if((req.rotation % 2 == 0) == x){
            req.rotation = Mathf.mod(req.rotation + 2, 4);
        }
        boolean left = UnaryInputSides.left(value);
        boolean right = UnaryInputSides.right(value);


        value = UnaryInputSides.left(value, right);
        value = UnaryInputSides.right(value, left);

        req.config = value;

    }

    public interface UnaryProcessor{
        Signal process(Signal signal);
    }

    public class UnaryLogicBuild extends LogicBuild{
        public byte inputType = unarySideState(false, true, false);

        @Override
        public void buildConfiguration(Table table){
            table.table(t -> {
                Button.ButtonStyle style = new Button.ButtonStyle(Styles.togglet);
                for(int i = 1; i < 4; i++){
                    int staticI = i;
                    float tailOffset = switch(i){
                        case backSideMaskIndex -> 0;
                        case leftSideMaskIndex -> -90;
                        case rightSideMaskIndex -> 90;
                        default -> throw new RuntimeException("Impossible value");
                    };
                    int sideMask = sideMasks[staticI];
                    t.button(button -> {
                        button.setStyle(style);
                        Image arrow = new Image(BLIcons.Drawables.unaryInputArrow64);
                        Image tail = new Image(BLIcons.Drawables.unaryInputBack64);
                        button.stack(arrow, tail).update(_n -> {
                            arrow.setRotationOrigin(rotdeg(), Align.center);
                            tail.setRotationOrigin(rotdeg() + tailOffset, Align.center);
                        }).size(32f);
                    }, () -> {
                        byte newValue = (byte)(inputType & ~sideMask | ((inputType & sideMask) != 0 ? 0 : sideMask));
                        if((newValue & combinedSideMask) == 0) return;
                        configureInputType(newValue);
                    }).checked(it -> (inputType & sideMask) != 0).size(48f);

                }
            });
        }

        protected void configureInputType(byte inputType){
            configure(inputType);
        }

        @Override
        public void draw(){
            Draw.rect(base, tile.drawx(), tile.drawy());
            Draw.color(signalColor());
            for(int i = 1; i < sideMasks.length; i++){
                if((inputType & sideMasks[i]) == 0) continue;
                if(i == rightSideMaskIndex) sideRegion.flip(false, true);
                Draw.rect(i == backSideMaskIndex ? region : sideRegion, x, y, drawrot());
                if(i == rightSideMaskIndex) sideRegion.flip(false, true);
            }

            this.drawTeamTop();
            Draw.color();
        }


        @Override
        public Object config(){
            return inputType;
        }

        @Override
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, Signal signal){
            int sideIndex = (relativeTo(otherBuilding.<Building>as()) + 4 - rotation) & 0b11;
            if((inputType & sideMasks[sideIndex]) == 0) return false;
            return super.acceptSignal(otherBuilding, signal);
        }

        @Override
        public void updateSignalState(){
            lastSignal.set(processor.process(nextSignal));
            nextSignal.setZero();

        }

        @Override
        public void beforeUpdateSignalState(){
            if(doOutput && canOutputSignal((byte)rotation)){
                front().<ByteLogicBuildingc>as().acceptSignal(this, lastSignal);
            }
        }

        @Override
        public byte version(){
            return (byte)(0x10 * 2 + super.version());
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, (byte)(revision & 0xF));
            revision = (byte)(revision / 0x10);
            if(revision != 1) return;
            setOldInput(read.i());
        }

        private void setOldInput(int oldInputType){
            inputType = updateInputType(oldInputType);
        }


        @Override
        public void customWrite(Writes write){
            write.b(inputType);
        }

        @Override
        public void customRead(Reads read){
            inputType = read.b();
            boolean any = false;
            for(int i = 0; i < sideMasks.length && !any; i++){
                if((inputType & sideMasks[i]) != 0) any = true;
            }
            if(!any){
                inputType = unarySideState(false, true, false);
            }

        }

        @Override
        public short customVersion(){
            return 1;
        }
        /*
        @Override
        public int signal(){
            return processor.process(sback());
        }*/
    }

}

@Struct
@RemoveFromCompilation
class UnaryInputSidesStruct{
    @StructField(2)
    int versionState;
    boolean left;
    boolean back;
    boolean right;
}
