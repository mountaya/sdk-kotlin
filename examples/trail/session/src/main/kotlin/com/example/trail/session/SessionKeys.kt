package com.example.trail.session

/** PUBLISHABLE_KEY is the variable the publishable key is read from, the one every session is minted for. */
const val PUBLISHABLE_KEY = "MOUNTAYA_PUBLISHABLE_KEY"

/** SECRET_KEY is the variable the secret key is read from, the one `postSession` is authenticated with. */
const val SECRET_KEY = "MOUNTAYA_SECRET_KEY"

/** SessionKeys is the two keys the server reads from the shell: the one each session is for, and the one that mints. */
class SessionKeys(
    /** The publishable key every session is minted for. */
    val publishable: String,
    /** The secret key `postSession` is authenticated with, which never leaves this process. */
    val secret: String,
) {
    override fun toString(): String = "SessionKeys(<redacted>)"

    /** The reading of the keys. */
    companion object {
        /**
         * Reads the keys [environment] holds, or the one sentence that refuses to start without them, naming each
         * missing one and never a key.
         */
        fun read(environment: Map<String, String>): Result<SessionKeys> {
            val publishable = environment[PUBLISHABLE_KEY]?.takeIf { it.isNotBlank() }
            val secret = environment[SECRET_KEY]?.takeIf { it.isNotBlank() }
            if (publishable != null && secret != null) return Result.success(SessionKeys(publishable, secret))
            val missing =
                listOfNotNull(PUBLISHABLE_KEY.takeIf { publishable == null }, SECRET_KEY.takeIf { secret == null })
            val sentence =
                if (missing.size == 1) {
                    "${missing.single()} is not set: export it in this shell, from your organization's API keys, " +
                        "then run mise run trail:session again."
                } else {
                    "${missing.joinToString(" and ")} are not set: export them in this shell, from your " +
                        "organization's API keys, then run mise run trail:session again."
                }
            return Result.failure(IllegalStateException(sentence))
        }
    }
}
