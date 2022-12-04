package bytelogic.ui.guide

import arc.func.*
import arc.scene.ui.*
import arc.scene.ui.layout.*

class GuideTabImpl(
    override val pageName: String,
    override val pageBuilder: (Table) -> Unit,
    override val pageButtonBuilder: Cons<Button>,
) : GuideTab {
}