package bytelogic.tools;

import arc.Core;
import arc.func.Boolf;
import arc.func.Func;
import arc.math.Interp;
import arc.scene.Element;
import arc.scene.actions.Actions;
import arc.scene.style.Drawable;
import arc.scene.ui.Button;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.Align;
import mindustry.Vars;
import mindustry.ui.Styles;

import java.io.PrintWriter;
import java.io.StringWriter;

import static mindustry.Vars.mobile;
import static mindustry.Vars.ui;

public class MenuButtons {
    private static Button currentMenu;
    private static Table submenu, t;

    static {
//        init();
    }

    public static void init() {
        Table container = (Table) Vars.ui.menuGroup.getChildren().find(el -> "menu container".equals(el.name));
        t = container.getCells().find(cell -> "buttons".equals(cell.get().name)).getTable();
        submenu = container.getCells().find(cell -> "submenu".equals(cell.get().name)).getTable();
    }

    private static void fadeInMenu() {
        submenu.clearActions();
        submenu.actions(Actions.alpha(1f, 0.15f, Interp.fade));
    }

    private static void fadeOutMenu() {
        //nothing to fade out
        if (submenu.getChildren().isEmpty()) {
            return;
        }

        submenu.clearActions();
        submenu.actions(Actions.alpha(1f), Actions.alpha(0f, 0.2f, Interp.fade), Actions.run(() -> submenu.clearChildren()));
    }
    private static String parseException(Throwable e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    public static void menuButton(MenuButton... menuButtons) {
        Table container=null;
        WidgetGroup widgetGroup=null;
        if (mobile)return;
        try {
            currentMenu = null;
            widgetGroup = (WidgetGroup) ui.menuGroup.getChildren().first();
            if (!mobile){
                container = (Table) widgetGroup.getChildren().find(el -> "menu container".equals(el.name));
                Func<String, Boolf<Cell>> find = (name) -> (cell -> cell.get() != null && name.equals(cell.get().name));
                t = (Table) container.getCells().find(find.get("buttons")).get();
                submenu = (Table) container.getCells().find(find.get("submenu")).get();
            } else {
                return;
             /*   container = (Table) widgetGroup.getChildren().find(el -> "menu container".equals(el.name));
                Func<String, Boolf<Cell>> find = (name) -> (cell -> cell.get() != null && name.equals(cell.get().name));
                t = (Table) container.getCells().find(find.get("buttons")).get();
                submenu = (Table) container.getCells().find(find.get("submenu")).get();*/
            }
            buttonsDesktop(t, menuButtons);
        } catch (Exception exception) {
//            String format = Strings.format("container: <<@>>\nwidgetGroup: <<@>>\nexception: <<@>>", container, widgetGroup, parseException(exception));

        }
    }

    private static void buttonsDesktop(Table t, MenuButton... menuButtons) {
        for (MenuButton b : menuButtons) {
            if (b == null) continue;
            Button[] out = {null};
            out[0] = t.button(b.text, b.icon, Styles.flatToggleMenut, () -> {

                if (currentMenu == out[0]) {
                    currentMenu = null;
                    checkNext:
                    {
                        for (Element child : t.getChildren()) {
                            if (out[0] != child && child instanceof Button && ((Button) child).isChecked()) {
                                break checkNext;
                            }
                        }
                        fadeOutMenu();
                    }
                } else {
                    for (Element child : t.getChildren()) {
                        if (out[0] != child && child instanceof Button && ((Button) child).isChecked()) {
                            child.fireClick();
                        }
                    }
                    if (b.submenu != null) {
                        currentMenu = out[0];
                        submenu.clearChildren();
                        fadeInMenu();
                        //correctly offset the button
                        submenu.add().height((Core.graphics.getHeight() - Core.scene.marginTop - Core.scene.marginBottom - out[0].getY(Align.topLeft)) / Scl.scl(1f));
                        submenu.row();
                        buttonsDesktop(submenu, b.submenu);
                    } else {
                        if (b.closeAfterPress) {
                            currentMenu = null;
                            fadeOutMenu();
                        }
                        b.runnable.run();
                    }
                }
            }).marginLeft(11f).get();
            out[0].update(() -> {
                out[0].setChecked(currentMenu == out[0]);

                if (out[0].isChecked()) {
                    for (Element child : t.getChildren()) {
                        if (child != out[0] && child instanceof Button && ((Button) child).isChecked()) {
                            out[0].fireClick();
                            break;
                        }
                    }
                }
            });
            t.row();
        }
    }

    public static class MenuButtonUnClose extends MenuButton {

        public MenuButtonUnClose(String text, Drawable icon, Runnable runnable) {
            super(text, icon, runnable);
            closeAfterPress = false;
        }

        public MenuButtonUnClose(String text, Drawable icon, MenuButton... buttons) {
            super(text, icon, buttons);
            closeAfterPress = false;
        }
    }

    public static class MenuButton {
        final Drawable icon;
        final String text;
        final Runnable runnable;
        final MenuButton[] submenu;
        boolean closeAfterPress = true;

        public MenuButton(String text, Drawable icon, Runnable runnable) {
            this.icon = icon;
            this.text = text;
            this.runnable = runnable;
            this.submenu = null;
        }

        public MenuButton(String text, Drawable icon, MenuButton... buttons) {
            this.icon = icon;
            this.text = text;
            this.runnable = () -> {
            };
            this.submenu = buttons;
        }

        public MenuButton closeAfterPress(boolean closeAfterPress) {
            this.closeAfterPress = closeAfterPress;
            return this;
        }
    }
}

