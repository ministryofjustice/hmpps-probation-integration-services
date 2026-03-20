package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.ZonedDateTime


@Entity
@Table(name = "contact")
@SQLRestriction("soft_deleted = 0")
class Contact(
    @Id
    @Column(name = "contact_id")
    val id: Long,

    @Column(name = "contact_date")
    val date: LocalDate,

    @Column(name = "contact_start_time")
    val startTime: ZonedDateTime,

    @Lob
    @Column(name = "notes")
    val notes: String?,

    val eventId: Long,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val type: ContactType,

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id")
    val outcomeType: ContactOutcomeType,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean

)

@Entity
@Table(name = "r_contact_outcome_type")
class ContactOutcomeType(
    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long,
    val code: String,
    val description: String,
    @Column(name = "enforceable", columnDefinition = "char(1)")
    @Convert(converter = YesNoConverter::class)
    val enforceable: Boolean?,
)

@Entity
@Table(name = "r_contact_type")
class ContactType(
    @Id
    @Column(name = "contact_type_id")
    val id: Long,
    val code: String,
    val description: String
)

interface ContactRepository : JpaRepository<Contact, Long> {
    @Query(
        """
        select c from Contact c
        join c.outcomeType cot
        where c.eventId = :eventId
        and cot.enforceable = true
    """
    )
    fun findEnforceableByEventId(eventId: Long): List<Contact>
}
