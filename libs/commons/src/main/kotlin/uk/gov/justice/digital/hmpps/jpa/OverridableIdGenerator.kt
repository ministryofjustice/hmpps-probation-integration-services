package uk.gov.justice.digital.hmpps.jpa

import jakarta.persistence.SequenceGenerator
import org.hibernate.annotations.IdGeneratorType
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.generator.AnnotationBasedGenerator
import org.hibernate.generator.GeneratorCreationContext
import org.hibernate.id.enhanced.SequenceStyleGenerator
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Member
import java.util.*
import kotlin.annotation.AnnotationTarget.FIELD

@Target(FIELD)
@Retention(AnnotationRetention.RUNTIME)
@IdGeneratorType(OverridableIdGenerator::class)
annotation class GeneratedId(
    val generator: String
)

/**
 * A custom ID generator implementation that extends the functionality of `SequenceStyleGenerator` to support both
 * sequence-generated IDs and manually assigned IDs. This allows us to manually set IDs when setting up data while
 * preserving the standard sequence generation during normal use.
 *
 * For Hibernate versions prior to 6.6, this was the default.
 * See https://discourse.hibernate.org/t/optimisticlockexception-when-manually-setting-the-id-for-the-entity/10975/14
 */
class OverridableIdGenerator : AnnotationBasedGenerator<GeneratedId>, SequenceStyleGenerator() {
    private lateinit var annotation: GeneratedId
    private lateinit var member: Member

    override fun initialize(annotation: GeneratedId?, member: Member?, context: GeneratorCreationContext?) {
        this.annotation = requireNotNull(annotation)
        this.member = requireNotNull(member)
    }

    override fun configure(context: GeneratorCreationContext, parameters: Properties?) {
        val sequenceGenerator = (member.annotations<SequenceGenerator>() + member.classAnnotations<SequenceGenerator>())
            .firstOrNull { it.name == annotation.generator }
            ?: throw IllegalArgumentException("No sequence generator annotation found with name ${annotation.generator}")

        val parameters = parameters ?: Properties()
        parameters[CATALOG] = sequenceGenerator.catalog
        parameters[SCHEMA] = sequenceGenerator.schema
        parameters[SEQUENCE_PARAM] = sequenceGenerator.sequenceName
        parameters[INITIAL_PARAM] = sequenceGenerator.initialValue
        parameters[INCREMENT_PARAM] = sequenceGenerator.allocationSize

        if (context.serviceRegistry != null) super.configure(context, parameters)
    }

    override fun generate(session: SharedSessionContractImplementor, obj: Any): Any {
        val id = session.getEntityPersister(null, obj).getIdentifier(obj, session).takeIf { it != 0L }
        return id ?: super.generate(session, obj)
    }

    override fun allowAssignedIdentifiers() = true

    private inline fun <reified T : Annotation> Member.annotations(): List<T> =
        (this as? AnnotatedElement)?.getAnnotationsByType(T::class.java).orEmpty().toList()

    private inline fun <reified T : Annotation> Member.classAnnotations(): List<T> =
        this.declaringClass.getAnnotationsByType(T::class.java).toList()
}
