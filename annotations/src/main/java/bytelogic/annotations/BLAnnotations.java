package bytelogic.annotations;

import java.lang.annotation.*;

public class BLAnnotations{

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface GenerateIconsClass{
    }
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface CustomSavingSerializers{
    }

}