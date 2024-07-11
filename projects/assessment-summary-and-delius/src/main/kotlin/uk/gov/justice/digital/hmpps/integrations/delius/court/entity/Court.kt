package uk.gov.justice.digital.hmpps.integrations.delius.court.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
class Court(
    @Column(columnDefinition = "char(6)")
    val code: String,

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,

    @Id
    @Column(name = "court_id")
    val id: Long
)

interface CourtRepository : JpaRepository<Court, Long> {
    fun findByCode(code: String): Court?
    fun findByCodeAndSelectableTrue(code: String): Court?
}

fun CourtRepository.getByCode(code: String) =
    findByCodeAndSelectableTrue(code) ?: findByCode(code)
