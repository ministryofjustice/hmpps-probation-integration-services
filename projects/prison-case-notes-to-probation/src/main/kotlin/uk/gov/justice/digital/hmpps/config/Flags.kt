package uk.gov.justice.digital.hmpps.config

import uk.gov.justice.digital.hmpps.flags.FeatureFlags

const val AlertsProcessing = "case-notes-alerts" // new processing for alerts and non-nsi case notes

fun FeatureFlags.personLevelAlerts() = enabled(AlertsProcessing)
fun FeatureFlags.setDescription() = enabled(AlertsProcessing)
