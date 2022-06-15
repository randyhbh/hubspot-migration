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
    engine {
        // this: CIOEngineConfig
        maxConnectionsCount = 1000
        endpoint {
            // this: EndpointConfig
            maxConnectionsPerRoute = 100
            pipelineMaxSize = 20
            keepAliveTime = 5000
            connectTimeout = 50000
            connectAttempts = 100
            socketTimeout = 50000
            requestTimeout = 5000
        }
    }
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
        parameter("hapikey", "1990e9bd-3c3f-4a16-8cde-487c968503e5")
        parameter("properties", "pipeline")
        offset?.let { parameter("offset", offset) }
        limit?.let { parameter("limit", limit) }
        download?.let { onDownload { bytesSentTotal, contentLength -> println("Received $bytesSentTotal bytes from $contentLength") } }
    }