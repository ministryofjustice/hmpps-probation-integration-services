package uk.gov.justice.digital.hmpps.integrations.delius.entity

interface Versioned {
    val id: Long
    val rowVersion: Long
}