package uk.gov.justice.digital.hmpps.entity.registration

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.PersonCrn
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import java.time.LocalDate

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0 and deregistered = 0")
class Registration(
    @Id
    @Column(name = "registration_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: PersonCrn,

    @ManyToOne
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @ManyToOne
    @JoinColumn(name = "register_category_id")
    val category: ReferenceData,

    @Column(name = "registration_date")
    val date: LocalDate,

    @Column(name = "next_review_date")
    val nextReviewDate: LocalDate?,

    @Column(name = "deregistered", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val deregistered: Boolean = false,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)