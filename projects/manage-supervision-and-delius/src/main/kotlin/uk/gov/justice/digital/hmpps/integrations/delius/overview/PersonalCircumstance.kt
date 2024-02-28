package uk.gov.justice.digital.hmpps.integrations.delius.overview

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "personal_circumstance")
@SQLRestriction("soft_deleted = 0 and (end_date is null or end_date > current_date)")
class PersonalCircumstance(
    @Id
    @Column(name = "personal_circumstance_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "circumstance_type_id")
    val type: ReferenceData,

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "circumstance_sub_type_id")
    val subType: ReferenceData,

    val startDate: LocalDate,

    val endDate: LocalDate? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    )

