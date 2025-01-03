package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User
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

    @ManyToOne
    @JoinColumn(name = "last_updated_user_id")
    val lastUpdatedUser: User,

    @Column(name = "notes", columnDefinition = "clob")
    val notes: String? = null,

    val finishDate: LocalDate? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    )

interface DisabilityRepository : JpaRepository<Disability, Long> {
    fun findByPersonId(personId: Long): List<Disability>
}