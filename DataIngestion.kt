import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.FileReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DataIngestion {

    // REQ-0001: Read CSV file
    fun readCSV(filename: String): List<FloodControlProject> {
        val projects = mutableListOf<FloodControlProject>()

        // TODO: Open the CSV file and create a parser
        val fileReader = FileReader(filename)
        val csvParser = CSVParser(fileReader, CSVFormat.DEFAULT
            .withFirstRecordAsHeader()  // First row is headers
            .withIgnoreHeaderCase()     // Ignore case when matching column names
            .withTrim())                // Trim whitespace

        var totalRows = 0
        var errorCount = 0

        for (record in csvParser) {
            totalRows++

            try {
                // TODO: REQ-0003 - Filter only 2021-2023 projects
                // Hint: Get "FundingYear" from record and check if it's in range
                val fundingYear = record["FundingYear"]?.toIntOrNull()

                if (fundingYear == null) {
                    continue
                }

                if (fundingYear !in 2021..2023) {
                    continue
                }
                // TODO: REQ-0005 - Parse financial fields as doubles
                // Hint: Get "ApprovedBudgetForContract" and "ContractCost" from record
                val approvedBudget = record["ApprovedBudget"]?.toDoubleOrNull()
                val contractCost = record["ContractCost"]?.toDoubleOrNull()


                // TODO: REQ-0005 - Parse dates
                // Hint: Get "StartDate" and "ActualCompletionDate" from record

                // TODO: REQ-0004 - Calculate costSavings
                // Formula: ApprovedBudgetForContract - ContractCost

                // TODO: REQ-0004 - Calculate completionDelayDays
                // Formula: days between StartDate and ActualCompletionDate
                // Hint: Use ChronoUnit.DAYS.between(startDate, endDate)

                // TODO: Create FloodControlProject object and add to projects list

            } catch (e: Exception) {
                errorCount++
                // REQ-0002: Log errors but continue
                if (errorCount <= 5) {
                    println("Warning: Error parsing row $totalRows: ${e.message}")
                }
            }
        }

        csvParser.close()

        // REQ-0002: Log validation info
        println("Total rows read: $totalRows")
        println("Errors encountered: $errorCount")
        println("Valid projects (2021-2023): ${projects.size}")

        return projects
    }

    // Helper: Safely parse integers
    private fun parseIntSafe(value: String?): Int? {
        // TODO: Implement safe integer parsing
        // Hint: Use try-catch and toIntOrNull()z
        return null
    }

    // Helper: Safely parse doubles (REQ-0005)
    private fun parseDoubleSafe(value: String?): Double? {
        // TODO: Implement safe double parsing
        // Hint: Remove commas from numbers first, then use toDoubleOrNull()
        return null
    }

    // Helper: Safely parse dates (REQ-0005)
    private fun parseDateSafe(value: String?, formatter: DateTimeFormatter): LocalDate? {
        // TODO: Implement safe date parsing
        // Hint: Check if value is blank first, then use LocalDate.parse()
        return null
    }
}