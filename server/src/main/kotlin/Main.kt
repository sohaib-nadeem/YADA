import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable
data class CanvasObject(
    val shape: Shape = Shape.Line,
    val color: ULong = 0UL,
    val strokeWidth: Float = 4f,
    val segmentPoints : List<Offset> = listOf()
) {
    @Serializable
    data class Offset (val x: Float, val y: Float)

    @Serializable
    enum class Shape { Rectangle, Oval, Line, StraightLine }
}

data class Session(
    var sessionObjects: MutableList<Pair<Int, CanvasObject>> = mutableListOf(),
    var sessionUserIndices: MutableList<Int> = mutableListOf(), // index of first object to send next
) {
    // return user id of new user
    fun addUser(): Int {
        val user_id = sessionUserIndices.size
        sessionUserIndices.add(0)
        return user_id
    }

    // assumes user_id is valid
    fun addObject(user_id: Int, canvasObject: CanvasObject) {
        sessionObjects.add(Pair(user_id, canvasObject))
    }

    // assumes user_id is valid
    fun getUserObjects(user_id: Int): List<CanvasObject> {
        var objectsToSend = sessionObjects
            .subList(sessionUserIndices[user_id], sessionObjects.size)
            .filter { it.first != user_id }
            .map { it.second }
        sessionUserIndices[user_id] = sessionObjects.size
        return objectsToSend
    }
}

fun main() {
    println("Starting server...")
    var sessions = mutableListOf<Session>()
    val server_ip = "172.20.10.2" //"169.254.22.221"

    embeddedServer(Netty, port = 8080, host = server_ip) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }

        routing {
            get("/create") {
                //print("create called")
                // create a new session and initial user for that session
                // send back session and user id
                val session_id = sessions.size
                sessions.add(Session())
                val user_id = sessions[session_id].addUser()

                call.respond(Pair(session_id, user_id))
            }

            get("/join/{session_id}") {
                //println("join called")
                // send back user id
                val session_id = call.parameters["session_id"]?.toInt()

                // check session id is valid
                if (session_id == null || session_id < 0 || session_id >= sessions.size) {
                    // fail
                    call.respond(-1) // 0 indicates success
                }

                val user_id = sessions[session_id!!].addUser()
                call.respond(user_id)
            }

            post("/send/{session_id}/{user_id}") {
                //println("send called")
                val session_id = call.parameters["session_id"]?.toInt()
                val user_id = call.parameters["user_id"]?.toInt()

                // check session and user ids are valid
                if (session_id == null || user_id == null) {
                    // fail
                    call.respond(-1) // 0 indicates success
                }

                val canvasObject = call.receive<CanvasObject>()
                sessions[session_id!!].addObject(user_id!!, canvasObject)
                call.respond(0) // 0 indicates success
            }
            get("/receive/{session_id}/{user_id}") {
                //println("receive called")
                val session_id = call.parameters["session_id"]?.toInt()
                val user_id = call.parameters["user_id"]?.toInt()

                // check session and user ids are valid
                if (session_id == null || user_id == null) {
                    // fail
                    return@get //call.respond(-1) // 0 indicates success
                }

                var objectsToSend = sessions[session_id!!].getUserObjects(user_id!!)
                objectsToSend.forEach({println(it)})
                call.respond(objectsToSend)
            }
        }
    }.start(wait = true)
}
