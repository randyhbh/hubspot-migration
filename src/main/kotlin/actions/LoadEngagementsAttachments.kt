package actions

import domain.Attachment
import domain.DealEngagements
import domain.EngagementAttachments
import dtos.EngagementAttachmentsResponse
import httpResponse
import io.ktor.client.call.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import logEngagementAttachments
import observeRateLimitAsync

suspend fun loadEngagementToAttachments(engagementList: List<DealEngagements>): List<EngagementAttachments> =
    coroutineScope {
        engagementList
            .flatMap { it.engagementsIds }
            .map { engagementId ->
                observeRateLimitAsync(500L) {
                    async {
                        getAttachment(engagementId)?.also { logEngagementAttachments(it) }
                    }
                }
            }
            .awaitAll()
            .filterNotNull()
    }

suspend inline fun getAttachment(engagementId: Long): EngagementAttachments? {
    val response =
        httpResponse("https://api.hubapi.com/engagements/v1/engagements/$engagementId").body<EngagementAttachmentsResponse>()

    return when {
        response.attachments.isEmpty() -> null

        else -> {
            val attachments = response.attachments.map { Attachment(it.id) }
            EngagementAttachments(engagementId, attachments)
        }
    }
}