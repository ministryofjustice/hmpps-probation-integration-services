package uk.gov.justice.digital.hmpps.data

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ResponsibleOfficerGenerator
import uk.gov.justice.digital.hmpps.data.repository.IapsPersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.ResponsibleOfficerRepository

@Component
class PersonAllocationDataLoader(
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val responsibleOfficerRepository: ResponsibleOfficerRepository,
    private val iapsPersonRepository: IapsPersonRepository
) {
    fun loadData() {
        personRepository.save(PersonGenerator.DEFAULT)
        PersonManagerGenerator.DEFAULT = personManagerRepository.save(PersonManagerGenerator.DEFAULT)
        ResponsibleOfficerGenerator.DEFAULT = responsibleOfficerRepository.save(ResponsibleOfficerGenerator.generate())
    }
}
