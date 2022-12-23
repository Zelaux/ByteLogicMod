package bytelogic.content;

import arc.struct.*;
import arc.util.*;
import bytelogic.type.*;
import bytelogic.world.blocks.*;
import bytelogic.world.blocks.logic.*;
import bytelogic.world.blocks.sandbox.*;
import mindustry.content.*;
import mindustry.io.*;
import mindustry.type.*;
import mindustry.world.*;
import mma.*;

import java.lang.reflect.*;

import static bytelogic.BLVars.fullName;
import static mindustry.Vars.tilesize;


public class ByteLogicBlocks{
    public static final Seq<ByteLogicBlocks> byteLogicBlocks = new Seq<>();
    public static ByteLogicBlocks erekirBlocks, serpuloBlock;
    public final Seq<LogicBlock> blocks = new Seq<>();
    public LogicBlock
        signalTimer,
        switchBlock, signalBlock, signalNode, signalRouter, analyzer, controller, relay,
        notGate, andGate, orGate, xorGate,
        adder, subtractor, divider, remainder, multiplier, equalizer, comparator,

    transformer,
        fontSignal, displayBlock,

    processor;
    public Planet planet;


    public ByteLogicBlocks(Planet planet, String namePrefix, Category blockCategory, ItemStack[] requirements, ItemStack[] displayRequirements){
        this.planet = planet;
        ItemStack[] bothRequirements = requirements;
        relay = new RelayBlock(namePrefix + "relay"){{
            requirements(blockCategory, bothRequirements.clone());
        }};

        if (planet==Planets.erekir){
            processor = new ByteLogicProcessor("processor"){{
                requirements(blockCategory, bothRequirements.clone());
                baseName = "processor-base";
            }};
        }
        signalTimer = new SignalTimer(namePrefix + "signal-timer"){{
            requirements(blockCategory, bothRequirements.clone());
        }};
        switchBlock = new SwitchBlock(namePrefix + "signal-switch-block"){{
            requirements(blockCategory, bothRequirements.clone());
        }};

        signalBlock = new SignalBlock(namePrefix + "signal-block"){{
            requirements(blockCategory, bothRequirements.clone());
        }};

        signalRouter = new LogicRouter(namePrefix + "signal-router"){{
            requirements(blockCategory, bothRequirements.clone());
            doOutput = true;
            rotate = false;
        }};

        signalNode = new NodeLogicBlock(namePrefix + "signal-node"){{
            requirements(blockCategory, bothRequirements.clone());
            range = 13.75f * tilesize;
        }};

        analyzer = new AnalyzerBlock(namePrefix + "analyzer"){{
            requirements(blockCategory, bothRequirements.clone());
        }};

        controller = new ControllerBlock(namePrefix + "controller"){{
            requirements(blockCategory, bothRequirements.clone());
        }};


        notGate = new UnaryLogicBlock(namePrefix + "not-gate"){{
            requirements(blockCategory, bothRequirements.clone());

            processor = input -> {
                Signal.valueOf(input, input.compareWithZero() == 0 ? 1 : 0);
                return input;
            };
        }};

        andGate = new BinaryLogicBlock(namePrefix + "and-gate"){{
            requirements(blockCategory, bothRequirements.clone());
            this.outputRegionName = ModVars.fullName("boolean-gate-output");
            this.sideOutputRegionName = ModVars.fullName("boolean-gate-output-side");
            needImageCompilation = true;
            ownsCenterRegion = false;

            processor = (left, right) -> {
                left.and(right);
                return left;
            };
            operatorName = "and";
        }};

        orGate = new BinaryLogicBlock(namePrefix + "or-gate"){{
            requirements(blockCategory, bothRequirements.clone());
            this.outputRegionName = ModVars.fullName("boolean-gate-output");
            this.sideOutputRegionName = ModVars.fullName("boolean-gate-output-side");
            ;
            needImageCompilation = true;
            ownsCenterRegion = false;

            processor = (left, right) -> {
                left.or(right);
                return left;
            };
            operatorName = "or";
        }};

        xorGate = new BinaryLogicBlock(namePrefix + "xor-gate"){{
            requirements(blockCategory, bothRequirements.clone());
            this.outputRegionName = ModVars.fullName("boolean-gate-output");
            this.sideOutputRegionName = fullName("xor-gate-output-side");
            ;
            needImageCompilation = true;
            ownsCenterRegion = false;

            processor = (left, right) -> {
                left.xor(right);
                return left;
            };
            operatorName = "xor";
        }};

        adder = new BinaryLogicBlock(namePrefix + "adder"){{
            requirements(blockCategory, bothRequirements.clone());

            processor = (left, right) -> {
                left.plus(right);
                return left;
            };
            operatorName = "+";
        }};

        subtractor = new BinaryLogicBlock(namePrefix + "subtractor"){{
            requirements(blockCategory, bothRequirements.clone());
            canFlip = true;
            operatorName = "-";
            processor = (left, right) -> {
                left.minus(right);
                return left;
            };
        }};

        divider = new BinaryLogicBlock(namePrefix + "divider"){{
            requirements(blockCategory, bothRequirements.clone());

            canFlip = true;
            operatorName = "/";
            processor = (left, right) -> {
                if(right.compareWithZero() == 0) return right;
                left.div(right);
                return left;
            };
        }};

        remainder = new BinaryLogicBlock(namePrefix + "remainder"){{
            requirements(blockCategory, bothRequirements.clone());
            canFlip = true;
            operatorName = "%";
            processor = (left, right) -> {
                if(right.compareWithZero() == 0) return right;
                left.mod(right);
                return left;
            };
        }};

        multiplier = new BinaryLogicBlock(namePrefix + "multiplier"){{
            requirements(blockCategory, bothRequirements.clone());

            operatorName = "*";
            processor = (left, right) -> {
                left.times(right);
                return left;
            };
        }};

        equalizer = new BinaryLogicBlock(namePrefix + "equalizer"){{
            requirements(blockCategory, bothRequirements.clone());

            operatorName = "==";
            processor = (left, right) -> {
                Signal.valueOf(left, left.equals(right) ? 1 : 0);
                return left;
            };
        }};

        comparator = new BinaryLogicBlock(namePrefix + "comparator"){{
            requirements(blockCategory, bothRequirements.clone());
            canFlip = true;
            operatorName = " > ";
            processor = (left, right) -> {
                Signal.valueOf(left, left.compareTo(right) > 0 ? 1 : 0);
                return left;
            };
        }};

        displayBlock = new DisplayBlock(namePrefix + "display"){{
            requirements(blockCategory, displayRequirements.clone());
            size = 2;
        }};

        transformer = new SignalTransformer(namePrefix + "signal-transformer"){{
            SaveVersion.modContentNameMap.put(fullName(namePrefix + "transformer"), name);
            requirements(blockCategory, displayRequirements.clone());
            size = 1;
        }};
        fontSignal = new FontSignalBlock(namePrefix + "font-signal"){{
            requirements(blockCategory, bothRequirements.clone());
        }};
        initFields();
        byteLogicBlocks.add(this);
    }

    public static void load(){
        new PlaceholderBlock("input-placeholder");
        SaveVersion.fallback.put(fullName("switch-block"), fullName("signal-switch-block"));
        //region logic
        if(ModVars.packSprites){
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
        for(int i = 0; i < serpuloBlock.blocks.size; i++){
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

    private void initFields(){
        blocks.clear();
        for(Field field : ByteLogicBlocks.class.getFields()){
            if(field.getType() == LogicBlock.class){
                LogicBlock block = Reflect.<LogicBlock>get(this, field);
                if(block == null) continue;
                block.byteLogicBlocks = this;
                blocks.add(block);
            }
        }
    }
}
