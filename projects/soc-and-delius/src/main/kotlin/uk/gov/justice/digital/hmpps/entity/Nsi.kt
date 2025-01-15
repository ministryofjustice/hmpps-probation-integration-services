package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "nsi")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class Nsi(

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "nsi_type_id")
    val type: NsiType,

    val referralDate: LocalDate,

    @Id
    @Column(name = "nsi_id")
    val id: Long = 0,

    val eventId: Long? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    var active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@Table(name = "r_nsi_type")
class NsiType(val code: String, @Id @Column(name = "nsi_type_id") val id: Long)

interface NsiRepository : JpaRepository<Nsi, Long> {

    @Query(
        """
           with latest_breach as (select nsi.referral_date as breachdate
                       from nsi nsi
                                join r_nsi_type ref on nsi.nsi_type_id = ref.nsi_type_id
                                left join event on event.event_id = nsi.event_id
                       where nsi.offender_id = :personId
                         and (event.event_id is null or (event.active_flag = 1 and event.soft_deleted = 0))
                         and nsi.active_flag = 1
                         and nsi.soft_deleted = 0
                         and ref.code = 'BRE'
                       order by nsi.referral_date desc
                           fetch next 1 row only),
                 latest_recall as (select nsi.referral_date as recalldate
                       from nsi nsi
                                join r_nsi_type ref on nsi.nsi_type_id = ref.nsi_type_id
                                left join event on event.event_id = nsi.event_id
                       where nsi.offender_id = :personId
                         and (event.event_id is null or (event.active_flag = 1 and event.soft_deleted = 0))
                         and nsi.active_flag = 1
                         and nsi.soft_deleted = 0
                         and ref.code = 'REC'
                       order by nsi.referral_date desc
                           fetch next 1 row only)
            select breachdate as referralDate, 'breach' as name
            from latest_breach
            union all
            select recalldate as referralDate, 'recall' as name
            from latest_recall
        """,
        nativeQuery = true
    )
    fun findBreachAndRecallDates(personId: Long): List<NsiDate>
}

interface NsiDate {
    val name: String
    val referralDate: LocalDate
}
