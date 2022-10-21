package uk.gov.justice.digital.hmpps.data

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.ApprovedPremisesGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator
import uk.gov.justice.digital.hmpps.data.repository.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremisesRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.security.ServiceContext
import uk.gov.justice.digital.hmpps.user.UserRepository

@Component
@Profile("dev", "integration-test")
class DataLoader(
    private val serviceContext: ServiceContext,
    private val userRepository: UserRepository,
    private val referenceDataRepository: ReferenceDataRepository,
    private val approvedPremisesRepository: ApprovedPremisesRepository,
    private val staffRepository: StaffRepository,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        userRepository.save(UserGenerator.APPLICATION_USER)
        serviceContext.setUp()

        referenceDataRepository.save(ApprovedPremisesGenerator.DEFAULT.code)
        approvedPremisesRepository.save(ApprovedPremisesGenerator.DEFAULT)

        referenceDataRepository.save(ApprovedPremisesGenerator.NO_STAFF.code)
        approvedPremisesRepository.save(ApprovedPremisesGenerator.NO_STAFF)

        referenceDataRepository.save(StaffGenerator.STAFF_GRADE)
        repeat(30) { staffRepository.save(StaffGenerator.generate("Key-worker", listOf(ApprovedPremisesGenerator.DEFAULT))) }
        repeat(10) { staffRepository.save(StaffGenerator.generate("Normal staff (not key-worker)", emptyList())) }
    }
}
