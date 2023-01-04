@file:Suppress("PackageDirectoryMismatch")

package bytelogic.type

import mindustry.gen.*
import mindustry.ui.*
import kotlin.math.*

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

    override fun absolute(`this&signal`: Signal) {
        `this&signal`.setNumber(Double.fromBits(`this&signal`.number).absoluteValue.toRawBits())
    }

    override fun applyControl(`this&signal`: Signal, building: Building) {
    }

    override fun log(signal: Signal) {
        signal.type=SignalTypes.floatType
        signal.setNumber(Math.log(Double.fromBits(signal.number)).toRawBits())
    }

    override fun sin(signal: Signal) {
        signal.type=SignalTypes.floatType
        signal.setNumber(Math.sin(Double.fromBits(signal.number)).toRawBits())
    }

    override fun cos(signal: Signal) {
        signal.type=SignalTypes.floatType
        signal.setNumber(Math.cos(Double.fromBits(signal.number)).toRawBits())
    }

    override fun tan(signal: Signal) {
        signal.type=SignalTypes.floatType
        signal.setNumber(Math.tan(Double.fromBits(signal.number)).toRawBits())
    }

    override fun asin(signal: Signal) {
        signal.type=SignalTypes.floatType
        signal.setNumber(Math.asin(Double.fromBits(signal.number)).toRawBits())
    }

    override fun acos(signal: Signal) {
        signal.type=SignalTypes.floatType
        signal.setNumber(Math.acos(Double.fromBits(signal.number)).toRawBits())
    }

    override fun atan(signal: Signal) {
        signal.type=SignalTypes.floatType
        signal.setNumber(Math.atan(Double.fromBits(signal.number)).toRawBits())
    }
}