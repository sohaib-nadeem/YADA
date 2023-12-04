package ca.uwaterloo.cs346project

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import org.junit.Test
import org.junit.Before
import org.junit.Assert.*
class UpperBarTest {
    private lateinit var drawnItems: MutableList<DrawnItem>
    private lateinit var undoStack: MutableList<Action<DrawnItem>>
    private lateinit var redoStack: MutableList<Action<DrawnItem>>

    @Before
    fun setUp() {
        drawnItems = mutableListOf()
        undoStack = mutableListOf()
        redoStack = mutableListOf()
    }

    // Helper method to create a DrawnItem
    private fun createDrawnItem(
        userObjectId: Pair<Int, Int>,
        shape: Shape = Shape.Line,
        color: Color = Color.Black,
        strokeWidth: Float = 4f,
        segmentPoints: List<Offset> = listOf()
    ): DrawnItem {
        return DrawnItem(
            userObjectId = userObjectId,
            shape = shape,
            color = color,
            strokeWidth = strokeWidth,
            segmentPoints = mutableStateListOf(*segmentPoints.toTypedArray())
        )
    }

    @Test
    fun testAddingItems() {
        val action = Action(ActionType.ADD, listOf(DrawnItem(userObjectId = Pair(1, 1))))
        applyAction(action, drawnItems)
        assertEquals(1, drawnItems.size)
    }

    @Test
    fun testRemovingItems() {
        val drawnItem = DrawnItem(userObjectId = Pair(1, 1))
        drawnItems.add(drawnItem)
        val action = Action(ActionType.REMOVE, listOf(drawnItem))
        applyAction(action, drawnItems)
        assertTrue(drawnItems.isEmpty())
    }

    @Test
    fun testModifyingItems() {
        val originalItem = DrawnItem(userObjectId = Pair(1, 1))
        val modifiedItem = DrawnItem(userObjectId = Pair(1, 1), color = Color.Red)
        drawnItems.add(originalItem)
        val action = Action(ActionType.MODIFY, listOf(originalItem, modifiedItem))
        applyAction(action, drawnItems)
        assertEquals(Color.Red, drawnItems[0].color)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInvalidModifyAction() {
        val action = Action(ActionType.MODIFY, listOf(DrawnItem(userObjectId = Pair(1, 1))))
        applyAction(action, drawnItems)
    }

    @Test(expected = NoSuchElementException::class)
    fun testModifyingNonExistentItem() {
        drawnItems.clear() // Ensure drawnItems is empty before the test
        val action = Action(ActionType.MODIFY, listOf(DrawnItem(userObjectId = Pair(2, 2)), DrawnItem(userObjectId = Pair(2, 2))))
        applyAction(action, drawnItems)
    }


    @Test
    fun testRemoveNonExistentItem() {
        val action = Action(ActionType.REMOVE, listOf(DrawnItem(userObjectId = Pair(1, 1))))
        applyAction(action, drawnItems)
        assertTrue(drawnItems.isEmpty())  // Ensures no error occurs
    }

    @Test
    fun testAddDuplicateItems() {
        val drawnItem = DrawnItem(userObjectId = Pair(1, 1))
        val action = Action(ActionType.ADD, listOf(drawnItem, drawnItem))
        applyAction(action, drawnItems)
        assertEquals(2, drawnItems.size)  // Assumes duplicates are allowed
    }



    @Test
    fun testReversingAddAction() {
        val originalAction = Action(ActionType.ADD, listOf(createDrawnItem(Pair(1, 1))))
        val reversedAction = createReversedAction(originalAction)
        assertEquals(ActionType.REMOVE, reversedAction.type)
        assertEquals(originalAction.items, reversedAction.items)
    }

    @Test
    fun testReversingRemoveAction() {
        val originalAction = Action(ActionType.REMOVE, listOf(createDrawnItem(Pair(1, 1))))
        val reversedAction = createReversedAction(originalAction)
        assertEquals(ActionType.ADD, reversedAction.type)
        assertEquals(originalAction.items, reversedAction.items)
    }

    @Test
    fun testReversingModifyActionWithValidItems() {
        val originalAction = Action(
            ActionType.MODIFY,
            listOf(createDrawnItem(Pair(1, 1)), createDrawnItem(Pair(1, 2)))
        )
        val reversedAction = createReversedAction(originalAction)
        assertEquals(ActionType.MODIFY, reversedAction.type)
        assertEquals(originalAction.items[1], reversedAction.items[0])
        assertEquals(originalAction.items[0], reversedAction.items[1])
    }

    @Test(expected = IllegalArgumentException::class)
    fun testReversingModifyActionWithInvalidItems() {
        val originalAction = Action(ActionType.MODIFY, listOf(createDrawnItem(Pair(1, 1))))
        createReversedAction(originalAction)
    }



    @Test
    fun testUndoOnEmptyStack() {
        performUndo(drawnItems, undoStack, redoStack)
        assertTrue(undoStack.isEmpty() && redoStack.isEmpty())
    }

    @Test
    fun testUndoAddAction() {
        val item = createDrawnItem(Pair(1, 1))
        drawnItems.add(item)
        undoStack.add(Action(ActionType.ADD, listOf(item)))
        performUndo(drawnItems, undoStack, redoStack)
        assertTrue(drawnItems.isEmpty() && redoStack.size == 1)
    }

    @Test
    fun testUndoRemoveAction() {
        val item = createDrawnItem(Pair(1, 1))
        undoStack.add(Action(ActionType.REMOVE, listOf(item)))
        performUndo(drawnItems, undoStack, redoStack)
        assertEquals(1, drawnItems.size)
        assertEquals(item, drawnItems.first())
        assertEquals(1, redoStack.size)
    }

    @Test
    fun testUndoModifyAction() {
        val originalItem = createDrawnItem(userObjectId = Pair(1, 1), color = Color.Black)
        val modifiedItem = createDrawnItem(userObjectId = Pair(1, 1), color = Color.Red)
        drawnItems.add(modifiedItem)
        undoStack.add(Action(ActionType.MODIFY, listOf(originalItem, modifiedItem)))
        performUndo(drawnItems, undoStack, redoStack)
        assertEquals(Color.Black, drawnItems.first().color)
        assertEquals(1, redoStack.size)
    }

    @Test
    fun testMultipleUndoActions() {
        // Setup for multiple undo actions
        val addItem = createDrawnItem(Pair(1, 1))
        val removeItem = createDrawnItem(Pair(2, 2))
        drawnItems.addAll(listOf(addItem, removeItem))

        undoStack.add(Action(ActionType.ADD, listOf(addItem)))  // Add action will be undone first
        undoStack.add(Action(ActionType.REMOVE, listOf(removeItem)))  // Remove action will be undone second

        // Perform undo actions
        performUndo(drawnItems, undoStack, redoStack)  // Undo add (remove addItem)
        performUndo(drawnItems, undoStack, redoStack)  // Undo remove (add removeItem back)

        // Assertions
        assertFalse(drawnItems.contains(addItem))  // addItem should be removed
        assertTrue(drawnItems.contains(removeItem))  // removeItem should be added back
        assertTrue(redoStack.size == 2)  // Two actions moved to redoStack
    }


    // Test cases for performRedo

    @Test
    fun testRedoOnEmptyStack() {
        performRedo(drawnItems, undoStack, redoStack)
        assertTrue(redoStack.isEmpty() && undoStack.isEmpty())
    }

    @Test
    fun testRedoAddAction() {
        val item = createDrawnItem(Pair(1, 1))
        redoStack.add(Action(ActionType.ADD, listOf(item)))
        performRedo(drawnItems, undoStack, redoStack)
        assertTrue(drawnItems.size == 1 && undoStack.size == 1)
    }

    @Test
    fun testRedoRemoveAction() {
        val item = createDrawnItem(Pair(1, 1))
        drawnItems.add(item)
        redoStack.add(Action(ActionType.REMOVE, listOf(item)))
        performRedo(drawnItems, undoStack, redoStack)
        assertTrue(drawnItems.isEmpty() && undoStack.size == 1)
    }

    @Test
    fun testRedoModifyAction() {
        val originalItem = createDrawnItem(userObjectId = Pair(1, 1), color = Color.Black)
        val modifiedItem = createDrawnItem(userObjectId = Pair(1, 1), color = Color.Red)
        drawnItems.add(originalItem)
        redoStack.add(Action(ActionType.MODIFY, listOf(originalItem, modifiedItem)))
        performRedo(drawnItems, undoStack, redoStack)
        assertEquals(Color.Red, drawnItems.first().color)
        assertEquals(1, undoStack.size)
    }

    @Test
    fun testMultipleRedoActions() {
        // Setup for multiple redo actions
        val addItem = createDrawnItem(Pair(1, 1))
        val removeItem = createDrawnItem(Pair(2, 2))
        drawnItems.add(removeItem)

        redoStack.add(Action(ActionType.REMOVE, listOf(removeItem)))  // Remove action will be redone first
        redoStack.add(Action(ActionType.ADD, listOf(addItem)))  // Add action will be redone second

        // Perform redo actions
        performRedo(drawnItems, undoStack, redoStack)  // Redo remove (remove removeItem)
        performRedo(drawnItems, undoStack, redoStack)  // Redo add (add addItem back)

        // Assertions
        assertTrue(drawnItems.contains(addItem))  // addItem should be added back
        assertFalse(drawnItems.contains(removeItem))  // removeItem should be removed
        assertTrue(undoStack.size == 2)  // Two actions moved to undoStack
    }

}