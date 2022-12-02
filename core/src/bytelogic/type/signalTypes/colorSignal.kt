@file:Suppress("PackageDirectoryMismatch")

package bytelogic.type

import arc.graphics.*
import arc.util.*
import mindustry.gen.*

internal object ColorSignalType : DefaultSignalTypeImpl("color-type", { Icon.pick }) {
    override fun barColor(signal: Signal): Color {
        return color(signal)
    }

    override fun color(signal: Signal): Color {
        return Tmp.c1.set(signal.intNumber)
    }

    override fun or(`this&signal`: Signal, signal: Signal) {
        super.or(`this&signal`, signal)
    }

    override fun xor(`this&signal`: Signal, signal: Signal) {
        super.xor(`this&signal`, signal)
    }

    override fun and(`this&signal`: Signal, signal: Signal) {
        super.and(`this&signal`, signal)
    }

    override fun plus(`this&signal`: Signal, signal: Signal) {
        super.plus(`this&signal`, signal)
    }

    override fun minus(`this&signal`: Signal, signal: Signal) {
        super.minus(`this&signal`, signal)
    }

    override fun div(`this&signal`: Signal, signal: Signal) {
        super.div(`this&signal`, signal)
    }

    override fun mod(`this&signal`: Signal, signal: Signal) {
        super.mod(`this&signal`, signal)
    }

    override fun times(`this&signal`: Signal, signal: Signal) {
        super.times(`this&signal`, signal)
    }
}