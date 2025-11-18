package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter

@Entity
@Table(name = "disposal")
@Immutable
class Disposal(
    @Id
    @Column(name = "disposal_id")
    val id: Long,

    val length: Long,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @ManyToOne
    @JoinColumn(name = "disposal_type_id")
    val disposalType: DisposalType,
)

@Entity
@Immutable
@Table(name = "r_disposal_type")
class DisposalType(
    @Id
    @Column(name = "disposal_type_id")
    val id: Long,

    @Column(name = "disposal_type_code")
    val code: String,

    val description: String,

    @Column(name = "pre_cja2003")
    @Convert(converter = YesNoConverter::class)
    val preCja2003: Boolean = false
)