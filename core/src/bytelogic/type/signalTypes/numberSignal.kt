@file:Suppress("PackageDirectoryMismatch")
package bytelogic.type

import arc.graphics.*
import bytelogic.graphics.*

internal object NumberSignalType : DefaultSignalTypeImpl("number-type") {
    override fun or(`this&signal`: Signal, signal: Signal) {
        super.or(`this&signal`, signal)
        if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType
    }

    override fun xor(`this&signal`: Signal, signal: Signal) {
        super.xor(`this&signal`, signal)
        if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType
    }

    override fun and(`this&signal`: Signal, signal: Signal) {
        super.and(`this&signal`, signal)
        if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType
    }

    override fun plus(`this&signal`: Signal, signal: Signal) {
        super.plus(`this&signal`, signal)
        if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType
    }

    override fun minus(`this&signal`: Signal, signal: Signal) {
        super.minus(`this&signal`, signal)
        if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType
    }

    override fun div(`this&signal`: Signal, signal: Signal) {
        super.div(`this&signal`, signal)
        if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType
    }

    override fun mod(`this&signal`: Signal, signal: Signal) {
        super.mod(`this&signal`, signal)
        if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType
    }

    override fun times(`this&signal`: Signal, signal: Signal) {
        super.times(`this&signal`, signal)
        if (`this&signal`.number == 0L) `this&signal`.type = NilSignalType
    }
}