package uk.gov.justice.digital.hmpps.data.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
@Table(name = "iaps_offender")
class IapsPerson(
    @Id
    @Column(name = "offender_id")
    val personId: Long,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val iapsFlag: Boolean
)

interface IapsPersonRepository : JpaRepository<IapsPerson, Long>