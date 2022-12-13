package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.Immutable

@Entity
@Immutable
class InstitutionalReport(
    @Id
    @Column(name = "institutional_report_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "institution_report_type_id", updatable = false)
    val type: StandardReference,

)
