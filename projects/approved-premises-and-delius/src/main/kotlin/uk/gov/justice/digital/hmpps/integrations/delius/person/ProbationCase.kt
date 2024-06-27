package uk.gov.justice.digital.hmpps.integrations.delius.person

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
class ProbationCase(

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "noms_number", columnDefinition = "char(7)")
    val nomsId: String?,

    @Column(name = "pnc_number", columnDefinition = "char(13)")
    val pnc: String?,

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

    val genderIdentityDescription: String?,

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
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
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

    @ManyToOne
    @JoinColumn(name = "district_id")
    val ldu: Ldu,

    @Column(name = "start_date")
    val startDate: LocalDate = LocalDate.now(),

    @Column(name = "end_date")
    val endDate: LocalDate? = null,

    @Id
    @Column(name = "team_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "district")
class Ldu(

    @Column(name = "code")
    val code: String,

    val description: String,

    @ManyToOne
    @JoinColumn(name = "borough_id")
    val borough: Borough,

    @Id
    @Column(name = "district_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "borough")
class Borough(

    @Id
    @Column(name = "borough_id")
    val id: Long,

    @Column(name = "code")
    val code: String,

    @Column(name = "description")
    val description: String
)

interface BoroughRepository : JpaRepository<Borough, Long>

interface ProbationCaseRepository : JpaRepository<ProbationCase, Long> {
    @EntityGraph(attributePaths = ["gender", "ethnicity", "nationality", "religion", "genderIdentity", "communityManagers.team.ldu"])
    fun findByCrnIn(crns: List<String>): List<ProbationCase>

    @EntityGraph(attributePaths = ["gender", "ethnicity", "nationality", "religion", "genderIdentity", "communityManagers.team.ldu"])
    fun findByCrn(crn: String): ProbationCase?
}
