import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        })
    }
}

suspend fun httpResponse(
    url: String,
    offset: Long? = null,
    limit: Long? = null,
    download: Boolean? = null
): HttpResponse =
    client.get(url) {
        parameter("hapikey", "")
        parameter("properties", "pipeline")
        offset?.let { parameter("offset", offset) }
        limit?.let { parameter("limit", limit) }
        download?.let { onDownload { bytesSentTotal, contentLength -> println("Received $bytesSentTotal bytes from $contentLength") } }
    }.let {
        val requestRemaining =
            it.headers["X-HubSpot-RateLimit-Remaining"]?.toInt() ?: error("Unknown rate limiting Header")

        val secondsRemaining =
            it.headers["X-HubSpot-RateLimit-Secondly-Remaining"]?.toInt() ?: error("Unknown rate limiting Header")

        when (requestRemaining) {
            0 -> delay(secondsRemaining.seconds + 1.seconds)
        }

        it
    }