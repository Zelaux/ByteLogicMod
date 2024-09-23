package bytelogic.logic;

import arc.scene.ui.layout.Table;
import bytelogic.content.ByteLogicBlocks;
import bytelogic.gen.BLLogicIO;
import bytelogic.logic.ByteLogicInstructions.IReadSignal;
import bytelogic.logic.ByteLogicInstructions.IWriteSignal;
import mindustry.annotations.Annotations;
import mindustry.graphics.Pal;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import mindustry.ui.Styles;

public class ByteLogicLogicStatements {
    public static final LCategory byteLogicCategory = new LCategory("byte-logic", Pal.darkMetal);

    protected abstract static class BLStatement extends LStatement {
        @Override
        public LCategory category() {
            return byteLogicCategory;
        }

        @Override
        public void write(StringBuilder builder) {
            BLLogicIO.write(this, builder);
        }
    }

    @Annotations.RegisterStatement("byte-logic-read")
    public static class ReadSignalStatement extends BLStatement {
        public String output = "result", output2 = "type", target = "relay1";
        public LogicSignalType type = LogicSignalType.asObject;


        @Override
        public void build(Table table) {
            rebuild(table);
        }

        private void rebuild(Table main) {
            main.clearChildren();
            main.table(table -> {
                table.add(" read ");

                table.button(b -> {
                    b.label(() -> type.name());
                    b.clicked(() -> showSelect(b, LogicSignalType.all, type, t -> {
                        type = t;

                        rebuild(main);
                    }, 2, cell -> cell.size(100, 50)));
                }, Styles.logict, () -> {
                }).size(200, 40).color(table.color).left().padLeft(2);
            }).left();
            main.row();
            main.table(table -> {

                //noinspection unused
                Void it = switch (type) {//Needs to caver all LogicSignalType variants

                    case asObject -> {
                        field(table, output, str -> output = str);

                        table.add(" = ");

                        fields(table, target, str -> target = str);
                        yield null;
                    }
                    case asValueAndType -> {
                        table.add("value ");
                        field(table, output, str -> output = str);
                        table.add("typeId ");
                        field(table, output2, str -> output2 = str);
                        table.add(" = ");
                        fields(table, target, str -> target = str);
                        yield null;
                    }
                };
            });
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return new IReadSignal(type, builder.var(output), builder.var(output2), builder.var(target));
        }
    }

    @Annotations.RegisterStatement("byte-logic-write-signal")
    public static class WriteSignalStatement extends BLStatement {
        public String input1 = "result", input2 = "type", target = "block1";
        public LogicSignalType type = LogicSignalType.asObject;


        @Override
        public void build(Table table) {
            rebuild(table);
        }

        private void rebuild(Table main) {
            main.clearChildren();
            main.table(table -> {
                table.add(" write to '" + ByteLogicBlocks.erekirBlocks.signalBlock.localizedName + "' ");
                row(table);
                table.button(b -> {
                    b.label(() -> type.name());
                    b.clicked(() -> showSelect(b, LogicSignalType.all, type, t -> {
                        type = t;

                        rebuild(main);
                    }, 2, cell -> cell.size(100, 50)));
                }, Styles.logict, () -> {
                }).size(200, 40).color(table.color).left().padLeft(2);

            }).left();

            main.row();
            main.table(table -> {
                //noinspection unused
                Void it = switch (type) {//Needs to caver all LogicSignalType variants

                    case asObject -> {
                        fields(table, target, str -> target = str);

                        table.add(" = ");

                        field(table, input1, str -> input1 = str);
                        yield null;
                    }
                    case asValueAndType -> {
                        fields(table, target, str -> target = str);
                        table.add(" = ");
                        table.add("value ");
                        field(table, input1, str -> input1 = str);
                        table.add("typeId ");
                        field(table, input2, str -> input2 = str);
                        yield null;
                    }
                };
            });
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder) {
            return new IWriteSignal(type, builder.var(input1), builder.var(input2), builder.var(target));
        }
    }
}
