package bytelogic;

import arc.*;
import arc.graphics.g2d.*;
import bytelogic.async.*;
import bytelogic.audio.*;
import bytelogic.content.*;
import bytelogic.game.*;
import bytelogic.gen.*;
import bytelogic.ui.*;
import bytelogic.ui.dialogs.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mma.*;
import mma.annotations.*;

import static bytelogic.BLVars.*;
import static mindustry.Vars.headless;

@ModAnnotations.ModAssetsAnnotation
public class ByteLogicMod extends MMAMod{
    public ByteLogicMod(){
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
//            Vars.ui.content.show(ByteLogicBlocks.erekirBlocks.analyzer);
//            modUI.guideDialog.show();
            /*new BaseDialog("test-dialog"){{
                World world = new World();
                world.resize(3, 3).each((x, y) -> {
                    world.tiles.set(x, y, new Tile(x, y, Blocks.metalFloor, Blocks.air, Blocks.air));
                });
                WorldContext context = new WorldContext(world);

                ByteLogicBlocks blocks = ByteLogicBlocks.erekirBlocks;

                context.inContext(builds -> {
                    world.tile(0, 0).setBlock(blocks.relay, Team.sharded, 1);
                    world.tile(0, 1).setBlock(blocks.relay, Team.sharded, 0);
                    world.tile(1, 1).setBlock(blocks.relay, Team.sharded, 3);
                    world.tile(1, 0).setBlock(blocks.notGate, Team.sharded, 2);

                    world.tile(1, 0).build.<UnaryLogicBuild>as().inputType = 2;


                    world.tile(2, 0).setBlock(Blocks.itemSource, Team.sharded);
                    world.tile(2, 0).build.<ItemSourceBuild>as().outputItem = Items.copper;

                    world.tile(2, 1).setBlock(Blocks.conveyor, Team.sharded, 1);
                    world.tile(2, 2).setBlock(Blocks.itemVoid, Team.sharded);
                });
                cont.add(new WorldElement(context));
                addCloseListener();
            }}.show();*/
        });

        BLGroups.init();
        Events.on(ResetEvent.class, e -> {
            BLGroups.clear();
        });
        Vars.asyncCore.processes.add(new BlockStateUpdater());
        modLog("Creating end");
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
