package bytelogic.world.blocks.logic;

import arc.Core;
import arc.Graphics.*;
import arc.Graphics.Cursor.*;
import arc.func.Cons;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.Vec2;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.ImageButton;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.*;
import arc.util.Align;
import arc.util.Eachable;
import arc.util.io.*;
import bytelogic.gen.*;
import mindustry.Vars;
import mindustry.annotations.Annotations.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.Styles;
import mindustry.world.*;
import mma.*;
import mma.core.ModContentLoader;
import mma.graphics.*;
import mma.io.ByteReads;
import mma.io.ByteWrites;

import static mindustry.Vars.world;

public abstract class BinaryLogicBlock extends LogicBlock {
    protected static final int leftSideIndex = 0;
    protected static final int rightSideIndex = 1;
    public boolean canFlip;
    public String operatorName;
    protected /*@NonNull*/ BinaryProcessor processor;
    public String outputRegionName = ModVars.fullName("binary-output-0");
    public String sideOutputRegionName = ModVars.fullName("binary-output-1");
    @Load("@outputRegionName")
    public TextureRegion outputsRegion;
    @Load("@sideOutputRegionName")
    public TextureRegion sideOutputsRegion;

    @Load("@centerRegionName()")
    public TextureRegion centerRegion;
    public boolean ownsCenterRegion = true;
    private static final ByteReads tmpReads = new ByteReads();
    private static final ByteWrites tmpWrites = new ByteWrites();

    public String centerRegionName() {
        if (ownsCenterRegion || originalMirror == null) return name + "-center";
        return originalMirror.name + "-center";
    }

    public BinaryLogicBlock(String name) {
        super(name);
        configurable = true;
        config(byte[].class, (BinaryLogicBuild build, byte[] bytes) -> {
            tmpReads.setBytes(bytes);
            build.flippedInputs = tmpReads.bool();
            build.inputType = tmpReads.i();

        });
    }

    public TextureRegion[] compiledRegions = new TextureRegion[2];
    public boolean needCompilation;

    @Override
    public void load() {
        super.load();
        for (int i = 0; needCompilation && i < compiledRegions.length; i++) {
            compiledRegions[i] = Core.atlas.find(name + "-compiled-" + i);
        }
    }

    @Override
    public void createIcons(MultiPacker packer) {
        super.createIcons(packer);
        BLContentRegions.loadRegions(this);
        if (needCompilation) {
            Pixmap center = Core.atlas.getPixmap(centerRegion).crop();
            Pixmap output = Core.atlas.getPixmap(outputsRegion).crop();
            Pixmap outputSide = Core.atlas.getPixmap(sideOutputsRegion).crop();

            Pixmap copy = center.copy();
            copy.draw(output, true);
            packer.add(MultiPacker.PageType.main, name + "-compiled-0", copy);
            Pixmap copy2 = center.copy();
            copy2.draw(outputSide, true);
            packer.add(MultiPacker.PageType.main, name + "-compiled-1", copy2);
        }
    }

    @Override
    public void init() {
        super.init();
        consumesTap = canFlip;
        if (processor == null) {
            throw new IllegalArgumentException("processor of " + name + " is null");
        }
        if (operatorName == null) {
            throw new IllegalArgumentException("processor of " + operatorName + " is null");
        }
    }

    protected TextureRegion getOutputsRegion() {
        if (needCompilation) return compiledRegions[0];
        return outputsRegion;
    }

    protected TextureRegion getSideOutputsRegion() {
        if (needCompilation) return compiledRegions[1];
        return sideOutputsRegion;
    }

    @Override
    public void drawPlanRegion(BuildPlan req, Eachable<BuildPlan> list) {


        if (!(req.config instanceof byte[] bytes)) {
//            req.config = bytesOfState(false, 0);
            super.drawPlanRegion(req, list);
            return;
        }
        tmpReads.setBytes(bytes);
        TextureRegion back = base;
        float scale = req.animScale * Draw.scl;

        Draw.rect(back, req.drawx(), req.drawy(),
                back.width * scale,
                back.height * scale,
                0);
        boolean flipped = tmpReads.bool();
        Draw.rect(centerRegion, req.drawx(), req.drawy(),
                region.width * scale,
                region.height * scale * Mathf.sign(!flipped),
                !rotate ? 0 : req.rotation * 90);
        int type = tmpReads.i();
        if (type == bothSideInputType) {
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
    public void flipRotation(BuildPlan req, boolean x) {
//        super.flipRotation(req, x);
        if (req.config instanceof Boolean bool && canFlip) {
            if ((x == (req.rotation % 2 == 0)) != invertFlip) {
                req.rotation = Mathf.mod(req.rotation + 2, 4);
            } else {
                req.config = !bool;
            }
        } else if (req.config instanceof byte[] bytes) {
            tmpReads.setBytes(bytes);
            boolean flipped = tmpReads.bool() && canFlip;
            int inputType = tmpReads.i();

            if ((req.rotation % 2 == 0) == x) {
                req.rotation = Mathf.mod(req.rotation + 2, 4);
            }
            flipped = !flipped;

            if (inputType != bothSideInputType) {
                if (inputType == leftFromBackInputType) {
                    inputType = rightFromBackInputType;
                } else {
                    inputType = leftFromBackInputType;
                }
            }

            req.config = bytesOfState(flipped && canFlip, inputType);
        } else {
            super.flipRotation(req, x);
        }
    }

    public interface BinaryProcessor {
        int process(int left, int right);
    }

    private static final int bothSideInputType = 0;
    private static final int rightFromBackInputType = 1;
    private static final int leftFromBackInputType = 2;

    public class BinaryLogicBuild extends LogicBuild {
        final int[] sides = {0, 0};
        boolean flippedInputs = false;
        int inputType = bothSideInputType;

        @Override
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, int signal) {
            if (right() == otherBuilding && (inputType != rightFromBackInputType)) {
                sides[rightSideIndex] = signal;
                return true;
            }
            if (left() == otherBuilding && (inputType != leftFromBackInputType)) {
                sides[leftSideIndex] = signal;
                return true;
            }
            if (back() == otherBuilding && inputType != bothSideInputType) {
                if (inputType == leftFromBackInputType) {
                    sides[leftSideIndex] = signal;
                } else {//1
                    sides[rightSideIndex] = signal;
                }
                return true;
            }
            return false;
//            return super.acceptSignal(otherBuilding, signal);
        }

        @Override
        public void updateSignalState() {

            lastSignal = getNextSignal();
            sides[0] = 0;
            sides[1] = 0;
            nextSignal = 0;

        }

        @Override
        public void beforeUpdateSignalState() {
            if (doOutput && output(rotation)) {
                front().<LogicBuild>as().acceptSignal(this, lastSignal);
            }
        }

        //        @Override
        public int getNextSignal() {
            int left, right;
            if (!flippedInputs) {
                left = sides[leftSideIndex];
                right = sides[rightSideIndex];
            } else {
                left = sides[rightSideIndex];
                right = sides[leftSideIndex];
            }

            return processor.process(left, right);
        }

        @Override
        public void drawSelect() {
            super.drawSelect();

            Tile left = tile.nearby((rotation + 1) % 4);
            Tile right = tile.nearby((rotation + 4 - 1) % 4);
            Tile front = tile.nearby((rotation) % 4);
            Tile back = tile.nearby((rotation + 2) % 4);
            float textSize = 0.15f;
            Color color = Pal.accent;
            if (inputType == leftFromBackInputType) {
                ADrawf.drawText(back.worldx(), back.worldy(), textSize, color, "a");//draw on back tile
            } else {
                ADrawf.drawText(left.worldx(), left.worldy(), textSize, color, "a");//draw on left tile
            }
            ADrawf.drawText(front.worldx(), front.worldy(), textSize, color, !flippedInputs ? "a " + operatorName + " b" : "b " + operatorName + " a");
            if (inputType == rightFromBackInputType) {
                ADrawf.drawText(back.worldx(), back.worldy(), textSize, color, "b");//draw on back tile
            } else {
                ADrawf.drawText(right.worldx(), right.worldy(), textSize, color, "b");//draw on right tile
            }
        }

        @Override
        public void draw() {


            Draw.rect(base, tile.drawx(), tile.drawy());

            Draw.color(currentSignal() > 0 ? Pal.accent : Color.white);
//            super.draw(tile);

            if (!needCompilation) {

                Draw.rect(centerRegion,
                        x, y,
                        region.width * Draw.scl * Draw.xscl, Draw.scl * Draw.yscl * region.height * Mathf.sign(!flippedInputs),
                        this.drawrot());
            }
            if (inputType == bothSideInputType) {
                Draw.rect(getOutputsRegion(),
                        x, y,
                        region.width * Draw.scl * Draw.xscl, Draw.scl * Draw.yscl * region.height,
                        this.drawrot());
            } else {
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
            if (Vars.control.input.config.getSelected() == this && buildUnderCursor != this) {
                Draw.draw(Layer.overlayUI, this::drawSelect);
            }
        }


        @Override
        public Cursor getCursor() {
            return SystemCursor.hand;
        }

        @Override
        public byte version() {
            return (byte) (3 + 0x10 * super.version());
        }

        @Override
        public void buildConfiguration(Table table) {
            super.buildConfiguration(table);
            TextureRegionDrawable[] drawables = {
                    BLIcons.Drawables.binaryInput0_64,
                    BLIcons.Drawables.binaryInput1_64,
                    BLIcons.Drawables.binaryInput2_64
            };

            table.table(t -> {
                t.table(inputTypeButtons -> {

                    ButtonGroup<ImageButton> group = new ButtonGroup<>();
                    ImageButton.ImageButtonStyle buttonStyle = new ImageButton.ImageButtonStyle(Styles.defaulti) {{
                        this.checked = this.down;
                    }};

                    for (int i = 0; i < 3; i++) {
                        int staticI = i;

                        inputTypeButtons.button(drawables[i], buttonStyle, 32f, () -> {
                            configureState(flippedInputs, staticI);
                        }).size(48f).checked(i == inputType).with(group::add).update(button -> {
                            button.getImage().setRotationOrigin(rotdeg(), Align.center);
                        });
                    }
                }).row();
                if (canFlip) {
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
        public void updateTableAlign(Table table) {
            Vec2 pos = Core.input.mouseScreen(x, y - (block.size * Vars.tilesize) / 2f - 1 - (Vars.tilesize - 1));
            table.setPosition(pos.x, pos.y, 2);
        }

        void configureState(boolean flippedInputs, int inputType) {
            configure(bytesOfState(flippedInputs, inputType));
        }

        @Override
        public void tapped() {
            super.tapped();
//            if (canFlip) {
//                Sounds.click.at(this);
//                configure(!flippedInputs);
//            }
        }


        @Override
        public void read(Reads read, byte revision) {
            super.read(read, (byte) (revision / 0x10));
            revision = (byte) (revision & 0xF);
            if (revision == 0) return;
            flippedInputs = read.bool();
            if (revision == 1) return;
            sides[leftSideIndex] = read.i();
            sides[rightSideIndex] = read.i();
            if (revision == 2) return;
            inputType = read.i();
            if (revision == 3) return;
        }

        @Override
        public Object config() {
            return bytesOfState(flippedInputs, inputType);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.bool(flippedInputs);
            write.i(sides[leftSideIndex]);
            write.i(sides[rightSideIndex]);
            write.i(inputType);
        }
    }

    private static byte[] bytesOfState(boolean flippedInputs, int inputType) {
        tmpWrites.reset();
        tmpWrites.bool(flippedInputs);
        tmpWrites.i(inputType);
        return tmpWrites.getBytes();
    }
}
