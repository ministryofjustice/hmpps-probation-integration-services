package uk.gov.justice.digital.hmpps.integrations.delius.person

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "offender")
@Where(clause = "soft_deleted = 0")
class ProbationCase(

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "noms_number", columnDefinition = "char(7)")
    val nomsId: String?,

    @Column(name = "first_name")
    val forename: String,

    @Column(name = "second_name")
    val secondName: String?,

    @Column(name = "third_name")
    val thirdName: String?,

    @Column(name = "surname")
    val surname: String,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "ethnicity_id")
    val ethnicity: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "nationality_id")
    val nationality: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "religion_id")
    val religion: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "gender_identity_id")
    val genderIdentity: ReferenceData?,

    @Column(columnDefinition = "number")
    val currentExclusion: Boolean?,

    @Column(columnDefinition = "number")
    val currentRestriction: Boolean?,

    @OneToMany(mappedBy = "person")
    val communityManagers: List<CommunityManager>,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "offender_id")
    val id: Long
) {
    fun currentManager() = communityManagers.first()
}

@Entity
@Immutable
@Table(name = "offender_manager")
@Where(clause = "active_flag = 1 and soft_deleted = 0")
class CommunityManager(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: ProbationCase,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: CommunityManagerTeam,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "offender_manager_id")
    val id: Long
)

@Entity
@Immutable
@Table(name = "team")
class CommunityManagerTeam(

    @Column(columnDefinition = "char(6)")
    val code: String,

    val description: String,

    @Id
    @Column(name = "team_id")
    val id: Long
)

interface ProbationCaseRepository : JpaRepository<ProbationCase, Long> {
    @EntityGraph(attributePaths = ["gender", "ethnicity", "nationality", "religion", "genderIdentity", "communityManagers.team"])
    fun findByCrnIn(crns: List<String>): List<ProbationCase>

    @EntityGraph(attributePaths = ["gender", "ethnicity", "nationality", "religion", "genderIdentity", "communityManagers.team"])
    fun findByCrn(crn: String): ProbationCase?
}
