package bytelogic.world.blocks.logic;


import arc.graphics.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import bytelogic.gen.*;
import bytelogic.type.*;
import bytelogic.ui.dialogs.*;
import mindustry.gen.*;

import static mindustry.Vars.*;

public class SignalBlock extends LogicBlock{

    public SignalBlock(String name){
        super(name);
        configurable = true;
      /*  this.<Long, SignalLogicBuild>config(Long.class, (build, value) -> {
            Signal.valueOf(build.nextSignal, value);
        });*/
        this.<byte[], SignalLogicBuild>config(byte[].class, (build, bytes) -> {

            build.nextSignal.fromBytes(bytes);
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

        public void configureNumber(long number){
            Signal.valueOf(nextSignal, number);
            configure(nextSignal.asBytes());
        }

        @Override
        public void buildConfiguration(Table table){

            table.button(Icon.pencilSmall, () -> {
                ui.showTextInput("@block.editsignal", "", 10, nextSignal + "", true, result -> {
                    configureNumber(Strings.parseLong(result, 0));
                });
                control.input.config.hideConfig();
            }).size(40f);
            table.button(Icon.imageSmall, () -> {
                new CanvasEditDialog(this).show();
            }).size(40f);

            table.button(Icon.pick, () -> {
                Color tmpColor = new Color();
                tmpColor.set(nextSignal.intNumber());
                ui.picker.show(tmpColor, true, out -> {
                    nextSignal.setNumber(out.rgba());
                    nextSignal.type = SignalTypes.colorType;
                    configure(nextSignal.asBytes());
                });
            }).size(40f);
        }

        private long absoluteNumber(){
            long signal = nextSignal.number();
            signal &= ~0b100_0000_0000_0000_0000_0000_0000_0000;
            if(signal < 0) signal = -signal;
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
        public byte[] config(){
            return nextSignal.asBytes();
        }
    }
}
