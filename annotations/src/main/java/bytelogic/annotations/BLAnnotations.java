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

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Serializable{
        String prefix() default "";
        boolean canCreateInstance() default true;

        Class<?> type() default Void.class;
    }


    @Retention(RetentionPolicy.SOURCE)
    public @interface RemoveFromCompilation{
    }
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    public @interface FinalField{
    }

    public @interface GenerateByteLogicGatesSerializer {
    }
}