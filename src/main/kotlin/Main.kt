import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.createDirectories

val pipelines = mutableListOf<HPipeline>()
val pipelineToDeals = mutableMapOf<Long, Set<Long>>()
val dealsToEngagements = mutableMapOf<Long, Set<Long>>()
val engagementsToAttachmentsIds = mutableMapOf<Long, Set<Long>>()
val engagementToAttachment = mutableMapOf<Long, Set<Attachment>>()

suspend fun main() {

    getPipelines()

    var pair = Pair(0L, true)
    while (pair.second) {
        pair = getPipelinesDeals(pair.first)
    }

    pipelineToDeals.entries.map { (_, deals) -> deals.map { deal -> getDealEngagements(deal) } }

    dealsToEngagements.map { (_, engagements) ->
        engagements.map { engagement -> getEngagementToAttachments(engagement) }
    }

    dealsToEngagements.entries.map { (_, engagements) ->
        engagements.map { engagement -> getEngagementFiles(engagement) }
    }

    createDirectoryHierarchyAndDownloadFiles()

    generateCSV()
}

private suspend fun getPipelines() {
    pipelines += httpResponse("https://api.hubapi.com/crm-pipelines/v1/pipelines/deals").body<PipeLineResponse>().pipelines
}

suspend fun getPipelinesDeals(offset: Long): Pair<Long, Boolean> {
    val url = "https://api.hubspot.com/deals/v1/deal/paged"
    val response: PipelineDealsResponse = httpResponse(url, offset, 250).body()

    val currentPipelineDeals = response
        .deals
        .groupBy(keySelector = { it.properties.pipeline.id }, valueTransform = { it.id })
        .mapValues { it.value.toSet() }

    pipelineToDeals.mergeReduce(currentPipelineDeals) { prev, current -> (prev + current) }

    return Pair(response.offset, response.hasMore)
}

suspend fun getDealEngagements(id: Long) {
    var hasMore = true
    var offset = 0L

    while (hasMore) {
        val url = "https://api.hubapi.com/crm-associations/v1/associations/$id/HUBSPOT_DEFINED/11"
        val response: DealEngagementsResponse = httpResponse(url, offset, 100).body()

        dealsToEngagements.merge(id, response.engagementsIds.toSet()) { prev, current -> prev + current }

        hasMore = response.hasMore
        offset = response.offset
    }
}

suspend fun getEngagementToAttachments(id: Long) {
    val url = "https://api.hubapi.com/engagements/v1/engagements/$id"
    val response = httpResponse(url).body<JsonObject>()

    engagementsToAttachmentsIds.merge(
        response.getObject("engagement")?.get("id").toLong(),
        response.getArray("attachments")?.map { it.jsonObject["id"].toLong() }?.toSet() ?: emptySet()
    ) { prev, current -> prev + current }
}

suspend fun getEngagementFiles(engagement: Long) {
    engagementsToAttachmentsIds[engagement]
        ?.map { getFileDownloadableUrl(it) }
        ?.toSet()
        ?.let {
            engagementToAttachment.merge(engagement, it) { prev, current -> prev + current }
        }
}

suspend fun getFileDownloadableUrl(fileId: Long): Attachment =
    httpResponse(url = "https://api.hubspot.com/filemanager/api/v3/files/$fileId/signed-url").body()

suspend fun createDirectoryHierarchyAndDownloadFiles() =
    pipelineToDeals.keys.forEach { pipeline ->
        dealsToEngagements.entries.forEach { (deal, engagements) ->
            engagements.map { engagement ->
                val attachments = engagementToAttachment[engagement] ?: emptySet()
                val currentDirectory = Paths.get("").toAbsolutePath().toString()
                val pipelineLabel = pipelines.first { it.id == pipeline }.label
                val exportFolder = Paths.get(currentDirectory, "export", pipelineLabel, "$deal").createDirectories()

                attachments.map { attachment: Attachment ->
                    val file = File.createTempFile(attachment.name, ".${attachment.extension}", exportFolder.toFile())
                    attachment.filePath = file.absolutePath
                    downloadFile(file = file, url = attachment.url)
                }
            }
        }
    }

suspend fun downloadFile(file: File, url: String) {
    runBlocking {
        val httpResponse: HttpResponse = client.get(url)
        val responseBody: ByteArray = httpResponse.body()
        file.writeBytes(responseBody)
        println("A file saved to ${file.path}")
    }
}

fun generateCSV() {
    pipelineToDeals.map { (pipeline, deals) ->
        when {
            pipelines.first { it.id == pipeline }.label.contains("strabag", false) -> {
                deals
                    .flatMap { dealsToEngagements[it] ?: emptySet() }
                    .flatMap { engagementToAttachment[it] ?: emptySet() }
                    .map { CSVLine.strabagFrom(it) }
            }
            else -> {
                deals
                    .flatMap { dealsToEngagements[it] ?: emptySet() }
                    .flatMap { engagementToAttachment[it] ?: emptySet() }
                    .map { CSVLine.strabagFrom(it) }
            }
        }
    }.forEach { lines -> CSV.saveSalesForceCsv(lines) }
}