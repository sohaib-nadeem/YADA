package plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import models.CanvasObject
import service.SessionServiceInMemory

// move input handling to service class!!!
fun Application.configureRouting() {
    val sessionService = SessionServiceInMemory // SessionService()
    routing {
        get("/create") {
            //print("create called")
            // create a new session and initial user for that session
            // send back session and user id
            val session_id = sessionService.createSession()
            val user_id = sessionService.addUser(session_id)

            call.respond(Pair(session_id, user_id))
        }

        get("/join/{session_id}") {
            //println("join called")
            // send back user id
            val session_id = call.parameters["session_id"]?.toInt()
            val user_id = sessionService.addUser(session_id)
            // check user id returned is valid (probably better to use exceptions???)
            if (user_id >= 0) {
                call.respond(user_id) // call.respond(HttpStatusCode.OK, user_id)
            }
            else {
                // fail
                call.respond(HttpStatusCode.NotFound)
            }
            // need to check status on client and repeat request or handle failure somehow!!!
        }

        post("/send/{session_id}/{user_id}") {
            //println("send called")
            val session_id = call.parameters["session_id"]?.toInt()
            val user_id = call.parameters["user_id"]?.toInt()

            val canvasObject = call.receive<CanvasObject>()
            val status = sessionService.addObject(session_id, user_id, canvasObject)

            // check session and user ids are valid
            if (status == 0) {
                call.respond(HttpStatusCode.OK)
            } else {
                // fail
                call.respond(HttpStatusCode.NotFound)
            }
            // need to check status on client and repeat request or handle failure somehow!!!
        }
        get("/receive/{session_id}/{user_id}") {
            //println("receive called")
            val session_id = call.parameters["session_id"]?.toInt()
            val user_id = call.parameters["user_id"]?.toInt()

            val objectsToSend = sessionService.getUserObjects(session_id, user_id)
            //objectsToSend.forEach({println(it)})
            call.respond(objectsToSend)
        }
    }
}
