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

suspend fun loadPipelineDealsConcurrent(dealsResponseList: List<DealsResponse>): List<DealEngagements> =
    coroutineScope {
        dealsResponseList.flatMap {
            it.deals
        }.map { deal ->
            async {
                log("starting loading for deal: $deal")
                getDealEngagements(deal.id)
                    .also { logDealEngagements(it) }
            }
        }.awaitAll()
    }

suspend fun getDealEngagements(id: Long): DealEngagements {
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

    return DealEngagements(dealId = id, engagementsIds = engagementsIds)
}
