package uk.gov.justice.digital.hmpps.integrations.oasys

sealed interface ScoredAnswer {
    val score: Int

    enum class YesNo(override val score: Int) :
        ScoredAnswer {
        YES(2),
        NO(0),
        Unknown(0),
        ;

        companion object {
            fun of(value: String?): YesNo = entries.firstOrNull { it.name.equals(value, true) } ?: Unknown
        }
    }

    enum class Problem(override val score: Int) :
        ScoredAnswer {
        NONE(0),
        SOME(1),
        SIGNIFICANT(2),
        MISSING(0),
        ;

        companion object {
            fun of(value: String?): Problem = when (value?.firstOrNull()) {
                '0' -> NONE
                '1' -> SOME
                '2' -> SIGNIFICANT
                else -> MISSING
            }
        }
    }
}