package uk.gov.justice.digital.hmpps.entity.sentence

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.Contact
import uk.gov.justice.digital.hmpps.entity.ContactType.Companion.LICENCE_SUPERVISION_TWO_THIRDS_POINT
import uk.gov.justice.digital.hmpps.entity.ContactType.Companion.SUPERVISION_TWO_THIRDS_POINT
import uk.gov.justice.digital.hmpps.entity.Person

@Entity
@Immutable
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Event(
    @Id
    @Column(name = "event_id")
    val id: Long,

    @Column(name = "event_number")
    val number: String,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal?,

    @OneToMany(mappedBy = "event")
    @SQLRestriction("contact_type_id in (select t.contact_type_id from r_contact_type t where t.code in ('${SUPERVISION_TWO_THIRDS_POINT}', '${LICENCE_SUPERVISION_TWO_THIRDS_POINT}'))")
    val twoThirdsContacts: List<Contact>,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,
) {
    fun twoThirdsDate() = twoThirdsContacts.maxOfOrNull { it.date }
}
