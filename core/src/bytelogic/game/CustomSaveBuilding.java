package bytelogic.game;

import arc.util.io.*;

public interface CustomSaveBuilding{
    void customWrite(Writes write);

    void customRead(Reads read);

    short customVersion();
}
