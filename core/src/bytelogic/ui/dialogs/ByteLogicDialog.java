package bytelogic.ui.dialogs;

import arc.func.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import bytelogic.type.*;
import bytelogic.type.ByteLogicOperators.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mma.ui.tiledStructures.*;
import mma.ui.tiledStructures.TiledStructures.*;
import zelaux.arclib.ui.utils.*;

import java.lang.annotation.*;
import java.lang.reflect.*;

public class ByteLogicDialog extends TiledStructuresDialog{
    public static final Seq<Prov<ByteLogicGate>> allByteLogicGates;

    static{
        allByteLogicGates = ByteLogicOperators.getProvidersAsSequence().as();
        TiledStructures tiledStructures = new TiledStructures(new Seq<>());
        ByteLogicOperators.registerAll(tiledStructures);
        setGlobalProvider(ByteLogicGate.class, (type, cons) -> new BaseDialog("@add"){{
            cont.pane(p -> {
                p.background(Tex.button);
                p.marginRight(14f);
                p.defaults().size(195f, 56f);

                int i = 0;
                ObjectMap<ByteLogicGateGroup, Seq<ByteLogicGate>> keyMap = new ObjectMap<>();
                for(Prov<ByteLogicGate> gate : allByteLogicGates){
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
                /*for(var gen : allByteLogicGates){
                    var obj = gen.get();
                    p.button(obj.typeName(), Styles.flatt, () -> {
                        cons.get(obj);
                        hide();
                    }).with(Table::left).get().getLabelCell().growX().left().padLeft(5f).labelAlign(Align.left);

                    if(++i % 3 == 0) p.row();
                }*/
            }).scrollX(false);

            addCloseButton();
            show();
        }});

    }

    public ByteLogicDialog(){
        super("@byte-logic", ByteLogicGate.class);
        settings.updateStructuresAfterConfig = false;
    }

    public static <T> FieldInterpreter<T> byteLogicInterpreter(){
        return (instance, cont, name, type, field, remover, indexer, get, set) -> cont.table(main -> {
            main.margin(0f, 10f, 0f, 10f);
            var header = main.table(Tex.button, t -> {
                t.left();
                t.margin(10f);

                if(name.length() > 0) t.add(name + ":").color(Pal.accent);
                t.add().growX();

                Cell<ImageButton> remove = null;
                if(remover != null) remove = t.button(Icon.trash, Styles.emptyi, remover).fill();
                if(indexer != null){
                    if(remove != null) remove.padRight(4f);
                    t.button(Icon.upOpen, Styles.emptyi, () -> indexer.get(true)).fill().padRight(4f);
                    t.button(Icon.downOpen, Styles.emptyi, () -> indexer.get(false)).fill();
                }
            }).growX().height(46f).pad(0f, -10f, -0f, -10f).get();

            main.row().table(Tex.button, t -> {
                t.left();
                t.top().margin(10f).marginTop(20f);

                t.defaults().minHeight(40f).left();
                var obj = get.get();

                int i = 0;
                boolean shouldNextRow = true;
                for(var e : JsonIO.json.getFields(type.raw).values()){
                    if(i++ > 0 && shouldNextRow) t.row();
                    shouldNextRow = true;

                    var f = e.field;
                    var ft = f.getType();
                    int mods = f.getModifiers();

                    if(!Modifier.isPublic(mods) || (Modifier.isFinal(mods) && (
                        String.class.isAssignableFrom(ft) ||
                            unbox(ft).isPrimitive()
                    )) || f.getAnnotation(CodeEdit.class) != null) continue;

                    var anno = Structs.find(f.getDeclaredAnnotations(), a -> instance.hasInterpreter(a.annotationType(), ft));
                    var noRow = f.getAnnotation(DoNotAddRow.class);
                    if(noRow != null) shouldNextRow = false;
                    instance.getInterpreter(anno == null ? Override.class : anno.annotationType(), ft).build(instance,
                        t, f.getName(),
                        new TypeInfo(f), f, null,
                        null,
                        () -> Reflect.get(obj, f),
                        Modifier.isFinal(mods) ? res -> {
                        } : res -> Reflect.set(obj, f, res));
                }
            }).padTop(-10f).growX().fillY();

            header.toFront();
        }).growX().fillY().pad(4f).colspan(2);
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface DoNotAddRow{

    }
}
