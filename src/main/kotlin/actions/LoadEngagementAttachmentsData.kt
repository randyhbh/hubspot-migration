package actions

import domain.Attachment
import domain.EngagementAttachments
import dtos.AttachmentResponse
import httpResponse
import io.ktor.client.call.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import log
import logEngagementAttachments
import observeRateLimitAsync

suspend fun loadEngagementAttachmentsDownloadableUrl(engagements: List<EngagementAttachments>) =
    coroutineScope {
        engagements
            .map { engagementAttachments ->
                observeRateLimitAsync(1000L) {
                    async {
                        log("starting loading attachments for engagement: ${engagementAttachments.id}")
                        val attachmentsUpdated = getFileDownloadableUrl(engagementAttachments.attachments)
                            .also { logEngagementAttachments(engagementAttachments) }
                        with(engagementAttachments) { attachments = attachmentsUpdated }
                        engagementAttachments
                    }
                }
            }.awaitAll()
    }

suspend fun getFileDownloadableUrl(attachments: List<Attachment>): List<Attachment> =
    attachments.map {
        val response: AttachmentResponse =
            httpResponse(url = "https://api.hubspot.com/filemanager/api/v3/files/${it.id}/signed-url").body()
        with(it) {
            name = response.name
            extension = response.extension
            url = response.url
        }
        it
    }