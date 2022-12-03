@file:Suppress("PackageDirectoryMismatch")

package bytelogic.type

import arc.*
import arc.graphics.*
import arc.scene.style.*
import arc.util.*
import mindustry.*
import mindustry.content.*
import mindustry.ctype.*
import mindustry.gen.*
import mindustry.logic.*
import mindustry.type.*
import mindustry.world.*

val validTypes: Array<ContentType> = ContentType.all.asList()
    .filter { !it.name.endsWith("UNUSED") && it != ContentType.bullet }
    .toTypedArray()

@Suppress("MemberVisibilityCanBePrivate")
object ContentSignalType : DefaultSignalTypeImpl("content-type", { TextureRegionDrawable(Items.copper.uiIcon) }) {
    @JvmField
    val instance = ContentSignalType
    override fun applyControl(`this&signal`: Signal, building: Building) {
        building.control(LAccess.config, getContent(`this&signal`), 0.0, 0.0, 0.0);
    }

    override fun toString(signal: Signal): String {
        return getContent(signal)?.localizedName ?: Core.bundle["content-type.unknown-content"]
    }

    override fun barColor(signal: Signal): Color {
        return color(signal)
    }

    fun getType(signal: Signal): ContentType? =
        Pack.leftInt(signal.number)
            .takeIf { it in validTypes.indices }
            ?.let { validTypes[it] }

    fun getContent(signal: Signal): UnlockableContent? =
        getType(signal)?.let { type -> Vars.content.getByID<Content?>(type, Pack.rightInt(signal.number)) as? UnlockableContent }

    override fun color(signal: Signal): Color {
        val content = getContent(signal)
        return when (content) {
            is Item -> content.color
            is Liquid -> content.color
            is StatusEffect -> content.color
            is Planet -> content.iconColor
            is TeamEntry -> content.team.color
            is Block -> content.mapColor
            else -> Color.gray
        }
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