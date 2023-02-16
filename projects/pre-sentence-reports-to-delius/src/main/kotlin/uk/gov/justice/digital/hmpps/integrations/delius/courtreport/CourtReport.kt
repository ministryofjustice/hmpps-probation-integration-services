package uk.gov.justice.digital.hmpps.integrations.delius.courtreport

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

@Entity
class CourtReport(

    @Id
    @Column(name = "court_report_id")
    val id: Long,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offender_id")
    val person: Person

)
