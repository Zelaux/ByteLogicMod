package bytelogic.ui.guide

import arc.func.*
import arc.scene.ui.*
import arc.scene.ui.layout.*
import arc.struct.*
import mindustry.gen.*

open class GuideTabParent(override val pageName: String, override var pageButtonBuilder: Cons<Button>) : GuideTab {
    @Suppress("LeakingThis")
    override val pageBuilder: (Table) -> Unit = this::buildPage
    val children: Seq<GuideTab> = Seq()
    @JvmField
    var selectedChild: Int = 0;
    private fun buildPage(pageTable: Table) {
        pageTable.clearChildren();
        var rebuilder = {}
        pageTable.pane { buttons ->
            buttons.background = Tex.pane
            for ((i, child) in children.withIndex()) {
                buttons.button(child.pageButtonBuilder) {
                    val scrollX = (buttons.parent as ScrollPane).scrollX
                    selectedChild=i;
                    rebuilder()
//                    buildPage(pageTable, i)
                    (pageTable.children[0] as ScrollPane).scrollX = scrollX;
                }.fillX()
                buttons.row();
            }/*
            buttons.fill { x, y, width, height ->
                Fill.crect(x,y,width,height)
            }*/
            buttons.add().grow();
        }.growY().minWidth(128f * 1.5f).fillX()
//        pageTable.add().fill()
        pageTable.table {
            rebuilder = {
                it.clearChildren()
                it.background = Tex.pane
                it.table(children[selectedChild].pageBuilder).top().grow()
            }
            rebuilder()
        }.grow()
    }
}