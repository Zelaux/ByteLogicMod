package bytelogic.ui.guide

import arc.func.*
import arc.scene.ui.*
import arc.scene.ui.layout.*

interface GuideTab {
    val pageName: String
    val pageBuilder: (Table) -> Unit
     val pageButtonBuilder: Cons<Button>
}