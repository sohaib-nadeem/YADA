package service

import models.CanvasObject

object SessionServiceInMemory {
    data class SessionOld (
        val sessionObjects: MutableList<Pair<Int, CanvasObject>> = mutableListOf(),
        val sessionUserIndices: MutableList<Int> = mutableListOf() // index of first object to send next
    )

    var sessions = mutableListOf<SessionOld>()

    fun createSession(): Int {
        val session_id = sessions.size
        sessions.add(SessionOld())
        return session_id
    }

    // return user id of new user
    fun addUser(session_id: Int?): Int {
        // check session id is valid
        if (session_id == null || session_id < 0 || session_id >= sessions.size) {
            // fail
            return (-1) // 0 indicates success
        }

        val session = sessions[session_id!!]
        val user_id = session.sessionUserIndices.size
        session.sessionUserIndices.add(0)
        return user_id
    }

    // assumes user_id is valid
    fun addObject(session_id: Int?, user_id: Int?, canvasObject: CanvasObject): Int {
        // check session and user ids are valid
        if (session_id == null || user_id == null) {
            // fail
            return -1 // 0 indicates success
        }

        val session = sessions[session_id!!]
        session.sessionObjects.add(Pair(user_id!!, canvasObject))
        return 0
    }

    // assumes user_id is valid
    fun getUserObjects(session_id: Int?, user_id: Int?): List<CanvasObject> {
        // check session and user ids are valid
        if (session_id == null || user_id == null) {
            // fail
            return listOf()
        }

        val session = sessions[session_id!!]
        val objectsToSend = session.sessionObjects
            .subList(session.sessionUserIndices[user_id!!], session.sessionObjects.size)
            .filter { it.first != user_id!! }
            .map { it.second }
        session.sessionUserIndices[user_id!!] = session.sessionObjects.size
        return objectsToSend
    }
}
