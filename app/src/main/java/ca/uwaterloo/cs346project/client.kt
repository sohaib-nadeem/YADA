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
            user_id = client.get("http://localhost:8080/join/0").body()
        } catch (e: Exception) {
            println("error joining")
        }
        return user_id
    }

    suspend fun receive(): List<DrawnItem> {
        var items = listOf<DrawnItem>()
        try {
            items = client.get("http://localhost:8080/receive").body()
        } catch (e: Exception) {
            println("error receiving")
        }
        return items
    }

    suspend fun send(item:DrawnItem) {
        println(item)
        try {
            client.post("http://localhost:8080/send") {
                contentType(ContentType.Application.Json)
                setBody(item)
            }
        } catch (e: Exception) {
            println("error sending")
        }
    }
}
