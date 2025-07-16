package uk.gov.justice.digital.hmpps.entity.sentence.custody

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import uk.gov.justice.digital.hmpps.entity.sentence.Disposal
import uk.gov.justice.digital.hmpps.entity.sentence.PssRequirement

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class Custody(
    @Id
    @Column(name = "custody_id")
    val id: Long,

    @OneToMany(mappedBy = "custody")
    val releases: List<Release>,

    @OneToMany(mappedBy = "custody")
    val postSentenceSupervisionRequirements: List<PssRequirement>,

    @OneToMany(mappedBy = "custody")
    @SQLRestriction("key_date_type_id in (select t.standard_reference_list_id from r_standard_reference_list t where t.code_value = '${KeyDate.Companion.POST_SENTENCE_SUPERVISION_END_DATE}')")
    val postSentenceSupervisionEndDate: List<KeyDate>,

    @OneToMany(mappedBy = "custody")
    @SQLRestriction("key_date_type_id in (select t.standard_reference_list_id from r_standard_reference_list t where t.code_value = '${KeyDate.Companion.PROBATION_RESET_DATE}')")
    val probationResetDate: List<KeyDate>,

    @OneToMany(mappedBy = "custody")
    @SQLRestriction("key_date_type_id in (select t.standard_reference_list_id from r_standard_reference_list t where t.code_value = '${KeyDate.Companion.LICENCE_EXPIRY_DATE}')")
    val licenceEndDate: List<KeyDate>,

    @OneToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,
) {
    fun mostRecentRelease() = releases.maxWithOrNull(compareBy({ it.date }, { it.createdDateTime }))
    fun postSentenceSupervisionEndDate() = postSentenceSupervisionEndDate.firstOrNull()?.date
    fun probationResetDate() = probationResetDate.firstOrNull()?.date
    fun licenceEndDate() = licenceEndDate.firstOrNull()?.date
}