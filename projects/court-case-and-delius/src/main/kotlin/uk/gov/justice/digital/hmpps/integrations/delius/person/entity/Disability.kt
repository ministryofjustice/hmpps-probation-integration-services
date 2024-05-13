package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import java.time.LocalDate
import java.time.ZonedDateTime

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

    @ManyToOne
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "disability_condition_id")
    val condition: ReferenceData,

    val startDate: LocalDate,

    @Column(name = "last_updated_datetime")
    val lastUpdated: ZonedDateTime,

//    @ManyToOne
//    @JoinColumn(name = "last_updated_user_id")
//    val lastUpdatedUser: User,

    @Column(name = "notes", columnDefinition = "clob")
    val notes: String? = null,

    val finishDate: LocalDate? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    )

interface DisabilityRepository : JpaRepository<Disability, Long> {
    fun findByPersonId(personId: Long): List<Disability>
}