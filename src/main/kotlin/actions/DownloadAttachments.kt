package actions

import client
import converters.toMap
import dealEngagements
import deals
import domain.Attachment
import engagementAttachments
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import pipelines
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.createDirectories

suspend fun createDirectoryHierarchyAndDownloadFiles() {

    deals.toMap().keys.forEach { pipeline ->
        dealEngagements.forEach { (deal, engagements) ->
            engagements.map { engagement ->
                val attachments = engagementAttachments.toMap()[engagement] ?: emptySet()
                val currentDirectory = Paths.get("").toAbsolutePath().toString()
                val pipelineLabel = pipelines.first { it.id == pipeline }.label
                val exportFolder = Paths.get(currentDirectory, "export", pipelineLabel, "$deal").createDirectories()

                attachments.map { attachment: Attachment ->
                    val file = File.createTempFile(
                        attachment.name,
                        ".${attachment.extension}",
                        exportFolder.toFile()
                    )
                    attachment.filePath = file.absolutePath
                    downloadFile(file = file, url = attachment.url)
                }
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