import java.util.concurrent.atomic.AtomicLong

object IdGenerator {
    private val id = AtomicLong(1)

    fun getAndIncrement() = id.getAndIncrement()

    fun get() = id.get()
}
