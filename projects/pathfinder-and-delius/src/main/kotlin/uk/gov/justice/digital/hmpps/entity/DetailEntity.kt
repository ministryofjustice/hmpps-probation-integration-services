package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

@Immutable
@Table(name = "offender")
@Entity
@SQLRestriction("soft_deleted = 0")
class DetailPerson(
    @Id
    @Column(name = "offender_id")
    val id: Long,
    @Column(columnDefinition = "char(7)")
    val crn: String,
    @Column(columnDefinition = "char(7)")
    val nomsNumber: String,
    @Column(columnDefinition = "char(13)")
    val pncNumber: String,
    @ManyToOne
    @JoinColumn(name = "religion_id")
    val religion: ReferenceData?,
    @OneToMany(mappedBy = "person")
    val personManager: List<PersonManager>,
    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,
    @Column(name = "surname", length = 35)
    val surname: String,
    @Column(name = "first_name", length = 35)
    val forename: String,
    @Column(name = "second_name", length = 35)
    val secondName: String? = null,
    @Column(name = "third_name", length = 35)
    val thirdName: String? = null,
    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,
)

@Immutable
@Entity
@SQLRestriction("active_flag = 1")
@Table(name = "offender_manager")
class PersonManager(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,
    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: DetailPerson,
    @Column(name = "probation_area_id")
    val providerId: Long,
    @ManyToOne
    @JoinColumn(name = "allocation_staff_id", nullable = false)
    val staff: DetailStaff,
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    val team: Team,
    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,
)

@Immutable
@Entity
@Table(name = "staff")
class DetailStaff(
    val forename: String,
    val surname: String,
    @Column(name = "forename2")
    val middleName: String? = null,
    @Id
    @Column(name = "staff_id")
    val id: Long,
)

@Entity
@Immutable
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long,
    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,
    @ManyToOne
    @JoinColumn(name = "probation_area_id", nullable = false)
    val probationArea: DetailProbationArea,
    @ManyToOne
    @JoinColumn(name = "district_id", nullable = false)
    val district: DetailDistrict,
)

@Immutable
@Table(name = "probation_area")
@Entity
class DetailProbationArea(
    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true,
    val description: String,
    @Column(columnDefinition = "char(3)")
    val code: String,
    @Id
    @Column(name = "probation_area_id")
    val id: Long,
)

@Immutable
@Entity
@Table(name = "district")
class DetailDistrict(
    @Column(nullable = false)
    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean = true,
    @Column(name = "code")
    val code: String,
    val description: String,
    @Id
    @Column(name = "district_id")
    val id: Long,
)

interface DetailRepository : JpaRepository<DetailPerson, Long> {
    @Query(
        """
        with latest_event as (
            select e.offender_id, e.event_id, r.actual_release_date, i.description as release_location, c.custody_id
            from offender o
                     join event e on e.offender_id = o.offender_id
                     left outer join disposal d on e.event_id = d.event_id
                     left outer join custody c on d.disposal_id = c.disposal_id
                     left outer join release r on c.custody_id = r.custody_id
                     left outer join r_institution i on r.institution_id = i.institution_id
            where o.crn in :crns
              and e.active_flag = 1
              and e.soft_deleted = 0
              and d.active_flag = 1
              and d.soft_deleted = 0
        )
        select o.first_name forename,
               o.second_name middleNameOne,
               o.third_name middleNameTwo,
               o.surname, 
               o.date_of_birth_date dateOfBirth,
               o.crn,
               o.noms_number nomisId,
               o.pnc_number pncNumber,
               d.description ldu,
               pa.description probationArea,
               s.forename omForename,
               s.forename2 omMiddleName,
               s.surname omSurname,
               off.description mainOffence,
               religion.code_description religion,
               e.actual_release_date releaseDate,
               e.release_location releaseLocation,
               kdt.code_value keyDateCode,
               kdt.code_description keyDateDesc,
               kd.key_date keydate
        from offender o
                 join offender_manager om on o.offender_id = om.offender_id and om.active_flag = 1
                 join probation_area pa on om.probation_area_id = pa.probation_area_id
                 join team t on om.team_id = t.team_id
                 join staff s on om.allocation_staff_id = s.staff_id
                 join district d on t.district_id = d.district_id
                 left outer join latest_event e on o.offender_id = e.offender_id
                 left outer join main_offence mo on mo.event_id = e.event_id
                 left outer join r_offence off on mo.offence_id = off.offence_id
                 left outer join r_standard_reference_list religion on o.religion_id = religion.standard_reference_list_id
                 left outer join key_date kd on kd.custody_id = e.custody_id
                 left outer join r_standard_reference_list kdt on kd.key_date_type_id = kdt.standard_reference_list_id
        where o.crn in :crns
    """,
        nativeQuery = true,
    )
    fun getByCrns(crns: List<String>): List<PersonDetail>
}

interface PersonDetail {
    val forename: String
    val middleNameOne: String
    val middleNameTwo: String
    val surname: String
    val dateOfBirth: LocalDate
    val crn: String
    val nomisId: String
    val pncNumber: String
    val ldu: String
    val probationArea: String
    val omForename: String
    val omMiddleName: String
    val omSurname: String
    val mainOffence: String
    val religion: String
    val releaseDate: LocalDate
    val releaseLocation: String
    val keyDateCode: String
    val keyDateDesc: String
    val keydate: LocalDate
}
