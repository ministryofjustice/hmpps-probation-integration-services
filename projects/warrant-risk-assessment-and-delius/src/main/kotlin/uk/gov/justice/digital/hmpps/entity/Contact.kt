package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Immutable
@Table(name = "contact")
@SQLRestriction("soft_deleted = 0")
class Contact(
    @Id
    @Column(name = "contact_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @Column(name = "contact_date")
    val date: LocalDate,

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(columnDefinition = "char(1)")
    @Convert(converter = YesNoConverter::class)
    val documentLinked: Boolean? = null,
)

@Entity
@Immutable
@Table(name = "r_contact_type")
class ContactType(
    @Id
    @Column(name = "contact_type_id")
    val id: Long,

    val code: String,

    @Column(columnDefinition = "char(1)")
    @Convert(converter = YesNoConverter::class)
    val homeVisit: Boolean? = null,
)

interface ContactRepository : JpaRepository<Contact, Long> {
    @Query(
        """
        select max(c.date)
        from Contact c
        where c.personId = :personId
        and c.type.homeVisit = true
        and (c.date < current_date or
             (c.date = current_date and to_char(c.startTime, 'HH24:MI:SS') <= to_char(local_time, 'HH24:MI:SS')))
        """
    )
    fun findLastHomeVisitDate(personId: Long): LocalDate?
}
