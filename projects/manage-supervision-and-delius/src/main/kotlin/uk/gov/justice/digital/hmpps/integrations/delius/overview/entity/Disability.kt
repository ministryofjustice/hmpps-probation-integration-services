package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "disability")
@SQLRestriction("soft_deleted = 0 and (finish_date is null or finish_date > current_date)")
class Disability(
    @Id
    @Column(name = "disability_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "disability_type_id")
    val type: ReferenceData,

    val startDate: LocalDate,

    @Column(name = "last_updated_datetime")
    val lastUpdated: LocalDate,

    val finishDate: LocalDate? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    )

