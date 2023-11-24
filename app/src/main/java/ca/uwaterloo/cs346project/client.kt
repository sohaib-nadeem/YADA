package ca.uwaterloo.cs346project
import android.graphics.Canvas
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
    val color: ULong = 0UL,
    val strokeWidth: Float = 4f,
    val segmentPoints : List<Offset> = listOf()
) {
    @Serializable
    data class Offset (val x: Float, val y: Float)

}
fun toCanvasObject(item: DrawnItem): CanvasObject {
    return CanvasObject(item.shape,item.color.value,item.strokeWidth, segmentPoints = item.segmentPoints.toList().map{
        CanvasObject.Offset(it.x, it.y)
    })
}

fun toDrawnItem(canvasobject: CanvasObject): DrawnItem {

    return DrawnItem(
        0,
        canvasobject.shape,
        Color(canvasobject.color),
        canvasobject.strokeWidth,
        canvasobject.segmentPoints.map {
            Offset(it.x,it.y)
        }.toMutableStateList())
}

class Client {
    val server_ip = "169.254.137.47"
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

    suspend fun send(item:DrawnItem) {
        val itemToSend = toCanvasObject(item)
        println(item)
        try {
            val response = client.post("http://$server_ip:8080/send/$session_id/$user_id") {
                contentType(ContentType.Application.Json)
                setBody(itemToSend)
            }
        } catch (e: Exception) {
            println(e.localizedMessage)
        }
    }

    suspend fun receive(): List<DrawnItem> {
        var items = listOf<CanvasObject>()
        try {
           items = client.get("http://$server_ip:8080/receive/$session_id/$user_id").body()
        } catch (e: Exception) {
            println(e.localizedMessage)
        }
        return items.map({toDrawnItem(it)})
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

    suspend fun sendAction(action:Action) {
        return
    }

    suspend fun receiveAction(action:Action): List<Action> {
        return listOf()
    }

}




