package bytelogic.ui;

import arc.graphics.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import mindustry.gen.*;
import mindustry.ui.*;
import bytelogic.gen.*;

import static mindustry.gen.Tex.*;
import static bytelogic.gen.BLTex.*;

public class ModStyles{
    private static final Color black = new Color(0f, 0f, 0f, 1f), black9 = new Color(0f, 0f, 0f, 0.9f), black8 = new Color(0f, 0f, 0f, 0.8f), black6 = new Color(0f, 0f, 0f, 0.6f), black5 = new Color(0f, 0f, 0f, 0.5f), black3 = new Color(0f, 0f, 0f, 0.3f), none = new Color(0f, 0f, 0f, 0f);

    public static ImageButton.ImageButtonStyle buttonSquarei, alphai, buttonPanei;
    public static TextButton.TextButtonStyle buttonEdge3t, buttonPanet, buttonPaneTopt, buttonPaneBottomt;
    public static Button.ButtonStyle buttonColor;

    public static void load(){
        buttonColor = new Button.ButtonStyle(Styles.defaultb){{
            over = BLTex.buttonColorOver;
            down = BLTex.buttonColorDown;
            up = disabled = BLTex.buttonColor;
        }};
        buttonEdge3t = new TextButton.TextButtonStyle(Styles.defaultt){{
            over = BLTex.buttonEdge3Over;
            down = BLTex.buttonEdge3Down;
            up = disabled = Tex.buttonEdge3;
        }};
        buttonPanet = new TextButton.TextButtonStyle(Styles.defaultt){{
            over = BLTex.buttonPaneOver;
            down = BLTex.buttonPaneDown;
            up = disabled = pane;
            checked = BLTex.buttonPaneOver;
        }};
        buttonPaneTopt = new TextButton.TextButtonStyle(Styles.defaultt){{
            over = BLTex.buttonPaneTopOver;
            down = BLTex.buttonPaneTopDown;
            up = disabled = BLTex.buttonPaneTop;
        }};
        buttonPaneBottomt = new TextButton.TextButtonStyle(Styles.defaultt){{
            over = BLTex.buttonPaneBottomOver;
            down = BLTex.buttonPaneBottomDown;
            up = disabled = BLTex.buttonPaneBottom;
        }};
        buttonSquarei = new ImageButton.ImageButtonStyle(){{
            imageDisabledColor = Color.gray;
            imageUpColor = Color.white;
            over = buttonSquareOver;
            disabled = buttonDisabled;
            down = buttonSquareDown;
            up = buttonSquare;
        }};
        alphai = new ImageButton.ImageButtonStyle(){{
            imageDisabledColor = Color.gray;
            imageUpColor = Color.white;
            over = ((ScaledNinePatchDrawable)buttonDisabled).tint(black3);
            disabled = ((ScaledNinePatchDrawable)buttonDisabled).tint(none);
            down = (buttonSquareDown).tint(none);
            up = (buttonSquare).tint(none);
        }};
        buttonPanei = new ImageButton.ImageButtonStyle(Styles.defaulti){{
            over = BLTex.buttonPaneOver;
            down = BLTex.buttonPaneDown;
            up = disabled = pane;
            checked = BLTex.buttonPaneOver;
        }};
    }
}
