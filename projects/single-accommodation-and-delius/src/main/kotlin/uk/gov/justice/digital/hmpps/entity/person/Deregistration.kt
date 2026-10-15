package uk.gov.justice.digital.hmpps.entity.person

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import java.time.LocalDate

@Entity
@Table(name = "deregistration")
@SQLRestriction("soft_deleted = 0")
class Deregistration(
    @Id
    @Column(name = "deregistration_id")
    val id: Long = 0,

    @Column(name = "deregistration_date")
    val endDate: LocalDate,

    @ManyToOne
    @JoinColumn(name = "registration_id")
    val registration: Registration,

    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)