package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
@Immutable
class InstitutionalReport(
    @Id
    @Column(name = "institutional_report_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "institution_report_type_id", updatable = false)
    val type: StandardReference,

) {
}