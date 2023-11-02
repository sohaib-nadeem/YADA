package ca.uwaterloo.cs346project
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import androidx.compose.ui.geometry.Offset
data class CanvasObject(
    val shape: Shape = Shape.Line,
    //val color: Color = Color.Black,
    val strokeWidth: Float = 4f,
    var start: Offset = Offset(0f, 0f),
    var end: Offset = Offset(0f, 0f)
)

class Client {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }

    suspend fun join(): Int {
        var user_id = 0
        try {
            user_id = client.get("http://10.32.20.234:8080/join/0").body()
            println("success joining")
        } catch (e: Exception) {
            println("error joining")
        }
        return user_id
    }

    suspend fun receive(): Int {
        var items = 0
        try {
            items = client.get("http://10.32.20.234:8080/receive").body()
        } catch (e: Exception) {
            //println("error receiving")
        }
        return items
    }

    suspend fun send(item:DrawnItem) {
        val itemToSend = CanvasObject(item.shape,item.strokeWidth,item.start, item.end)
        println(item)
        try {
            client.post("http://localhost:8080/send") {
                contentType(ContentType.Application.Json)
                setBody(itemToSend)
            }
        } catch (e: Exception) {
            println("error sending")
        }
    }
}
