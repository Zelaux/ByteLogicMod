package bytelogic.world.blocks.logic;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import bytelogic.gen.*;
import mindustry.*;
import mindustry.annotations.Annotations.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.*;
import mma.*;

import static mindustry.Vars.*;

public class AnalyzerBlock extends LogicBlock {
    private static final int modeItem = 0, modeLiquid = 1, modePowerBalance = 2, modePowerBattery = 3;

    public AnalyzerBlock(String name) {
        super(name);
        configurable = true;
        this.<Integer, AnaylzerBuild>config(Integer.class, (build, value) -> {
            build.analyzeMode = value;
        });
    }


    public class AnaylzerBuild extends LogicBuild {
        public int analyzeMode;

        @Override
        public void buildConfiguration(Table table) {
            Runnable[] rebuild = {null};
            rebuild[0] = () -> {
                table.clearChildren();
                ButtonGroup<Button> group = new ButtonGroup<>();

                Cons2<Integer, Drawable> toggler = (mode, tex) -> {
                    table.button(tex, Styles.clearTogglei, () -> {
//                        this.mode = ;
                        configure(AnalyzeMode.get(mode, 0));
                        rebuild[0].run();
                    }).group(group).size(40f).checked(AnalyzeMode.mode(this.analyzeMode) == mode);
                };

//                toggler.get(modeItem, Icon.itemSmall);
                toggler.get(modeItem, new TextureRegionDrawable(Items.copper.fullIcon));
                toggler.get(modeLiquid, Icon.liquidSmall);
                toggler.get(modePowerBalance, Icon.powerSmall);
                toggler.get(modePowerBattery, BLIcons.Drawables.batteryIcon32);
//                toggler.get(modePowerBattery, Icon.batterySmall);
                table.row();
                Table next = table.table().colspan(4).get();

                int mode = AnalyzeMode.mode(this.analyzeMode);
                if (mode == modeItem) {
                    ItemSelection.buildTable(next, Vars.content.items(), () -> Vars.content.item(AnalyzeMode.selection(this.analyzeMode)), item -> {
                        configure(AnalyzeMode.get(modeItem, item == null ? Short.MAX_VALUE : item.id));
                    });
                } else if (mode == modeLiquid) {
                    ItemSelection.buildTable(next, Vars.content.liquids(), () -> Vars.content.liquid(AnalyzeMode.selection(this.analyzeMode)), item -> {
                        configure(AnalyzeMode.get(modeLiquid, item == null ? Short.MAX_VALUE : item.id));
                    });
                }

                table.pack();
            };

            rebuild[0].run();
        }

        @Override
        public void drawSelect() {
            super.drawSelect();

            int mode = AnalyzeMode.mode(analyzeMode);
            int selection = AnalyzeMode.selection(analyzeMode);
            TextureRegion region;
            switch (mode) {
                case modeItem, modeLiquid -> {
                    @Nullable UnlockableContent content;
                    if (mode == modeItem) {
                        content = Vars.content.item(selection);
                    } else {
                        content = Vars.content.liquid(selection);
                    }
                    region = content == null ? Icon.none.getRegion() : content.fullIcon;
                }
                case modePowerBalance -> {
                    region = Icon.power.getRegion();
                }
                case modePowerBattery -> {
                    region = Core.atlas.find(ModVars.fullName("battery-icon-32"));
                }
                default -> throw new IllegalStateException("Unexpected value: " + mode);
            }
//            if(mode != modeItem && mode != modeLiquid) return;
            float dx = x - size * tilesize / 2f, dy = y + size * tilesize / 2f, s = iconSmall / 6f;
            float regionInvAspect = region.height / (float) region.width;
            Draw.mixcol(Color.darkGray, 1f);
            Draw.rect(region, dx, dy - 1, s, s * regionInvAspect);
            Draw.reset();
            Draw.rect(region, dx, dy, s, s * regionInvAspect);
        }

        //        @Override
        private int calculateNextSignal() {
            Building back = back();

            int mode = AnalyzeMode.mode(this.analyzeMode);
            int selection = AnalyzeMode.selection(this.analyzeMode);

            if (back == null) {
                return 0;
            } else if (mode == modePowerBalance && back.block().hasPower) {
                return Math.round(back.power.graph.getPowerBalance() * 60);
            } else if (mode == modePowerBattery && back.block().hasPower) {
                return Math.round(back.power.graph.getBatteryStored());
            } else if (mode == modeItem && back.block().hasItems) {
                Item item = Vars.content.item(selection);
                return item == null ? back.items.total() : back.items.get(item);
            } else if (mode == modeLiquid && back.block().hasLiquids) {
                Liquid liquid = Vars.content.liquid(selection);
                return liquid == null ? (int) back.liquids.currentAmount() : (int) back.liquids.get(liquid);
            }

            return 0;
        }

        @Override
        public void updateSignalState() {

            nextSignal = calculateNextSignal();
            super.updateSignalState();
        }

        @Override
        public void beforeUpdateSignalState() {
            if (doOutput && output(rotation)) {
                front().<LogicBuild>as().acceptSignal(this, lastSignal);
            }
        }

        @Override
        public Integer config() {
            return analyzeMode;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(analyzeMode);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            analyzeMode = read.i();
        }

    }

    @Struct
    class AnalyzeModeStruct {
        /**
         * mode of analysis, e.g. power, liquid, item
         */
        @StructField(16)
        int mode;
        /**
         * mode-specific selection, e.g. liquid/item id or scan mode
         */
        @StructField(16)
        int selection;
    }
}
