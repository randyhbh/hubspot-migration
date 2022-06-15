package dtos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class PipeLineResponse(@SerialName("results") val pipelines: List<HPipeline>)

@Serializable
data class HPipeline(@SerialName("pipelineId") val id: Long, val label: String)

@Serializable
data class DealsResponse(val deals: List<Deal>, val offset: Long, val hasMore: Boolean) {
    @Serializable
    data class Deal(@SerialName("dealId") val id: Long, val properties: Properties)

    @Serializable
    data class Properties(val pipeline: PipeLine)

    @Serializable
    data class PipeLine(@SerialName("value") val id: Long)
}

@Serializable
data class DealEngagementsResponse(
    @SerialName("results") val engagementsIds: List<Long>,
    val hasMore: Boolean,
    val offset: Long
)

@Serializable
data class EngagementAttachmentsResponse(val attachments: List<AttachmentIDResponse>) {
    @Serializable
    data class AttachmentIDResponse(val id: Long)
}

@Serializable
data class AttachmentResponse constructor(val name: String, val extension: String, val url: String) {
    @Transient
    lateinit var filePath: String
}