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
import log
import logEngagementAttachments

suspend fun loadEngagementToAttachments(engagementList: List<DealEngagements>): List<EngagementAttachments> =
    coroutineScope {
        engagementList
            .flatMap { it.engagementsIds }
            .map { engagementId ->
                async {
                    log("starting loading for engagementId: $engagementId")
                    getAttachment(engagementId)
                        .also { logEngagementAttachments(it) }
                }
            }
            .awaitAll()
    }

private suspend fun getAttachment(engagementId: Long): EngagementAttachments {
    val response =
        httpResponse("https://api.hubapi.com/engagements/v1/engagements/$engagementId").body<EngagementAttachmentsResponse>()

    return when {
        response.attachments.isEmpty() -> EngagementAttachments(engagementId, emptyList())

        else -> {
            val attachments = response.attachments.map { Attachment(it.id) }
            EngagementAttachments(engagementId, attachments)
        }
    }
}