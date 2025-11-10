fun main() {
    println("Select Language Implementation:")
    println("[1] Load the file")
    println("[2] Generate Reports")
    println()
    print("Enter choice: ")

    val choice = readLine()?.toIntOrNull() ?: 1

    when (choice) {
        1 -> {
            // Load and process the CSV file
            val dataIngestion = DataIngestion()

            try {
                print("Processing dataset...")
                val projects = dataIngestion.loadProjects("dpwh_flood_control_projects.csv")

            } catch (e: Exception) {
                println("Error loading data: ${e.message}")
                e.printStackTrace()
            }
        }
        2 -> {
            // Generate reports
            val dataIngestion = DataIngestion()

            try {
                println()
                println("Generating reports...")
                println("Outputs saved to individual files…")
                println()

                val projects = dataIngestion.loadProjects("dpwh_flood_control_projects.csv", showOutput = true)

                val reportGenerator = ReportGenerator(projects)

                reportGenerator.generateReport1()

                reportGenerator.generateReport2()

                reportGenerator.generateReport3()

                reportGenerator.generateSummaryJson()

                println()
                print("Back to Report Selection (Y/N): ")

            } catch (e: Exception) {
                println("Error generating reports: ${e.message}")
                e.printStackTrace()
            }
        }
        else -> {
            println("Invalid choice")
        }
    }
}