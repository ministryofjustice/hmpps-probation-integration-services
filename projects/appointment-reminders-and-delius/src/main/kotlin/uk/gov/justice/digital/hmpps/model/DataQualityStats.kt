package uk.gov.justice.digital.hmpps.model

interface DataQualityStats {
    val missing: Int
    val invalid: Int
}