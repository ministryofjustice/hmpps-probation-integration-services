package uk.gov.justice.digital.hmpps

object MovementReasonCodes {
    const val DIED = "DEC"
    const val DETAINED_MENTAL_HEALTH = "HO"
    const val RELEASE_MENTAL_HEALTH = "HQ"
    const val FINAL_DISCHARGE_PSYCHIATRIC = "HP"
}

object FeatureFlagCodes {
    const val MULTIPLE_EVENTS = "release_recall_multiple_events"
    const val HOSPITAL_RELEASE = "messages_released_hospital"
}
