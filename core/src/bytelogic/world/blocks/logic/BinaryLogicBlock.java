package bytelogic.world.blocks.logic;

import arc.*;
import arc.Graphics.*;
import arc.Graphics.Cursor.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import bytelogic.gen.*;
import bytelogic.type.*;
import bytelogic.ui.guide.*;
import mindustry.*;
import mindustry.annotations.Annotations.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mma.*;
import mma.graphics.*;
import mma.io.*;
import mma.type.*;
import mma.type.pixmap.*;

import static mindustry.Vars.world;

public abstract class BinaryLogicBlock extends LogicBlock implements ImageGenerator{
    protected static final int leftSideIndex = 0;
    protected static final int rightSideIndex = 1;
    private static final ByteReads tmpReads = new ByteReads();
    private static final ByteWrites tmpWrites = new ByteWrites();
    private static final int bothSideInputType = 0;
    private static final int rightFromBackInputType = 1;
    private static final int leftFromBackInputType = 2;
    public boolean canFlip;
    public String operatorName;
    public String outputRegionName = ModVars.fullName("binary-output-0");
    public String sideOutputRegionName = ModVars.fullName("binary-output-1");
    @Load("@outputRegionName")
    public TextureRegion outputsRegion;
    @Load("@sideOutputRegionName")
    public TextureRegion sideOutputsRegion;
    @Load("@centerRegionName()")
    public TextureRegion centerRegion;
    public boolean ownsCenterRegion = true;
    public TextureRegion[] compiledRegions = new TextureRegion[2];
    public boolean needImageCompilation;
    protected /*@NonNull*/ BinaryProcessor processor;

    public BinaryLogicBlock(String name){
        super(name);
        configurable = true;
        config(byte[].class, (BinaryLogicBuild build, byte[] bytes) -> {
            tmpReads.setBytes(bytes);
            build.flippedInputs = tmpReads.bool();
            build.inputType = tmpReads.i();

        });
    }
    private static byte[] bytesOfState(boolean flippedInputs, int inputType){
        tmpWrites.reset();
        tmpWrites.bool(flippedInputs);
        tmpWrites.i(inputType);
        return tmpWrites.getBytes();
    }


    public String centerRegionName(){
        if(ownsCenterRegion || originalMirror == null) return name + "-center";
        return originalMirror.name + "-center";
    }

    @Override
    public void load(){
        super.load();
        for(int i = 0; needImageCompilation && i < compiledRegions.length; i++){
            compiledRegions[i] = Core.atlas.find(name + "-compiled-" + i);
        }
    }

    @Override
    public Pixmap generate(Pixmap icon, PixmapProcessor processor){
        icon = super.generate(icon, processor);
        if(!needImageCompilation) return icon;

        Pixmap base = processor.get(centerRegion);



        Pixmap output = applyMask(outputsRegion,processor);
        Pixmap outputSide = applyMask(sideOutputsRegion,processor);


        Pixmap compiledOutput = base.copy();
        compiledOutput.draw(output, true);
        processor.save(compiledOutput, name + "-compiled-0");

        Pixmap compiledSideOutput = base.copy();
        compiledSideOutput.draw(outputSide, true);
        processor.save(compiledSideOutput, name + "-compiled-1");
        return icon;

    }

    @Override
    public void init(){
        if(blockPreview == null)
            blockPreview = new SchematicBlockPreview(
                this,
                Schematics.readBase64("bXNjaAF4nF2MwY7CMAxEpw1dpCI4ISS0l/2BHPgexCFpLbDWTaIkPfTrIak4deZgeebZ6KAUds5MhKNlZ+KiM6WsbziMlIbIIbN3AH7EWJKE9v7o8GeXTFr8kwc9+VGzC3PWQcxALy8jRZw3RCQxC66bdJolcxAuB7+bKvHTGdFW/PCPy6YcOYX6D9ijqkdTR9OvA20P1aAabXFzKlm3AlA1qft1JQ9unixFnZdA76/UrlCF/ABgfUyQ")
            );
        super.init();
        consumesTap = canFlip;
        if(processor == null){
            throw new IllegalArgumentException("processor of " + name + " is null");
        }
        if(operatorName == null){
            throw new IllegalArgumentException("operatorName of " + name + " is null");
        }
    }

    protected TextureRegion getOutputsRegion(){
        if(needImageCompilation) return compiledRegions[0];
        return outputsRegion;
    }

    protected TextureRegion getSideOutputsRegion(){
        if(needImageCompilation) return compiledRegions[1];
        return sideOutputsRegion;
    }

    @Override
    public void drawPlanRegion(BuildPlan req, Eachable<BuildPlan> list){


        if(!(req.config instanceof byte[] bytes)){
            req.config = bytesOfState(false, 0);
            drawPlanRegion(req, list);
            return;
        }
        tmpReads.setBytes(bytes);
        float scale = req.animScale * Draw.scl;

        boolean flipped = tmpReads.bool();
        int type = tmpReads.i();


        TextureRegion back = base;
        Draw.rect(back, req.drawx(), req.drawy(),
            back.width * scale,
            back.height * scale,
            0);
        if(!needImageCompilation){
            Draw.rect(centerRegion, req.drawx(), req.drawy(),
                region.width * scale,
                region.height * scale * Mathf.sign(!flipped),
                !rotate ? 0 : req.rotation * 90);
        }
        if(type == bothSideInputType){
            Draw.rect(getOutputsRegion(), req.drawx(), req.drawy(),
                region.width * scale,
                region.height * scale,
                !rotate ? 0 : req.rotation * 90);
            return;
        }

        TextureRegion sideOutputsRegion = getSideOutputsRegion();
        sideOutputsRegion.flip(false, type == leftFromBackInputType);
        Draw.rect(sideOutputsRegion, req.drawx(), req.drawy(),
            region.width * scale,
            region.height * scale,
            !rotate ? 0 : req.rotation * 90);
        sideOutputsRegion.flip(false, type == leftFromBackInputType);

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
        }else if(req.config instanceof byte[] bytes){
            tmpReads.setBytes(bytes);
            boolean flipped = tmpReads.bool() && canFlip;
            int inputType = tmpReads.i();

            if((req.rotation % 2 == 0) == x){
                req.rotation = Mathf.mod(req.rotation + 2, 4);
            }
            flipped = !flipped;

            if(inputType != bothSideInputType){
                if(inputType == leftFromBackInputType){
                    inputType = rightFromBackInputType;
                }else{
                    inputType = leftFromBackInputType;
                }
            }

            req.config = bytesOfState(flipped && canFlip, inputType);
        }else{
            super.flipRotation(req, x);
        }
    }

    public interface BinaryProcessor{
        Signal process(Signal left, Signal right);
    }

    public class BinaryLogicBuild extends LogicBuild{
        final Signal[] sides = {new Signal(), new Signal()};
        boolean flippedInputs = false;
        int inputType = bothSideInputType;

        @Override
        public void nextBuildings(IntSeq positions){
            Tile front = frontTile();
            if(front != null) positions.add(front.array());
        }

        @Override
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, Signal signal){
            if(right() == otherBuilding && (inputType != rightFromBackInputType)){
                sides[rightSideIndex].set(signal);
                return true;
            }
            if(left() == otherBuilding && (inputType != leftFromBackInputType)){
                sides[leftSideIndex].set(signal);
                return true;
            }
            if(back() == otherBuilding && inputType != bothSideInputType){
                if(inputType == leftFromBackInputType){
                    sides[leftSideIndex].set(signal);
                }else{//1
                    sides[rightSideIndex].set(signal);
                }
                return true;
            }
            return false;
//            return super.acceptSignal(otherBuilding, signal);
        }

        @Override
        public void updateSignalState(){

            lastSignal.set(getNextSignal());
            sides[0].setZero();
            sides[1].setZero();
            nextSignal.setZero();

        }

        @Override
        public void beforeUpdateSignalState(){
            if(doOutput && canOutputSignal(rotation)){
                front().<ByteLogicBuildingc>as().acceptSignal(this, lastSignal);
            }
        }

        //        @Override
        public Signal getNextSignal(){
            Signal left, right;
            if(!flippedInputs){
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
            if(!canDrawSelect()) return;

            Tile left = tile.nearby((rotation + 1) % 4);
            Tile right = tile.nearby((rotation + 4 - 1) % 4);
            Tile front = tile.nearby((rotation) % 4);
            Tile back = tile.nearby((rotation + 2) % 4);
            float textSize = 0.15f;
            Color color = Pal.accent;
            if(inputType == leftFromBackInputType){
                ADrawf.drawText(back.worldx(), back.worldy(), textSize, color, "a");//draw on back tile
            }else{
                ADrawf.drawText(left.worldx(), left.worldy(), textSize, color, "a");//draw on left tile
            }
            ADrawf.drawText(front.worldx(), front.worldy(), textSize, color, !flippedInputs ? "a " + operatorName + " b" : "b " + operatorName + " a");
            if(inputType == rightFromBackInputType){
                ADrawf.drawText(back.worldx(), back.worldy(), textSize, color, "b");//draw on back tile
            }else{
                ADrawf.drawText(right.worldx(), right.worldy(), textSize, color, "b");//draw on right tile
            }
        }

        @Override
        public void draw(){


//            super.draw(tile);

            Draw.rect(base, tile.drawx(), tile.drawy());
            Draw.color(signalColor());
            if(!needImageCompilation){
                Draw.rect(centerRegion,
                    x, y,
                    region.width * Draw.scl * Draw.xscl, Draw.scl * Draw.yscl * region.height * Mathf.sign(!flippedInputs),
                    this.drawrot());
            }
            if(inputType == bothSideInputType){
                Draw.rect(getOutputsRegion(),
                    x, y,
                    region.width * Draw.scl * Draw.xscl, Draw.scl * Draw.yscl * region.height,
                    this.drawrot());
            }else{
                TextureRegion sideOutputsRegion = getSideOutputsRegion();
                sideOutputsRegion.flip(false, inputType == leftFromBackInputType);
                Draw.rect(sideOutputsRegion,
                    x, y,
                    region.width * Draw.scl * Draw.xscl, Draw.scl * Draw.yscl * region.height,
                    this.drawrot());
                sideOutputsRegion.flip(false, inputType == leftFromBackInputType);
            }

            this.drawTeamTop();
            Draw.color();

            Vec2 vec = Core.input.mouseWorld(Vars.control.input.getMouseX(), Vars.control.input.getMouseY());
            Building buildUnderCursor = world.buildWorld(vec.x, vec.y);
            if(Vars.control.input.config.getSelected() == this && buildUnderCursor != this){
                Draw.draw(Layer.overlayUI, this::drawSelect);
            }
        }


        @Override
        public Cursor getCursor(){
            return SystemCursor.hand;
        }


        @Override
        public void buildConfiguration(Table table){
            super.buildConfiguration(table);
            TextureRegionDrawable[] drawables = {
                BLIcons.Drawables.binaryInput0_64,
                BLIcons.Drawables.binaryInput1_64,
                BLIcons.Drawables.binaryInput2_64
            };

            table.table(t -> {
                t.table(inputTypeButtons -> {

                    ButtonGroup<ImageButton> group = new ButtonGroup<>();
                    ImageButton.ImageButtonStyle buttonStyle = new ImageButton.ImageButtonStyle(Styles.defaulti){{
                        this.checked = this.down;
                    }};

                    for(int i = 0; i < 3; i++){
                        int staticI = i;

                        inputTypeButtons.button(drawables[i], buttonStyle, 32f, () -> {
                            configureState(flippedInputs, staticI);
                        }).size(48f).checked(i == inputType).with(group::add).update(button -> {
                            button.getImage().setRotationOrigin(rotdeg(), Align.center);
                        });
                    }
                }).row();
                if(canFlip){
                    t.table(flipButtons -> {
                        ButtonGroup<TextButton> group = new ButtonGroup<>();
                        TextButton.TextButtonStyle style = Styles.togglet;

                        Cons<TextButton> fixer = it -> it.getLabel().setWrap(false);
                        flipButtons.button("a " + operatorName + " b", style, () -> {
                            configureState(false, inputType);
                        }).checked(!flippedInputs).with(group::add).with(fixer);
                        flipButtons.button("b " + operatorName + " a", style, () -> {
                            configureState(true, inputType);
                        }).checked(flippedInputs).with(group::add).with(fixer);
                    });
                }
            });
        }

        @Override
        public void updateTableAlign(Table table){
            Vec2 pos = Core.input.mouseScreen(x, y - (block.size * Vars.tilesize) / 2f - 1 - (Vars.tilesize - 1));
            table.setPosition(pos.x, pos.y, 2);
        }

        void configureState(boolean flippedInputs, int inputType){
            configure(bytesOfState(flippedInputs, inputType));
        }


        @Override
        public byte version(){
            return (byte)(4 + 0x10 * super.version());
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, (byte)(revision / 0x10));
            revision = (byte)(revision & 0xF);

            if(revision == 0 || revision == 4) return;
            flippedInputs = read.bool();
            if(revision == 1) return;

            Signal.valueOf(sides[leftSideIndex], read.i());
            Signal.valueOf(sides[rightSideIndex], read.i());
            if(revision == 2) return;
            inputType = read.i();
            if(revision == 3) return;
        }

        @Override
        public Object config(){
            return bytesOfState(flippedInputs, inputType);
        }

        @Override
        public void customWrite(Writes write){
            write.bool(flippedInputs);
            sides[leftSideIndex].write(write);
            sides[rightSideIndex].write(write);
            write.i(inputType);
        }

        @Override
        public void customRead(Reads read){
            flippedInputs = read.bool();
            sides[leftSideIndex].read(read);
            sides[rightSideIndex].read(read);
            inputType = read.i();
        }

        @Override
        public short customVersion(){
            return 1;
        }
    }
}
