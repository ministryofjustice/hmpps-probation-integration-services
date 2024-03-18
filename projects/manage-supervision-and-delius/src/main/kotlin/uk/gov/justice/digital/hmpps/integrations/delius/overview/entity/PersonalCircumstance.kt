package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "personal_circumstance")
@SQLRestriction("soft_deleted = 0")
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
    val subType: PersonalCircumstanceSubType,

    @Column(name = "last_updated_datetime")
    val lastUpdated: LocalDate,

    @ManyToOne
    @JoinColumn(name = "last_updated_user_id")
    val lastUpdatedUser: User,

    @Column(name = "notes", columnDefinition = "clob")
    val notes: String? = null,

    val evidenced: Boolean = false,

    val startDate: LocalDate,

    val endDate: LocalDate? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    )

interface PersonCircumstanceRepository : JpaRepository<PersonalCircumstance, Long> {

    @Query(
        """
        select pc from PersonalCircumstance pc 
        where pc.personId = :personId
        and (pc.endDate is null or pc.endDate > current_date )
    """
    )
    fun findCurrentCircumstances(personId: Long): List<PersonalCircumstance>

    @Query(
        """
        select pc from PersonalCircumstance pc 
        where pc.personId = :personId
    """
    )
    fun findAllCircumstances(personId: Long): List<PersonalCircumstance>
}

@Immutable
@Entity
@Table(name = "r_circumstance_sub_type")
class PersonalCircumstanceSubType(
    @Id
    @Column(name = "circumstance_sub_type_id")
    val id: Long,

    @Column(name = "code_description")
    val description: String,
)