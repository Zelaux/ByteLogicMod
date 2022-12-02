package bytelogic.world.blocks.logic;


import arc.graphics.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import bytelogic.gen.*;
import bytelogic.type.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import static mindustry.Vars.*;

public class SignalBlock extends LogicBlock{

    public SignalBlock(String name){
        super(name);
        configurable = true;
        this.<Long, SignalLogicBuild>config(Long.class, (build, value) -> {
            Signal.valueOf(build.nextSignal, value);
        });
    }

    public class SignalLogicBuild extends LogicBuild{

        @Override
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, Signal signal){
            return false;
        }

        @Override
        public Signal currentSignal(){
            return nextSignal;
        }

        @Override
        public void buildConfiguration(Table table){

            table.button(Icon.pencilSmall, () -> {
                ui.showTextInput("@block.editsignal", "", 10, nextSignal + "", true, result -> {
                    configure(Strings.parseLong(result, 0));
                });
                control.input.config.hideConfig();
            }).size(40f);
            table.button(Icon.imageSmall, () -> {
                var disabledColor = Color.grays(0.2f);
                new BaseDialog("@block.editsignal.as-image"){{
                    cont.table(minus -> {
                        minus.bottom();
                        minus.table(Tex.pane, minusTable -> {
                            minusTable.bottom();
                            minusTable.button(button -> {
                                    button.label(() -> {
                                        return  nextSignal.number() < 0 ? "-" : "+";
                                    });
                                    button.setStyle(Styles.flatt);
                                }, () -> configure(-nextSignal.number())
                            ).size(32f);
                        }).size(48f).bottom();
                    }).fillY();
                    cont.table(Tex.pane, table -> {
                        table.defaults().pad(3);

                        for(int dy = 0; dy < 5; dy++){
                            for(int dx = 0; dx < 5; dx++){
                                int pow = (4 - dy) * 5 + dx;
                                int mask = 1 << pow;
                                table.image().update(i -> {
                                    if((absoluteNumber() & mask) != 0){
                                        i.color.set(Color.white);
                                    }else{
                                        i.color.set(disabledColor);
                                    }
                                }).size(64f).with(i -> {
                                    i.clicked(() -> {
                                        long value = absoluteNumber() & mask;
                                        configure(absoluteNumber() + Mathf.sign(value == 0) * mask);
                                    });
                                });
                            }
                            table.row();
                        }
                    });
//                    getCell(cont).expand(false, false);
                    addCloseButton();
                    this.hidden(control.input.config::hideConfig);
                }}.show();
                /*ui.showTextInput("$block.editsignal", "", 10, nextSignal + "", true, result -> {
                    nextSignal = Strings.parseInt(result, 0);
                });*/
            }).size(40f);
        }

        private long absoluteNumber(){
            long signal = nextSignal.number();
            signal &= ~0b100_0000_0000_0000_0000_0000_0000_0000;
            if (signal < 0) signal = -signal;
            return signal;
        }

        @Override
        public void beforeUpdateSignalState(){
            if(doOutput && canOutputSignal(rotation)){
                front().<ByteLogicBuildingc>as().acceptSignal(this, lastSignal);
            }
        }

        @Override
        public void updateSignalState(){
            lastSignal.set(nextSignal);
        }
        /*
        @Override
        public int signal(){
            return nextSignal;
        }*/

        @Override
        public Long config(){
            return nextSignal.number();
        }
    }
}
