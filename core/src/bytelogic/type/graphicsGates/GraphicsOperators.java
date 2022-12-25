package bytelogic.type.graphicsGates;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.scene.ui.TextField.*;
import arc.scene.ui.*;
import arc.struct.*;
import arc.util.*;
import bytelogic.type.byteGates.*;
import bytelogic.type.byteGates.ByteLogicOperators.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.world.blocks.logic.*;
import mma.ui.tiledStructures.*;
import mma.ui.tiledStructures.TiledStructures.*;
import mma.utils.*;
import org.jetbrains.annotations.Nullable;
import zelaux.arclib.ui.tooltips.*;

import java.lang.reflect.*;

public class GraphicsOperators{
    static Prov<? extends TiledStructure>[] providers;

    static{

        Seq<Class<?>> seq = new Seq<>();
        collectClasses(GraphicsOperators.class, seq, new ObjectSet<>());
        providers = seq.map(Reflect::cons).reverse().toArray(Prov.class);
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

    public static Seq<Prov<? extends TiledStructure>> getProvidersAsSequence(){
        return Seq.with(providers);
    }

    public static <T extends GraphicsOperator> void addProvider(Class<T> type, Prov<T> prov){
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
        if(!Modifier.isAbstract(clazz.getModifiers()) && GraphicsOperator.class.isAssignableFrom(clazz)){
            seq.add(clazz);
        }
        for(Class<?> aClass : clazz.getDeclaredClasses()){
            if(GraphicsOperator.class.isAssignableFrom(aClass) && set.add(aClass)){
                collectClasses(aClass, seq, set);
            }
        }
    }

    public static abstract class GraphicsOperator extends ByteLogicGate{
        @Nullable
        public transient DrawCommandListener listener;

        @Override
        public final boolean canUseInGraphics(){
            return true;
        }

        @Override
        public int objHeight(){
            return Math.max(2, inputConnections() - 1);
        }

        @Override
        public ByteLogicGateGroup group(){
            return ByteLogicGateGroup.draw;
        }


        @Override
        public final int outputConnections(){
            return 1;
        }

        public abstract void updateDraw();

        @Override
        public final void updateSignals(){
            updateDraw();
            signals[0].set(inputSignals[0]);
        }

        @Override
        @Nullable
        public final Tooltip outputConnectorTooltip(int outputIndex){
            if(outputIndex != 0) return null;
            return SideTooltips.getInstance().create("@enabled");
        }

        @Override
        public String name(){
            return getClass().getSimpleName()/*.replace("Gate", "")*/;
        }

        @Override
        public String typeName(){
            String className = name().replace("Operator", "");
            return Core.bundle == null ? className : Core.bundle.get("byte-logic-operator." + className.toLowerCase() + ".name", className);
        }

        public interface DrawCommandListener{
            void executeDrawCommand(long drawCommand);

            void flushDisplay();
        }
    }

    public static class FlushOperator extends GraphicsOperator{

        @Override
        public @Nullable Tooltip inputConnectorTooltip(int inputIndex){
            if(inputIndex == 0) return SideTooltips.getInstance().create("@enabled");
            return null;
        }

        @Override
        public void updateDraw(){
            if(listener != null && inputSignals[0].compareWithZero() != 0){
                listener.flushDisplay();
            }
        }

        @Override
        public int inputConnections(){
            return 1;
        }
    }

    public static class ClearOperator extends GraphicsOperator{

        @Override
        public @Nullable Tooltip inputConnectorTooltip(int inputIndex){
            if(inputIndex == 0) return SideTooltips.getInstance().create("@enabled");
            if(inputIndex == 1) return SideTooltips.getInstance().create("@color");
            return null;
        }

        @Override
        public int inputConnections(){
            return 2;
        }

        @Override
        public void updateDraw(){
            if(listener == null) return;
            if(inputSignals[0].compareWithZero() == 0) return;
            int rgba = inputSignals[1].intNumber();

            int r = Color.ri(rgba);
            int g = Color.gi(rgba);
            int b = Color.bi(rgba);
            int a = Color.ai(rgba);
            listener.executeDrawCommand(DisplayCmd.get(LogicDisplay.commandClear, r, g, b, a, 0, 0));
        }
    }

    public static class ColorOperator extends GraphicsOperator{

        @Override
        public @Nullable Tooltip inputConnectorTooltip(int inputIndex){
            if(inputIndex == 0) return SideTooltips.getInstance().create("@enabled");
            if(inputIndex == 1) return SideTooltips.getInstance().create("@color");
            return null;
        }

        @Override
        public int inputConnections(){
            return 2;
        }

        @Override
        public void updateDraw(){
            if(listener == null) return;
            if(inputSignals[0].compareWithZero() == 0) return;
            int rgba = inputSignals[1].intNumber();

            int r = Color.ri(rgba);
            int g = Color.gi(rgba);
            int b = Color.bi(rgba);
            int a = Color.ai(rgba);
            listener.executeDrawCommand(DisplayCmd.get(LogicDisplay.commandColor, r, g, b, a, 0, 0));
        }
    }

    public static class StrokeOperator extends GraphicsOperator{

        @Override
        public @Nullable Tooltip inputConnectorTooltip(int inputIndex){
            if(inputIndex == 0) return SideTooltips.getInstance().create("@enabled");
            if(inputIndex == 1) return SideTooltips.getInstance().create("@stroke");
            return null;
        }

        @Override
        public int inputConnections(){
            return 2;
        }

        @Override
        public void updateDraw(){
            if(listener == null) return;
            if(inputSignals[0].compareWithZero() == 0) return;
            listener.executeDrawCommand(DisplayCmd.get(LogicDisplay.commandStroke, inputSignals[1].intNumber(), 0, 0, 0, 0, 0));
        }
    }

    public static class DrawLineOperator extends GraphicsOperator{

        @Override
        public @Nullable Tooltip inputConnectorTooltip(int inputIndex){
            return switch(inputIndex - 1){
                case -1 -> SideTooltips.getInstance().create("@enabled");
                case 0 -> SideTooltips.getInstance().create("x");
                case 1 -> SideTooltips.getInstance().create("y");
                case 2 -> SideTooltips.getInstance().create("x2");
                case 3 -> SideTooltips.getInstance().create("y2");
                default -> null;
            };
        }

        @Override
        public int inputConnections(){
            return 5;
        }

        @Override
        public void updateDraw(){
            if(listener == null || inputSignals[0].compareWithZero() == 0) return;
            int x2 = inputSignals[3].intNumber();
            int y2 = inputSignals[4].intNumber();
            listener.executeDrawCommand(DisplayCmd.get(
                LogicDisplay.commandRect, inputSignals[1].intNumber(), inputSignals[2].intNumber(), x2, y2, 0, 0
            ));
        }
    }

    public static class DrawRectOperator extends GraphicsOperator{

        @Override
        public @Nullable Tooltip inputConnectorTooltip(int inputIndex){
            return switch(inputIndex - 1){
                case -1 -> SideTooltips.getInstance().create("@enabled");
                case 0 -> SideTooltips.getInstance().create("x");
                case 1 -> SideTooltips.getInstance().create("y");
                case 2 -> SideTooltips.getInstance().create("@width");
                case 3 -> SideTooltips.getInstance().create("@height");
                default -> null;
            };
        }

        @Override
        public int inputConnections(){
            return 5;
        }

        @Override
        public void updateDraw(){
            if(listener == null || inputSignals[0].compareWithZero() == 0) return;
            int width = inputSignals[3].intNumber();
            int height = inputSignals[4].intNumber();
            if(width == 0 || height == 0) return;
            listener.executeDrawCommand(DisplayCmd.get(
                LogicDisplay.commandRect, inputSignals[1].intNumber(), inputSignals[2].intNumber(), width, height, 0, 0
            ));
        }
    }

    public static class DrawLineRectOperator extends GraphicsOperator{

        @Override
        public @Nullable Tooltip inputConnectorTooltip(int inputIndex){
            return switch(inputIndex - 1){
                case -1 -> SideTooltips.getInstance().create("@enabled");
                case 0 -> SideTooltips.getInstance().create("x");
                case 1 -> SideTooltips.getInstance().create("y");
                case 2 -> SideTooltips.getInstance().create("@width");
                case 3 -> SideTooltips.getInstance().create("@height");
                default -> null;
            };
        }

        @Override
        public int inputConnections(){
            return 5;
        }

        @Override
        public void updateDraw(){
            if(listener == null || inputSignals[0].compareWithZero() == 0) return;
            int width = inputSignals[3].intNumber();
            int height = inputSignals[4].intNumber();
            if(width == 0 || height == 0) return;
            listener.executeDrawCommand(DisplayCmd.get(
                LogicDisplay.commandLineRect, inputSignals[1].intNumber(), inputSignals[2].intNumber(), width, height, 0, 0
            ));
        }
    }

    public static class DrawPolyOperator extends GraphicsOperator{

        @Override
        public @Nullable Tooltip inputConnectorTooltip(int inputIndex){
            return switch(inputIndex - 1){
                case -1 -> SideTooltips.getInstance().create("@enabled");
                case 0 -> SideTooltips.getInstance().create("x");
                case 1 -> SideTooltips.getInstance().create("y");
                case 2 -> SideTooltips.getInstance().create("@sides");
                case 3 -> SideTooltips.getInstance().create("@radius");
                case 4 -> SideTooltips.getInstance().create("@rotation");
                default -> null;
            };
        }

        @Override
        public int inputConnections(){
            return 6;
        }

        @Override
        public void updateDraw(){
            if(listener == null || inputSignals[0].compareWithZero() == 0) return;
            listener.executeDrawCommand(DisplayCmd.get(
                LogicDisplay.commandPoly,
                inputSignals[1].intNumber(),
                inputSignals[2].intNumber(),
                inputSignals[3].intNumber(),
                inputSignals[4].intNumber(),
                inputSignals[5].intNumber(),
                0
            ));
        }
    }

    public static class DrawLinePolyOperator extends GraphicsOperator{

        @Override
        public @Nullable Tooltip inputConnectorTooltip(int inputIndex){
            return switch(inputIndex - 1){
                case -1 -> SideTooltips.getInstance().create("@enabled");
                case 0 -> SideTooltips.getInstance().create("x");
                case 1 -> SideTooltips.getInstance().create("y");
                case 2 -> SideTooltips.getInstance().create("@sides");
                case 3 -> SideTooltips.getInstance().create("@radius");
                case 4 -> SideTooltips.getInstance().create("@rotation");
                default -> null;
            };
        }

        @Override
        public int inputConnections(){
            return 6;
        }

        @Override
        public void updateDraw(){
            if(listener == null || inputSignals[0].compareWithZero() == 0) return;
            listener.executeDrawCommand(DisplayCmd.get(
                LogicDisplay.commandLinePoly,
                inputSignals[1].intNumber(),
                inputSignals[2].intNumber(),
                inputSignals[3].intNumber(),
                inputSignals[4].intNumber(),
                inputSignals[5].intNumber(),
                0
            ));
        }
    }

    public static class DrawTriangleOperator extends GraphicsOperator{

        @Override
        public @Nullable Tooltip inputConnectorTooltip(int inputIndex){
            return switch(inputIndex - 1){
                case -1 -> SideTooltips.getInstance().create("@enabled");
                case 0 -> SideTooltips.getInstance().create("x");
                case 1 -> SideTooltips.getInstance().create("y");
                case 2 -> SideTooltips.getInstance().create("x2");
                case 3 -> SideTooltips.getInstance().create("y2");
                case 4 -> SideTooltips.getInstance().create("x3");
                case 5 -> SideTooltips.getInstance().create("y3");
                default -> null;
            };
        }

        @Override
        public int inputConnections(){
            return 7;
        }

        @Override
        public void updateDraw(){
            if(listener == null || inputSignals[0].compareWithZero() == 0) return;
            listener.executeDrawCommand(DisplayCmd.get(
                LogicDisplay.commandTriangle,
                inputSignals[1].intNumber(),
                inputSignals[2].intNumber(),
                inputSignals[3].intNumber(),
                inputSignals[4].intNumber(),
                inputSignals[5].intNumber(),
                inputSignals[6].intNumber()
            ));
        }
    }

    public static class DrawImageOperator extends GraphicsOperator{

        @Override
        public @Nullable Tooltip inputConnectorTooltip(int inputIndex){
            return switch(inputIndex - 1){
                case -1 -> SideTooltips.getInstance().create("@enabled");
                case 0 -> SideTooltips.getInstance().create("x");
                case 1 -> SideTooltips.getInstance().create("y");
                case 2 -> SideTooltips.getInstance().create("@image");
                case 3 -> SideTooltips.getInstance().create("@size");
                case 4 -> SideTooltips.getInstance().create("@rotation");
                default -> null;
            };
        }

        @Override
        public int inputConnections(){
            return 6;
        }

        @Override
        public void updateDraw(){
            if(listener == null || inputSignals[0].compareWithZero() == 0) return;
            listener.executeDrawCommand(DisplayCmd.get(
                LogicDisplay.commandImage,
                inputSignals[1].intNumber(),
                inputSignals[2].intNumber(),
                inputSignals[3].intNumber(),
                inputSignals[4].intNumber(),
                inputSignals[5].intNumber(),
                0
            ));
        }
    }

}
