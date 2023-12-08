package uk.gov.justice.digital.hmpps.integrations.delius.caseview

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
class CaseViewPerson(
    @Id
    @Column(name = "offender_id")
    val id: Long,
    @Column(columnDefinition = "char(7)")
    val crn: String,
    @Column(name = "first_name", length = 35)
    val forename: String,
    @Column(name = "second_name", length = 35)
    val secondName: String? = null,
    @Column(name = "third_name", length = 35)
    val thirdName: String? = null,
    @Column(name = "surname", length = 35)
    val surname: String,
    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,
    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData?,
    @Column(columnDefinition = "char(13)")
    val pncNumber: String? = null,
    @Column(updatable = false, columnDefinition = "number")
    val softDeleted: Boolean = false,
)

interface CaseViewPersonRepository : JpaRepository<CaseViewPerson, Long> {
    @EntityGraph(attributePaths = ["gender.dataset"])
    fun findByCrn(crn: String): CaseViewPerson?

    @Query(
        """
        select ma from CaseViewPersonAddress ma
        join fetch ma.status s
        join fetch s.dataset
        left join fetch ma.type t
        left join fetch t.dataset
        where ma.personId = :personId 
        and ma.status.code = 'M'
        and ma.endDate is null
    """,
    )
    fun findMainAddress(personId: Long): CaseViewPersonAddress?

    @Query(
        """
       select e.event_id                                               as eventId,
       d.disposal_date                                                 as startDate,
       dt.description                                                  as description,
       d.entry_length || ' ' || u.code_description                     as length,
       greatest(nvl(d.notional_end_date, to_date('1970-01-01', 'YYYY-MM-DD')),
                nvl(d.entered_notional_end_date, to_date('1970-01-01', 'YYYY-MM-DD')),
                nvl(kd.key_date, to_date('1970-01-01', 'YYYY-MM-DD'))) as endDate,
       o.main_category_description                                     as offenceMainCategory,
       o.sub_category_description                                      as offenceSubCategory
       from event e
         join disposal d on d.event_id = e.event_id and d.soft_deleted = 0 and d.active_flag = 1
         join r_disposal_type dt on dt.disposal_type_id = d.disposal_type_id
         join main_offence mo on mo.event_id = e.event_id and mo.soft_deleted = 0
         join r_offence o ON o.offence_id = mo.offence_id
         left join r_standard_reference_list u on d.entry_length_units_id = u.standard_reference_list_id
         left join custody c on d.disposal_id = c.disposal_id and c.soft_deleted = 0
         left join (key_date kd
                    join r_standard_reference_list kdt 
                        on kd.key_date_type_id = kdt.standard_reference_list_id and kdt.code_value = 'SED')
                   on c.custody_id = kd.custody_id and kd.soft_deleted = 0
       where e.offender_id = :personId
         and e.event_number = :eventNumber
         and e.soft_deleted = 0
         and e.active_flag = 1
    """,
        nativeQuery = true,
    )
    fun findSentenceSummary(
        personId: Long,
        eventNumber: String,
    ): SentenceSummary?
}

fun CaseViewPersonRepository.getByCrn(crn: String) =
    findByCrn(crn)
        ?: throw NotFoundException("Person", "crn", crn)
