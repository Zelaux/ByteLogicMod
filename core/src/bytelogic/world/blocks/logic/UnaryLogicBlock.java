package bytelogic.world.blocks.logic;


import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import bytelogic.game.*;
import bytelogic.gen.*;
import mindustry.annotations.*;
import mindustry.entities.units.*;
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
        int process(int signal);
    }

    public class UnaryLogicBuild extends LogicBuild{
        int inputType = backInput;

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
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, int signal){
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
            lastSignal = processor.process(nextSignal);
            nextSignal = 0;

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
            if(revision !=1) return;
            inputType = read.i();
        }

        @Override
        public void write(Writes write){
            super.write(write);
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
