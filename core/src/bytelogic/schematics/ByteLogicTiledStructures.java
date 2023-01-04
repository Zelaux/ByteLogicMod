package bytelogic.schematics;

import arc.func.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import bytelogic.type.byteGates.ByteLogicOperators.*;
import mindustry.io.*;
import mma.ui.tiledStructures.*;
import mma.ui.tiledStructures.TiledStructures.*;
import mma.ui.tiledStructures.TiledStructures.TiledStructure.*;

public class ByteLogicTiledStructures extends TiledStructures{
    public ByteLogicTiledStructures(Seq<Prov<? extends ByteLogicGate>> allObjectiveTypes){
        super(allObjectiveTypes.as());
    }
    public void set(Seq<Prov<? extends ByteLogicGate>> allObjectiveTypes){
        this.allObjectiveTypes.set(allObjectiveTypes);
    }
    public void write(Writes write){
        write.i(0);//version
        //region all
        write.i(all.size);
        Seq<ByteLogicGate> gates = all.as();
        for(ByteLogicGate gate : gates){
            write.str(gate.name());
            gate.write(write);
        }
        //endregion
        //region wires
        write.i(gates.count(it->it.inputWires.any()));
        for(int i = 0; i < gates.size; i++){

            ByteLogicGate root = gates.get(i);
            if (root.inputWires.isEmpty())continue;
            write.i(i);
            write.i(root.inputWires.count(it->gates.indexOf(it.obj)!=-1));
            for(ConnectionWire<ByteLogicGate> inputWire : root.inputWires){
                int objIndex = gates.indexOf(inputWire.obj);
                if(objIndex==-1)continue;
                write.i(objIndex);
                write.i(inputWire.input);
                write.i(inputWire.parentOutput);
            }
        }
        //endregion
    }
    public void read(Reads read){
        read.i();//version
        //region all
        int allAmount = read.i();
        all.clear();
        for(int i = 0; i < allAmount; i++){
            String gateName = read.str();
            Class<? extends ByteLogicGate> clazz =JsonIO.json.getClass(gateName);
            ByteLogicGate gate = Reflect.cons(clazz).get();
            gate.read(read);
            all.add(gate);
        }
        //endregion
        //region wires
        Seq<ByteLogicGate> gates = all.as();
        int wiresAmount = read.i();
        for(int i = 0; i < wiresAmount; i++){
            int rootIndex = read.i();
            ByteLogicGate root = gates.get(rootIndex);
            int wires = read.i();
            for(int j = 0; j < wires; j++){
                int objIndex = read.i();
                int input = read.i();
                int parentOutput = read.i();
                root.addParent(gates.get(objIndex),input,parentOutput);
            }
        }
        //endregion
    }
}
