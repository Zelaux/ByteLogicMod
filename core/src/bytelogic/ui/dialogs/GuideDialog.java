package bytelogic.ui.dialogs;

import arc.graphics.g2d.*;
import bytelogic.ui.guide.*;
import mindustry.ui.dialogs.*;

public class GuideDialog extends BaseDialog{
    public GuideDialog(){
        super("guide-dialog");
        resized(this::setup);
//        buttons.remove();
        setup();
        addCloseButton();
        addCloseListener();
    }

    private void setup(){
        cont.clearChildren();
//        getCell(cont).grow();
        cont.fill(DefaultGuideTabs.rootTab.getPageBuilder()::invoke);
    }
}
