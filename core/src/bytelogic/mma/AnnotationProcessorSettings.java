package bytelogic.mma;

import bytelogic.annotations.BLAnnotations;
import mma.annotations.ModAnnotations.*;
import bytelogic.*;

@AnnotationSettings(
        classPrefix = "BL",
        assetsPath = "core/assets",
        assetsRawPath = "core/assets-raw"
)
@BLAnnotations.GenerateIconsClass
@MainClass(ByteLogicMod.class)
class AnnotationProcessorSettings {
}
