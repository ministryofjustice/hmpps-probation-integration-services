package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "custody")
class Custody(
    @Id
    @Column(name = "custody_id")
    val id: Long,

    @Column(name = "disposal_id")
    val disposalId: Long,

    @Column
    var prisonerNumber: String?,

    @ManyToOne
    @JoinColumn(name = "custodial_status_id")
    val status: ReferenceData,

    @OneToMany(mappedBy = "custody")
    val keyDates: List<KeyDate> = listOf(),

    @OneToOne
    @JoinColumn(name = "disposal_id", updatable = false, insertable = false)
    val disposal: Disposal,

    @Column(name = "soft_deleted", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

@Entity
@SQLRestriction("soft_deleted = 0")
class KeyDate(

    @Id
    @Column(name = "key_date_id")
    val id: Long?,

    @ManyToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody? = null,

    @ManyToOne
    @JoinColumn(name = "key_date_type_id")
    val type: ReferenceData,

    @Column(name = "key_date")
    var date: LocalDate,

    @Column(name = "soft_deleted", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

interface CustodyRepository : JpaRepository<Custody, Long> {
    fun getCustodyByDisposalId(disposalId: Long): Custody?

    @Query(
        """
            select count(c) from Custody c
            join Disposal d on d.id = c.disposalId and d.active = true and d.softDeleted = false 
            where d.event.convictionEventPerson.id = :personId
            and c.status.code = 'D'
        """
    )
    fun isInCustodyCount(personId: Long): Int?
}

fun CustodyRepository.isInCustody(personId: Long) = (isInCustodyCount(personId) ?: 0) > 0
