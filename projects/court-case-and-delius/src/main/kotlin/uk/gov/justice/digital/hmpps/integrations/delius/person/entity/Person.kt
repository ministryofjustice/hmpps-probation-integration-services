package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
class Person(

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "pnc_number", columnDefinition = "char(13)")
    val pnc: String? = null,

    @Column(name = "cro_number")
    val croNumber: String? = null,

    @Column(name = "noms_number", columnDefinition = "char(9)")
    val nomsNumber: String? = null,

    @Column(name = "immigration_number")
    val immigrationNumber: String? = null,

    @Column(name = "ni_number", columnDefinition = "char(9)")
    val niNumber: String? = null,

    @Column(name = "most_recent_prisoner_number")
    val mostRecentPrisonerNumber: String? = null,

    @ManyToOne
    @JoinColumn(name = "title_id")
    val title: ReferenceData? = null,

    @Column(name = "first_name", length = 35)
    val forename: String,

    @Column(name = "second_name", length = 35)
    val secondName: String? = null,

    @Column(name = "third_name", length = 35)
    val thirdName: String? = null,

    @Column(name = "surname", length = 35)
    val surname: String,

    @Column(name = "preferred_name", length = 35)
    val preferredName: String? = null,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @Column(name = "telephone_number")
    val telephoneNumber: String? = null,

    @Column(name = "mobile_number")
    val mobileNumber: String? = null,

    @Column(name = "e_mail_address")
    val emailAddress: String? = null,

    @Column(name = "previous_surname")
    val previousSurname: String? = null,

    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "religion_id")
    val religion: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "language_id")
    val language: ReferenceData? = null,

    @Column(name = "language_concerns")
    val languageConcerns: String? = null,

    @ManyToOne
    @JoinColumn(name = "sexual_orientation_id")
    val sexualOrientation: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "gender_identity_id")
    val genderIdentity: ReferenceData? = null,

    @Column(name = "gender_identity_description")
    val genderIdentityDescription: String? = null,

    @Column(name = "Interpreter_required")
    @Convert(converter = YesNoConverter::class)
    val requiresInterpreter: Boolean? = false,

    @Column(name = "current_disposal", columnDefinition = "number")
    val currentDisposal: Boolean,

    @Column(name = "current_remand_status")
    val currentRemandStatus: String? = null,

    @OneToMany(mappedBy = "personId")
    val disabilities: Set<Disability> = emptySet(),

    @OneToMany(mappedBy = "personId")
    val provisions: Set<Provision> = emptySet(),

    @ManyToOne
    @JoinColumn(name = "ethnicity_id")
    val ethnicity: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "nationality_id")
    val nationality: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "second_nationality_id")
    val secondNationality: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "immigration_status_id")
    val immigrationStatus: ReferenceData? = null,

    @Column(name = "allow_sms")
    @Convert(converter = YesNoConverter::class)
    val allowSms: Boolean? = false,

    @Column(name = "current_exclusion", columnDefinition = "number")
    val currentExclusion: Boolean = false,

    @Column(name = "current_restriction", columnDefinition = "number")
    val currentRestriction: Boolean = false,

    @Column(name = "current_highest_risk_colour")
    val currentHighestRiskColour: String? = null,

    @Column(name = "offender_details")
    val offenderDetails: String? = null,

    @ManyToOne
    @JoinColumn(name = "partition_area_id")
    val partitionArea: PartitionArea,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false

)

@Immutable
@Entity
@Table(name = "partition_area")
class PartitionArea(
    @Id
    @Column(name = "partition_area_id")
    val partitionAreaId: Long,

    @Column(name = "area")
    val area: String,
)

interface PersonRepository : JpaRepository<Person, Long> {

    @Query(
        """
        select
           o.crn,
           sum(case when d.active_flag = 1 then 1 else 0 end)                                         as currentCount,
           sum(case when d.active_flag = 0 then 1 else 0 end)                                         as previousCount,
           max(d.termination_date)                                                                    as terminationDate,
           sum(e.in_breach)                                                                           as breachCount,
           sum(case when e.active_flag = 1 and d.disposal_id is null then 1 else 0 end)               as preSentenceCount,
           sum(case when d.disposal_id is null and ca.outcome_code = '101' then 1 else 0 end)         as awaitingPsrCount
        from offender o
             join event e on e.offender_id = o.offender_id and e.soft_deleted = 0
             left join disposal d on d.event_id = e.event_id and d.soft_deleted = 0
             left join (select ca.event_id, oc.code_value as outcome_code
                        from court_appearance ca
                                 join r_standard_reference_list oc on ca.outcome_id = oc.standard_reference_list_id) ca
                       on ca.event_id = e.event_id
        where crn = :crn
            and o.soft_deleted = 0
        group by o.crn
        """,
        nativeQuery = true
    )
    fun statusOf(crn: String): SentenceCounts?

    @Query(
        """
            select p from Person p 
            left join fetch p.ethnicity eth
            left join fetch p.nationality nat
            left join fetch p.gender gen
            left join fetch p.language lang
            left join fetch p.genderIdentity gi
            left join fetch p.immigrationStatus is
            left join fetch p.provisions prov
            left join fetch p.disabilities dis
            left join fetch p.secondNationality sn
            left join fetch p.sexualOrientation so
            left join fetch p.religion rel
            left join fetch p.partitionArea pa
            left join fetch p.title title
            left join fetch prov.category pcat
            left join fetch prov.type ptype
            left join fetch dis.type dtype
            left join fetch dis.condition dcond
            where p.crn = :crn
            and p.softDeleted = false 
        """
    )
    fun findByCrn(crn: String): Person?

    fun findByCrnAndSoftDeletedIsFalse(crn: String): Person?
}

fun PersonRepository.getPerson(crn: String) = findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)

interface SentenceCounts {
    val crn: String
    val currentCount: Int
    val previousCount: Int
    val terminationDate: LocalDate?
    val breachCount: Int
    val preSentenceCount: Int
    val awaitingPsrCount: Int
}
