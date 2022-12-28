package bytelogic.type;

import arc.func.*;
import arc.struct.*;
import bytelogic.type.byteGates.*;
import bytelogic.type.byteGates.ByteLogicOperators.*;
import bytelogic.type.graphicsGates.*;
import mma.ui.tiledStructures.TiledStructures.*;

public class ByteLogicGateProvider{
    public static final ByteLogicGateProvider defaultProvider = new ByteLogicGateProvider("default",ByteLogicOperators.getProvidersAsSequence().as());
    public static final ByteLogicGateProvider graphicsProvider = new ByteLogicGateProvider("graphics", GraphicsOperators.getProvidersAsSequence().as());
    public static final ObjectMap<String, ByteLogicGateProvider> providerMap = new ObjectMap<>();
    public static final Seq<ByteLogicGateProvider> allProviders = new Seq<>();
    public final int id;
    public final String name;
    public final Seq<Prov<? extends TiledStructure<?>>> providers;

    public ByteLogicGateProvider(String name, Seq<Prov<? extends TiledStructure<?>>> providers){
        this.name = name;
        this.providers = providers;
        id=allProviders.size;
        allProviders.add(this);
        providerMap.put(name,this);
    }
}
