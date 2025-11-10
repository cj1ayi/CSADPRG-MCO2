import java.io.File
import java.io.FileWriter
import com.google.gson.Gson
import com.google.gson.GsonBuilder

class ReportGenerator(private val projects: List<FloodControlProject>) {

    // REQ-0006: Generate Report 1 - Regional Flood Mitigation Efficiency Summary
    fun generateReport1(outputPath: String = "report1_regional_summary.csv") {
        println("\nReport 1: Regional Flood Mitigation Efficiency Summary")

        // S Group projects by Region and MainIsland
        val grouped = projects.groupBy { project ->
            "${project.region}|${project.mainIsland}"
        }

        data class RegionalSummary(
            val region: String,
            val mainIsland: String,
            val totalBudget: Double,
            val medianSavings: Double,
            val avgDelay: Double,
            val highDelayPct: Double,
            val efficiencyScore: Double
        )

        // Calculate metrics for each group
        val summaries = grouped.map { (key, groupProjects) ->
            // Split the key back into region and mainIsland
            val parts = key.split("|")
            val region = parts[0]
            val mainIsland = parts[1]

            val totalBudget = groupProjects.sumOf { it.approvedBudgetForContract }
            val medianSavings = groupProjects.map { it.costSavings }.median()
            val avgDelay = groupProjects.map { it.completionDelayDays.toDouble() }.average()
            val projectsWithHighDelay = groupProjects.count { it.completionDelayDays > 30 }
            val highDelayPct = (projectsWithHighDelay * 100.0) / groupProjects.size

            // Normalize
            val rawScore: Double
            if (avgDelay > 0) {
                rawScore = (medianSavings / avgDelay) * 100
            } else {
                rawScore = 0.0
            }
            val efficiencyScore = rawScore.coerceIn(0.0, 100.0)  // Cap between 0 and 10
            RegionalSummary(
                region = region,
                mainIsland = mainIsland,
                totalBudget = totalBudget,
                medianSavings = medianSavings,
                avgDelay = avgDelay,
                highDelayPct = highDelayPct,
                efficiencyScore = efficiencyScore
            )
        }

        // Sort by EfficiencyScore descending (highest first)
        val sortedSummaries = summaries.sortedByDescending { it.efficiencyScore }

        //  Write to CSV file
        val writer = FileWriter(outputPath)

        // Write header row
        writer.write("Region,MainIsland,TotalBudget,MedianSavings,AvgDelay,HighDelayPct,EfficiencyScore\n")

        // Write data rows
        for (summary in sortedSummaries) {
            writer.write(
                "${summary.region}," +
                        "${summary.mainIsland}," +
                        "${summary.totalBudget.format2Decimals()}," +
                        "${summary.medianSavings.format2Decimals()}," +
                        "${summary.avgDelay.format2Decimals()}," +
                        "${summary.highDelayPct.format2Decimals()}," +
                        "${summary.efficiencyScore.format2Decimals()}\n"
            )
        }

        writer.close()

        // Print to console
        println("\nRegional Flood Mitigation Efficiency Summary")
        println("(Filtered: 2021-2023 Projects)")
        println()
        println("| Region                           | MainIsland | TotalBudget   | MedianSavings | AvgDelay | HighDelayPct | EfficiencyScore |")
        println("|----------------------------------|------------|---------------|---------------|----------|--------------|-----------------|")

        for (summary in sortedSummaries.take(5)) {  // Show top 5 in console
            println("| %-32s | %-10s | %13s | %13s | %8s | %12s | %15s |".format(
                summary.region,
                summary.mainIsland,
                summary.totalBudget.format2Decimals(),
                summary.medianSavings.format2Decimals(),
                summary.avgDelay.format2Decimals(),
                summary.highDelayPct.format2Decimals(),
                summary.efficiencyScore.format2Decimals()
            ))
        }
        println("Full table exported to $outputPath")
    }

    // REQ-0007: Generate Report 2 - Top Contractors Performance Ranking
    fun generateReport2(outputPath: String = "report2_contractor_ranking.csv") {
        println("\nReport 2: Top Contractors Performance Ranking")

        // Group projects by Contractor
        val grouped = projects.groupBy { it.contractor }

        // Filter contractors with >= 5 projects
        val filtered = grouped.filter { (_, contractorProjects) ->
            contractorProjects.size >= 5
        }

        data class ContractorSummary(
            val rank: Int,
            val contractor: String,
            val totalCost: Double,
            val numProjects: Int,
            val avgDelay: Double,
            val totalSavings: Double,
            val reliabilityIndex: Double,
            val riskFlag: String
        )

        // Calculate metrics for each contractor
        val summaries = filtered.map { (contractor, contractorProjects) ->
            val totalCost = contractorProjects.sumOf { it.contractCost }
            val numProjects = contractorProjects.size
            val avgDelay = contractorProjects.map { it.completionDelayDays.toDouble() }.average()
            val totalSavings = contractorProjects.sumOf { it.costSavings }

            // Reliability Index = (1 - (avg delay / 90)) * (total savings / total cost) * 100
            val reliabilityIndex: Double
            if (totalCost > 0) {
                val delayFactor = 1 - (avgDelay / 90)
                val savingsRatio = totalSavings / totalCost
                val rawIndex = delayFactor * savingsRatio * 100
                // Cap between 0 and 100
                reliabilityIndex = rawIndex.coerceIn(0.0, 100.0)
            } else {
                reliabilityIndex = 0.0
            }

            val riskFlag = if (reliabilityIndex < 50) {
                "High Risk"
            } else {
                "Low Risk"
            }

            ContractorSummary(
                rank = 0,
                contractor = contractor,
                totalCost = totalCost,
                numProjects = numProjects,
                avgDelay = avgDelay,
                totalSavings = totalSavings,
                reliabilityIndex = reliabilityIndex,
                riskFlag = riskFlag
            )
        }

        // Sort by TotalCost descending and take top 15
        val sorted = summaries.sortedByDescending { it.totalCost }.take(15)

        // Assign ranks
        val ranked = sorted.mapIndexed { index, summary ->
            summary.copy(rank = index + 1)
        }

        // Write to CSV
        val writer = FileWriter(outputPath)

        // Write header
        writer.write("Rank,Contractor,TotalCost,NumProjects,AvgDelay,TotalSavings,ReliabilityIndex,RiskFlag\n")

        // Write data rows
        for (summary in ranked) {
            writer.write(
                "${summary.rank}," +
                        "${summary.contractor}," +
                        "${summary.totalCost.format2Decimals()}," +
                        "${summary.numProjects}," +
                        "${summary.avgDelay.format2Decimals()}," +
                        "${summary.totalSavings.format2Decimals()}," +
                        "${summary.reliabilityIndex.format2Decimals()}," +
                        "${summary.riskFlag}\n"
            )
        }

        writer.close()

        // Print to console
        println()
        println("Top Contractors Performance Ranking")
        println("(Top 15 by TotalCost, >=5 Projects)")
        println()
        println("| Rank | Contractor                                                           | TotalCost     | NumProjects | AvgDelay | TotalSavings  | ReliabilityIndex | RiskFlag  |")
        println("|------|----------------------------------------------------------------------|---------------|-------------|----------|---------------|------------------|-----------|")

        for (summary in ranked) {  // Show all 15 in console
            // Truncate contractor name if too long
            val contractorName = if (summary.contractor.length > 68) {
                summary.contractor.substring(0, 65) + "..."
            } else {
                summary.contractor
            }

            println("| %4d | %-68s | %13s | %11d | %8s | %13s | %16s | %-9s |".format(
                summary.rank,
                contractorName,
                summary.totalCost.format2Decimals(),
                summary.numProjects,
                summary.avgDelay.format2Decimals(),
                summary.totalSavings.format2Decimals(),
                summary.reliabilityIndex.format2Decimals(),
                summary.riskFlag
            ))
        }
        println("Full table exported to $outputPath")
    }

    // REQ-0008: Generate Report 3 - Annual Project Type Cost Overrun Trends
    fun generateReport3(outputPath: String = "report3_annual_trends.csv") {
        println("\nReport 3: Annual Project Type Cost Overrun Trends")

        // Group by FundingYear and TypeOfWork
        val grouped = projects.groupBy { project ->
            "${project.fundingYear}|${project.typeOfWork}"
        }

        data class AnnualTrend(
            val fundingYear: Int,
            val typeOfWork: String,
            val totalProjects: Int,
            val avgSavings: Double,
            val overrunRate: Double,
            val yoyChange: Double
        )

        // Calculate metrics for each group
        val trends = grouped.map { (key, groupProjects) ->
            val parts = key.split("|")
            val fundingYear = parts[0].toInt()
            val typeOfWork = parts[1]

            val totalProjects = groupProjects.size
            val avgSavings = groupProjects.map { it.costSavings }.average()

            // Overrun rate = percentage with negative savings
            val projectsWithOverrun = groupProjects.count { it.costSavings < 0 }
            val overrunRate = (projectsWithOverrun * 100.0) / totalProjects

            AnnualTrend(
                fundingYear = fundingYear,
                typeOfWork = typeOfWork,
                totalProjects = totalProjects,
                avgSavings = avgSavings,
                overrunRate = overrunRate,
                yoyChange = 0.0  // Will calculate in next step
            )
        }

        // Calculate Year-over-Year % change (2021 baseline)
        // Group trends by TypeOfWork to calculate YoY change
        val trendsByType = trends.groupBy { it.typeOfWork }

        val trendsWithYoY = trends.map { trend ->
            // Find 2021 baseline for this type of work
            val baseline2021Trend = trendsByType[trend.typeOfWork]
                ?.firstOrNull { it.fundingYear == 2021 }

            val baseline2021 = if (baseline2021Trend != null) {
                baseline2021Trend.avgSavings
            } else {
                0.0
            }

            // Calculate YoY change
            val yoyChange: Double
            if (trend.fundingYear == 2021) {
                yoyChange = 0.0
            } else if (baseline2021 == 0.0) {
                // No baseline found, set to 0
                yoyChange = 0.0
            } else {
                // Calculate percentage change from baseline
                val difference = trend.avgSavings - baseline2021
                yoyChange = (difference / baseline2021) * 100
            }

            trend.copy(yoyChange = yoyChange)
        }

        // Sort by FundingYear ascending, then AvgSavings descending
        val sorted = trendsWithYoY.sortedWith(
            compareBy<AnnualTrend> { it.fundingYear }
                .thenByDescending { it.avgSavings }
        )

        // Write to CSV
        val writer = FileWriter(outputPath)

        // Write header
        writer.write("FundingYear,TypeOfWork,TotalProjects,AvgSavings,OverrunRate,YoYChange\n")

        // Write data rows
        for (trend in sorted) {
            writer.write(
                "${trend.fundingYear}," +
                        "${trend.typeOfWork}," +
                        "${trend.totalProjects}," +
                        "${trend.avgSavings.format2Decimals()}," +
                        "${trend.overrunRate.format2Decimals()}," +
                        "${trend.yoyChange.format2Decimals()}\n"
            )
        }

        writer.close()

        // print to console
        println("\nAnnual Project Type Cost Overrun Trends")
        println("(Grouped by FundingYear and TypeOfWork)")
        println()
        println("| FundingYear | TypeOfWork                                  | TotalProjects | AvgSavings   | OverrunRate | YoYChange |")
        println("|-------------|---------------------------------------------|---------------|--------------|-------------|-----------|")

        for (trend in sorted.take(5)) {  // Show top 5 in console
            println("| %11d | %-43s | %13d | %12s | %11s | %9s |".format(
                trend.fundingYear,
                trend.typeOfWork,
                trend.totalProjects,
                trend.avgSavings.format2Decimals(),
                trend.overrunRate.format2Decimals(),
                trend.yoyChange.format2Decimals()
            ))
        }
        println("Full table exported to $outputPath")
    }

    // REQ-0009: Generate summary.json
    fun generateSummaryJson(outputPath: String = "summary.json") {
        // Calculate stats
        val totalProjects = projects.size
        val totalContractors = projects.map { it.contractor }.distinct().size
        val totalProvinces = projects.map { it.province }.distinct().size
        val globalAvgDelay = projects.map { it.completionDelayDays.toDouble() }.average()
        val totalSavings = projects.sumOf { it.costSavings }

        val json = """
        {
          "total_projects": $totalProjects,
          "total_contractors": $totalContractors,
          "total_provinces": $totalProvinces,
          "global_avg_delay": ${"%.2f".format(globalAvgDelay)},
          "total_savings": ${totalSavings.toLong()}
        }
        """.trimIndent()

        File(outputPath).writeText(json)
        println("\nSummary Stats ($outputPath):")
        println(json)
    }

    // HELPER FUNCTIONS
    private fun List<Double>.median(): Double {
        val sorted = this.sorted()
        return if (sorted.isEmpty()) {
            0.0
        } else if (sorted.size % 2 == 0) {
            (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
        } else {
            sorted[sorted.size / 2]
        }
    }

    private fun Double.format2Decimals(): String {
        return "%.2f".format(this)
    }
}