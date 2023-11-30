package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.Immutable
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "nsi")
@SQLRestriction("soft_deleted = 0")
@EntityListeners(AuditingEntityListener::class)
@SequenceGenerator(name = "nsi_id_generator", sequenceName = "nsi_id_seq", allocationSize = 1)
class Nsi(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    val referralDate: LocalDate,

    @ManyToOne
    @JoinColumn(name = "nsi_type_id")
    val type: NsiType,

    @ManyToOne
    @JoinColumn(name = "nsi_sub_type_id")
    val subType: NsiSubType?,

    @ManyToOne
    @JoinColumn(name = "nsi_status_id")
    val status: NsiStatus,

    @Column(name = "nsi_status_date")
    val statusDate: ZonedDateTime,

    val actualStartDate: ZonedDateTime?,

    @Column(name = "intended_provider_id")
    val intendedProviderId: Long? = null,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Column(columnDefinition = "number")
    val pendingTransfer: Boolean = false,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nsi_id_generator")
    @Column(name = "nsi_id")
    val id: Long = 0,

    @Version
    @Column(name = "row_version")
    val version: Long = 0
) {
    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now()

    @CreatedBy
    var createdByUserId: Long = 0

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0

    @Lob
    var notes: String? = null
        private set

    @Column(name = "active_flag", columnDefinition = "number")
    var active: Boolean = true
        private set

    var actualEndDate: ZonedDateTime? = null
        set(value) {
            field = value
            active = field == null
        }

    fun appendNotes(notes: String) {
        this.notes = this.notes?.plus(notes) ?: notes
    }
}

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "nsi_manager")
@SQLRestriction("soft_deleted = 0")
@SequenceGenerator(name = "nsi_manager_id_generator", sequenceName = "nsi_manager_id_seq", allocationSize = 1)
class NsiManager(
    @ManyToOne
    @JoinColumn(name = "nsi_id")
    val nsi: Nsi,

    @Column(name = "probation_area_id")
    val providerId: Long,

    @Column(name = "team_id")
    val teamId: Long,

    @Column(name = "staff_id")
    val staffId: Long,

    @Column
    val startDate: ZonedDateTime,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Column
    val partitionAreaId: Long = 0,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nsi_manager_id_generator")
    @Column(name = "nsi_manager_id")
    val id: Long = 0
) {
    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now()

    @CreatedBy
    var createdByUserId: Long = 0

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0
}

@Entity
@Immutable
@Table(name = "r_nsi_type")
class NsiType(
    val code: String,
    @Id
    @Column(name = "nsi_type_id")
    val id: Long
) {
    enum class Code(val value: String) {
        OPD_COMMUNITY_PATHWAY("OPD1")
    }
}

@Entity
@Immutable
@Table(name = "r_nsi_status")
class NsiStatus(
    val code: String,

    @ManyToOne
    @JoinColumn(name = "contact_type_id")
    val contactType: ContactType?,

    @Id
    @Column(name = "nsi_status_id")
    val id: Long
) {
    enum class Code(val value: String, val contactTypeCode: ContactType.Code?) {
        READY_FOR_SERVICE("OPD01", ContactType.Code.READY_FOR_SERVICES)
    }
}

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class NsiSubType(
    @Column(name = "code_value")
    val code: String,
    @Column(name = "reference_data_master_id")
    val datasetId: Long,
    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long
) {
    enum class Code(val value: String) {
        COMMUNITY_PATHWAY("OPD01"),
        COMMUNITY_PATHWAY_OVERRIDE("OPD02")
    }
}

@Immutable
@Entity
@Table(name = "r_reference_data_master")
class Dataset(
    @Column(name = "code_set_name")
    val code: String,

    @Id
    @Column(name = "reference_data_master_id")
    val id: Long
) {
    companion object {
        val NSI_SUB_TYPE = "NSI SUB TYPE"
    }
}

interface NsiRepository : JpaRepository<Nsi, Long> {
    fun findNsiByPersonIdAndTypeCode(personId: Long, code: String): Nsi?
}

interface NsiTypeRepository : JpaRepository<NsiType, Long> {
    fun findByCode(code: String): NsiType?
}

fun NsiTypeRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("NsiType", "code", code)

interface NsiSubTypeRepository : JpaRepository<NsiSubType, Long> {
    @Query(
        """
        select nst from NsiSubType nst
        join Dataset ds on nst.datasetId = ds.id
        where nst.code = :code and ds.code = :datasetCode
        """
    )
    fun findByCodeDataSet(code: String, datasetCode: String): NsiSubType?
}

fun NsiSubTypeRepository.nsiSubType(code: String) =
    findByCodeDataSet(code, Dataset.NSI_SUB_TYPE) ?: throw NotFoundException("NsiSubType", "code", code)

interface NsiStatusRepository : JpaRepository<NsiStatus, Long> {
    @EntityGraph(attributePaths = ["contactType"])
    fun findByCode(code: String): NsiStatus?
}

fun NsiStatusRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("NsiStatus", "code", code)

interface NsiManagerRepository : JpaRepository<NsiManager, Long>
