package bytelogic.ui.guide

import arc.Core
import arc.math.Interp
import arc.scene.actions.Actions
import arc.scene.ui.layout.Table
import arc.util.Align
import bytelogic.ui.guide.signalTypes.ContentSignalTab
import mindustry.Vars
import mindustry.content.Blocks
import mindustry.ctype.ContentType
import mindustry.gen.Building

fun ContentType.localizedName() = "@content.$name.name"

object DefaultGuideTabs {
    @JvmField
    val rootTab = tabGroup("root") {
        tabGroup("signal-types") {
            tab("number-signal") {}
            tab("color-signal") {}
            tab("content-signal", pageBuilder = ContentSignalTab)
        }
        val globalTable = Table()
        Core.scene.add(globalTable)
        /*tabGroup("signal-blocks") {
            val blocks = ByteLogicBlocks.erekirBlocks
            for (block in blocks.blocks) {
                tab(block.localizedName) { table ->
                    block.blockPreview.buildDemoPage(table)
                }
            }
        }*/
    }

    fun applyConfigTable(table: Table, selected: Building) {
        table.visible = true
        table.clear()
        selected.buildConfiguration(table)
        table.pack()
        table.isTransform = true
        table.actions(
            Actions.scaleTo(0f, 1f), Actions.visible(true),
            Actions.scaleTo(1f, 1f, 0.07f, Interp.pow3Out)
        )

        table.toFront()
        table.update {
            if (selected != null && selected.shouldHideConfigure(Vars.player)) {
//                hideConfig()
                return@update
            }
            table.setOrigin(Align.center)
            if (selected == null || selected.block === Blocks.air || !selected.isValid()) {
//                hideConfig()
            } else {
                selected.updateTableAlign(table)
            }
        }
    }
}