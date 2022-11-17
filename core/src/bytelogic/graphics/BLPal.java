package bytelogic.graphics;

import arc.graphics.Color;
import mindustry.graphics.Pal;

public class BLPal {
    public static final Color
            positiveSignalColor = Pal.accent.cpy(),
            zeroSignalColor = Color.white.cpy(),
            negativeSignalColor = Pal.remove.cpy();
    public static final Color
            positiveSignalBarColor = Pal.accent.cpy(),
            zeroSignalBarColor = Color.darkGray.cpy(),
            negativeSignalBarColor = Pal.remove.cpy();
}
