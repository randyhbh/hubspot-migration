import actions.*
import domain.DealEngagements
import domain.EngagementAttachments
import dtos.AttachmentResponse
import dtos.DealsResponse
import dtos.HPipeline
import export.generateCSV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

val pipelines = mutableListOf<HPipeline>()

val deals = mutableListOf<DealsResponse>()
var dealEngagements: List<DealEngagements> = emptyList()
var engagementAttachments: List<EngagementAttachments> = emptyList()

suspend fun main() = coroutineScope {

    getPipelines()

    loadPipelineDeals()

    dealEngagements = loadPipelineDealsConcurrent(deals)

    engagementAttachments = loadEngagementToAttachments(dealEngagements)

    engagementAttachments = loadEngagementAttachmentsDownloadableUrl(engagementAttachments)

    createDirectoryHierarchyAndDownloadFiles()

    generateCSV()

    client.close()
}