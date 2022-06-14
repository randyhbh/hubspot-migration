import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class PipeLineResponse(@SerialName("results") val pipelines: List<HPipeline>)

@Serializable
data class HPipeline(@SerialName("pipelineId") val id: Long, val label: String)

@Serializable
data class Attachment constructor(val name: String, val extension: String, val url: String) {
    @Transient
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
        fun strabagFrom(attachment: Attachment): CSVLine {
            return CSVLine(
                attachment.name,
                null,
                attachment.filePath,
                attachment.filePath,
                null,
                null,
                "0121x000003Y916AAC",
                null
            )
        }

        fun standardFrom(attachment: Attachment): CSVLine {
            return CSVLine(
                attachment.name,
                null,
                attachment.filePath,
                attachment.filePath,
                null,
                null,
                "0121x000003Y915AAC",
                null
            )
        }
    }
}

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
