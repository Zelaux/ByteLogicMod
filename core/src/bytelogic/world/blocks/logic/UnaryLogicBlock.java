package bytelogic.world.blocks.logic;


import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.Button;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.io.Reads;
import arc.util.io.Writes;
import bytelogic.gen.*;
import mindustry.annotations.Annotations;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mma.ModVars;

public abstract class UnaryLogicBlock extends LogicBlock {
    protected /*@NonNull*/ UnaryProcessor processor;
//    public String sideRegionName = ModVars.fullName("binary-output-0");

    @Annotations.Load("@nameWithoutPrefix()-side")
    public TextureRegion sideRegion;

    public String nameWithoutPrefix() {
        if (originalMirror == null) return name;
        return originalMirror.name;
    }

    public UnaryLogicBlock(String name) {
        super(name);
        configurable = true;
        this.<Integer, UnaryLogicBuild>config(Integer.class, (build, value) -> {
            build.inputType = value;
        });
    }

    @Override
    public void flipRotation(BuildPlan req, boolean x) {
        super.flipRotation(req, x);
    }

    public interface UnaryProcessor {
        int process(int signal);
    }

    private static final int backInput = 0;
    private static final int leftInput = 1;
    private static final int rightInput = 2;

    public class UnaryLogicBuild extends LogicBuild {
        int inputType = backInput;

        @Override
        public void buildConfiguration(Table table) {
            table.table(t -> {
                Button.ButtonStyle style = new Button.ButtonStyle(Styles.togglet);
                ButtonGroup<Button> group = new ButtonGroup<>();
                for (int i = 0; i < 3; i++) {
                    int staticI = i;
                    float tailOffset = switch (i) {
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
        public void draw() {
            if (inputType == backInput) {
                super.draw();
                return;
            }
            sideRegion.flip(false, inputType == rightInput);
            Draw.rect(base, tile.drawx(), tile.drawy());

            Draw.color(currentSignal() > 0 ? Pal.accent : Color.white);

            Draw.rect(sideRegion, x, y, drawrot());

            this.drawTeamTop();
            Draw.color();
            sideRegion.flip(false, inputType == rightInput);


        }

        @Override
        public Object config() {
            return inputType;
        }

        @Override
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, int signal) {
            Building build = switch (inputType) {
                case backInput -> back();
                case leftInput -> left();
                case rightInput -> right();
                default -> null;
            };
            if (build == otherBuilding) return super.acceptSignal(otherBuilding, signal);
            return false;
        }

        @Override
        public void updateSignalState() {
            lastSignal = processor.process(nextSignal);
            nextSignal = 0;

        }

        @Override
        public void beforeUpdateSignalState() {
            if (doOutput && output((byte) rotation)) {
                front().<LogicBuild>as().acceptSignal(this, lastSignal);
            }
        }

        @Override
        public byte version() {
            return (byte) (0x10 * 1 + super.version());
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, (byte) (revision & 0xF));
            revision = (byte) (revision / 0x10);
            if (revision == 0) return;
            inputType = read.i();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(inputType);
        }
        /*
        @Override
        public int signal(){
            return processor.process(sback());
        }*/
    }
}
