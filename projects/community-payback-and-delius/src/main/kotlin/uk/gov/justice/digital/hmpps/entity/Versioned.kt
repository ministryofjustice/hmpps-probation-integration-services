package uk.gov.justice.digital.hmpps.entity

interface Versioned {
    val id: Long?
    val rowVersion: Long
}