package domain

import dtos.AttachmentResponse
import kotlinx.serialization.Serializable

@Serializable
data class DealEngagements(val dealId: Long, val engagementsIds: List<Long>)

@Serializable
data class EngagementAttachments(val id: Long, var attachments: List<Attachment>)

@Serializable
data class Attachment(val id:Long) {
    lateinit var name: String
    lateinit var extension: String
    lateinit var url: String
    lateinit var filePath: String
}

@Serializable
data class CSVLine(
    val title: String,
    val description: String?,
    val versionData: String,
    val pathOnClient: String,
    val ownerId: Long?,
    val firstPublishLocationId: Long?,
    val recordTypeId: String,
    val tagsCsv: String?
) {

    companion object {
        fun strabagFrom(attachmentResponse: Attachment): CSVLine {
            return CSVLine(
                attachmentResponse.name,
                null,
                attachmentResponse.filePath,
                attachmentResponse.filePath,
                null,
                null,
                "0121x000003Y916AAC",
                null
            )
        }

        fun standardFrom(attachmentResponse: Attachment): CSVLine {
            return CSVLine(
                attachmentResponse.name,
                null,
                attachmentResponse.filePath,
                attachmentResponse.filePath,
                null,
                null,
                "0121x000003Y915AAC",
                null
            )
        }
    }
}