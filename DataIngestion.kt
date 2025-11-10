import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.FileReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class DataIngestion {

    // REQ-0001: Read CSV file
    fun loadProjects(filename: String, showOutput: Boolean = true): List<FloodControlProject> {
        val projects = mutableListOf<FloodControlProject>()

        // Open the CSV file and create a parser
        val fileReader = FileReader(filename)
        val csvParser = CSVParser(fileReader, CSVFormat.DEFAULT
            .withFirstRecordAsHeader()  // First row should be headers
            .withIgnoreHeaderCase()     // Ignore case when matching column names
            .withTrim())                // Trim whitespace

        var totalRows = 0
        var errorCount = 0
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        // Loop through each record in the CSV
        for (record in csvParser) {
            totalRows++

            try {
                // REQ-0003 - Filter only 2021-2023 projects
                val fundingYear = parseInt(record["FundingYear"])
                if (fundingYear == null || fundingYear !in 2021..2023) {
                    continue
                }

                // REQ-0005 - Parse financial fields as doubles
                val approvedBudget = parseDouble(record["ApprovedBudgetForContract"]) ?: 0.0
                val contractCost = parseDouble(record["ContractCost"]) ?: 0.0

                // REQ-0005 - Parse dates
                val startDate = parseDate(record["StartDate"], dateFormatter)
                val actualCompletionDate = parseDate(record["ActualCompletionDate"], dateFormatter)

                // REQ-0004 - Calculate costSavings
                val costSavings = approvedBudget - contractCost

                // REQ-0004 - Calculate completionDelayDays
                val completionDelayDays = if (startDate != null && actualCompletionDate != null) {
                    ChronoUnit.DAYS.between(startDate, actualCompletionDate)
                } else {
                    0L  // Default to 0 if dates are missing
                }

                // Create FloodControlProject object and add to projects list
                val project = FloodControlProject(
                    region = record["Region"] ?: "",
                    mainIsland = record["MainIsland"] ?: "",
                    province = record["Province"] ?: "",
                    approvedBudgetForContract = approvedBudget,
                    contractCost = contractCost,
                    startDate = startDate,
                    actualCompletionDate = actualCompletionDate,
                    fundingYear = fundingYear,
                    typeOfWork = record["TypeOfWork"] ?: "",
                    contractor = record["Contractor"] ?: "",
                    costSavings = costSavings,
                    completionDelayDays = completionDelayDays
                )

                projects.add(project)

            } catch (e: Exception) {
                errorCount++
                // REQ-0002: Log errors
                if (errorCount <= 5) {
                    println("Warning: Error parsing row $totalRows: ${e.message}")
                }
            }
        }

        csvParser.close()

        // REQ-0002: Log total row count
        if (showOutput) {
            print("($totalRows rows loaded, ")
            if (errorCount > 0) {
                print("$errorCount errors encountered, ")
            }
            print("${projects.size} filtered for 2021-2023)\n")
        }
        return projects
    }

    // HELPER FUNCTIONS (for parsing)
    private fun parseInt(value: String?): Int? {
        return try {
            value?.trim()?.toIntOrNull()
        } catch (e: Exception) {
            null
        }
    }

    private fun parseDouble(value: String?): Double? {
        return try {
            value?.trim()?.replace(",", "")?.toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }

    private fun parseDate(value: String?, formatter: DateTimeFormatter): LocalDate? {
        return try {
            if (value.isNullOrBlank()) null
            else LocalDate.parse(value.trim(), formatter)
        } catch (e: Exception) {
            null
        }
    }
}