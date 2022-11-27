package bytelogic.mma;

import bytelogic.*;
import bytelogic.annotations.*;
import mma.annotations.ModAnnotations.*;

@AnnotationSettings(
classPrefix = "BL",
assetsPath = "core/assets",
assetsRawPath = "core/assets-raw",
rootPackage = "bytelogic"
)
@RootDirectoryPath(rootDirectoryPath = "../")
@BLAnnotations.GenerateIconsClass
@MainClass(ByteLogicMod.class)
class AnnotationProcessorSettings{
}
