package bytelogic.ui.guide.signalTypes

import arc.*
import arc.graphics.*
import arc.scene.ui.layout.*
import arc.util.*
import bytelogic.gen.*
import bytelogic.tools.*
import bytelogic.type.*
import bytelogic.ui.guide.*
import mindustry.*
import mindustry.ctype.*
import mindustry.graphics.*

object ContentSignalTab : (Table) -> Unit {
    private fun Table.header(text: String): Cell<Table> {
        return table {
            UIUtils.horizontalSeparator(it, Pal.accent)
            it.add(text).color(Pal.accentBack).padLeft(8f).padRight(8f)
            UIUtils.horizontalSeparator(it, Pal.accent)
        }.growX()
    }

    lateinit var table: Table
    override fun invoke(t: Table) {
        t.pane {
            table = it
        }.grow()


        table.left()

        table.header("@content.signal.info.intro").colspan(3).row()

        table.add(bundleFormat("content.signal.info.text", "contentSignal = shift + id")).colspan(3).row()
        UIUtils.horizontalSeparator(table, Color.darkGray).colspan(3).row()


        table.header("@content.signal.info.values").padTop(4f).colspan(3).row()


        val validTypes = ContentSignal.validTypes
        val opened = BooleanArray(validTypes.size)
        for ((i, type) in validTypes.withIndex()) {
            table.button({ button ->
                button.image {
                    if (opened[i]) {
                        BLIcons.Regions.collapser0Opened32
                    } else {
                        BLIcons.Regions.collapser0Closed32
                    }
                }
            }) {
                opened[i] = !opened[i]
            }
            table.add(type.localizedName())
            val shiftOffset = Pack.longInt(1, 0)
            table.add(bundleFormat("content-signal.shift", "#shift $i * $shiftOffset", "$i * $shiftOffset"))
                .color(Color.lightGray)
            table.row();
            table.collapser({ innerTable ->
                innerTable.defaults().left()
                innerTable.left()
                for ((j, content) in Vars.content.getBy<UnlockableContent>(type).withIndex()) {
                    innerTable.add(bundleFormat("content-signal.id", "#id $j", j))
                        .color(Color.lightGray)
                    val uiIcon = content.uiIcon
                    val aspectH = uiIcon.width / uiIcon.height
                    val aspectW = uiIcon.height / uiIcon.width
                    if (content.fullIcon.width > content.fullIcon.height) {
                        innerTable.image(uiIcon).size(48f, 48f * content.fullIcon.height/content.fullIcon.width )
                    } else {
                        innerTable.image(uiIcon).size(48f *content.fullIcon.width/ content.fullIcon.height , 48f)
                    }
                    innerTable.add(content.localizedName);
                    innerTable.add().fillX()
                    if ((Vars.mobile || type==ContentType.block) && (j + 1) % 2 == 0 || type!=ContentType.block && (j + 1) % 3 == 0) {
                        innerTable.row();
                    }
                }
            }) { opened[i] }.colspan(3);
            table.row();
        }
    }

}


private fun bundleFormat(key: String, default: String, vararg values: Any?) = when (Core.bundle.has(key)) {
    true -> Core.bundle.format(key, *values)
    false -> default
}