package com.example.model

enum class MotifType(val label: String, val iconName: String) {
    COZY_LAMP_CUP("Cozy Desk & Coffee", "desk"),
    ZEN_BONSAI("Tranquil Bonsai", "nature"),
    ZEN_ENSO("Zen Enso Circle", "circle"),
    CYBER_GRID("Neon Cyber Skyline", "cyber"),
    COSMIC_PLANET("Cosmic Nebula & Planet", "planet"),
    AURORA_PEAKS("Aurora & Mountain Pines", "aurora"),
    SUN_DUNES("Sunset Golden Dunes", "sunset"),
    SAKURA_BRANCH("Sakura Blossoms", "sakura"),
    PIXEL_FIREPLACE("Warm Hearth Fire", "fire"),
    LOTUS_FLOWER("Sacred Lotus", "lotus"),
    HOURGLASS("Flowing Hourglass", "hourglass")
}

enum class ParticleEffectType(val label: String) {
    NONE("None"),
    RAIN("Gentle Raindrops"),
    FALLING_PETALS("Blossom Petals"),
    FLOATING_SPARKS("Ambient Sparks"),
    STARRY_GLOW("Twinkling Stars"),
    FLOATING_LEAVES("Zen Leaves"),
    NEON_GRID_STREAM("Cyber Stream"),
    FIRE_EMBERS("Warm Embers")
}

data class ArtTheme(
    val id: String,
    val name: String,
    val subtitle: String,
    val category: String,
    val gradientColors: List<Long>,
    val ambientMoodColor: Long,
    val motifType: MotifType,
    val particleEffect: ParticleEffectType,
    val defaultMantra: String,
    val isCustom: Boolean = false
) {
    companion object {
        val BuiltInThemes = listOf(
            ArtTheme(
                id = "lofi_study",
                name = "Lo-Fi Cozy Room",
                subtitle = "Warm desk lamp, steaming mug, and gentle evening rain",
                category = "Cozy & Warm",
                gradientColors = listOf(0xFF1E152A, 0xFF2E1C2B, 0xFF422835),
                ambientMoodColor = 0xFFFFB86C,
                motifType = MotifType.COZY_LAMP_CUP,
                particleEffect = ParticleEffectType.RAIN,
                defaultMantra = "Quiet mind, deep focus, warm tea."
            ),
            ArtTheme(
                id = "zen_bamboo",
                name = "Zen Bamboo Mist",
                subtitle = "Morning sunlight filtering through peaceful misty garden",
                category = "Nature & Zen",
                gradientColors = listOf(0xFF0F2027, 0xFF203A43, 0xFF2C5364),
                ambientMoodColor = 0xFF81C784,
                motifType = MotifType.ZEN_BONSAI,
                particleEffect = ParticleEffectType.FLOATING_LEAVES,
                defaultMantra = "Flow like water. Still as the mountain."
            ),
            ArtTheme(
                id = "cyber_synth",
                name = "Cyberpunk Horizon",
                subtitle = "Futuristic neon skyline with deep purple and electric glow",
                category = "Sci-Fi & Cyber",
                gradientColors = listOf(0xFF0D0221, 0xFF240046, 0xFF3C096C),
                ambientMoodColor = 0xFF00F0FF,
                motifType = MotifType.CYBER_GRID,
                particleEffect = ParticleEffectType.NEON_GRID_STREAM,
                defaultMantra = "Locked into the digital flow state."
            ),
            ArtTheme(
                id = "deep_nebula",
                name = "Deep Space Cosmos",
                subtitle = "Ethereal interstellar nebula with serene distant planets",
                category = "Celestial",
                gradientColors = listOf(0xFF050510, 0xFF120E2E, 0xFF251A4A),
                ambientMoodColor = 0xFFBD93F9,
                motifType = MotifType.COSMIC_PLANET,
                particleEffect = ParticleEffectType.STARRY_GLOW,
                defaultMantra = "Expand your focus to infinity."
            ),
            ArtTheme(
                id = "aurora_night",
                name = "Aurora Borealis",
                subtitle = "Dancing emerald northern lights over snow-capped pines",
                category = "Nature & Zen",
                gradientColors = listOf(0xFF081C15, 0xFF1B4332, 0xFF2D6A4F),
                ambientMoodColor = 0xFF52B788,
                motifType = MotifType.AURORA_PEAKS,
                particleEffect = ParticleEffectType.STARRY_GLOW,
                defaultMantra = "Illuminate your highest aspirations."
            ),
            ArtTheme(
                id = "sakura_twilight",
                name = "Sakura Petal Breeze",
                subtitle = "Spring cherry blossom dusk with soft pastel winds",
                category = "Calm & Aesthetic",
                gradientColors = listOf(0xFF2B1028, 0xFF4A1942, 0xFF6B2D5C),
                ambientMoodColor = 0xFFFF79C6,
                motifType = MotifType.SAKURA_BRANCH,
                particleEffect = ParticleEffectType.FALLING_PETALS,
                defaultMantra = "Mastery unfolds petal by petal."
            ),
            ArtTheme(
                id = "sunset_solitude",
                name = "Golden Desert Dunes",
                subtitle = "Shimmering golden dunes under a radiant warm dusk sun",
                category = "Warm & Minimal",
                gradientColors = listOf(0xFF331800, 0xFF662D00, 0xFF994400),
                ambientMoodColor = 0xFFFF9E00,
                motifType = MotifType.SUN_DUNES,
                particleEffect = ParticleEffectType.FLOATING_SPARKS,
                defaultMantra = "One step at a time across the horizon."
            ),
            ArtTheme(
                id = "zen_enso_ink",
                name = "Zen Enso Brush",
                subtitle = "Minimalist ink wash and breathing circle of presence",
                category = "Minimalist",
                gradientColors = listOf(0xFF141416, 0xFF1E1E24, 0xFF2A2A35),
                ambientMoodColor = 0xFFFF5555,
                motifType = MotifType.ZEN_ENSO,
                particleEffect = ParticleEffectType.FLOATING_SPARKS,
                defaultMantra = "Here and now. Whole and complete."
            ),
            ArtTheme(
                id = "cozy_campfire",
                name = "Forest Hearth Fire",
                subtitle = "Crackling warm hearth fire in an alpine pine cabin",
                category = "Cozy & Warm",
                gradientColors = listOf(0xFF1F0D06, 0xFF381A0C, 0xFF542914),
                ambientMoodColor = 0xFFFF7043,
                motifType = MotifType.PIXEL_FIREPLACE,
                particleEffect = ParticleEffectType.FIRE_EMBERS,
                defaultMantra = "Keep the inner flame of dedication burning."
            )
        )

        val DefaultFocusTheme = BuiltInThemes[0]
        val DefaultShortBreakTheme = BuiltInThemes[1]
        val DefaultLongBreakTheme = BuiltInThemes[3]

        fun getById(id: String, customThemes: List<ArtTheme> = emptyList()): ArtTheme {
            return customThemes.find { it.id == id }
                ?: BuiltInThemes.find { it.id == id }
                ?: DefaultFocusTheme
        }
    }
}
