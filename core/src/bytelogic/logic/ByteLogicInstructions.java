package bytelogic.logic;

import bytelogic.gen.ByteLogicBuildingc;
import bytelogic.type.Signal;
import bytelogic.type.SignalType;
import bytelogic.world.blocks.logic.SignalBlock;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import mindustry.gen.Building;
import mindustry.io.TypeIO;
import mindustry.logic.LExecutor;

public class ByteLogicInstructions {
    private final static String BYTE_LOGIC_SIGNAL_ID = "byte-logic-signal";

    @AllArgsConstructor
    @NoArgsConstructor
    public static class IReadSignal implements LExecutor.LInstruction {
        LogicSignalType type;
        int var1, var2, blockSource;

        @Override
        public void run(LExecutor exec) {
            Building from = exec.building(blockSource);

            if (from instanceof ByteLogicBuildingc buildingc && (exec.privileged || from.team == exec.team)) {

                Signal signal = buildingc.displaySignal();
                //noinspection unused
                Void it = switch (type) {

                    case asObject -> {
                        exec.setobj(var1, new Object[]{BYTE_LOGIC_SIGNAL_ID, signal.number(), signal.type.getId()});
                        yield null;
                    }
                    case asValueAndType -> {
                        exec.setnum(var1, signal.number());
                        exec.setnum(var2, signal.type.getId());
                        yield null;
                    }
                };

            }
        }

    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class IWriteSignal implements LExecutor.LInstruction {
        protected static final Signal tmpSignal = new Signal();
        LogicSignalType type;
        int p1, p2, block;

        @Override
        public void run(LExecutor exec) {
            if (exec.building(block) instanceof SignalBlock.SignalLogicBuild build) {
                tmpSignal.setZero();
                build.configureSignal(switch (type) {

                    case asObject -> {
                        if (exec.obj(p1) instanceof Object[] arr && arr.length == 3 && BYTE_LOGIC_SIGNAL_ID.equals(arr[0])) {

                            tmpSignal.setNumber((long) arr[1]);
                            tmpSignal.type = SignalType.all[(int) arr[2]];
                        }
                        yield tmpSignal;
                    }
                    case asValueAndType -> {
                        tmpSignal.setNumber((long) exec.num(p1));
                        tmpSignal.type = SignalType.all[exec.numi(p2)];
                        yield tmpSignal;
                    }
                });
            }
        }
    }
}
