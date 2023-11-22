import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
import plugins.configureRouting
import plugins.configureSerialization

fun main() {
    println("Starting server...")

    val server_ip = "127.0.0.1" //"172.20.10.2" //"169.254.22.221"

    embeddedServer(Netty, port = 8080, host = server_ip) {
        configureSerialization()
        configureRouting()
    }.start(wait = true)

    //io.ktor.server.netty.EngineMain.main(args)
}
