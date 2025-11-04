import java.time.LocalDate

data class FloodControlProject(
    val region: String,
    val mainIsland: String,
    val province: String,
    val approvedBudgetForContract: Double,
    val contractCost: Double,
    val startDate: LocalDate?,
    val actualCompletionDate: LocalDate?,
    val fundingYear: Int,
    val typeOfWork: String,
    val contractor: String,

    // Derived fields (REQ-0004)
    val costSavings: Double,
    val completionDelayDays: Long
)