package bytelogic.event;

import bytelogic.gen.ByteLogicBuildingc;
import lombok.AllArgsConstructor;
import mindustry.gen.Building;
@AllArgsConstructor
public class ByteLogicTapEvent {
    public Building build;
    public ByteLogicBuildingc build(){
return build.as();
    }
}
