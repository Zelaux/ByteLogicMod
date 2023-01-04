package bytelogic.ui.dialogs;

import arc.func.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import bytelogic.type.byteGates.*;
import bytelogic.type.byteGates.ByteLogicOperators.*;
import bytelogic.type.graphicsGates.*;
import bytelogic.type.graphicsGates.GraphicsOperators.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mma.ui.tiledStructures.*;
import zelaux.arclib.ui.utils.*;

public class GraphicsLogicDialog extends BaseTiledStructuresDialog<ByteLogicGate>{
    public static final Seq<Prov<ByteLogicGate>> allGraphicsGates;

    static{

        allGraphicsGates=ByteLogicOperators.getProvidersAsSequence().as();
        allGraphicsGates.removeAll(it->!it.get().canUseInGraphics());
        allGraphicsGates.addAll(GraphicsOperators.getProvidersAsSequence().as());
        TiledStructures tiledStructures = new TiledStructures(new Seq<>());
        ByteLogicOperators.registerAll(tiledStructures);
        GraphicsOperators.registerAll(tiledStructures);
        tiledStructures.allObjectiveTypes.removeAll(it->!((ByteLogicGate)it.get()).canUseInGraphics());
        setGlobalProvider(ByteLogicGate.class, (type, cons) -> new BaseDialog("@add"){{
            cont.pane(p -> {
                p.background(Tex.button);
                p.marginRight(14f);
                p.defaults().size(195f, 56f);

                int i = 0;
                ObjectMap<ByteLogicGateGroup, Seq<ByteLogicGate>> keyMap = new ObjectMap<>();
                for(Prov<ByteLogicGate> gate : allGraphicsGates){
                    ByteLogicGate logicGate = gate.get();
                    keyMap.get(logicGate.group(), Seq::new).add(logicGate);
                }
                Seq<ByteLogicGateGroup> groups = keyMap.keys().toSeq().sort();

                for(ByteLogicGateGroup group : groups){
                    Seq<ByteLogicGate> gates = keyMap.get(group);
                    i = 0;
                    p.table(title -> {
                        Separators.horizontalSeparator(title, Pal.accent);
                        title.add(group.localized());
                        Separators.horizontalSeparator(title, Pal.accent);
                    }).fillX().colspan(3).row();
                    for(ByteLogicGate obj : gates){
                        p.button(obj.typeName(), Styles.flatt, () -> {
                            cons.get(obj);
                            hide();
                        }).with(Table::left).get().getLabelCell().growX().left().padLeft(5f).labelAlign(Align.left);

                        if(++i % 3 == 0) p.row();
                    }
                    p.row();
                }
            }).scrollX(false);

            addCloseButton();
            show();
        }});

    }

    public GraphicsLogicDialog(){
        super("@graphics-logic", ByteLogicGate.class, allGraphicsGates);
    }

}
