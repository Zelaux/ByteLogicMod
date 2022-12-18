package bytelogic.tools;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.TextField.*;
import arc.scene.ui.layout.*;
import arc.scene.utils.*;
import arc.util.*;
import bytelogic.ui.tooltips.*;
import kotlin.Unit;
import kotlin.jvm.internal.Ref.*;
import mindustry.gen.*;
import mindustry.ui.*;
import org.jetbrains.annotations.Nullable;

public class UIUtils{

    public static String wrapText(String originalString, Font font, float maxWidth){

        GlyphLayout obtain = GlyphLayout.obtain();

        obtain.setText(font, originalString);
        if(obtain.width <= maxWidth){
            obtain.free();
            return originalString;
        }
        String[] words = originalString.split(" ");
        StringBuilder builder = new StringBuilder();
        int wordIndex = 0;
        while(wordIndex < words.length){
            builder.append(words[wordIndex]);
            if(wordIndex + 1 == words.length){
                break;
            }
            obtain.setText(font, builder + " " + words[wordIndex + 1]);
            if(obtain.width <= maxWidth){
                builder.append(" ");
            }else{
                builder.append("\n");
            }
            wordIndex++;
        }
        obtain.free();
        return builder.toString();
    }

    @Nullable
    public static Element hovered(Boolf<Element> validator){
        Element e = Core.scene.hit(Core.input.mouseX(), Core.input.mouseY(), true);
        if(e != null){
            while(e != null && !validator.get(e)){
                e = e.parent;
            }
        }
        return e;
        /*
        if(e == null || isDescendantOf(e)) return null;
        return (StatementElem)e;*/
    }
    public static <T extends Element> T hitChild(Group group,float stageX,float stageY,@Nullable Boolf<Element> filter){
        //noinspection unchecked
        return (T)group.getChildren().find(it -> {
            if(filter!=null && !filter.get(it)){
                return false;
            }

            it.stageToLocalCoordinates(Tmp.v1.set(stageX,stageY));
            return it.hit(Tmp.v1.x, Tmp.v1.y, false) != null;
        });
    }
    public static Table defaultTooltipTableSetup(Table table){
        return table.background(Styles.black6).margin(4f);
    }

    public static Cell<TextField> rangedNumberField(Table table, @Nullable String name, int defaultValue, int minValue, int maxValue, Intc consumer){
        return rangedNumberField(table, name == null ? null : () -> Core.bundle.get(name), defaultValue, minValue, maxValue, consumer);
    }

    public static Cell<TextField> rangedNumberField(Table table, @Nullable Prov<String> nameProv, int defaultValue, int minValue, int maxValue, Intc consumer){
        ObjectRef<TextField> fieldRef = new ObjectRef<>();
        ObjectRef<String> tooltipText = new ObjectRef<>();
        if(nameProv != null){
            table.label(nameProv::get);
        }
        return table.field(defaultValue + "", TextFieldFilter.digitsOnly, text -> {
            if(!Strings.canParseInt(text)){
                fieldRef.element.color.set(Color.scarlet);
                tooltipText.element = Core.bundle.get("too-big-number");
                return;
            }
            int number = Strings.parseInt(text);
            if(number > maxValue || number < minValue){
                fieldRef.element.color.set(Color.scarlet);
                tooltipText.element = Core.bundle.format("number-out-of-range", minValue, maxValue);
                return;
            }
            fieldRef.element.color.set(Color.white);
            tooltipText.element = null;
            consumer.get(number);
        }).with(i -> fieldRef.element = i).tooltip(t -> {
            t.visible(() -> tooltipText.element != null);
            t.background(Styles.black6).margin(4f).label(() -> tooltipText.element);
        });
    }

    public static Cell<TextField> rangedFloatNumberField(Table table, @Nullable String name, float defaultValue, float minValue, float maxValue, Floatc consumer){
        return rangedFloatNumberField(table, name == null ? null : () -> Core.bundle.get(name), defaultValue, minValue, maxValue, consumer);
    }

    public static Cell<TextField> rangedFloatNumberField(Table table, @Nullable Prov<String> nameProv, float defaultValue, float minValue, float maxValue, Floatc consumer){
        ObjectRef<TextField> fieldRef = new ObjectRef<>();
        ObjectRef<String> tooltipText = new ObjectRef<>();
        if(nameProv != null){
            table.label(nameProv::get);
        }
        return table.field(defaultValue % 1f == 0 ? ((int)defaultValue) + "" : defaultValue + "", TextFieldFilter.floatsOnly, text -> {
            if(!Strings.canParseFloat(text)){
                fieldRef.element.color.set(Color.scarlet);
                tooltipText.element = Core.bundle.get("too-big-number");
                return;
            }
            float number = Strings.parseFloat(text);
            if(number > maxValue || number < minValue){
                fieldRef.element.color.set(Color.scarlet);
                tooltipText.element = Core.bundle.format("number-out-of-range", minValue, maxValue);
                return;
            }
            fieldRef.element.color.set(Color.white);
            tooltipText.element = null;
            consumer.get(number);
        }).with(i -> fieldRef.element = i).tooltip(t -> {
            t.visible(() -> tooltipText.element != null);
            t.background(Styles.black6).margin(4f).label(() -> tooltipText.element);
        });
    }

    public static int invertAlign(int align){
        int result = align & Align.center;

        if((align & Align.left) != 0){
            result |= Align.right;
        }
        if((align & Align.right) != 0){
            result |= Align.left;
        }
        if((align & Align.top) != 0){
            result |= Align.bottom;
        }
        if((align & Align.bottom) != 0){
            result |= Align.top;
        }
        return result;
    }


    public static <T extends Element> Cell<T> changeableTooltip(Table table, T element, ObjectRef<String> tooltipText){
        return table.add(element).tooltip(t -> {
            t.background(Styles.black6).margin(4f).label(() -> tooltipText.element).visible(() -> tooltipText.element != null);
        });
    }

    public static <T extends Element> Cell<T> changeableSideTooltip(Table table, int align, T element, ObjectRef<String> tooltipText){
        return table.add(element).self(cell -> LeftSideTooltipsKt.tooltipSide(cell, align, t -> {
            t.visible(() -> tooltipText.element != null);
            t.background(Styles.black6).margin(4f).label(() -> tooltipText.element);
            return Unit.INSTANCE;
        }));
    }

    public static TextField uniqueField(String def, String duplicateKey, Cons<String> listener, Boolf<String> validator, ObjectRef<String> tooltipText){
        String duplicateMessage = Core.bundle.get(duplicateKey);
        TextField textField = Elem.newField(def, listener);
        textField.setValidator(t -> tooltipText.element == null);
        textField.update(() -> {

            tooltipText.element = validator.get(textField.getText()) ? null : duplicateMessage;
        });
        return textField;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static Cell<Image> verticalSeparator(Table table, Color color){
        return table.image(Tex.whiteui, color).growY().width(3f);
    }

    public static Cell<Image> horizontalSeparator(Table table, Color color){
        return table.image(Tex.whiteui, color).growX().height(3f);
    }

    public static float getX(float x, float width, int align){
        float offset = 0;
        if((align & Align.right) != 0){
            offset = width;
        }
        if((align & Align.center) != 0){
            offset = width / 2f;
        }
        return x + offset;
    }

    public static float getY(float y, float height, int align){
        float offset = 0;
        if((align & Align.top) != 0){
            offset = height;
        }
        if((align & Align.center) != 0){
            offset = height / 2f;
        }
        return y + offset;
    }

    public static void replaceClickListener(Button button, ClickListener newListener){
        button.removeListener(button.getClickListener());

        Reflect.set(Button.class, button, "clickListener", newListener);
        button.addListener(newListener);

    }
}
