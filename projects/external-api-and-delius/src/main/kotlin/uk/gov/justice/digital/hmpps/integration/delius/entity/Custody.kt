package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0")
class Custody(

    @OneToOne
    @JoinColumn(name = "disposal_id", updatable = false)
    val disposal: Disposal,

    @Column(columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "custody_id")
    val id: Long
)

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class KeyDate(

    @ManyToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody,

    @ManyToOne
    @JoinColumn(name = "key_date_type_id")
    val type: ReferenceData,

    @Column(name = "key_date")
    var date: LocalDate,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "key_date_id")
    val id: Long
) {
    enum class Type(val code: String) {
        EXPECTED_RELEASE_DATE("EXP")
    }
}

interface KeyDateRepository : JpaRepository<KeyDate, Long> {
    @EntityGraph(attributePaths = ["type"])
    fun findByCustodyIdAndTypeCode(custodyId: Long, typeCode: String): KeyDate?
}

fun KeyDateRepository.getExpectedReleaseDate(custodyId: Long) =
    findByCustodyIdAndTypeCode(custodyId, KeyDate.Type.EXPECTED_RELEASE_DATE.code)