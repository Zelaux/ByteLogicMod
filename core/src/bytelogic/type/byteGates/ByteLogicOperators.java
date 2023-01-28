package bytelogic.type.byteGates;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.TextField.*;
import arc.scene.ui.Tooltip.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import arc.util.serialization.*;
import arc.util.serialization.Json.*;
import bytelogic.*;
import bytelogic.annotations.BLAnnotations.*;
import bytelogic.gen.*;
import bytelogic.type.*;
import bytelogic.ui.dialogs.*;
import bytelogic.world.blocks.ByteLogicProcessor.*;
import bytelogic.world.blocks.logic.*;
import mindustry.gen.*;
import mindustry.io.*;
import mma.ui.tiledStructures.*;
import mma.ui.tiledStructures.TiledStructures.*;
import mma.ui.tiledStructures.TiledStructuresDialog.*;
import mma.utils.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.*;
import zelaux.arclib.ui.components.*;
import zelaux.arclib.ui.components.ComboBox.*;
import zelaux.arclib.ui.tooltips.*;

import java.lang.annotation.*;
import java.lang.reflect.*;

import static mindustry.Vars.*;

public class ByteLogicOperators{
    static Prov<? extends TiledStructure>[] providers;

    static{

        if(!BLVars.packSprites){
            Seq<Json> jsons = Seq.with(JsonIO.json, Reflect.get(JsonIO.class, "jsonBase"));
            SignalTypes.nilType.getId();
            Serializer signalTypeSerializer = new Serializer<SignalType>(){
                @Override
                public void write(Json json, SignalType object, Class knownType){
                    json.writeValue(object.getName());
                }

                @Override
                public SignalType read(Json json, JsonValue jsonData, Class type){
                    return SignalType.findByName(jsonData.asString());
                }
            };
            for(Json json : jsons){
                if(json.getSerializer(SignalType.class) == null){
                    json.setSerializer(SignalType.class, signalTypeSerializer);
                }
                for(SignalType type : SignalType.all){

                    Class<? extends SignalType> aClass = type.getClass();
                    if(aClass.isAnonymousClass()) aClass = (Class<? extends SignalType>)aClass.getSuperclass();
                    if(json.getSerializer(aClass) == null){
                        json.setSerializer(aClass, signalTypeSerializer);
                    }
                }
            }


            Seq<Class<?>> seq = new Seq<>();
            collectClasses(ByteLogicOperators.class, seq, new ObjectSet<>());
            providers = seq.map(Reflect::cons).reverse().toArray(Prov.class);

            String schemTag = Reflect.cons(SchematicGate.class).get().name();
            JsonIO.classTag(schemTag, SchematicGate.class);
            JsonIO.classTag(Strings.camelize(schemTag), SchematicGate.class);
            for(Class<?> type : seq){
                JsonIO.classTag(type.getName(), type);
                TiledStructuresDialog.setGlobalInterpreter(type, TiledStructuresDialog.defaultInterpreter());
            }

            TiledStructuresDialog.setGlobalProvider(long.class, (type, cons) -> cons.get(0L));
            TiledStructuresDialog.setGlobalInterpreter(long.class, (instance, cont, name, type, field, remover, indexer, get, set) -> {
                TiledStructuresDialog.name(cont, name, remover, indexer);
                cont.field(Long.toString(get.get()), str -> set.get(Strings.parseLong(str, 0L)))
                    .growX().fillY()
                    .valid(ModStrings::canParseLong)
                    .get().setFilter(TextFieldFilter.digitsOnly);
            });
        }
    }

    public static Seq<Prov<? extends TiledStructure>> getProvidersAsSequence(){
        return Seq.with(providers);
    }

    public static <T extends ByteLogicGate> void addProvider(Class<T> type, Prov<T> prov){
        Prov<? extends TiledStructure>[] prevProviders = providers;
        providers = new Prov[prevProviders.length + 1];
        System.arraycopy(prevProviders, 0, providers, 0, prevProviders.length);
        providers[prevProviders.length] = prov;

        TiledStructuresDialog.setGlobalInterpreter(type, TiledStructuresDialog.defaultInterpreter());
    }

    public static void registerAll(TiledStructures structures){
        structures.registerStructures(
            providers
        );
    }

    private static void collectClasses(Class<?> clazz, Seq<Class<?>> seq, ObjectSet<Class<?>> set){
        if(!Modifier.isAbstract(clazz.getModifiers()) && ByteLogicGate.class.isAssignableFrom(clazz)){
            seq.add(clazz);
        }
        for(Class<?> aClass : clazz.getDeclaredClasses()){
            if(ByteLogicGate.class.isAssignableFrom(aClass) && set.add(aClass)){
                collectClasses(aClass, seq, set);
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface ShortName{
        String value();
    }

    @Serializable(prefix = "BL", canCreateInstance = false, type = TiledStructure.class)

    @GenerateByteLogicGatesSerializer
    public static abstract class ByteLogicGate extends TiledStructure<ByteLogicGate> implements TiledStructureWithGroup{
        protected transient static final Cons2<TiledStructuresDialog, Table> unsetEditor = (a, b) -> {
        };
        public transient final Class<? extends ByteLogicGate> clazz = getClass().isAnonymousClass() ? (Class<? extends ByteLogicGate>)getClass().getSuperclass() : getClass();
        protected transient final Signal tmpSignal = new Signal();
        @CodeEdit

        @FinalField
        public Signal[] signals;
        @CodeEdit
        @FinalField
        public Signal[] inputSignals;
        @CodeEdit
        public boolean step;
        protected transient Cons2<TiledStructuresDialog, Table> editor = unsetEditor;

        protected ByteLogicGate(){
            initSignals();
        }

        public void afterRead(){

        }

        protected void initSignals(){
            signals = signals();
            inputSignals = inputSignals();
            int max = Math.max(signals.length, inputSignals.length);
            for(int i = 0; i < max; i++){
                if(i < signals.length) signals[i] = new Signal();
                if(i < inputSignals.length) inputSignals[i] = new Signal();
            }
        }

        public abstract ByteLogicGateGroup group();

        @NotNull
        protected Signal[] signals(){
            return new Signal[outputConnections()];
        }

        @NotNull
        protected Signal[] inputSignals(){
            return new Signal[inputConnections()];
        }

        @Override
        public boolean update(){
            if(step)
                updateSignals();
            else
                updateInputs();
            step = !step;
            return true;
        }

        public abstract void updateSignals();

        public void updateInputs(){
            for(Signal signal : inputSignals){
                signal.setZero();
            }
            for(ConnectionWire<ByteLogicGate> wire : inputWires){
                inputSignals[wire.input].or(wire.obj.signals[wire.parentOutput]);
            }
        }

        public int[] outputSides(){
            return new int[0];
        }

        public int[] inputSides(){
            return new int[0];
        }

        protected boolean useShortInlineFields(){
            return false;
        }

        protected boolean shouldInlineFields(){
            return false;
        }

        @Nullable
        @Override
        public Cons2<TiledStructuresDialog, Table> editor(){
            if(!shouldInlineFields()) return null;
            if(editor == unsetEditor){
                editor = (instance, t) -> {
//                    t.background(Tex.pane);
                    t.left();
                    t.top().margin(10f).marginTop(20f);

                    t.defaults().minHeight(40f).left();
                    var obj = this;

                    int i = 0;
                    for(var e : JsonIO.json.getFields(getClass()).values()){
                        if(i++ > 0) t.row();

                        var f = e.field;
                        var ft = f.getType();
                        int mods = f.getModifiers();

                        if(!Modifier.isPublic(mods) || (Modifier.isFinal(mods) && (
                            String.class.isAssignableFrom(ft) ||
                                TiledStructuresDialog.unbox(ft).isPrimitive()
                        )) || f.getAnnotation(CodeEdit.class) != null) continue;

                        var anno = Structs.find(f.getDeclaredAnnotations(), a -> instance.hasInterpreter(a.annotationType(), ft));
                        var nameAnnotation = f.getAnnotation(ShortName.class);
                        String fieldName;
                        String realFieldName = nameAnnotation == null ? f.getName() : nameAnnotation.value();
                        if(useShortInlineFields())
                            fieldName = " ";
                        else
                            fieldName = realFieldName;

                        instance.getInterpreter(anno == null ? Override.class : anno.annotationType(), ft).build(instance,
                            t, fieldName,
                            new TypeInfo(f), f, null,
                            null,
                            () -> Reflect.get(obj, f),
                            Modifier.isFinal(mods) ? res -> {
                            } : res -> Reflect.set(obj, f, res));
                        if(useShortInlineFields()){
                            Seq<Label> names = new Seq<>();
                            t.find(it -> {
                                if(it instanceof Label label && label.getText().toString().contains(fieldName)){
                                    names.add(label);
                                }
                                return false;
                            });
                            for(Label label : names){
                                label.remove();
                            }
                            t.addListener(Tooltips.getInstance().create(realFieldName));
                        }

                    }
                };
            }
            return editor;
        }

        @Override
        public Color colorForInput(int input){
            return signals[input].color();
        }

        @Override
        public String name(){
            return getClass().getSimpleName()/*.replace("Gate", "")*/;
        }

        @Override
        public int outputConnections(){
            return 1;
        }

        @Override
        public String typeName(){

            String className = name().replace("Gate", "");
            return Core.bundle == null ? className : Core.bundle.get("byte-logic-gate." + className.toLowerCase() + ".name", className);
        }

        protected boolean alwaysQualified(){
            return true;
        }

        @Override
        public boolean qualified(){
            return alwaysQualified() || super.qualified();
        }

        public boolean canUseInGraphics(){
            return true;
        }

        public void setLink(ByteLogicProcessorBuild build){

        }

        public void write(Writes write){

            BLSerializer.writeTiledStructure(write, this);
            ByteLogicGateSerializer.write(write, clazz, this);
        }

        public void read(Reads read){
            BLSerializer.readTiledStructure(read, this);
            ByteLogicGateSerializer.read(read, clazz, this);
            afterRead();
        }
    }

    public static abstract class SignalProviderGate extends ByteLogicGate{
        protected transient final Signal lastSignal = new Signal();

        @Override
        public ByteLogicGateGroup group(){
            return ByteLogicGateGroup.signalProviders;
        }

        @Override
        public int inputConnections(){
            return 0;
        }


        public static class ConstantGate extends SignalProviderGate{
            static{
                TiledStructuresDialog.setGlobalInterpreter(ConstantGateNumber.class, long.class, (instance, cont, name, type, field, remover, indexer, get, set) -> {
                    long number = get.get();
                    cont.button(Icon.pencilSmall, () -> {
                        ui.showTextInput("@block.editsignal", "", 10, number + "", true, result -> {
                            set.get(Strings.parseLong(result, 0));
                        });
                        control.input.config.hideConfig();
                    }).size(40f);
                    cont.button(Icon.imageSmall, () -> {
                        new CanvasEditDialog(set, get).show();
                    }).size(40f);

                    cont.button(Icon.pick, () -> {
                        Color tmpColor = new Color();
                        tmpColor.set((int)number);
                        ui.picker.show(tmpColor, true, out -> {
                            set.get((long)out.rgba());
//                            tmpSignal.type = SignalTypes.colorType;
//                            configure(tmpSignal.asBytes());
                        });
                    }).size(40f);
                });
            }

            @ConstantGateNumber
            public long number;

            @Override
            protected boolean shouldInlineFields(){
                return true;
            }

            @Override
            public void updateSignals(){
                Signal.valueOf(signals[0], number);
            }

            @Retention(RetentionPolicy.RUNTIME)
            public @interface ConstantGateNumber{

            }
        }

        public static class SwitchGate extends SignalProviderGate{
            public boolean enabled;

            @Override
            protected boolean shouldInlineFields(){
                return true;
            }

            @Override
            public void updateSignals(){
                Signal.valueOf(signals[0], enabled ? 1 : 0);
            }
        }

    }

    public static abstract class UnaryGate extends ByteLogicGate{
        public final String operationLetter;

        protected UnaryGate(String operationLetter){
            this.operationLetter = operationLetter;
        }

        @Override
        public ByteLogicGateGroup group(){
            return ByteLogicGateGroup.unaryOperators;
        }

        @Nullable
        @Override
        public Tooltip inputConnectorTooltip(int inputIndex){
            return SideTooltips.getInstance().create("a");
        }

        @Nullable
        @Override
        public Tooltip outputConnectorTooltip(int inputIndex){
            if(operationLetter.startsWith("@"))
                return SideTooltips.getInstance().create(Core.bundle.format(operationLetter, "a"));
            return SideTooltips.getInstance().create(operationLetter + "a");
        }

        @Override
        public boolean hasFields(){
            return false;
        }

        abstract Signal process(Signal value);

        @Override
        public int inputConnections(){
            return 1;
        }

        @Override
        public int objWidth(){
            return 4;
        }

        @Override
        public int objHeight(){
            return 1;
        }

        @Override
        public void updateSignals(){
            signals[0].set(process(inputSignals[0]));
        }

        public static class RelayGate extends UnaryGate{
            public RelayGate(){
                super("");
            }

            @Override
            Signal process(Signal value){
                return value;
            }
        }

        public static class NotGate extends UnaryGate{
            public NotGate(){
                super("!");
            }

            @Override
            Signal process(Signal value){
                return Signal.valueOf(value, value.compareWithZero() == 0 ? 1 : 0);
            }
        }

        public static class FontGate extends UnaryGate{
            private final transient static Signal def = Signal.valueOf(0);

            public FontGate(){
                super("@byte-logic.font-gate");
            }

            @Override
            Signal process(Signal value){
                return FontSignalBlock.font.get(value.intNumber(), def);
            }
        }


        public static class DelayerGate extends UnaryGate{
            public transient Seq<Signal> signalsQueue = Seq.with(new Signal());
            public int currentDelay = 1;
            @CodeEdit
            public int tickCounter = 0;
            transient int previousDelay = 1;
            private transient int realAmount = 1;

            public DelayerGate(){
                super("");
            }

            @Override
            public boolean hasFields(){
                return true;
            }

            @Override
            public int objHeight(){
                return 2;
            }

            @Override
            Signal process(Signal value){
                checkQueue();
                if(currentDelay == 0) return value;
                signalsQueue.get(tickCounter).set(value);
                tickCounter = Mathf.mod(tickCounter + 1, signalsQueue.size);
                value.set(signalsQueue.get(tickCounter));
                return value;
            }

            private void checkQueue(){
                if(currentDelay != previousDelay){
                    if(realAmount < currentDelay){
                        signalsQueue.size = realAmount;
                        for(int i = 0; i < currentDelay - realAmount; i++){
                            signalsQueue.add(new Signal());
                        }
                        realAmount = signalsQueue.size;
                    }else{
                        signalsQueue.size = currentDelay;
                    }
                    for(int i = 0; i < signalsQueue.size; i++){
                        signalsQueue.get(i).setZero();
                    }
                    tickCounter = 0;
                    previousDelay = currentDelay;
                }
            }
        }

        public static class TransformerGate extends UnaryGate{
            static{
                TiledStructuresDialog.setGlobalInterpreter(SIGNAL_TYPE.class, int.class, (instance, cont, name, type, field, remover, indexer, get, set) -> {
                    ComboBox comboBox = new ComboBox();
                    for(SignalType signalType : SignalType.all){
                        if(signalType == SignalTypes.nilType) continue;
                        ComboBoxItem item = new ComboBoxItem(signalType.getIcon(), signalType.getName());
                        comboBox.items.add(item);
                        if(signalType.getId() == get.get()){
                            comboBox.selectItem(comboBox.items.size - 1);
                        }
                        item.object = signalType;
                    }
                    comboBox.addItemListener((old, newItem) -> {
                        SignalType signalType = (SignalType)newItem.object;
                        set.get(signalType.getId());
                    });
                    TiledStructuresDialog.name(cont, name, remover, indexer);
                    cont.add(comboBox).fillX();
                });
            }

            @SIGNAL_TYPE
            public int typeId = SignalTypes.numberType.getId();
            public boolean signalAsParam = false;

            public TransformerGate(){
                super("");
            }

            @Override
            public boolean hasFields(){
                return true;
            }

            @Override
            public int objHeight(){
                return 2;
            }

            @Nullable
            @Override
            public Tooltip inputConnectorTooltip(int inputIndex){
                if(inputIndex == 0) return null;
                return SideTooltips.getInstance().create("typeId");
            }

            @Override
            public boolean enabledInput(int index){
                return index == 0 || signalAsParam;
            }

            @Override
            public int inputConnections(){
                return 2;
            }

            @Override
            Signal process(Signal value){
                int typeId = this.typeId;
                if(signalAsParam){

                    typeId = inputSignals[1].intNumber();
                    if(typeId >= SignalTypes.nilType.getId()) typeId++;
                }
                if(typeId < 0 || typeId >= SignalType.all.length || SignalTypes.nilType.getId() == typeId) typeId = SignalTypes.numberType.getId();
                value.type = SignalType.all[typeId];
                return value;
            }

            @Retention(RetentionPolicy.RUNTIME)
            public @interface SIGNAL_TYPE{

            }
        }
        public static class TypeOfGate extends UnaryGate{


            public TypeOfGate(){
                super("");
            }

            @Override
            public boolean hasFields(){
                return true;
            }

            @Override
            public int objHeight(){
                return 2;
            }

            @Nullable
            @Override
            public Tooltip outputConnectorTooltip(int inputIndex){
                return SideTooltips.getInstance().create("typeId");
            }
            @Override
            Signal process(Signal value){
                int id = value.type.getId();
                if (id>=SignalTypes.nilType.getId()){
                    id--;
                }
                Signal.valueOf(value, id);
                return value;
            }
        }

        public static abstract class FloatOperationGate extends UnaryGate{
            protected FloatOperationGate(String operationLetter){
                super(operationLetter);
            }

            @Override
            public ByteLogicGateGroup group(){
                return ByteLogicGateGroup.floatOperations;
            }

            public static class ToFloatGate extends FloatOperationGate{
                public ToFloatGate(){
                    super("a");
                }

                @Override
                Signal process(Signal value){
                    if(value.type == SignalTypes.floatType) return value;
                    value.type = SignalTypes.floatType;
                    value.setNumber(Double.doubleToRawLongBits(value.number()));
                    return value;
                }
            }

            public static class RoundGate extends FloatOperationGate{
                public RoundGate(){
                    super("@round");
                }

                @Override
                Signal process(Signal value){
                    if(value.type != SignalTypes.floatType) return value;

                    double number = Double.longBitsToDouble(value.number());


                    long longValue;
                    double floatPart = number % 1.0;
                    if(floatPart >= 0.5){
                        longValue = (long)number + 1;
                    }else{
                        longValue = (long)number;
                    }
                    return Signal.valueOf(value, longValue);
                }
            }

            public static class CeilGate extends FloatOperationGate{
                public CeilGate(){
                    super("@round");
                }

                @Override
                Signal process(Signal value){
                    if(value.type != SignalTypes.floatType) return value;

                    double number = Double.longBitsToDouble(value.number());


                    long longValue;
                    double floatPart = number % 1.0;
                    if(floatPart <= 0.000001){
                        longValue = (long)number;
                    }else{
                        longValue = (long)number + 1;
                    }
                    return Signal.valueOf(value, longValue);
                }
            }

            public static class FloorGate extends FloatOperationGate{
                public FloorGate(){
                    super("@floor");
                }

                @Override
                Signal process(Signal value){
                    if(value.type != SignalTypes.floatType) return value;

                    double number = Double.longBitsToDouble(value.number());
                    return Signal.valueOf(value, (long)number);
                }
            }
        }

    }

    public static abstract class BinaryGate extends ByteLogicGate{
        public final String operationLetter;
        protected transient final Signal[] tmpValues = {new Signal(), new Signal()};

        protected BinaryGate(String operationLetter){
            this.operationLetter = operationLetter;
        }

        @Override
        public ByteLogicGateGroup group(){
            return ByteLogicGateGroup.binaryOperators;
        }

        @Nullable
        @Override
        public Tooltip inputConnectorTooltip(int inputIndex){
            return SideTooltips.getInstance().create(inputIndex == 0 ? "a" : "b");
        }

        @Nullable
        @Override
        public Tooltip outputConnectorTooltip(int outputIndex){
            return SideTooltips.getInstance().create("a " + operationLetter + " b");
        }

        abstract Signal process(Signal a, Signal b);

        @Override
        public boolean hasFields(){
            return false;
        }

        @Override
        public int objWidth(){
            return 4;
        }

        @Override
        public int objHeight(){
            return 2;
        }

        @Override
        public int inputConnections(){
            return 2;
        }

        @Override
        public void updateSignals(){
            signals[0].set(process(inputSignals[0], inputSignals[1]));
        }

        public static class OrGate extends BinaryGate{
            public OrGate(){
                super("|");
            }

            @Override
            public ByteLogicGateGroup group(){
                return ByteLogicGateGroup.bitOperators;
            }

            @Override
            Signal process(Signal a, Signal b){
                a.or(b);
                return a;
            }
        }

        public static class XorGate extends BinaryGate{
            public XorGate(){
                super("^");
            }

            @Override
            public ByteLogicGateGroup group(){
                return ByteLogicGateGroup.bitOperators;
            }

            @Override
            Signal process(Signal a, Signal b){
                a.xor(b);
                return a;
            }
        }

        public static class AndGate extends BinaryGate{
            public AndGate(){
                super("&");
            }

            @Override
            public ByteLogicGateGroup group(){
                return ByteLogicGateGroup.bitOperators;
            }

            @Override
            Signal process(Signal a, Signal b){
                a.and(b);
                return a;
            }
        }

        public static class PlusGate extends BinaryGate{
            public PlusGate(){
                super("+");
            }

            @Override
            public String name(){
                return "AddGate";
            }

            @Override
            public String typeName(){

                String className = "Plus";
                return Core.bundle == null ? className : Core.bundle.get("byte-logic-gate." + className.toLowerCase() + ".name", className);
            }

            @Override
            Signal process(Signal a, Signal b){
                a.plus(b);
                return a;
            }
        }

        public static class SubtractGate extends BinaryGate{
            public SubtractGate(){
                super("-");
            }

            @Override
            Signal process(Signal a, Signal b){
                a.minus(b);
                return a;
            }
        }

        public static class MultipleGate extends BinaryGate{
            public MultipleGate(){
                super("*");
            }

            @Override
            Signal process(Signal a, Signal b){
                a.times(b);
                return a;
            }
        }

        public static class DivideGate extends BinaryGate{
            public DivideGate(){
                super("/");
            }

            @Override
            Signal process(Signal a, Signal b){
                if(b.compareWithZero() == 0) return b;
                a.div(b);
                return a;
            }
        }

        public static class RemainGate extends BinaryGate{
            public RemainGate(){
                super("%");
            }

            @Override
            Signal process(Signal a, Signal b){
                if(b.compareWithZero() == 0) return b;
                a.mod(b);
                return a;
            }
        }

        public static class EqualsGate extends BinaryGate{
            public EqualsGate(){
                super("=");
            }

            @Override
            Signal process(Signal a, Signal b){
                return Signal.valueOf(a, a.equals(b) ? 1 : 0);
            }
        }

        public static class CompareGate extends BinaryGate{
            public CompareGate(){
                super(">");
            }

            @Override
            Signal process(Signal a, Signal b){
                return Signal.valueOf(a, a.compareTo(b) > 0 ? 1 : 0);
            }
        }

    }

    public static abstract class LinkedGate extends ByteLogicGate implements ConfigGroupStructure{
        public transient @Nullable ByteLogicProcessorBuild link;
        @ShortName("position")
        public int clockWisePosition;

        @Override
        public void setLink(ByteLogicProcessorBuild build){
            this.link = build;
        }

        @Override
        public void updateConfig(int index){
            clockWisePosition = index;
        }

        @Override
        public ByteLogicGateGroup group(){
            return ByteLogicGateGroup.inputOutput;
        }


        @Override
        public int objWidth(){
            return 3;
        }

        @Override
        public int objHeight(){
            return 2;
        }

        public static class OutputGate extends LinkedGate{
            public OutputGate(){
                super();
            }

            @Override
            public int[] outputSides(){
                return new int[]{clockWisePosition};
            }

            @Override
            public boolean canUseInGraphics(){
                return false;
            }

            @Override
            public int outputConnections(){
                return 0;
            }

            @Override
            public int inputConnections(){
                return 1;
            }

            @Override
            public void updateSignals(){
                if(link != null) clockWisePosition = clockWisePosition % (link.block.size * 4);
                if(link != null && link.buildingUpdate){
                    link.transferSignal(clockWisePosition, inputSignals[0]);
                }
            }

        }

        public static class InputGate extends LinkedGate{
            public InputGate(){
                super();
            }

            @Override
            public int[] inputSides(){
                return new int[]{clockWisePosition};
            }

            @Override
            public int outputConnections(){
                return 1;
            }

            @Override
            public int inputConnections(){
                return 0;
            }

            @Override
            public void updateSignals(){
                if(link != null) clockWisePosition = clockWisePosition % (link.block.size * 4);
                if(link != null){
                    signals[0].set(link.inputSignal(clockWisePosition));
                }else{
                    signals[0].setZero();
                }
            }

        }
    }

    public static abstract class BitOperationGate extends ByteLogicGate{
        @Override
        public int objWidth(){
            return 4;
        }

        @Override
        public int outputConnections(){
            return 1;
        }

        @Override
        public ByteLogicGateGroup group(){
            return ByteLogicGateGroup.bitOperators;
        }

        @Override
        public boolean hasFields(){
            return false;
        }

        public static class SetBitGate extends BitOperationGate{
            @Override
            public @Nullable Tooltip inputConnectorTooltip(int inputIndex){
                if(inputIndex == 1) return SideTooltips.getInstance().create("@byte-logic.bitIndex");
                if(inputIndex == 2) return SideTooltips.getInstance().create("@byte-logic.bitValue");
                return null;
            }

            @Override
            public void updateSignals(){
                long mask = 1L << inputSignals[1].number();

                if(inputSignals[2].compareWithZero() == 0){
                    signals[0].setNumber(
                        inputSignals[0].number() & ~mask
                    );
                }else{
                    signals[0].setNumber(
                        inputSignals[0].number() | mask
                    );
                }
                signals[0].type = inputSignals[0].type;
            }

            @Override
            public int inputConnections(){
                return 3;
            }
        }

        public static class GetBitGate extends BitOperationGate{
            @Override
            public @Nullable Tooltip inputConnectorTooltip(int inputIndex){
                if(inputIndex == 1) return SideTooltips.getInstance().create("@byte-logic.bitIndex");
                return null;
            }

            @Override
            public void updateSignals(){
                long mask = 1L << inputSignals[1].number();

                Signal.valueOf(signals[0], (inputSignals[0].number() & mask) == 0 ? 0 : 1);
            }

            @Override
            public int inputConnections(){
                return 2;
            }
        }

        public static class ShiftGate extends BitOperationGate implements ConfigGroupStructure{
            public boolean isLeft;
            public int shiftValue;
            public boolean signalAsShiftIndex;

            @Override
            public boolean hasFields(){
                return true;
            }

            @Override
            public void updateSignals(){
                signals[0].type = inputSignals[0].type;
                long shiftValue = this.shiftValue;
                if(signalAsShiftIndex){
                    shiftValue = inputSignals[1].number();
                }
                if(isLeft){
                    signals[0].setNumber(inputSignals[0].number() << shiftValue);
                }else{
                    signals[0].setNumber(inputSignals[0].number() >>> shiftValue);
                }
            }

            @Override
            public int outputConnections(){
                return 1;
            }

            @Override
            public boolean enabledInput(int index){
                return index == 0 || signalAsShiftIndex;
            }

            @Override
            public int inputConnections(){
                return 2;
            }

            @Override
            public void updateConfig(int index){
                shiftValue = index % 64;
            }
        }
    }

    public static abstract class MathGate extends ByteLogicGate{
        public transient ConnectionSettings connections;//not null

        @Override
        public @Nullable Tooltip inputConnectorTooltip(int inputIndex){
            String inputName = connections.inputWires.get(inputIndex).name;
            return inputName == null ? null : SideTooltips.INSTANCE.create(inputName);
        }

        @Override
        public @Nullable Tooltip outputConnectorTooltip(int outputIndex){
            String outputName = connections.outputWires.get(outputIndex).name;
            return outputName == null ? null : SideTooltips.INSTANCE.create(outputName);
        }

        @Override
        public int objWidth(){
            return 4;
        }

        @Override
        protected void initSignals(){
            connections = new ConnectionSettings();
            initConnections();
            super.initSignals();
        }

        protected abstract void initConnections();

        @Override
        public int outputConnections(){
            return connections.outputWires.size;
        }

        @Override
        public int inputConnections(){
            return connections.inputWires.size;
        }

        @Override
        public ByteLogicGateGroup group(){
            return ByteLogicGateGroup.math;
        }

        public static class AbsGate extends MathGate{


            @Override
            public void initConnections(){
                connections.inputWire("a");
                connections.outputWire("|a|");
            }

            @Override
            public void updateSignals(){
                signals[0].set(inputSignals[0]);
                signals[0].absolute();
            }
        }
        private static final Signal radiansSignal = new Signal(){{
            type = SignalTypes.floatType;
            setNumber(Double.doubleToRawLongBits(Math.toDegrees(1)));
        }};
        public static class SinGate extends MathGate{

            public boolean radians = false;

            @Override
            protected void initConnections(){
                connections.inputWire("a");
                connections.outputWire("sin(a)");
            }

            @Override
            public void updateSignals(){
                signals[0].set(inputSignals[0]);
                inputSignals[0].set(radiansSignal);
                if(!radians){
                    signals[0].div(inputSignals[0]);
                }
                inputSignals[0].type.sin(signals[0]);
            }
        }
        public static class CosGate extends MathGate{
            public boolean radians = false;
            @Override
            protected void initConnections(){
                connections.inputWire("a");
                connections.outputWire("cos(a)");
            }

            @Override
            public void updateSignals(){
                signals[0].set(inputSignals[0]);
                if(!radians){
                    signals[0].div(radiansSignal);
                }
                inputSignals[0].type.cos(signals[0]);
            }
        }
        public static class TanGate extends MathGate{
            public boolean radians = false;
            @Override
            protected void initConnections(){
                connections.inputWire("a");
                connections.outputWire("tan(a)");
            }

            @Override
            public void updateSignals(){
                signals[0].set(inputSignals[0]);
                if(!radians){
                    signals[0].div(radiansSignal);
                }
                inputSignals[0].type.tan(signals[0]);
            }
        }
        public static class AsinGate extends MathGate{
            public boolean radians = false;
            @Override
            protected void initConnections(){
                connections.inputWire("a");
                connections.outputWire("asin(a)");
            }

            @Override
            public void updateSignals(){
                signals[0].set(inputSignals[0]);
                inputSignals[0].type.atan(signals[0]);
                if(!radians){
                    signals[0].times(radiansSignal);
                }
            }
        }
        public static class AcosGate extends MathGate{
            public boolean radians = false;
            @Override
            protected void initConnections(){
                connections.inputWire("a");
                connections.outputWire("acos(a)");
            }

            @Override
            public void updateSignals(){
                signals[0].set(inputSignals[0]);
                inputSignals[0].type.acos(signals[0]);
                if(!radians){
                    signals[0].times(radiansSignal);
                }
            }
        }
        public static class AtanGate extends MathGate{
            public boolean radians = false;
            @Override
            protected void initConnections(){
                connections.inputWire("a");
                connections.outputWire("atan(a)");
            }

            @Override
            public void updateSignals(){
                signals[0].set(inputSignals[0]);
                inputSignals[0].type.atan(signals[0]);
                if(!radians){
                    signals[0].times(radiansSignal);
                }
            }
        }
    }
}
