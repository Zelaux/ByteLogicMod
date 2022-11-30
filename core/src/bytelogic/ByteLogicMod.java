package bytelogic;

import arc.*;
import arc.graphics.g2d.*;
import bytelogic.async.*;
import bytelogic.game.*;
import bytelogic.ui.*;
import bytelogic.ui.dialogs.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.content.TechTree.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mma.*;
import mma.annotations.*;
import bytelogic.audio.*;
import bytelogic.gen.*;

import static mindustry.Vars.headless;
import static bytelogic.BLVars.*;

@ModAnnotations.ModAssetsAnnotation
public class ByteLogicMod extends MMAMod {
    public ByteLogicMod() {
        super();
        disableBlockOutline = true;
        CustomBuildSaving.register();
        modLog("Creating start");
//        TMEntityMapping.init();
//        TMCall.registerPackets();
        BLVars.load();
//        TMLogicIO.init();

        Events.on(ClientLoadEvent.class, (e) -> {
            ModAudio.reload();
        });

        BLGroups.init();
        Events.on(ResetEvent.class, e -> {
            BLGroups.clear();
        });
        Vars.asyncCore.processes.add(new BlockStateUpdater());
        modLog("Creating end");
    }

    public static TextureRegion getIcon() {
        if (modInfo == null || modInfo.iconTexture == null) return Core.atlas.find("nomap");
        return new TextureRegion(modInfo.iconTexture);
    }

    @Override
    protected void modContent(Content content) {
        super.modContent(content);
//        modLog("Content: @",content);
        if (content instanceof MappableContent && !headless) {
            BLContentRegions.loadRegions((MappableContent) content);
//            TMContentRegions.loadRegions((MappableContent) content);
        }
    }

    public void init() {
        if (!loaded) return;
        modLog("init start");
        BLIcons.load();
      /*  for(TechNode techNode : TechTree.all){
            techNode.content.alwaysUnlocked=true  ;
        }*/
//        Core.atlas.addRegion("logic-base",Core.atlas.find(fullName("logic-base")));
        if (!headless) {
            ModMetaDialogFinder.onNewListener(d -> {
                if (d instanceof ByteLogicModDialog) return;
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
        if (neededInit) listener.init();
        modLog("init end");
    }

    public void loadContent() {
        modInfo = Vars.mods.getMod(this.getClass());
        modLog("loadContent start");
        ModAudio.reload();
        if (!headless) {
            inTry(BLSounds::load);
            inTry(BLMusics::load);
        }
        super.loadContent();
        loaded = true;
        modLog("loadContent end");
    }
}
