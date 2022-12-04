package bytelogic.world.blocks.logic;


import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import bytelogic.gen.*;
import bytelogic.type.*;
import bytelogic.ui.guide.*;
import mindustry.annotations.*;
import mindustry.content.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.game.Schematic.*;
import mindustry.gen.*;
import mindustry.ui.*;

public abstract class UnaryLogicBlock extends LogicBlock{
    protected static final int backInput = 0;
    //    public String sideRegionName = ModVars.fullName("binary-output-0");
    protected static final int leftInput = 1;
    protected static final int rightInput = 2;
    @Annotations.Load("@nameWithoutPrefix()-side")
    public TextureRegion sideRegion;
    protected /*@NonNull*/ UnaryProcessor processor;

    public UnaryLogicBlock(String name){
        super(name);
        configurable = true;
        this.<Integer, UnaryLogicBuild>config(Integer.class, (build, value) -> {
            build.inputType = value;
        });
    }

    @Override
    public void init(){
        if(blockShowcase == null){
            blockShowcase=new SchematicBlockShowcase(
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
        if(!(req.config instanceof Integer value && value != backInput)){
            super.drawPlanRegion(req, list);
            return;
        }
        TextureRegion back = base;
        Draw.rect(back, req.drawx(), req.drawy(),
            back.width * req.animScale * Draw.scl,
            back.height * req.animScale * Draw.scl,
            0);

        Draw.rect(sideRegion, req.drawx(), req.drawy(),
            region.width * req.animScale * Draw.scl,
            region.height * req.animScale * Draw.scl * Mathf.sign(value == leftInput),
            req.rotation * 90);
    }

    @Override
    public void flipRotation(BuildPlan req, boolean x){
        if(!(req.config instanceof Integer value && value != backInput)){
            super.flipRotation(req, x);
            return;
        }
        if((req.rotation % 2 == 0) == x){
            req.rotation = Mathf.mod(req.rotation + 2, 4);
        }
        if(value == leftInput){
            req.config = rightInput;
        }else{
            req.config = leftInput;
        }

    }

    public interface UnaryProcessor{
        Signal process(Signal signal);
    }

    public class UnaryLogicBuild extends LogicBuild{
        public int inputType = backInput;

        @Override
        public void buildConfiguration(Table table){
            table.table(t -> {
                Button.ButtonStyle style = new Button.ButtonStyle(Styles.togglet);
                ButtonGroup<Button> group = new ButtonGroup<>();
                for(int i = 0; i < 3; i++){
                    int staticI = i;
                    float tailOffset = switch(i){
                        case backInput -> 0;
                        case leftInput -> -90;
                        case rightInput -> 90;
                        default -> throw new RuntimeException("Impossible value");
                    };

                    t.button(button -> {
                        button.setStyle(style);
                        Image arrow = new Image(BLIcons.Drawables.unaryInputArrow64);
                        Image tail = new Image(BLIcons.Drawables.unaryInputBack64);
                        button.stack(arrow, tail).update(_n -> {
                            arrow.setRotationOrigin(rotdeg(), Align.center);
                            tail.setRotationOrigin(rotdeg() + tailOffset, Align.center);
                        }).size(32f);
                    }, () -> configure(staticI)).checked(i == inputType).size(48f).with(group::add);
                }
            });
        }

        @Override
        public void draw(){
            if(inputType == backInput){
                super.draw();
                return;
            }
            sideRegion.flip(false, inputType == rightInput);
            Draw.rect(base, tile.drawx(), tile.drawy());

            Draw.color(signalColor());

            Draw.rect(sideRegion, x, y, drawrot());

            this.drawTeamTop();
            Draw.color();
            sideRegion.flip(false, inputType == rightInput);


        }

        @Override
        public Object config(){
            return inputType;
        }

        @Override
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, Signal signal){
            Building build = switch(inputType){
                case backInput -> back();
                case leftInput -> left();
                case rightInput -> right();
                default -> null;
            };
            if(build == otherBuilding) return super.acceptSignal(otherBuilding, signal);
            return false;
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
            inputType = read.i();
        }


        @Override
        public void customWrite(Writes write){
            write.i(inputType);
        }

        @Override
        public void customRead(Reads read){
            inputType = read.i();
        }

        @Override
        public short customVersion(){
            return 0;
        }
        /*
        @Override
        public int signal(){
            return processor.process(sback());
        }*/
    }
}
