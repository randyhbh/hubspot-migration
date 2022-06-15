import dtos.DealsResponse
import io.ktor.client.call.*


suspend fun loadPipelineDeals() {
    var pair = Pair(0L, true)
    while (pair.second) {
        log("Processed amount ${pair.first}")
        pair = getPipelinesDeals(pair.first)
    }
}

suspend fun getPipelinesDeals(offset: Long): Pair<Long, Boolean> {
    val url = "https://api.hubspot.com/deals/v1/deal/paged"
    val response: DealsResponse = httpResponse(url, offset, 250).body()
    deals.add(response)
    return Pair(response.offset, response.hasMore)
}