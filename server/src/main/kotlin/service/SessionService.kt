package service

import models.Action
import models.ActionObject
import models.ActionType
import models.CanvasObject
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import sessionData
import models.ActiveUser
import models.ObjectPoint
import models.Session
import models.SessionAction
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert

//import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction


class SessionService {
    fun createSession(): Int {
        println("${Thread.currentThread().name}")

        // create a new session and return session_id
        var session_id = -1
        transaction(sessionData) {
            // print sql to std-out
            addLogger(StdOutSqlLogger)

            // INSERT INTO Session (sessionCode) VALUES ("ABC123")
            session_id = (Session.insert {
                it[sessionCode] = "ABC123"
            } get Session.id).value
        }
        return session_id
    }

    fun addUser(session_id: Int?): Int {
        println("${Thread.currentThread().name}")

        // check session id is valid
        if (session_id == null || session_id < 0) {
            // fail
            return (-1) // 0 indicates success
        }

        // create a new user and return user id
        var user_id = -1
        transaction(sessionData) {
            // print sql to std-out
            addLogger(StdOutSqlLogger)

            user_id = (ActiveUser.insert {
                it[sessionId] = session_id
                it[initialReceiveActionId] = 0
            } get ActiveUser.id).value
        }
        return user_id
    }

    // assumes user_id is valid
    fun addAction(session_id: Int?, user_id: Int?, action: Action<CanvasObject>): Int {
        // check session and user ids are valid
        if (session_id == null || user_id == null) {
            // fail
            return -1 // 0 indicates success
        }

        val status = transaction(sessionData) {
            // print sql to std-out
            addLogger(StdOutSqlLogger)

            val action_id = (SessionAction.insert {
                it[sessionId] = session_id
                it[userId] = user_id
                it[actionType] = action.type.toUInt()
            } get SessionAction.id).value

            println(action_id)

            action.items.forEach { canvasObject ->
                val objectId = (ActionObject.insert {
                    it[actionId] = action_id
                    it[shape] = canvasObject.shape.toUInt()
                    it[color] = canvasObject.color.toLong()
                    it[strokeWidth] = canvasObject.strokeWidth
                    it[userObjectIdUser] = canvasObject.userObjectId.first
                    it[userObjectIdObject] = canvasObject.userObjectId.second
                } get ActionObject.id).value

                var sequenceNumber = 0
                val result = ObjectPoint.batchInsert(canvasObject.segmentPoints) { point ->
                    this[ObjectPoint.objectId] = objectId
                    this[ObjectPoint.sequenceNumber] = sequenceNumber
                    this[ObjectPoint.xVal] = point.x
                    this[ObjectPoint.yVal] = point.y
                    sequenceNumber++
                }
            }

            return@transaction 0
        }

        return status
    }

    // assumes user_id is valid
    fun getUserActions(session_id: Int?, user_id: Int?): List<Action<CanvasObject>> {
        // check session and user ids are valid
        if (session_id == null || user_id == null) {
            // fail
            return listOf()
        }

        val actionsToSend = transaction(sessionData) {
            // print sql to std-out
            addLogger(StdOutSqlLogger)

            var initialReceiveActionId = ActiveUser
                .select() { ActiveUser.id eq user_id }
                .single()[ActiveUser.initialReceiveActionId]

            // get actions to send from SessionAction table along with their ids
            val actionsToSendWithIds = SessionAction
                .select() {
                    (SessionAction.sessionId eq session_id) and
                            (SessionAction.userId neq user_id!!) and
                            (SessionAction.id greaterEq initialReceiveActionId)
                }
                .map {
                    Pair(
                        it[SessionAction.id].value,
                        Action<CanvasObject>(
                            ActionType.fromUInt(it[SessionAction.actionType]),
                            listOf()
                        )
                    )
                }

            // get objects for each action
            actionsToSendWithIds.forEach { (actionId, action) ->
                // get objects to send from SessionObject table
                val objectsWithIds = ActionObject
                    .select() {
                        (ActionObject.actionId eq actionId)
                    }
                    .orderBy(ActionObject.id to SortOrder.ASC)
                    .map {
                        Pair(
                            it[ActionObject.id].value,
                            CanvasObject(
                                Pair(
                                    it[ActionObject.userObjectIdUser],
                                    it[ActionObject.userObjectIdObject]
                                ),
                                CanvasObject.Shape.fromUInt(it[ActionObject.shape]),
                                it[ActionObject.color].toULong(),
                                it[ActionObject.strokeWidth],
                                listOf()
                            )
                        )
                    }

                // get segmentPoints for each object
                objectsWithIds.forEach() { (objectId, canvasObject) ->
                    canvasObject.segmentPoints = ObjectPoint
                        .select() {
                            (ObjectPoint.objectId eq objectId)
                        }
                        .orderBy(ObjectPoint.sequenceNumber to SortOrder.ASC)
                        .map {
                            CanvasObject.Offset(
                                it[ObjectPoint.xVal],
                                it[ObjectPoint.yVal]
                            )
                        }
                }

                // get only the objects from objectsWithIds
                action.items = objectsWithIds.map { it.second }
            }

            // update initialReceiveObjectId
            initialReceiveActionId = actionsToSendWithIds.maxOf { it.first } + 1

            ActiveUser.update({ ActiveUser.id eq user_id }) {
                it[ActiveUser.initialReceiveActionId] = initialReceiveActionId
            }

            return@transaction actionsToSendWithIds
        }.map { it.second }

        if (actionsToSend.size > 0) {
            //println("initialReceiveObjectId is: ${initialReceiveActionId}")
            println("objectsToSend is not empty: ${actionsToSend}")
        }

        return actionsToSend
    }
}