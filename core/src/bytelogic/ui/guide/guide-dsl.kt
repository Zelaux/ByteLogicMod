package bytelogic.ui.guide

import arc.func.*
import arc.scene.ui.*
import arc.scene.ui.layout.*

@DslMarker
annotation class GuideDsl

@GuideDsl
fun tabGroup(name: String, block: GuideTabParent.() -> Unit): GuideTabParent =
    GuideTabParent(name) { it.add(name) }.apply(block)

@GuideDsl
fun GuideTabParent.tabGroup(name: String, block: GuideTabParent.() -> Unit): GuideTabParent =
    GuideTabParent(name) { it.add(name) }.apply(block).also { children.add(it) }

@GuideDsl
fun GuideTabParent.tab(name: String, pageBuilderButton: Cons<Button>? = null, pageBuilder: (Table) -> Unit): GuideTabImpl =

    GuideTabImpl(
        pageName = name,
        pageBuilder = pageBuilder,
        pageButtonBuilder = pageBuilderButton ?: Cons { it.add(name) })
        .also { children.add(it) }