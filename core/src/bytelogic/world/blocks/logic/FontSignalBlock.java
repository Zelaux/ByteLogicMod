package bytelogic.world.blocks.logic;


import arc.struct.*;
import arc.util.*;
import bytelogic.type.*;
import bytelogic.world.meta.*;
import mindustry.gen.*;

public class FontSignalBlock extends UnaryLogicBlock{
    protected IntMap<Signal> font = IntMap.of(
        //region letters
        65, signal(4521364556104772L),//A
        66, signal(305158613876622460L),//B
        67, signal(16892913896391740L),//C
        68, signal(2027820792217347100L),//D
        69, signal(4324586197994439740L),//E
        70, signal(8936272078982874116L),//F
        71, signal(4324585958283682876L),//G
        72, signal(4919131753928737860L),//H
        73, signal(2019873263463172124L),//I
        74, signal(7890129935403534L),//J
        75, signal(2599724927008646180L),//K
        76, signal(289360691352306716L),//L
        77, signal(4930408344243684420L),//M
        78, signal(4921392417885938756L),//N
        79, signal(15837658689586232L),//O
        80, signal(2027785434968622084L),//P
        81, signal(15837658689586360L),//Q
        82, signal(2027785435239162916L),//R
        83, signal(4036355581661945884L),//S
        84, signal(8939662921505443856L),//T
        85, signal(4919131752989213752L),//U
        86, signal(4774451406807770136L),//V
        87, signal(-9042521536814885848L),//W
        88, signal(2459565815927939618L),//X
        89, signal(4919131631854293008L),//Y
        90, signal(9097341753874449022L),//Z
        //endregion
        //region digits
        48, signal(4333628479081030716L),//0
        49, signal(2319397943718715424L),//1
        50, signal(4054436070992381052L),//2
        51, signal(4341540651125456956L),//3
        52, signal(2604246325182603296L),//4
        53, signal(8936272492310316088L),//5
        54, signal(4324585958417900604L),//6
        55, signal(8953226703640989712L),//7
        56, signal(8953226703640989712L),//8
        57, signal(4333628582092873788L)//9
        //endregion
    );
    protected Signal defaultValue = signal(0);

    public FontSignalBlock(String name){
        super(name);
        processor = in -> font.get((char)in.intNumber(), defaultValue);

    }

    private Signal signal(long value){
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
