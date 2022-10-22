package bytelogic.ui.dialogs;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.mod.Mods.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mma.*;

import java.lang.reflect.*;
import java.util.*;

import static mindustry.Vars.*;
import static mma.ModVars.*;

public class ByteLogicModDialog extends BaseDialog{
    public static final float oneFrameTime = 5f;
    private static final Runnable reinstaller;
    private static TextureRegion[] drawRegions = null;

    static{
        try{
            Method mod = ModsDialog.class.getDeclaredMethod("githubImportMod", String.class, boolean.class, String.class);
            mod.setAccessible(true);
            reinstaller = () -> {
                try{
                    mod.invoke(ui.mods, modInfo.getRepo(), modInfo.isJava(), null);
                }catch(IllegalAccessException | InvocationTargetException e){
                    throw new RuntimeException(e);
                }
            };
        }catch(NoSuchMethodException e){
            throw new RuntimeException(e);
        }
    }

    private float timerFlow = 0;
    private static float staticTimerFlow = 0;

    public ByteLogicModDialog(){
        super(modInfo.meta.displayName());
        initRegions();
        setup();
    }

    public static void initRegions(){
        if(drawRegions != null) return;
        Fi file = modInfo.root.child("sprites").child(("logo-info.properties"));
        ObjectMap<String, String> entries = new ObjectMap<>();
        PropertiesUtils.load(entries, file.reader());
        int size = Integer.parseInt(entries.get("size"));
        drawRegions = new TextureRegion[size];
        for(int i = 0; i < drawRegions.length; i++){
            drawRegions[i] = Core.atlas.find(fullName("logo-" + i));
        }
        final String ELEMENT_NAME = "BYTE_LOGIC_ANIMATED_ICON";
        Events.run(Trigger.update,()->{
            Dialog dialog = Core.scene.getDialog();

            if (dialog!= ui.mods){
                staticTimerFlow=0;
                return;
            }

            if(dialog.find(ELEMENT_NAME)!=null){
                return;
            }
            int index = 2;
            if(!mobile || Core.graphics.isPortrait()){
                index++;
            }
            Element element = dialog.cont.getChildren().get(index);
            if(!(element instanceof ScrollPane)){
                return;
            }
            Element widget = ((ScrollPane)element).getWidget();
            if(!(widget instanceof Table)){
                return;
            }

            SnapshotSeq<Element> children1 = ((Table)widget).getChildren();
            for(Element elm : children1){
                if (elm instanceof Button button){
                    Element element1 = button.getChildren().get(0);
                    if(element1 instanceof Table table){
                        if(table.getChildren().get(0) instanceof BorderImage borderImage){
                            boolean valid=false;
                            if (borderImage.getDrawable() instanceof TextureRegionDrawable drawable){
                                valid = modInfo.iconTexture==drawable.getRegion().texture;
                            }
                            if (!valid)continue;
//                            borderImage.borderColor=Pal.berylShot.cpy();
                            borderImage.name=ELEMENT_NAME;
                            borderImage.update(()->{
                                borderImage.setDrawable(
                                drawRegions[(int)(staticTimerFlow = Mathf.mod(staticTimerFlow + Time.delta / oneFrameTime, drawRegions.length))]
                                );
                            });
                        }
                    }
                }
            }

        });
    }

    private void setup(){
        LoadedMod mod = modInfo;
        this.addCloseButton();

        if(!mobile){
            this.buttons.button("@mods.openfolder", Icon.link, () -> Core.app.openFolder(mod.file.absolutePath()));
        }

        if(mod.getRepo() != null){
            boolean showImport = !mod.hasSteamID();
            this.buttons.button("@mods.github.open", Icon.link, () -> Core.app.openURI("https://github.com/" + mod.getRepo()));
            if(mobile && showImport) this.buttons.row();
            if(showImport) this.buttons.button("@mods.browser.reinstall", Icon.download, reinstaller);
        }

        this.cont.pane(desc -> {
            desc.center();
            desc.defaults().padTop(10).left();
            desc.image(() -> drawRegions[(int)(timerFlow = Mathf.mod(timerFlow + Time.delta / oneFrameTime, drawRegions.length))]).width(400f).scaling(Scaling.bounded);
            desc.row();
            desc.add("@editor.name").padRight(10).color(Color.gray).padTop(0);
            desc.row();
            desc.add(mod.meta.displayName()).growX().wrap().padTop(2);
            desc.row();
            if(mod.meta.author != null){
                desc.add("@editor.author").padRight(10).color(Color.gray);
                desc.row();
                desc.add(mod.meta.author).growX().wrap().padTop(2);
                desc.row();
            }
            if(mod.meta.description != null){
                desc.add("@editor.description").padRight(10).color(Color.gray).top();
                desc.row();
                desc.add(mod.meta.description).growX().wrap().padTop(2);
                desc.row();
            }

        }).width(400f);

        Seq<UnlockableContent> all = Seq.with(content.getContentMap()).<Content>flatten().select(c -> c.minfo.mod == mod && c instanceof UnlockableContent).as();
        if(all.any()){
            this.cont.row();
            this.cont.button("@mods.viewcontent", Icon.book, () -> {
                BaseDialog d = new BaseDialog(mod.meta.displayName());
                d.cont.pane(cs -> {
                    int i = 0;
                    for(UnlockableContent c : all){
                        cs.button(new TextureRegionDrawable(c.uiIcon), Styles.flati, iconMed, () -> {
                            ui.content.show(c);
                        }).size(50f).with(im -> {
                            var click = im.getClickListener();
                            im.update(() -> im.getImage().color.lerp(!click.isOver() ? Color.lightGray : Color.white, 0.4f * Time.delta));

                        }).tooltip(c.localizedName);

                        if(++i % (int)Math.min(Core.graphics.getWidth() / Scl.scl(110), 14) == 0) cs.row();
                    }
                }).grow();
                d.addCloseButton();
                d.show();
            }).size(300, 50).pad(4);
        }

        this.show();
    }
}
