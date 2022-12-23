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
//        if (`this&signal`.number in 0L..1L && `this&signal`.type == SignalTypes.numberType) `this&signal`.type = signal.type
        super.plus(`this&signal`, signal)
        /*if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType*/
    }

    override fun minus(`this&signal`: Signal, signal: Signal) {
//        if (`this&signal`.number in 0L..1L && `this&signal`.type == SignalTypes.numberType) `this&signal`.type = signal.type
        super.minus(`this&signal`, signal)
        /*if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType*/
    }

    override fun div(`this&signal`: Signal, signal: Signal) {
//        if (`this&signal`.number in 0L..1L && `this&signal`.type == SignalTypes.numberType) `this&signal`.type = signal.type
        super.div(`this&signal`, signal)
        /*if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType*/
    }

    override fun mod(`this&signal`: Signal, signal: Signal) {
//        if (`this&signal`.number in 0L..1L && `this&signal`.type == SignalTypes.numberType) `this&signal`.type = signal.type
        super.mod(`this&signal`, signal)
        /*if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType*/
    }

    override fun times(`this&signal`: Signal, signal: Signal) {
        if (`this&signal`.number in 0L..1L && `this&signal`.type == SignalTypes.numberType) `this&signal`.type = signal.type
        super.times(`this&signal`, signal)
        /*if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType*/
    }
}