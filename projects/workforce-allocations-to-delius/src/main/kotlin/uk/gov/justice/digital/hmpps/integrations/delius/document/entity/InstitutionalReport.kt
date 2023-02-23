package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import java.time.LocalDate

@Entity
@Immutable
class InstitutionalReport(
    @Id
    @Column(name = "institutional_report_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "institution_report_type_id", updatable = false)
    val type: ReferenceData,

    @Column(name = "date_required")
    val dateRequired: LocalDate,

    @Column(name = "date_completed")
    val dateCompleted: LocalDate? = null,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false,

    @Column(name = "custody_id")
    val custodyId: Long
)
