package uk.gov.justice.digital.hmpps.entity.sentence.offence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.model.Offence
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "r_offence")
class OffenceEntity(
    @Id
    @Column(name = "offence_id")
    val id: Long,

    @Column(columnDefinition = "char(3)")
    val mainCategoryCode: String,

    @Column
    val mainCategoryDescription: String,

    @Column(columnDefinition = "char(2)")
    val subCategoryCode: String,

    @Column
    val subCategoryDescription: String,
) {
    fun toOffence(date: LocalDate) =
        Offence(date, mainCategoryCode, mainCategoryDescription, subCategoryCode, subCategoryDescription)
}