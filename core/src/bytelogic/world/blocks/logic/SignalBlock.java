package bytelogic.world.blocks.logic;


import arc.graphics.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import bytelogic.gen.*;
import mindustry.gen.*;
import mindustry.ui.dialogs.*;

import static mindustry.Vars.*;

public class SignalBlock extends LogicBlock{

    public SignalBlock(String name){
        super(name);
        configurable = true;
        this.<Integer, SignalLogicBuild>config(Integer.class, (build, value) -> {
            build.nextSignal = value;
        });
    }

    public class SignalLogicBuild extends LogicBuild{

        @Override
        public boolean acceptSignal(ByteLogicBuildingc otherBuilding, int signal){
            return  false;
        }
        @Override
        public int currentSignal(){
            return nextSignal;
        }

        @Override
        public void buildConfiguration(Table table){

            table.button(Icon.pencilSmall, () -> {
                ui.showTextInput("@block.editsignal", "", 10, nextSignal + "", true, result -> {
                    nextSignal = Strings.parseInt(result, 0);
                });
                control.input.config.hideConfig();
            }).size(40f);
            table.button(Icon.imageSmall, () -> {
                var disabledColor = Color.grays(0.2f);
                new BaseDialog("@block.editsignal.as-image"){{
                    cont.table(Tex.pane, table -> {
                        table.defaults().pad(3);

                        for(int dy = 0; dy < 5; dy++){
                            for(int dx = 0; dx < 5; dx++){
                                int pow = (4 - dy) * 5 + dx;
                                int mask = 1 << pow;
                                table.image().update(i -> {
                                    if((nextSignal & mask) != 0){
                                        i.color.set(Color.white);
                                    }else{
                                        i.color.set(disabledColor);
                                    }
                                }).size(64f).with(i -> {
                                    i.clicked(() -> {
                                        int value = nextSignal & mask;
                                        configure(nextSignal + Mathf.sign(value == 0) * mask);
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

        @Override
        public void beforeUpdateSignalState(){
            if(doOutput && canOutputSignal(rotation)){
                front().<ByteLogicBuildingc>as().acceptSignal(this, lastSignal);
            }
        }

        @Override
        public void updateSignalState(){
            lastSignal = nextSignal;
        }
        /*
        @Override
        public int signal(){
            return nextSignal;
        }*/

        @Override
        public Integer config(){
            return nextSignal;
        }
    }
}
