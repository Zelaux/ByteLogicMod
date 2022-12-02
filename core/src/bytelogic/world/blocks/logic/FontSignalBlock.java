package bytelogic.world.blocks.logic;


import arc.struct.*;
import arc.util.*;
import bytelogic.type.*;
import bytelogic.world.meta.*;
import mindustry.gen.*;

import java.math.*;

public class FontSignalBlock extends UnaryLogicBlock{
    protected IntMap<Signal> font = IntMap.of(
        65, signal(33095217), 66, signal(16317999), 67, signal(32539711), 68, signal(16303663), 69, signal(32554047), 70, signal(32554017),
        71, signal(32568895), 72, signal(18415153), 73, signal(32641183), 74, signal(15863975), 75, signal(9604265), 76, signal(1082415),
        77, signal(18732593), 78, signal(18470705), 79, signal(33080895), 80, signal(33094689), 81, signal(33081151), 82, signal(33094961),
        83, signal(32570911), 84, signal(32641156), 85, signal(18400831), 86, signal(18393412), 87, signal(18405233), 88, signal(18157905),
        89, signal(18157700), 90, signal(32772191), 48, signal(15255086), 49, signal(6426756), 50, signal(13181086), 51, signal(32010782),
        52, signal(19495440), 53, signal(29456926), 54, signal(31554142), 55, signal(31989890), 56, signal(32059998), 57, signal(32078366)
    );
    protected Signal defaultValue = signal(0);

    public FontSignalBlock(String name){
        super(name);
        processor = in -> font.get((char)in.intNumber(), defaultValue);

    }

    private Signal signal(int value){
        return Signal.valueOf(value);
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(BLStat.keyCodes, root_table -> {
            root_table.row();
            root_table.table(Tex.pane, table -> {
                IntSeq seq = font.keys().toArray();
                seq.sort();
                for(int i = 0; i < seq.size; i++){
                    int item = seq.items[i];
                    table.add(item + "").labelAlign(Align.center).width(32f);
                    table.add("->").labelAlign(Align.center).width(48f);
                    table.add(((char)item) + "").labelAlign(Align.center).width(32f);
                    table.row();
                }
            });
        });
    }

    public class FontSignalBuild extends UnaryLogicBuild{

    }
}
