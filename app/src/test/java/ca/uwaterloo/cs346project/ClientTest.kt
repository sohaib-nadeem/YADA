package ca.uwaterloo.cs346project

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import ca.uwaterloo.cs346project.data.Client
import ca.uwaterloo.cs346project.model.Action
import ca.uwaterloo.cs346project.model.ActionType
import ca.uwaterloo.cs346project.model.Shape
import ca.uwaterloo.cs346project.ui.util.DrawnItem
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

class ClientTest {
    private fun assertDrawnItemsEqual(drawnItem1: DrawnItem, drawnItem2: DrawnItem) {
        assertEquals(drawnItem1.userObjectId, drawnItem2.userObjectId)
        assertEquals(drawnItem1.shape, drawnItem2.shape)
        assertEquals(drawnItem1.color, drawnItem2.color)
        assertEquals(drawnItem1.strokeWidth, drawnItem2.strokeWidth)
        assertEquals(drawnItem1.segmentPoints.toList(), drawnItem2.segmentPoints.toList())
    }

    private fun assertActionsEqual(action1: Action<DrawnItem>, action2: Action<DrawnItem>) {
        assertEquals(action1.type, action2.type)
        assertEquals(action1.items.size, action2.items.size)
        for (i in 0 until action1.items.size) {
            assertDrawnItemsEqual(action1.items[i], action2.items[i])
        }
    }

    @Test
    fun testCreateSuccess() = runBlocking {
        val client = Client()
        val result = client.create()
        assertTrue(result)
        assertTrue(client.session_id > 0)
        assertTrue(client.user_id > 0)
    }

    @Test
    fun testJoinSuccess() = runBlocking {
        // create session
        val client1 = Client()
        var result = client1.create()
        assertTrue(result)
        assertTrue(client1.session_id > 0)
        assertTrue(client1.user_id > 0)

        // join session using different client
        val client2 = Client()
        val sessionId = client1.session_id  // Replace with a valid session ID for testing
        result = client2.join(sessionId.toString())
        assertTrue(result)
        assertEquals(sessionId.toInt(), client2.session_id)
        assertTrue(client2.user_id > 0)
    }

    @Test
    fun testJoinFailure() = runBlocking {
        val client = Client()
        val sessionId = -1  // Use an invalid session ID to simulate failure
        val result = client.join(sessionId.toString())
        assertFalse(result)
        assertEquals(-1, client.session_id)
        assertEquals(-1, client.user_id)
    }

    @Test
    fun testSendAndReceiveAction() = runBlocking {
        // create session using client1
        val client1 = Client()
        var result = client1.create()
        assertTrue(result)
        assertTrue(client1.session_id > 0)
        assertTrue(client1.user_id > 0)

        // send action using client2
        val drawnItem = DrawnItem(Pair(client1.user_id, 1), Shape.Line, Color.Black, 5f,
            mutableStateListOf(Offset(0f, 0f), Offset(100f, 100f), )
        )
        val addAction = Action(ActionType.ADD, listOf(drawnItem))
        client1.sendAction(addAction)

        // join session using different client2, receive and check action
        val client2 = Client()
        val sessionId = client1.session_id
        result = client2.join(sessionId.toString())
        val actionsReceived = client2.receiveAction()
        assertTrue(result)
        assertEquals(actionsReceived.size, 1)
        assertActionsEqual(actionsReceived[0], addAction)

        // receive again and check that no new actions
        val actionsReceived2 = client2.receiveAction()
        assertEquals(actionsReceived2.size, 0)
    }
}
