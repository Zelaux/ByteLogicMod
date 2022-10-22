package bytelogic.world.blocks.logic;


import arc.struct.*;
import arc.util.*;
import bytelogic.world.meta.*;
import mindustry.gen.*;

public class FontSignalBlock extends UnaryLogicBlock{
    protected IntIntMap font = IntIntMap.of(
    65, 33095217, 66, 16317999, 67, 32539711, 68, 16303663, 69, 32554047, 70, 32554017,
    71, 32568895, 72, 18415153, 73, 32641183, 74, 15863975, 75, 9604265, 76, 1082415,
    77, 18732593, 78, 18470705, 79, 33080895, 80, 33094689, 81, 33081151, 82, 33094961,
    83, 32570911, 84, 32641156, 85, 18400831, 86, 18393412, 87, 18405233, 88, 18157905,
    89, 18157700, 90, 32772191, 48, 15255086, 49, 6426756, 50, 13181086, 51, 32010782,
    52, 19495440, 53, 29456926, 54, 31554142, 55, 31989890, 56, 32059998, 57, 32078366
    );

    public FontSignalBlock(String name){
        super(name);
        processor = in -> font.get(Character.toUpperCase((char)in), 0);

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
