package bytelogic.core;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.input.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import bytelogic.content.*;
import bytelogic.gen.*;
import bytelogic.ui.*;
import bytelogic.ui.dialogs.*;
import bytelogic.ui.fragments.*;
import bytelogic.world.blocks.logic.*;
import mindustry.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.*;
import mma.ui.dialogs.*;
import zelaux.arclib.ui.components.ComboBox.ComboBoxItem.*;

import static bytelogic.BLVars.inTry;
import static mindustry.Vars.headless;

public class BLUI extends mma.core.ModUI implements Disposable, ApplicationListener{
    public ModColorPicker colorPicker;
    public GuideDialog guideDialog;
    private boolean inited = false;

    public BLUI(){
        super();
        Vars.schematics = new SchematicsWrapper(Vars.schematics){
            @Nullable
            Planet findCampaignPlanet(){
                if(!Vars.state.isCampaign()){
                    return null;
                }
                Sector sector = Vars.state.getSector();
                if(sector == null){
                    return null;
                }
                return sector.planet;
            }

            @Override
            public Seq<BuildPlan> toPlans(Schematic schem, int x, int y){

                Planet planet = findCampaignPlanet();
                if(planet == null){
                    planet = findPlanetByEnv();
                }
                if(planet == null) return super.toPlans(schem, x, y);
                Planet staticPlanet = planet;
                ByteLogicBlocks currentPlanetBlocks = ByteLogicBlocks.byteLogicBlocks.find(it -> it.planet == staticPlanet);
                if(currentPlanetBlocks == null){
                    return super.toPlans(schem, x, y);
                }
                return schem.tiles.map(t -> {
                        Block tBlock = t.block;
                        if(tBlock instanceof LogicBlock block && block.byteLogicBlocks != null){
                            ByteLogicBlocks current = block.byteLogicBlocks;
                            int index = current.blocks.indexOf(block);
                            if(index >= 0 && index < currentPlanetBlocks.blocks.size){
                                tBlock = currentPlanetBlocks.blocks.get(index);
                            }
                        }
                        return new BuildPlan(t.x + x - schem.width / 2, t.y + y - schem.height / 2, t.rotation, tBlock, t.config).original(t.x, t.y, schem.width, schem.height);
                    })
                           .removeAll(s -> (!s.block.isVisible() && !(s.block instanceof CoreBlock)) || !s.block.unlockedNow()).sort(Structs.comparingInt(s -> -s.block.schematicPriority));
            }

            Planet findPlanetByEnv(){
                int env = Vars.state.rules.env;
                if(Vars.state.rules.hiddenBuildItems.isEmpty()) return null;
                return Vars.content.planets().find(it -> it.defaultEnv == env && it.solarSystem != it);
            }
        };

    }

    public static void showExceptionDialog(Throwable t){
        showExceptionDialog("", t);
    }

    public static Dialog getInfoDialog(String title, String subTitle, String message, Color lineColor){
        return new Dialog(title){{
            setFillParent(true);
            cont.margin(15.0F);
            cont.add(subTitle);
            cont.row();
            cont.image().width(300.0F).pad(2.0F).height(4.0F).color(lineColor);
            cont.row();
            cont.add(message).pad(2.0F).growX().wrap().get().setAlignment(1);
            cont.row();
            cont.button("@ok", this::hide).size(120.0F, 50.0F).pad(4.0F);
            closeOnBack();
        }};
    }

    public static void showExceptionDialog(final String text, final Throwable exc){
        new Dialog(""){{
            String message = Strings.getFinalMessage(exc);
            setFillParent(true);
            cont.margin(15.0F);
            cont.add("@error.title").colspan(2);
            cont.row();
            cont.image().width(300.0F).pad(2.0F).colspan(2).height(4.0F).color(Color.scarlet);
            cont.row();
            cont.add((text.startsWith("@") ? Core.bundle.get(text.substring(1)) : text) + (message == null ? "" : "\n[lightgray](" + message + ")")).colspan(2).wrap().growX().center().get().setAlignment(1);
            cont.row();
            Collapser col = new Collapser((base) -> {
                base.pane((t) -> {
                    t.margin(14.0F).add(Strings.neatError(exc)).color(Color.lightGray).left();
                });
            }, true);
            cont.button("@details", Styles.togglet, col::toggle).size(180.0F, 50.0F).checked((b) -> {
                return !col.isCollapsed();
            }).fillX().right();
            cont.button("@ok", this::hide).size(110.0F, 50.0F).fillX().left();
            cont.row();
            cont.add(col).colspan(2).pad(2.0F);
            closeOnBack();
        }}.show();
    }

    public static void showTextInput(String title, String text, String def, Cons<String> confirmed){
        showTextInput(title, text, 32, def, confirmed);
    }

    public static void showTextInput(String titleText, String text, int textLength, String def, Cons<String> confirmed){
        showTextInput(titleText, text, textLength, def, (t, c) -> {
            return true;
        }, confirmed);
    }

    public static void showTextInput(final String titleText, final String dtext, final int textLength, final String def, final TextField.TextFieldFilter filter, final Cons<String> confirmed){
        if(Vars.mobile){
            Core.input.getTextInput(new Input.TextInput(){
                {
                    title = titleText.startsWith("@") ? Core.bundle.get(titleText.substring(1)) : titleText;
                    text = def;
                    numeric = filter == TextField.TextFieldFilter.digitsOnly;
//                    numeric = inumeric;
                    maxLength = textLength;
                    accepted = confirmed;
                }
            });
        }else{
            new Dialog(titleText){{
                cont.margin(30.0F).add(dtext).padRight(6.0F);
                TextField field = cont.field(def, (t) -> {
                }).size(330.0F, 50.0F).get();
                field.setFilter((f, c) -> {
                    return field.getText().length() < textLength && filter.acceptChar(f, c);
                });
                buttons.defaults().size(120.0F, 54.0F).pad(4.0F);
                buttons.button("@cancel", this::hide);
                buttons.button("@ok", () -> {
                    confirmed.get(field.getText());
                    hide();
                }).disabled((b) -> {
                    return field.getText().isEmpty();
                });
                keyDown(KeyCode.enter, () -> {
                    String text = field.getText();
                    if(!text.isEmpty()){
                        confirmed.get(text);
                        hide();
                    }

                });
                keyDown(KeyCode.escape, this::hide);
                keyDown(KeyCode.back, this::hide);
                show();
                Core.scene.setKeyboardFocus(field);
                field.setCursorPosition(def.length());
            }};
        }

    }

    @Override
    public void init(){
        if(headless) return;
        if(inited) throw new IllegalStateException("BLUI already inited");
        inited = true;

        inTry(BLTex::load);
        inTry(ModStyles::load);
        inTry(ModHudFragment::init);

        colorPicker = new ModColorPicker();
        guideDialog = new GuideDialog();

        Vars.ui.settings.graphics.checkPref(SettingManager.enabledLogicNetSelection.key,SettingManager.enabledLogicNetSelection.def(),bool->{

        });
//        radiusRenderer =new RadiusRenderer();
    }

    @Override
    public void dispose(){
    }

    @Override
    public void update(){
        if(!inited) return;
    }

}
