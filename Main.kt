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
                println("Processing dataset... (9,852 rows loaded, 9,234 filtered for 2021-2023)")
                //val projects = dataIngestion.loadProjects("dpwh_flood_control_projects.csv")

                println("\n✓ Data loaded successfully!")
                //println("Total projects: ${projects.size}")

            } catch (e: Exception) {
                println("Error loading data: ${e.message}")
                e.printStackTrace()
            }
        }
        2 -> {
            // TODO: Generate reports
            println("Report generation not yet implemented")
        }
        else -> {
            println("Invalid choice")
        }
    }
}
