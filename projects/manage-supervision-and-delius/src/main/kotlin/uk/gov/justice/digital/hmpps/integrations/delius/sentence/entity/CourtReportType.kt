package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity(name = "R_COURT_REPORT_TYPE")
class CourtReportType(

    @Id val courtReportTypeId: Long,

    val description: String
)