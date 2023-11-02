import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*

data class CanvasObject(
    val shape: Shape = Shape.Line,
    //val color: Color = Color.Black,
    val strokeWidth: Float = 4f,
    var start: Offset = Offset(0f, 0f),
    var end: Offset = Offset(0f, 0f)
)

data class Offset (
    val x: Float,
    val y: Float
)
enum class Shape { Rectangle, Oval, Line, StraightLine }


fun main() {
    println("Starting server...")
    var userIndices = mutableListOf<Int>()
    var objectList = mutableListOf<CanvasObject>()
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }

        routing {
            get("/join/{session_id}") {
                // send back user id
                val user_id = userIndices.size
                userIndices.add(0)
                call.respond(user_id)
            }
            post("/send/{user_id}") {
                if (call.parameters["user_id"] == null) {
                    // fail
                }
                else {
                    val user_id = call.parameters["user_id"]!!.toInt()
                    val canvasObject = call.receive<CanvasObject>()
                    objectList.add(canvasObject)
                    call.respond(0) // 0 indicates success
                }

            }
            get("/receive/{user_id}") {
                println("receive called")
                if (call.parameters["user_id"] == null) {
                    // fail
                }
                else {
                    val user_id = call.parameters["user_id"]!!.toInt()
                    var objectsToSend = listOf<CanvasObject>()
                    if (userIndices[user_id] < objectList.size) {
                        objectsToSend = objectList.subList(
                            userIndices[user_id],
                            objectList.size
                        ) // send everything at once???; safety check that < objectList.size
                        userIndices[user_id] = objectList.size
                    }
                    call.respond(objectsToSend)
                }
            }
        }
    }.start(wait = true)
}
