package bytelogic.type;

import arc.*;
import arc.util.*;

public enum ByteLogicGateGroup{
    signalProviders,
    inputOutput,
    bitOperators,
    unaryOperators,
    binaryOperators,
    ;

    public String localized(){
        return Core.bundle.get("byte-logic-gate-group." + Strings.camelToKebab(name()), name());
    }
}
