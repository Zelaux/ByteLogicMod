package bytelogic.world.blocks.logic;

import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.io.*;
import bytelogic.gen.*;
import bytelogic.type.*;
import mindustry.ui.*;

public class SignalTransformer extends UnaryLogicBlock{
    protected static final Signal tmpSignal = new Signal();

    public SignalTransformer(String name){
        super(name);
        /*this.<byte[], SignalTransformerBuild>config(byte[].class, (build, bytes) -> {
            build.selectedTypeSignal.fromBytes(bytes);
        });*/
        this.<Integer, SignalTransformerBuild>config(Integer.class, (build, id) -> {
            SignalType type = SignalType.all[id];
            if(type == SignalTypes.nilType)
                type = SignalTypes.numberType;
            build.selectedType = type;
        });
        this.<String, SignalTransformerBuild>config(String.class, (build, typeName) -> {
            SignalType type = SignalType.findByName(typeName);
            if(type == SignalTypes.nilType)
                type = SignalTypes.numberType;
            build.selectedType = type;
        });
        processor = it -> it;
    }

    public class SignalTransformerBuild extends UnaryLogicBuild{
        SignalType selectedType = SignalTypes.numberType;

        @Override
        public void buildConfiguration(Table table){
            table.table(t -> {
                int i = 0;
                ButtonGroup<Button> group = new ButtonGroup<>();
                for(SignalType type : SignalType.all){
                    if(type == SignalTypes.nilType) continue;
                    t.button(type.getIcon(), Styles.squareTogglei, () -> {
                        configure(type.getId());
                    }).size(48f).checked(selectedType == type).group(group);
                    i++;
                    if(i % 4 == 0){
                        t.row();
                    }
                }
            });
            table.row();
            super.buildConfiguration(table);
        }

        @Override
        public void beforeUpdateSignalState(){
            if(doOutput && canOutputSignal((byte)rotation)){
                lastSignal.type = selectedType;
                front().<ByteLogicBuildingc>as().acceptSignal(this, lastSignal);
            }
        }

        @Override
        public void updateSignalState(){
            lastSignal.set(nextSignal);
            lastSignal.type = selectedType;
            nextSignal.setZero();

        }

        @Override
        public Object config(){
            return selectedType.getName();
        }

        @Override
        public void customWrite(Writes write){
            write.str(selectedType.getName());
        }

        @Override
        public void customRead(Reads read){
            selectedType = SignalType.findByName(read.str());
            if(selectedType == SignalTypes.nilType){
                selectedType = SignalTypes.numberType;
            }
        }

        @Override
        public short customVersion(){
            return 1;
        }
    }
}
