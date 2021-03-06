package actions

import domain.DealEngagements
import dtos.DealEngagementsResponse
import dtos.DealsResponse
import httpResponse
import io.ktor.client.call.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import log
import logDealEngagements
import observeRateLimitAsync

suspend fun loadPipelineDealsConcurrent(dealsResponseList: List<DealsResponse>): List<DealEngagements> =
    coroutineScope {
        val deals = dealsResponseList.flatMap { it.deals }
        val dealSize = deals.size

        deals
            .mapIndexed { index, deal ->
                observeRateLimitAsync(500L) {
                    async {
                        log("starting loading for deal: $index of $dealSize")
                        getDealEngagements(deal.id)?.also { logDealEngagements(it) }
                    }
                }
            }.awaitAll()
            .filterNotNull()
    }

suspend fun getDealEngagements(id: Long): DealEngagements? {
    var hasMore = true
    var offset = 0L

    val engagementsIds = mutableListOf<Long>()
    while (hasMore) {
        val url = "https://api.hubapi.com/crm-associations/v1/associations/$id/HUBSPOT_DEFINED/11"
        val response: DealEngagementsResponse = httpResponse(url, offset, 100).body()

        engagementsIds.addAll(response.engagementsIds)

        hasMore = response.hasMore
        offset = response.offset
    }
    if (engagementsIds.isEmpty()) return null
    return DealEngagements(dealId = id, engagementsIds = engagementsIds)
}
