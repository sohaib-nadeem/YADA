package ca.uwaterloo.cs346project

import kotlinx.coroutines.runBlocking
import models.Action
import models.ActionType
import models.CanvasObject
import org.junit.Assert
import org.junit.Test
import service.SessionService

class SessionServiceTest {
    val sessionService = SessionService()

    private fun assertCanvasObjectsEqual(drawnItem1: CanvasObject, drawnItem2: CanvasObject) {
        Assert.assertEquals(drawnItem1.userObjectId, drawnItem2.userObjectId)
        Assert.assertEquals(drawnItem1.shape, drawnItem2.shape)
        Assert.assertEquals(drawnItem1.color, drawnItem2.color)
        Assert.assertEquals(drawnItem1.strokeWidth, drawnItem2.strokeWidth)
        Assert.assertEquals(drawnItem1.segmentPoints.toList(), drawnItem2.segmentPoints.toList())
    }

    private fun assertActionsEqual(action1: Action<CanvasObject>, action2: Action<CanvasObject>) {
        Assert.assertEquals(action1.type, action2.type)
        Assert.assertEquals(action1.items.size, action2.items.size)
        for (i in 0 until action1.items.size) {
            assertCanvasObjectsEqual(action1.items[i], action2.items[i])
        }
    }

    @Test
    fun testCreateSuccess() = runBlocking {
        val sessionId = sessionService.createSession()
        Assert.assertTrue(sessionId > 0)
    }

    @Test
    fun testAddUserSuccess() = runBlocking {
        // create session
        val sessionId = sessionService.createSession()
        val userId = sessionService.addUser(sessionId)
        Assert.assertTrue(userId > 0)
    }

    @Test
    fun testAddUserFail() = runBlocking {
        // create session
        val sessionId = sessionService.createSession()
        val userId = sessionService.addUser(-1)
        Assert.assertEquals(userId, -1)
    }



    @Test
    fun testAddAndGetActions() = runBlocking {
        val sessionId = sessionService.createSession()
        val userId1 = sessionService.addUser(sessionId)

        // send action using client2
        val canvasObject = CanvasObject(Pair(userId1, 1), CanvasObject.Shape.Line, 0UL, 5f,
            listOf(CanvasObject.Offset(0f, 0f), CanvasObject.Offset(100f, 100f), )
        )
        val addAction = Action(ActionType.ADD, listOf(canvasObject))
        sessionService.addAction(sessionId, userId1, addAction)

        // join session using different client2, receive and check action
        val userId2 = sessionService.addUser(sessionId)
        val actions = sessionService.getUserActions(sessionId, userId2)

        Assert.assertEquals(actions.size, 1)
        assertActionsEqual(actions[0], addAction)
    }
}