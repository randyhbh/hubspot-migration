package export

import com.opencsv.bean.StatefulBeanToCsvBuilder
import converters.toMap
import dealEngagements
import deals
import domain.Attachment
import domain.CSVLine
import engagementAttachments
import pipelines
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Paths

object CSV {
    private const val CSV_DELIMITER = ';'

    fun saveSalesForceCsv(csvLines: List<CSVLine>) {
        val currentDirectory = Paths.get("").toAbsolutePath().toString()
        val exportFolder = Paths.get(currentDirectory, "export", "export.csv")
        val writer: Writer = Files.newBufferedWriter(exportFolder)

        try {
            val csvWriter = StatefulBeanToCsvBuilder<CSVLine>(writer)
                .withSeparator(CSV_DELIMITER)
                .withIgnoreField(CSVLine::class.java, CSVLine::class.java.getField("Companion"))
                .build()

            csvWriter.write(csvLines)
        } catch (ex: Exception) {
            throw ex
        } finally {
            writer.close()
        }
    }
}

fun generateCSV() {
    deals.toMap()
        .map { (pipeline, deals) ->
            when {
                pipelines.first { it.id == pipeline }.label.contains("strabag", false) -> {
                    csvLines(deals) { CSVLine.strabagFrom(it) }
                }
                else -> csvLines(deals) { CSVLine.standardFrom(it) }
            }
        }.forEach { lines -> CSV.saveSalesForceCsv(lines) }
}

private fun csvLines(deals: Set<Long>, block: (Attachment) -> CSVLine) = deals
    .flatMap { dealId -> dealEngagements.filter { it.dealId == dealId } }
    .flatMap { engagement -> engagementAttachments.filter { it.id == engagement.dealId } }
    .flatMap { it.attachments }
    .map { block.invoke(it) }