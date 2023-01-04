@file:Suppress("PackageDirectoryMismatch")

package bytelogic.type

import mindustry.ui.*

internal object IntegerSignalType : DefaultSignalTypeImpl(
    "number-type",
    { Fonts.getGlyph(Fonts.outline, '0') }
) {
    override fun or(`this&signal`: Signal, signal: Signal) {
        if (`this&signal`.number in 0L..1L && `this&signal`.type == SignalTypes.numberType) `this&signal`.type = signal.type
        super.or(`this&signal`, signal)
        /*if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType*/
    }

    override fun xor(`this&signal`: Signal, signal: Signal) {
//        if (`this&signal`.number in 0L..1L && `this&signal`.type == SignalTypes.numberType) `this&signal`.type = signal.type
        super.xor(`this&signal`, signal)
        /*if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType*/
    }

    override fun and(`this&signal`: Signal, signal: Signal) {
//        if (`this&signal`.number in 0L..1L && `this&signal`.type == SignalTypes.numberType) `this&signal`.type = signal.type
        super.and(`this&signal`, signal)
        /*if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType*/
    }

    override fun plus(`this&signal`: Signal, signal: Signal) {
        if (signal.type ==SignalTypes.floatType) {
            `this&signal`.type = signal.type;
            `this&signal`.setNumber(`this&signal`.number.toDouble().toRawBits())
            `this&signal`.plus(signal)
        } else
//        if (`this&signal`.number in 0L..1L && `this&signal`.type == SignalTypes.numberType) `this&signal`.type = signal.type
            super.plus(`this&signal`, signal)
        /*if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType*/
    }

    override fun minus(`this&signal`: Signal, signal: Signal) {
//        if (`this&signal`.number in 0L..1L && `this&signal`.type == SignalTypes.numberType) `this&signal`.type = signal.type
        if (signal.type ==SignalTypes.floatType) {
            `this&signal`.type = signal.type;
            `this&signal`.setNumber(`this&signal`.number.toDouble().toRawBits())
            signal.type.minus(`this&signal`, signal)
        } else
            super.minus(`this&signal`, signal)
        /*if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType*/
    }

    override fun div(`this&signal`: Signal, signal: Signal) {
//        if (`this&signal`.number in 0L..1L && `this&signal`.type == SignalTypes.numberType) `this&signal`.type = signal.type
        if (signal.type ==SignalTypes.floatType) {
            `this&signal`.type = signal.type;
            `this&signal`.setNumber(`this&signal`.number.toDouble().toRawBits())
            `this&signal`.div(signal)
        } else
            super.div(`this&signal`, signal)
        /*if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType*/
    }

    override fun mod(`this&signal`: Signal, signal: Signal) {
//        if (`this&signal`.number in 0L..1L && `this&signal`.type == SignalTypes.numberType) `this&signal`.type = signal.type
        if (signal.type ==SignalTypes.floatType) {
            `this&signal`.type = signal.type;
            `this&signal`.setNumber(`this&signal`.number.toDouble().toRawBits())
            `this&signal`.mod(signal)
        } else
            super.mod(`this&signal`, signal)
        /*if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType*/
    }

    override fun times(`this&signal`: Signal, signal: Signal) {
        if (`this&signal`.number in 0L..1L && `this&signal`.type == SignalTypes.numberType) `this&signal`.type = signal.type
        if (signal.type == SignalTypes.floatType) {
            `this&signal`.type = signal.type;
            `this&signal`.setNumber(`this&signal`.number.toDouble().toRawBits())
            `this&signal`.times(signal)
        } else
            super.times(`this&signal`, signal)
        /*if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType*/
    }
}