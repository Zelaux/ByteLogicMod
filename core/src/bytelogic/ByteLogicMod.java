package bytelogic;

import arc.*;
import arc.graphics.g2d.*;
import bytelogic.async.*;
import bytelogic.audio.*;
import bytelogic.game.*;
import bytelogic.gen.*;
import bytelogic.ui.*;
import bytelogic.ui.dialogs.*;
import bytelogic.world.blocks.logic.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.world.*;
import mma.*;
import mma.annotations.*;

import static bytelogic.BLVars.*;
import static mindustry.Vars.headless;

@ModAnnotations.ModAssetsAnnotation
public class ByteLogicMod extends MMAMod{
    private static boolean registered = false;

    public ByteLogicMod(){
        super();
        registerMain();
        registered = true;
        disableBlockOutline = true;
        modLog("Creating start");
//        TMEntityMapping.init();
//        TMCall.registerPackets();
        BLVars.load();
//        TMLogicIO.init();

        Events.on(ClientLoadEvent.class, (e) -> {
            ModAudio.reload();
//            Vars.ui.content.show(ByteLogicBlocks.erekirBlocks.analyzer);
        });
        modLog("Creating end");
    }

    public static boolean registerSmall(){
        if(registered){
            return false;
        }
        registered = true;
        registerMain();
        ClientLauncher clientLauncher = (ClientLauncher)Core.app.getListeners().find(it -> it instanceof ClientLauncher);
        if(!headless) clientLauncher.add(new ApplicationListener(){
            @Override
            public void init(){
                for(Block block : Vars.content.blocks()){
                    if(block instanceof LogicBlock){
                        BLContentRegions.loadRegions(block);
                    }
                }
            }
        });


        Events.on(ClientLoadEvent.class, (e) -> {
            ModAudio.reload();
//            Vars.ui.content.show(ByteLogicBlocks.erekirBlocks.analyzer);
        });

        return true;
    }

    private static void registerMain(){
        CustomBuildSaving.register();

        BLGroups.init();
        Events.on(ResetEvent.class, e -> {
            BLGroups.clear();
        });
        Vars.asyncCore.processes.add(new BlockStateUpdater());
    }

    public static TextureRegion getIcon(){
        if(modInfo == null || modInfo.iconTexture == null) return Core.atlas.find("nomap");
        return new TextureRegion(modInfo.iconTexture);
    }

    @Override
    protected void modContent(Content content){
        super.modContent(content);
//        modLog("Content: @",content);
        if(content instanceof MappableContent && !headless){
            BLContentRegions.loadRegions((MappableContent)content);
//            TMContentRegions.loadRegions((MappableContent) content);
        }
    }

    public void init(){
        if(!loaded) return;
        modLog("init start");
        BLIcons.load();
      /*  for(TechNode techNode : TechTree.all){
            techNode.content.alwaysUnlocked=true  ;
        }*/
//        Core.atlas.addRegion("logic-base",Core.atlas.find(fullName("logic-base")));
        if(!headless){
            ModMetaDialogFinder.onNewListener(d -> {
                if(d instanceof ByteLogicModDialog) return;
                d.hide(null);
                new ByteLogicModDialog().show();
            });
            ByteLogicModDialog.initRegions();
        }
      /*  if (!headless){
            String prefix=ModVars.fullName("");
            ObjectMap<String, AtlasRegion> regionMap = Core.atlas.getRegionMap();
            Seq<String> names = regionMap.keys().toSeq().select(it -> it.startsWith(prefix));
            for(String name : names){
                regionMap.put()
            }
        }*/
        super.init();
        if(neededInit) listener.init();
        modLog("init end");
    }

    public void loadContent(){
        modInfo = Vars.mods.getMod(this.getClass());
        modLog("loadContent start");
        ModAudio.reload();
        if(!headless){
            inTry(BLSounds::load);
            inTry(BLMusics::load);
        }
        super.loadContent();
        loaded = true;
        modLog("loadContent end");
    }
}
