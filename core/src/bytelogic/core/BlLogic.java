package bytelogic.core;

import arc.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import bytelogic.gen.*;
import bytelogic.tools.*;
import bytelogic.world.blocks.logic.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mma.utils.*;

import static mindustry.Vars.content;

public class BlLogic implements ApplicationListener{
    private static final EventReceiver blLogicTileTapped = new EventReceiver("bl-logic-tile-tapped");
    Bits wasConfigurable;
    Seq<Block> logicBlocks;
    boolean wasLogicNet = false;

    private static void rebuildLogicNet(){
        ByteLogicBuildingc first = LogicNet.first();
        if(first != null) LogicNet.set(first);
    }

    @Override
    public void init(){
//        Seq<UnlockableContent> all = Seq.with(content.getContentMap()).<Content>flatten().select(c -> c.minfo.mod == mod && c instanceof UnlockableContent).as();
        logicBlocks = content.blocks().select(c -> c instanceof LogicBlockI).as();
        wasConfigurable = new Bits(logicBlocks.size);
        for(int i = 0; i < logicBlocks.size; i++){
            wasConfigurable.set(i, logicBlocks.get(i).configurable);
        }
        blLogicTileTapped.post(event -> {
            ByteLogicBuildingc build = event.getParameter("build");
            if(SettingManager.enabledLogicNetSelection.get()){
                if(LogicNet.first() == build){
                    LogicNet.reset();
                }else{
                    LogicNet.set(build);
                }
            }
        });
        Events.run(TileChangeEvent.class, BlLogic::rebuildLogicNet);
        Events.run(ConfigEvent.class, BlLogic::rebuildLogicNet);
        Events.run(BuildDamageEvent.class, BlLogic::rebuildLogicNet);
        Events.run(Trigger.draw, () -> {
            if(SettingManager.enabledLogicNetSelection.get()){
                Draw.draw(Layer.overlayUI, LogicNet::draw);
            }
        });
    }

    void disableConfigs(){
        for(Block block : logicBlocks){
            block.configurable = false;
        }
    }

    void restoreConfigs(){
        for(int i = 0; i < logicBlocks.size; i++){
            logicBlocks.get(i).configurable = wasConfigurable.get(i);
        }
    }

    @Override
    public void update(){
        if(logicBlocks == null) return;
        boolean currentLogicNet = SettingManager.enabledLogicNetSelection.get();
        if(wasLogicNet != currentLogicNet){
            if(currentLogicNet){
                disableConfigs();
            }else{
                restoreConfigs();
                LogicNet.reset();
            }
            wasLogicNet = currentLogicNet;
        }else if(wasLogicNet){
            LogicNet.update();
        }
    }
}
