import com.opencsv.bean.StatefulBeanToCsvBuilder
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