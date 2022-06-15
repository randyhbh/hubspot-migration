import dtos.DealsResponse
import io.ktor.client.call.*


suspend fun loadPipelineDeals() {
    var pair = Pair(0L, true)
    while (pair.second) {
        pair = getPipelinesDeals(pair.first)
    }
    log("Total amount of deals ${deals.size}")
}

suspend fun getPipelinesDeals(offset: Long): Pair<Long, Boolean> {
    val url = "https://api.hubspot.com/deals/v1/deal/paged"
    val response: DealsResponse = httpResponse(url, offset, 250).body()
    deals.add(response)
    return Pair(response.offset, response.hasMore)
}