@file:Suppress("PackageDirectoryMismatch")

package bytelogic.type

import arc.graphics.*
import arc.util.*
import mindustry.gen.*
import mindustry.logic.*

internal object ColorSignalType : DefaultSignalTypeImpl("color-type", { Icon.pick }) {
    override fun toString(signal: Signal): String {
        return "#${color(signal)}"
    }

    override fun applyControl(`this&signal`: Signal, building: Building) {
        val color = color(`this&signal`)
        building.control(LAccess.color, color.toDoubleBits(), 0.0, 0.0, 0.0);
    }

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
        if (signal.type != SignalTypes.colorType) {
            super.plus(`this&signal`, signal)
            return
        }
        `this&signal`.setNumber(
            Tmp.c2.set(color(`this&signal`)).add(signal.color())
                .rgba()
        )
    }

    override fun minus(`this&signal`: Signal, signal: Signal) {
        if (signal.type != SignalTypes.colorType) {
            super.minus(`this&signal`, signal)
            return
        }
        `this&signal`.setNumber(
            Tmp.c2.set(color(`this&signal`)).sub(signal.color())
                .rgba()
        )
    }

    override fun div(`this&signal`: Signal, signal: Signal) {
        if (signal.type != SignalTypes.colorType) {
            super.div(`this&signal`, signal)
            return
        }
        val b = signal.color()
        val a = Tmp.c2.set(color(`this&signal`))
        a.r /= b.r;
        a.g /= b.g;
        a.b /= b.b;
        a.a /= b.a;
        `this&signal`.setNumber(
            a.rgba()
        )
    }

    override fun mod(`this&signal`: Signal, signal: Signal) {
        super.mod(`this&signal`, signal)
    }

    override fun times(`this&signal`: Signal, signal: Signal) {
        if (signal.type != SignalTypes.colorType) {
            super.times(`this&signal`, signal)
            return
        }
        `this&signal`.setNumber(
            Tmp.c2.set(color(`this&signal`)).mul(signal.color())
                .rgba()
        )
    }
}