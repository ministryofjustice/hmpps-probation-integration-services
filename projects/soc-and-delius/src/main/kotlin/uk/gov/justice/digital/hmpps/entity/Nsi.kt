package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

@Entity
@Table(name = "nsi")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class Nsi(

    @Column(name = "offender_id")
    val personId: Long,

    @JoinColumn(name = "nsi_outcome_id")
    @ManyToOne
    var outcome: ReferenceData? = null,

    val referralDate: LocalDate,

    @Id
    @Column(name = "nsi_id")
    val id: Long = 0,

    @Column(name = "active_flag", columnDefinition = "number")
    var active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

interface NsiRepository : JpaRepository<Nsi, Long> {

    @Query(
        """
           with latest_breach as (select nsi.referral_date as breachdate
                       from nsi nsi
                                join r_standard_reference_list ref
                                     on nsi.nsi_outcome_id = ref.standard_reference_list_id
                       where nsi.offender_id = :personId
                         and nsi.active_flag = 1
                         and nsi.soft_deleted = 0
                         and ref.code_value in (
                                                'BRE01', 'BRE02', 'BRE03', 'BRE04', 'BRE05', 'BRE06', 'BRE07',
                                                'BRE08', 'BRE10', 'BRE13', 'BRE14', 'BRE16'
                           )
                       order by nsi.referral_date desc
                           fetch next 1 row only),
            latest_recall as (select nsi.referral_date as recalldate
                       from nsi nsi
                                join r_standard_reference_list ref
                                     on nsi.nsi_outcome_id = ref.standard_reference_list_id
                       where nsi.offender_id = :personId
                         and nsi.active_flag = 1
                         and nsi.soft_deleted = 0
                         and ref.code_value in (
                                                'REC01', 'REC02'
                           )
                       order by nsi.referral_date desc
                           fetch next 1 row only)
            select breachdate as referralDate, 'breach' as name from latest_breach
            union all
            select recalldate as referralDate, 'recall' as name from latest_recall  
        """,
        nativeQuery = true
    )
    fun findBreachAndRecallDates(personId: Long): List<NsiDate>
}

interface NsiDate {
    val name: String
    val referralDate: LocalDate
}
