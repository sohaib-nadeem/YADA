package service

//import DatabaseFactory.dbQuery
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
import models.SessionObject
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

fun ULong.toLongReinterpret(): Long {
    if (this > Long.MAX_VALUE.toULong()) {
        return this.toLong()
    }
    else {

    }
    return 0L
}

fun Long.toULongReinterpret(): ULong {
    if (this > ULong.MAX_VALUE.toLong()) {

    }
    else {

    }
    return 0UL
}

class SessionService {
    /*
    suspend fun getAllUsers(): List<ProfileType> = dbQuery {
        Profile.selectAll().map { toProfileType(it) }
    }

    suspend fun getProfileByEmail(email: String): ProfileType? = dbQuery {
        Profile.select {
            (Profile.email eq email)
        }.mapNotNull { toProfileType(it) }
            .singleOrNull()
    }

    suspend fun registerProfile(email: String, passwordHash: String) = dbQuery {
        Profile.insert {
            it[Profile.email] = email
            it[password] = passwordHash
        }
    }

    private fun toProfileType(row: ResultRow): ProfileType =
        ProfileType(
            id = row[Profile.id],
            email = row[Profile.email],
            password = row[Profile.password]
        )
    */

    // change to suspend functions!!!

    // return session code too!!!
    fun createSession(): Int {
        println("${Thread.currentThread().name}")

        // create a new session and return session_id
        var session_id = -1
        transaction(sessionData) {
            // print sql to std-out
            addLogger(StdOutSqlLogger)

            // INSERT INTO Session (sessionCode) VALUES ("ABC123")
            session_id = (Session.insert {
                it[sessionCode] = "ABC123" // needs to be random
            } get Session.id).value

            // print row inserted
            //println("Session: ${Session.selectAll()}")
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
                it[initialReceiveObjectId] = 0 //???
            } get ActiveUser.id).value

            // print row inserted
            //println("ActiveUser: ${ActiveUser.selectAll()}")
        }
        return user_id
    }

    // assumes user_id is valid
    fun addObject(session_id: Int?, user_id: Int?, canvasObject: CanvasObject): Int {
        // check session and user ids are valid
        if (session_id == null || user_id == null) {
            // fail
            return -1 // 0 indicates success
        }

        val status = transaction(sessionData) {
            // print sql to std-out
            addLogger(StdOutSqlLogger)

            val objectId = (SessionObject.insert {
                it[sessionId] = session_id
                it[userId] = user_id
                it[shape] = canvasObject.shape.toUInt()
                it[color] = canvasObject.color.toLong()
                it[strokeWidth] = canvasObject.strokeWidth
            } get SessionObject.id).value
            println(objectId)

            var sequenceNumber = 0
            val result = ObjectPoint.batchInsert(canvasObject.segmentPoints) { point ->
                this[ObjectPoint.objectId] = objectId
                this[ObjectPoint.sequenceNumber] = sequenceNumber
                this[ObjectPoint.xVal] = point.x
                this[ObjectPoint.yVal] = point.y
                sequenceNumber++
            }

            return@transaction 0
        }
        // need to handle failed queries!!! (in all methods)

        return status
    }

    // assumes user_id is valid
    fun getUserObjects(session_id: Int?, user_id: Int?): List<CanvasObject> {
        // check session and user ids are valid
        if (session_id == null || user_id == null) {
            // fail
            return listOf()
        }

        var objectsToSend = listOf<CanvasObject>()

        transaction(sessionData) {
            // print sql to std-out
            addLogger(StdOutSqlLogger)

            var initialReceiveObjectId = ActiveUser
                .select() { ActiveUser.id eq user_id }// compare session id??? depends on whethe user_id id local or global!!!
                .single()[ActiveUser.initialReceiveObjectId]
            //.first()[ActiveUser.initialReceiveObjectId]
            //.let { ActiveUser.id }

            // get objects to send from SessionObject table
            objectsToSend = SessionObject
                .select() {
                    (SessionObject.sessionId eq session_id) and
                    (SessionObject.userId neq user_id!!) and
                    (SessionObject.id greaterEq initialReceiveObjectId)
                }
                .map {
                    CanvasObject(
                        it[SessionObject.id].value,
                        CanvasObject.Shape.fromUInt(it[SessionObject.shape]),
                        it[SessionObject.color].toULong(),
                        it[SessionObject.strokeWidth],
                        listOf()
                    )
                }

            objectsToSend.forEach() {
                it.segmentPoints = ObjectPoint
                    .select() {
                        (ObjectPoint.objectId eq it.objectId)
                    }
                    .orderBy(ObjectPoint.sequenceNumber to SortOrder.ASC)
                    .map {
                        CanvasObject.Offset(
                            it[ObjectPoint.xVal],
                            it[ObjectPoint.yVal]
                        )
                    }
            }

            // optimize the above two queries with a join!!!

            // update initialReceiveObjectId
            initialReceiveObjectId = objectsToSend.last().objectId + 1

            ActiveUser.update({ ActiveUser.id eq user_id }) {
                it[ActiveUser.initialReceiveObjectId] = initialReceiveObjectId
            }

            // object id needed by user?
            //return@transaction 0
            if (objectsToSend.size > 0) {
                println("initialReceiveObjectId is: ${initialReceiveObjectId}")
                println("objectsToSend is not empty: ${objectsToSend}")
            }
        }

        return objectsToSend
    }
}