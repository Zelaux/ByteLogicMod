package bytelogic.ui.dialogs;

import bytelogic.type.ConnectionSettings.*;
import mindustry.*;

public class WireDescriptorEditDialog{

    public static void showDialog(WireDescriptor wireDescriptor){
        Vars.ui.showTextInput("@edit", "@name", wireDescriptor.name == null ? "" : wireDescriptor.name, newName -> {
            wireDescriptor.name = newName.isEmpty() ? null : newName;
        });
    }
}
