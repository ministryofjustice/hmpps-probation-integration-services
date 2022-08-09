package uk.gov.justice.digital.hmpps.integrations.delius.courtreport

import org.springframework.data.jpa.repository.JpaRepository

interface CourtReportRepository : JpaRepository<CourtReport, Long>
