package cs.ut.exceptions

class NirdizatiRuntimeException(
    message: String,
    cause: Throwable? = null,
    enableSupression: Boolean = true,
    writeableStackTrace: Boolean = true
) : RuntimeException(message, cause, enableSupression, writeableStackTrace) {
}