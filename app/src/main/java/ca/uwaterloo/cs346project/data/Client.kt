package ca.uwaterloo.cs346project.data

import ca.uwaterloo.cs346project.model.Action
import ca.uwaterloo.cs346project.model.CanvasObject
import ca.uwaterloo.cs346project.ui.util.DrawnItem
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*

class Client {
    val server_ip = "34.124.118.88"
    var session_id = -1
    var user_id = -1

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }

    // request server to create new session
    // returns session id
    suspend fun create(): Boolean {
        var session_user = Pair(0, 0)
        try {
            session_user = client.get("http://$server_ip:8080/create").body()
            println("success creating")
        } catch (e: Exception) {
            println(e.localizedMessage)
            return false
        }
        session_id = session_user.first
        user_id = session_user.second
        return true
    }

    suspend fun join(session_id: String): Boolean {
        var user_id = 0
        try {
            user_id = client.get("http://$server_ip:8080/join/${session_id.toInt()}").body()
            println("success joining")
            this.session_id = session_id.toInt()
            this.user_id = user_id
        } catch (e: Exception) {
            println(e.localizedMessage)
            return false
        }
        return true
    }

    suspend fun sendAction(action: Action<DrawnItem>) {
        // convert items in action from DrawnItem to CanvasObject
        val actionToSend = Action<CanvasObject>(action.type, action.items.map { toCanvasObject(it) })
        println(actionToSend)
        try {
            val response = client.post("http://$server_ip:8080/sendAction/$session_id/$user_id") {
                contentType(ContentType.Application.Json)
                setBody(actionToSend)
            }
        } catch (e: Exception) {
            println(e.localizedMessage)
        }
        return
    }

    suspend fun receiveAction(): List<Action<DrawnItem>> {
        var actionsReceived = listOf<Action<CanvasObject>>()

        try {
            actionsReceived = client.get("http://$server_ip:8080/receiveAction/$session_id/$user_id").body()
        } catch (e: Exception) {
            println(e.localizedMessage)
        }

        // convert items in actions received from CanvasObject to DrawnItem
        return actionsReceived.map {
            Action(
                it.type,
                it.items.map { canvasObject -> toDrawnItem(canvasObject) }
            )
        }
    }

}


