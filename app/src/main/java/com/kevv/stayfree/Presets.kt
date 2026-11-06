package com.kevv.stayfree

data class Preset(
    val name: String,
    val emoji: String,
    val unitName: String,
    val cost: Double,
    val units: Double,
    val wiki: List<String>,
    val triggers: List<String>,
    val tips: List<String>,
    val warning: String? = null
)

data class Milestone(val hours: Double, val title: String, val desc: String)

val genericTips = listOf(
    "Delay: promise yourself 10 minutes before you decide anything.",
    "Drink a full glass of cold water, slowly.",
    "Walk outside for 5 minutes. Change your surroundings.",
    "Text or call someone you trust and tell them you're having an urge.",
    "Name it out loud: \"This is a craving. It will pass.\"",
    "Do 20 squats or 10 push-ups to burn off the restless energy.",
    "Splash cold water on your face and wash your hands.",
    "Write down what you were feeling right before the urge hit.",
    "Remember your reason for starting. Read it again.",
    "Open this app's timer and look at how far you've come."
)

val presets = listOf(
    Preset(
        "Smoking", "\uD83D\uDEAC", "cigarettes", 0.5, 10.0,
        listOf("Nicotine", "Smoking cessation"),
        listOf("Stress", "After meals", "Coffee", "Social", "Boredom", "Alcohol", "Driving"),
        listOf("Chew sugar-free gum or sip tea instead.", "Brush your teeth. The fresh taste helps.", "Keep your hands busy: a pen, a stress ball.")
    ),
    Preset(
        "Vaping", "\uD83D\uDCA8", "pods", 5.0, 0.3,
        listOf("Electronic cigarette", "Nicotine"),
        listOf("Stress", "Boredom", "Social", "Waking up", "Work break", "Alcohol"),
        listOf("Hold a toothpick or straw for the hand-to-mouth habit.", "Sip water in small, frequent mouthfuls.")
    ),
    Preset(
        "Alcohol", "\uD83C\uDF7A", "drinks", 5.0, 3.0,
        listOf("Alcohol use disorder", "Alcohol withdrawal syndrome"),
        listOf("Stress", "Social", "Loneliness", "After work", "Celebrating", "Weekend", "Anger"),
        listOf("Pour a fizzy non-alcoholic drink in a proper glass.", "Eat something. Hunger and cravings feel alike.", "Leave the situation or move to a place without alcohol."),
        "If you drink heavily every day, do not stop suddenly without medical advice. Alcohol withdrawal can be dangerous. Talk to a doctor first."
    ),
    Preset(
        "Social media", "\uD83D\uDCF1", "hours", 0.0, 4.0,
        listOf("Social media addiction", "Digital detox"),
        listOf("Boredom", "Loneliness", "Before bed", "Procrastinating", "Anxious", "Notifications"),
        listOf("Put the phone in another room.", "Switch off all notifications for the next hour.", "Pick up a book or start a 15-minute task instead.")
    ),
    Preset(
        "Gambling", "\uD83C\uDFB0", "bets", 10.0, 2.0,
        listOf("Problem gambling"),
        listOf("Stress", "Boredom", "Chasing losses", "Payday", "Sports events", "Alone", "Alcohol"),
        listOf("Hand your cards or cash to someone you trust.", "Delete betting apps and use self-exclusion tools.", "Call a gambling helpline and talk it through.")
    ),
    Preset(
        "Gaming", "\uD83C\uDFAE", "hours", 0.0, 5.0,
        listOf("Gaming disorder", "Video game addiction"),
        listOf("Boredom", "Stress", "Friends online", "Avoiding tasks", "Late night", "New release"),
        listOf("Set a hard stop with a timer before you start.", "Go outside or exercise for 15 minutes first.")
    ),
    Preset(
        "Caffeine", "\u2615", "cups", 3.0, 3.0,
        listOf("Caffeine"),
        listOf("Tired", "Work", "Habit", "Social", "After meals"),
        listOf("Drink water or herbal tea instead.", "Take a 10-minute walk in daylight.")
    ),
    Preset(
        "Sugar", "\uD83C\uDF6C", "treats", 2.0, 3.0,
        listOf("Food addiction"),
        listOf("Stress", "Tired", "Boredom", "After meals", "Emotional", "Late night"),
        listOf("Eat a protein-rich snack first.", "Brush your teeth, then wait 10 minutes.")
    ),
    Preset(
        "Custom", "\u2B50", "times", 0.0, 1.0,
        emptyList(),
        listOf("Stress", "Boredom", "Social", "Tired", "Anxious", "Celebrating", "Alone", "After meals"),
        emptyList()
    )
)

fun presetFor(category: String): Preset = presets.firstOrNull { it.name == category } ?: presets.last()

private val smokingMs = listOf(
    Milestone(0.33, "20 minutes", "Heart rate and blood pressure begin to drop."),
    Milestone(12.0, "12 hours", "Carbon monoxide in your blood is falling toward normal."),
    Milestone(72.0, "3 days", "Nicotine has largely left your body. Withdrawal usually peaks around now."),
    Milestone(336.0, "2 weeks", "Circulation and lung function start to improve."),
    Milestone(720.0, "1 month", "Coughing and breathlessness often ease."),
    Milestone(2160.0, "3 months", "Lung function can be noticeably better."),
    Milestone(4320.0, "6 months", "Many people handle stress without reaching for a cigarette."),
    Milestone(8760.0, "1 year", "Your risk of coronary heart disease is about half that of a smoker.")
)

private val alcoholMs = listOf(
    Milestone(24.0, "1 day", "Your body is starting to recover. Hydration and rest help."),
    Milestone(72.0, "3 days", "The most intense physical withdrawal period is usually passing."),
    Milestone(168.0, "1 week", "Sleep and mood often begin to stabilise."),
    Milestone(336.0, "2 weeks", "Energy and concentration commonly improve."),
    Milestone(720.0, "1 month", "Many people notice better skin, sleep and clearer thinking."),
    Milestone(2160.0, "3 months", "New routines are taking root. Keep your support close."),
    Milestone(4320.0, "6 months", "Half a year. Your old triggers have less hold."),
    Milestone(8760.0, "1 year", "A full year. Look back at how much has changed.")
)

private val genericMs = listOf(
    Milestone(1.0, "1 hour", "You made the decision and it's already working."),
    Milestone(24.0, "1 day", "The first day is often the hardest."),
    Milestone(72.0, "3 days", "Cravings usually start to come in shorter waves."),
    Milestone(168.0, "1 week", "A full week. New habits are forming."),
    Milestone(336.0, "2 weeks", "Your routines are shifting."),
    Milestone(720.0, "1 month", "One month. Celebrate it."),
    Milestone(2160.0, "3 months", "A real change in lifestyle."),
    Milestone(4320.0, "6 months", "Half a year of freedom."),
    Milestone(8760.0, "1 year", "One year. You've rebuilt your life.")
)

fun milestonesFor(category: String): List<Milestone> = when (category) {
    "Smoking", "Vaping" -> smokingMs
    "Alcohol" -> alcoholMs
    else -> genericMs
}

val fallbackQuotes = listOf(
    "A craving is a wave. Wait it out and it will drop." to "StayFree",
    "Every urge you outlast makes the next one weaker." to "StayFree",
    "You don't have to be perfect. You have to keep going." to "StayFree",
    "One day at a time. One urge at a time." to "StayFree",
    "Slips are data, not defeat. Learn from them and continue." to "StayFree"
)
