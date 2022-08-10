package uk.gov.justice.digital.hmpps.integrations.delius.courtreport

import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class CourtReport(

  @Id
  @Column(name = "court_report_id")
  val id: Long,

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "offender_id")
  val person: Person,

)
