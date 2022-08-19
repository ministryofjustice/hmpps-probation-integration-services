package uk.gov.justice.digital.hmpps.integrations.delius.event

import org.hibernate.annotations.Where
import uk.gov.justice.digital.hmpps.integrations.delius.institution.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne

@Entity
@Where(clause = "soft_deleted = 0")
data class Custody(
    @Id
    @Column(name = "custody_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "custodial_status_id")
    val custodialStatus: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "institution_id")
    val institution: Institution,

    @Column(name = "soft_deleted", columnDefinition = "NUMBER", nullable = false)
    val softDeleted: Boolean = false,

    @OneToOne
    @JoinColumn(name = "disposal_id", updatable = false)
    val disposal: Disposal? = null,
)
