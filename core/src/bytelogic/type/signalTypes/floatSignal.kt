@file:Suppress("PackageDirectoryMismatch")

package bytelogic.type

import mindustry.gen.*
import mindustry.ui.*

internal object FloatSignalType : DefaultSignalTypeImpl(
    "float-number-type",
    { Fonts.getGlyph(Fonts.outline, 'F') }
) {


    override fun toString(signal: Signal): String {
        return Double.fromBits(signal.number).toString()
    }

    override fun or(`this&signal`: Signal, signal: Signal) {
        val number = Double.fromBits(`this&signal`.number)
        if (number in 0.0..1.0) `this&signal`.type = signal.type
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
        val number = Double.fromBits(`this&signal`.number)
        val secondNumber: Double = if (signal.type is FloatSignalType) {
            Double.fromBits(signal.number)
        } else {
            signal.number.toDouble()
        }
//        if (`this&signal`.number in 0L..1L && `this&signal`.type == SignalTypes.numberType) `this&signal`.type = signal.type
        `this&signal`.setNumber((number + secondNumber).toRawBits())
        /*if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType*/
    }

    override fun minus(`this&signal`: Signal, signal: Signal) {
        val number = Double.fromBits(`this&signal`.number)
        val secondNumber: Double = if (signal.type is FloatSignalType) {
            Double.fromBits(signal.number)
        } else {
            signal.number.toDouble()
        }
        `this&signal`.setNumber((number - secondNumber).toRawBits())
    }

    override fun div(`this&signal`: Signal, signal: Signal) {
        val number = Double.fromBits(`this&signal`.number)
        val secondNumber: Double = if (signal.type is FloatSignalType) {
            Double.fromBits(signal.number)
        } else {
            signal.number.toDouble()
        }
        `this&signal`.setNumber((number / secondNumber).toRawBits())
    }

    override fun mod(`this&signal`: Signal, signal: Signal) {
        val number = Double.fromBits(`this&signal`.number)
        val secondNumber: Double = if (signal.type is FloatSignalType) {
            Double.fromBits(signal.number)
        } else {
            signal.number.toDouble()
        }
        `this&signal`.setNumber((number % secondNumber).toRawBits())
    }

    override fun times(`this&signal`: Signal, signal: Signal) {

        val number = Double.fromBits(`this&signal`.number)
        if (number in 0.0..1.0) `this&signal`.type = signal.type

        val secondNumber: Double = if (signal.type is FloatSignalType) {
            Double.fromBits(signal.number)
        } else {
            signal.number.toDouble()
        }
        `this&signal`.setNumber((number * secondNumber).toRawBits())
    }

    override fun applyControl(`this&signal`: Signal, building: Building) {
    }
}