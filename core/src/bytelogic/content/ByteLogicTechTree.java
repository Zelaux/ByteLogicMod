package bytelogic.content;

//import mindustry.content.*;

import arc.struct.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.game.*;
import mma.utils.*;

import static mindustry.content.TechTree.node;

public class ByteLogicTechTree{

    public static void load(){
        initTechTree(Blocks.siliconArcFurnace,ByteLogicBlocks.erekirBlocks);
        initTechTree(Blocks.mechanicalDrill,ByteLogicBlocks.serpuloBlock);
    }
    public static void initTechTree(UnlockableContent context, ByteLogicBlocks blocks){
        TechTreeContext.contextNode(context,()->{
            node(blocks.relay, () -> {
                node(blocks.signalTimer);
                node(blocks.switchBlock, () -> {
                    node(blocks.signalBlock, () -> {
                        node(blocks.displayBlock, () -> {
                            node(blocks.fontSignal);
                        });
                        node(blocks.analyzer, () -> {
                            node(blocks.controller);
                        });
                    });
                });
                node(blocks.signalRouter, () -> {
                    node(blocks.signalNode);
                });
                node(blocks.comparator, () -> {
                    node(blocks.equalizer);
                    node(blocks.xorGate, Seq.with(new Objectives.Research(blocks.orGate),
                    new Objectives.Research(blocks.andGate),
                    new Objectives.Research(blocks.notGate)), () -> {
                    });
                });
                node(blocks.notGate);
                node(blocks.andGate, () -> {
                    node(blocks.multiplier, () -> {
                        node(blocks.divider, () -> {
                            node(blocks.remainder, () -> {
                            });
                        });
                    });
                });
                node(blocks.orGate, () -> {
                    node(blocks.adder, () -> {
                        node(blocks.subtractor, () -> {
                        });
                    });
                });

            });
        });
    }
}
