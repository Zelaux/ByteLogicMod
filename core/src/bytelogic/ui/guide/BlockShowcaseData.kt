package bytelogic.ui.guide

import arc.*
import arc.func.*
import arc.math.*
import arc.math.geom.*
import arc.scene.actions.*
import arc.scene.ui.layout.*
import arc.struct.*
import arc.util.*
import bytelogic.tools.*
import bytelogic.ui.elements.*
import bytelogic.ui.elements.WorldElement.*
import bytelogic.world.blocks.logic.*
import bytelogic.world.blocks.sandbox.*
import mindustry.content.*
import mindustry.core.*
import mindustry.game.*
import mindustry.graphics.*
import mindustry.world.*
import kotlin.jvm.internal.Ref.*

fun worldFiller(world: World): Intc2 {
    return Intc2 { x: Int, y: Int -> world.tiles[x, y] = Tile(x, y, Blocks.metalFloor, Blocks.air, Blocks.air) }
}

class DefaultBlockShowcase(val block: LogicBlock) : BlockShowcase(block, block.size, block.size, { world, _ ->
    world.tile(0, 0).setBlock(block, Team.sharded)
    arrayOf(Tmp.p1.set(0, 0))
}) {
    init {
        hasNoSwitchMirror = false
    }
}


class SchematicBlockShowcase @JvmOverloads constructor(
    val blockReference: LogicBlock,
    val schematic: Schematic,
    square: Boolean = true,
    worldWidth: Int = -1,
    worldHeight: Int = -1,
    schematicOffset: Point2 = Point2(0, 0),
) :
    BlockShowcase(
        blockReference,
        if (square) Math.max(schematic.width, schematic.height) else worldWidth,
        if (square) Math.max(schematic.width, schematic.height) else worldHeight,
        { world, isSwitch ->

            var (offsetX, offsetY) = schematicOffset.x to schematicOffset.y
            if (square) {
                if (schematic.height < schematic.width) {
                    offsetY += (schematic.width - schematic.height) / 2
                }
                if (schematic.width < schematic.height) {
                    offsetX += (schematic.height - schematic.width) / 2
                }
            }
            val points = Seq<Point2>()

            for (tile in schematic.tiles) {
                var block=tile.block
                var config=tile.config
                if (block is PlaceholderBlock) {
                    block = if (config == true) {
                        points.add(Point2(tile.x+offsetX,tile.y+offsetY))
                        blockReference
                    } else {
                        if (isSwitch) {
                            blockReference.byteLogicBlocks.switchBlock
                        } else {
                            blockReference.byteLogicBlocks.signalBlock
                        }
                    }
                    config = null
                }
                world.tile(tile.x + offsetX, tile.y + offsetY)
                    .setBlock(block, Team.sharded, tile.rotation.toInt())
                world.tile(tile.x + offsetX, tile.y + offsetY).build.configured(null, config)
            }
            points.toArray(Point2::class.java)
        }
    )

open class BlockShowcase(
    val blockContext: LogicBlock,
    val worldWidth: Int,
    val worldHeight: Int,
    val worldBuilder: (world: World, isSwitch: Boolean) -> Array<Point2>,
) {
    @set:JvmName("hasNoSwitchMirror")
    @get:JvmName("hasNoSwitchMirror")
    var hasNoSwitchMirror = true;
    open fun createWorldContext(isSwitch: Boolean): Pair<WorldLogicContext, Array<Point2>> {
        val world = World()
        val context = WorldLogicContext(world)
        val points = ObjectRef<Array<Point2>>()
        context.inContext {
            world.isGenerating = true;
            world.resize(worldWidth, worldHeight).each(worldFiller(world))
            world.isGenerating = false
            points.element = worldBuilder(world, isSwitch)
        }
        return context to points.element
    }

    open fun shouldBuildConfiguration(block: Block): Boolean {
        return block is SignalBlock
    }

    open fun LogicBlock.createPreview(table: Table, isSwitch: Boolean): BlockShowcaseData {

        val (context, points) = createWorldContext(isSwitch)
        val world = context.world
        val worldElement = WorldElement(context, 64f)
        for (tilePos in points) {
            val selection = TileSelection(tilePos.x, tilePos.y)
            selection.color.set(Pal.heal)
            worldElement.tileSelections.add(selection)
        }
        world.tiles.eachTile { it: Tile ->
            if (it.block() !is SwitchBlock && it.block() !is SignalBlock) {
                return@eachTile
            }
            val selection = TileSelection(it.x.toInt(), it.y.toInt())
            selection.color.set(Pal.lancerLaser)
            if (isSwitch && it.block() is SwitchBlock) {
                selection.clickListener = Runnable {
                    worldElement.onNextUpdate {
                        it.build.tapped()
                    }
                }
            }
            worldElement.tileSelections.add(selection)
        }
        val selection = TileSelection(-1, -1, Pal.accent)
        selection.enabled = false
        worldElement.tileSelections.add(selection)
        table.add(worldElement)
        val selectedInfoTable = ObjectRef<Table>()

        var wasShown = false
        table.table { selectedInfo ->
            selectedInfo.isTransform = true;
            selectedInfoTable.element = selectedInfo
            val duration = 10 / Time.toSeconds
            worldElement.tileClickListener = Cons tileClickListener@{ tile: Tile? ->

                if (tile?.build == null || selection.x == tile.x.toInt() && selection.y == tile.y.toInt()) {
                    if (wasShown) {
                        selectedInfo.clearActions()
                        selectedInfo.actions(
                            Actions.scaleTo(1f, 1f),
                            Actions.scaleTo(0f, 1f, duration, Interp.pow3Out),
                        )
                    }
                    wasShown = false;
                    selection.x = -1
                    selection.enabled = false
                    return@tileClickListener
                }
                selectedInfo.clearChildren()
                if (!wasShown) {
                    selectedInfo.clearActions()
                    selectedInfo.actions(
                        Actions.scaleTo(0f, 1f),
                        Actions.scaleTo(1f, 1f, duration, Interp.pow3Out)
                    )
                }
                selection.enabled = true
                selection.x = tile.x.toInt()
                selection.y = tile.y.toInt()
                tile.build.display(selectedInfo.table().get())
                selectedInfo.row()
                wasShown = true;
                if (shouldBuildConfiguration(tile.block())) {
                    tile.build.buildConfiguration(selectedInfo.table().get())
                }
            }
        }.minWidth(256f * 1.5f)
        return BlockShowcaseData(context, worldElement, table, selectedInfoTable.element, selection)
    }

    open fun buildDemoPage(table: Table): Array<BlockShowcaseData> = blockContext.run {
        table.labelWrap(Core.bundle[name + ".guide-info", description]).labelAlign(Align.center).fillX().row()
        val first = createPreview(table, true)
        if (!hasNoSwitchMirror) return@run arrayOf(first)
        table.row()
        val second = createPreview(table, false)
        arrayOf(first, second)
    }

}


class BlockShowcaseData(
    @JvmField val worldContext: WorldLogicContext,
    @JvmField val worldElement: WorldElement,
    @JvmField val mainTable: Table,
    @JvmField val selectionInfoTable: Table,
    @JvmField val selection: TileSelection,
) {
}