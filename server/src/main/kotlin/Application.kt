import io.ktor.server.engine.*
import io.ktor.server.netty.*
import plugins.configureRouting
import plugins.configureSerialization
import models.ActiveUser
import models.ObjectPoint
import models.Session
import models.SessionObject
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

val sessionData: Database = Database.connect("jdbc:sqlite:data.db", "org.sqlite.JDBC") // whiteboardSessions.db
fun main() {
    println("Starting server...")

    // connection to sqlite DB
    //val sessionData = Database.connect("jdbc:sqlite:data.db", "org.sqlite.JDBC") // whiteboardSessions.db
    transaction(sessionData) {
        // print sql to std-out
        addLogger(StdOutSqlLogger)

        // create tables (Session, ActiveUser, SessionObject) if they don't exist
        SchemaUtils.create (Session)
        SchemaUtils.create (ActiveUser)
        SchemaUtils.create (SessionObject)
        SchemaUtils.create (ObjectPoint)
    }

    val server_ip = "172.20.10.2" //"169.254.22.221"

    embeddedServer(Netty, port = 8080, host = server_ip) {
        configureSerialization()
        configureRouting()
    }.start(wait = true)

    //io.ktor.server.netty.EngineMain.main(args)
}
