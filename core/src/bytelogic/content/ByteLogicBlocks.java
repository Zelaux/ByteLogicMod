package bytelogic.content;

import arc.struct.*;
import arc.util.*;
import bytelogic.world.blocks.logic.*;
import mindustry.content.*;
import mindustry.io.*;
import mindustry.type.*;
import mindustry.world.*;
import mma.*;

import java.lang.reflect.*;

import static bytelogic.BLVars.fullName;
import static mindustry.Vars.tilesize;


public class ByteLogicBlocks {
    public static final Seq<ByteLogicBlocks> byteLogicBlocks = new Seq<>();
    public static ByteLogicBlocks erekirBlocks, serpuloBlock;
    public final Seq<LogicBlock> blocks = new Seq<>();
    public LogicBlock
            signalTimer,
            switchBlock, signalBlock, signalNode, signalRouter, analyzer, controller, relay,
            notGate, andGate, orGate, xorGate,
            adder, subtractor, divider, remainder, multiplier, equalizer, comparator,
            fontSignal, displayBlock;
    public Planet planet;


    public ByteLogicBlocks(Planet planet, String namePrefix, Category blockCategory, ItemStack[] requirements, ItemStack[] displayRequirements) {
        this.planet = planet;
        ItemStack[] bothRequirements = requirements;
        relay = new RelayBlock(namePrefix + "relay") {{
            requirements(blockCategory, bothRequirements.clone());
        }};
        signalTimer = new SignalTimer(namePrefix + "signal-timer") {{
            requirements(blockCategory, bothRequirements.clone());
        }};
        switchBlock = new SwitchBlock(namePrefix + "signal-switch-block") {{
            requirements(blockCategory, bothRequirements.clone());
        }};

        signalBlock = new SignalBlock(namePrefix + "signal-block") {{
            requirements(blockCategory, bothRequirements.clone());
        }};

        signalRouter = new LogicRouter(namePrefix + "signal-router") {{
            requirements(blockCategory, bothRequirements.clone());
            doOutput = true;
            rotate = false;
        }};

        signalNode = new NodeLogicBlock(namePrefix + "signal-node") {{
            requirements(blockCategory, bothRequirements.clone());
            range = 13.75f * tilesize;
        }};

        analyzer = new AnalyzerBlock(namePrefix + "analyzer") {{
            requirements(blockCategory, bothRequirements.clone());
        }};

        controller = new ControllerBlock(namePrefix + "controller") {{
            requirements(blockCategory, bothRequirements.clone());
        }};


        notGate = new UnaryLogicBlock(namePrefix + "not-gate") {{
            requirements(blockCategory, bothRequirements.clone());

            processor = input -> input != 0 ? 0 : 1;
        }};

        andGate = new BinaryLogicBlock(namePrefix + "and-gate") {{
            requirements(blockCategory, bothRequirements.clone());
            this.outputRegionName = ModVars.fullName("boolean-gate-output");
            this.sideOutputRegionName = ModVars.fullName("boolean-gate-output-side");
            needImageCompilation =true;
            ownsCenterRegion=false;

            processor = (left, right) -> left & right;
            operatorName = "and";
        }};

        orGate = new BinaryLogicBlock(namePrefix + "or-gate") {{
            requirements(blockCategory, bothRequirements.clone());
            this.outputRegionName = ModVars.fullName("boolean-gate-output");
            this.sideOutputRegionName = ModVars.fullName("boolean-gate-output-side");;
            needImageCompilation =true;
            ownsCenterRegion=false;

            processor = (left, right) -> left | right;
            operatorName = "or";
        }};

        xorGate = new BinaryLogicBlock(namePrefix + "xor-gate") {{
            requirements(blockCategory, bothRequirements.clone());
            this.outputRegionName = ModVars.fullName("boolean-gate-output");
            this.sideOutputRegionName =  fullName("xor-gate-output-side");;
            needImageCompilation =true;
            ownsCenterRegion=false;

            processor = (left, right) -> left ^ right;
            operatorName = "xor";
        }};

        adder = new BinaryLogicBlock(namePrefix + "adder") {{
            requirements(blockCategory, bothRequirements.clone());

            processor = (left, right) -> {
                long result = (long) left + right;
                return (int) Math.min(result, Integer.MAX_VALUE);
            };
            operatorName = "+";
        }};

        subtractor = new BinaryLogicBlock(namePrefix + "subtractor") {{
            requirements(blockCategory, bothRequirements.clone());
            canFlip = true;
            operatorName = "-";
            processor = (left, right) -> Math.max(left - right, 0);
        }};

        divider = new BinaryLogicBlock(namePrefix + "divider") {{
            requirements(blockCategory, bothRequirements.clone());

            canFlip = true;
            operatorName = "/";
            processor = (left, right) -> right == 0 ? 0 : left / right;
        }};

        remainder = new BinaryLogicBlock(namePrefix + "remainder") {{
            requirements(blockCategory, bothRequirements.clone());
            canFlip = true;
            operatorName = "%";
            processor = (left, right) -> right == 0 ? 0 : left % right;
        }};

        multiplier = new BinaryLogicBlock(namePrefix + "multiplier") {{
            requirements(blockCategory, bothRequirements.clone());

            operatorName = "*";
            processor = (left, right) -> left * right;
        }};

        equalizer = new BinaryLogicBlock(namePrefix + "equalizer") {{
            requirements(blockCategory, bothRequirements.clone());

            operatorName = "==";
            processor = (left, right) -> left == right ? 1 : 0;
        }};

        comparator = new BinaryLogicBlock(namePrefix + "comparator") {{
            requirements(blockCategory, bothRequirements.clone());
            canFlip = true;
            operatorName = " > ";
            processor = (left, right) -> left > right ? 1 : 0;
        }};

        displayBlock = new DisplayBlock(namePrefix + "display") {{
            requirements(blockCategory, displayRequirements.clone());
            size = 2;
        }};
        fontSignal = new FontSignalBlock(namePrefix + "font-signal") {{
            requirements(blockCategory, bothRequirements.clone());
        }};
        for (Field field : ByteLogicBlocks.class.getFields()) {
            if (field.getType() == LogicBlock.class) {
                LogicBlock block = Reflect.<LogicBlock>get(this, field);
                block.byteLogicBlocks = this;
                blocks.add(block);
            }
        }
        byteLogicBlocks.add(this);
    }

    public static void load() {
        SaveVersion.fallback.put(fullName("switch-block"), fullName("signal-switch-block"));
        //region logic
        if (ModVars.packSprites) {
            new Block("air-block");
        }
        erekirBlocks = new ByteLogicBlocks(Planets.erekir, "",
                Category.logic,
                ItemStack.with(Items.beryllium, 2, Items.silicon, 1), //simple requirements
                ItemStack.with(Items.beryllium, 8, Items.silicon, 4/*, Items.metaglass, 4*/)); //display requirements

        serpuloBlock = new ByteLogicBlocks(Planets.serpulo, "serpulo-",
                Category.logic,
                ItemStack.with(Items.copper, 2, Items.lead, 1),//simple requirements
                ItemStack.with(Items.copper, 8, Items.lead, 4, Items.metaglass, 4)); //display requirements
        for (int i = 0; i < serpuloBlock.blocks.size; i++) {
            LogicBlock serpulo = serpuloBlock.blocks.get(i);
            LogicBlock erekir = erekirBlocks.blocks.get(i);
            serpulo.baseName = "serpulo-logic-base";
            serpulo.originalMirror = erekir;
            serpulo.localizedName = erekir.localizedName;
            serpulo.description = erekir.description;
            serpulo.details = erekir.details;
        }
        //endregion
    }
}
