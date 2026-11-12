package uk.gov.justice.digital.hmpps.integrations.delius.person.registration

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person

@Entity
@Immutable
@SQLRestriction("deregistered = 0 and soft_deleted = 0")
class Registration(
    @Id
    @Column(name = "registration_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val deregistered: Boolean = false,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

