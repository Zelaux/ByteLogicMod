package bytelogic.world.blocks.logic;


import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
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
import mindustry.world.*;
import mma.type.pixmap.*;

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
    @Annotations.Load("@realName()-center")
    public TextureRegion centerRegion;
    protected /*@NonNull*/ UnaryProcessor processor;

    public UnaryLogicBlock(String name){
        super(name);
        configurable = true;
        this.<Byte, UnaryLogicBuild>config(Byte.class, (build, value) -> {
            build.inputType = value;
        });
        this.<byte[], UnaryLogicBuild>config(byte[].class, (build, value) -> {
            build.inputType = value[0];
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
    public Pixmap generate(Pixmap icon, PixmapProcessor processor){
        icon = super.generate(icon, processor);
        if (centerRegion.found()){
            icon.draw(processor.get(centerRegion),true);
        }
        applyMask(sideRegion,processor);
        return icon;
    }

    @Override
    public void init(){
        if(blockPreview == null){
            blockPreview = new SchematicBlockPreview(
                this,
                Schematics.readBase64("bXNjaAF4nF1Ouw7CMAy8Ni0DRUwIqWLhBzLwPYghaa0S4SZRkg79ekiAqWfp/DifZexQCzRWzYTDYlVYZaKY5A3dSHEIxifjLIAdK00cUd8fLa56TSTZTWaQsxulsX5J0rMa6Ol4pIDTZiMQqxX9ZjovnIxnkw2XjRTNZBVLzW544bwRRxN9uZffQsG+UJVzlbkuTQ0BHHPR/iSRoyp9/zV0dpk1BZlWT+8/RIMmS+IDAytMKA==")
            );
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
        if(req.config instanceof Byte value){
            req.config =new byte[]{value};
            drawPlanRegion(req, list);
            return;
        }
        if(!(req.config instanceof byte[] valueWrapped)){
            req.config = unarySideState(false, true, false);
            drawPlanRegion(req, list);
            return;
        }
        byte value = valueWrapped[0];
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
        if(req.config instanceof Byte value){
            req.config=new byte[]{value};
            flipRotation(req, x);
            return;
        }
        if(!(req.config instanceof byte[] valueWrapped)){
            super.flipRotation(req, x);
            return;
        }
        byte value = valueWrapped[0];
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
        public void nextBuildings(IntSeq positions){
            Tile front = frontTile();
            if(front != null) positions.add(front.array());
        }

        @Override
        public void draw(){
            Draw.rect(base, tile.drawx(), tile.drawy());
            Draw.color(signalColor());
            if(centerRegion.found()){
                Draw.rect(centerRegion,x,y,drawrot());
            }
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
            return new byte[]{inputType};
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
