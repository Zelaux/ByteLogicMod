package bytelogic;

import arc.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.game.*;
import mma.*;
import bytelogic.content.*;
import bytelogic.core.*;
import bytelogic.customArc.*;

import static mindustry.Vars.headless;

public class BLVars extends ModVars{
    private final static Seq<Runnable> onLoad = new Seq<>();
    public static ModSettings settings;
    public static BLUI modUI;
    public static ByteLogicMod mod;

    static{
        new BLVars();
    }

    public static void create(){
        //none
    }

    public static void init(){
    }

    public static void load(){
        onLoad.each(Runnable::run);
        onLoad.clear();
        settings = new ModSettings();
        if(!headless) listener.add(modUI = new BLUI());
    }

    public static String modName(){
        return modInfo == null ? "no name" : modInfo.name;
    }


    public static void inspectBuilding(){

    }

    public static void addResearch(String researchName, UnlockableContent unlock){
        TechTree.TechNode node = new TechTree.TechNode(null, unlock, unlock.researchRequirements());
        TechTree.TechNode parent = TechTree.all.find((t) -> {
            return t.content.name.equals(researchName) || t.content.name.equals(fullName(researchName));
        });

        if(parent == null){
            showException(new IllegalArgumentException("Content '" + researchName + "' isn't in the tech tree, but '" + unlock.name + "' requires it to be researched."));
//            throw new IllegalArgumentException("Content '" + researchName + "' isn't in the tech tree, but '" + unlock.name + "' requires it to be researched.");
        }else{
            if(!parent.children.contains(node)){
                parent.children.add(node);
            }

            node.parent = parent;
        }
    }

    public static String fullName(String name){
        if(packSprites) return name;
        return Strings.format("@-@", modInfo == null ? "braindustry" : modInfo.name, name);
    }

    public static String getTranslateName(String name){
        return Strings.format("@.@", modInfo.name, name);
    }

    public static void showException(Exception exception){
        Log.err(exception);
        if(settings != null && !settings.debug()) return;
        try{
            Vars.ui.showException(Strings.format("@: error", modInfo.meta.displayName), exception);
        }catch(NullPointerException n){
            Events.on(EventType.ClientLoadEvent.class, event -> {
                BLUI.showExceptionDialog(Strings.format("@: error", modInfo == null ? null : modInfo.meta.displayName), exception);
            });
        }
    }

    public static void modLog(String text, Object... args){
        Log.info("[@] @", modInfo == null ? "test-java" : modInfo.name, Strings.format(text, args));
    }

    @Override
    protected void onLoad(Runnable runnable){
        onLoad.add(runnable);
    }

    @Override
    public void loadContent(){
//        TMItems.load();
//        ModStatusEffects.load();
//        ModLiquids.load();
//        ModGasses.load();
//        ModBullets.load();
//        ModUnitTypes.load();
        ByteLogicBlocks.load();
//        TMWeathers.load();
//        TMPlanets.load();
//        TMSectorPresets.load();
        ByteLogicTechTree.load();
    }

    @Override
    protected void showException(Throwable ex){
        BLVars.showException((Exception)ex);
    }

    public interface ThrowableRunnable{
        void run() throws Exception;
    }
}
