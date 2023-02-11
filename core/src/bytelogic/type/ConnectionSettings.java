package bytelogic.type;

import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import bytelogic.type.ConnectionSettings.WireDescriptor.*;
import mindustry.io.*;

public class ConnectionSettings{
    public Seq<WireDescriptor> inputWires = new Seq<>();
    public Seq<WireDescriptor> outputWires = new Seq<>();

    private static void writeWires(Writes write, Seq<WireDescriptor> wires){
        write.i(wires.size);
        for(WireDescriptor wire : wires){
            write.i(0);
            TypeIO.writeString(write, wire.name);
            write.i(wire.connectedStructures.size);
            for(StructureDescriptor structure : wire.connectedStructures){
                write.i(structure.x);
                write.i(structure.y);
                write.i(structure.connectionIndex);
            }
        }
    }

    public WireDescriptor inputWire(){
        return inputWire(null);
    }

    public WireDescriptor inputWire(String name){
        WireDescriptor descriptor = new WireDescriptor();
        descriptor.name = name;
        inputWires.add(descriptor);
        return descriptor;
    }

    public WireDescriptor outputWire(){
        return outputWire(null);
    }

    public WireDescriptor outputWire(String name){
        WireDescriptor descriptor = new WireDescriptor();
        descriptor.name = name;
        outputWires.add(descriptor);
        return descriptor;
    }

    public void write(Writes write){
        write.i(1);
        writeWires(write, inputWires);
        writeWires(write, outputWires);
    }

    public void read(Reads read){
        if(read.i() == 0) return;
        readWires(read, inputWires);
        readWires(read, outputWires);
    }

    private void readWires(Reads read, Seq<WireDescriptor> wires){
        wires.clear();
        int size = read.i();
        for(int i = 0; i < size; i++){
            read.i();//version
            WireDescriptor descriptor = new WireDescriptor();
            descriptor.name = TypeIO.readString(read);
            int structuresAmount = read.i();
            for(int j = 0; j < structuresAmount; j++){
                int x = read.i();
                int y = read.i();
                int connectionIndex = read.i();
                descriptor.connectedStructures.add(new StructureDescriptor(x, y, connectionIndex));
            }
            wires.add(descriptor);
        }
    }

    public static class WireDescriptor{
        public String name;
        public Seq<StructureDescriptor> connectedStructures = new Seq<>();

        public static class StructureDescriptor{
            public long packed;
            public int x, y;
            public int connectionIndex;

            public StructureDescriptor(int x, int y, int connectionIndex){
                this.x = x;
                this.y = y;
                this.packed = Pack.longInt(x, y);
                this.connectionIndex = connectionIndex;
            }

            public long packed(){
                updatePacked();
                return packed;
            }

            public void updatePacked(){
                packed = Pack.longInt(x, y);
            }
        }
    }
}
