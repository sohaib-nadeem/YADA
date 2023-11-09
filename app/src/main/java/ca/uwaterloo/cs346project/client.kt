package ca.uwaterloo.cs346project
import androidx.compose.ui.geometry.Offset
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

@Serializable
data class CanvasObject(
    val shape: Shape = Shape.Line,
    //val color: Color = Color.Black,
    val strokeWidth: Float = 4f,
    var start: ca.uwaterloo.cs346project.Offset = ca.uwaterloo.cs346project.Offset(0f, 0f),
    var end: ca.uwaterloo.cs346project.Offset = ca.uwaterloo.cs346project.Offset(0f, 0f)
)

@Serializable
data class Offset (val x: Float, val y: Float)

class Client {
    val server_ip = "169.254.22.221"

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
            user_id = client.get("http://$server_ip:8080/join/0").body()
            println("success joining")
        } catch (e: Exception) {
            println(e.localizedMessage)
        }
        return user_id
    }

    suspend fun send(user_id:Int, item:DrawnItem) {
        // convert DrawnItem to CanvasObject
//        val itemToSend = CanvasObject(item.shape,item.strokeWidth,
//            ca.uwaterloo.cs346project.Offset(item.start.x, item.start.y),
//            ca.uwaterloo.cs346project.Offset(item.end.x, item.end.y))

        val itemToSend = CanvasObject(item.shape,item.strokeWidth,
            ca.uwaterloo.cs346project.Offset(item.segmentPoints.first().x, item.segmentPoints.first().y),
            ca.uwaterloo.cs346project.Offset(item.segmentPoints.last().x, item.segmentPoints.last().y))
        println(item)
        try {
            val response = client.post("http://$server_ip:8080/send/$user_id") {
                contentType(ContentType.Application.Json)
                setBody(itemToSend)
            }
        } catch (e: Exception) {
            println(e.localizedMessage)
        }
    }

    suspend fun receive(user_id:Int): List<CanvasObject> {
        var items = listOf<CanvasObject>()
        try {
           items = client.get("http://$server_ip:8080/receive/$user_id").body()
        } catch (e: Exception) {
            //println(e.localizedMessage)
        }
        return items
    }


    suspend fun fakeJoin(): Int {
        return 0
    }

    suspend fun fakeSend(user_id:Int, item:DrawnItem) {
        return
    }

    suspend fun fakeReceive(user_id:Int): List<CanvasObject> {
        return listOf()
    }

}


