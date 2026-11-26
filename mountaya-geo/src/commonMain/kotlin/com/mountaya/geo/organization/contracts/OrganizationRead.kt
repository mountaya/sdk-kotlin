package com.mountaya.geo

/**
 * OrganizationRead is where a map's read of its organization stands (`map.organization.read`), as its watermark reads
 * it (`geo.organization.watermark`), in one of five states: waiting for an answer within one attempt's time budget;
 * answered, the organization holding white label or not, the organization itself held by `MapEngine.organization`;
 * failed, a read that reached the transport's own time budget among the failures; timed out, one attempt's time budget
 * passed without an answer while further attempts may go on; and no key, a client holding no publishable key, which
 * makes no read. The five carry the same names in every SDK: waiting, answered, failed, timed out and no key.
 */
public sealed interface OrganizationRead {
    /**
     * Waiting is a read sent and not yet answered within one attempt's time budget, the client's budget for
     * `postOrganizationLookup`: the map draws no mark yet, and keeps the mark's frame clear for it.
     */
    public object Waiting : OrganizationRead {
        override fun toString(): String = "OrganizationRead.Waiting"
    }

    /** Answered is a read the API answered, the organization holding white label or not. */
    public class Answered(
        /** Whether the organization holds white label, `Organization.white_label`. */
        public val whiteLabel: Boolean,
    ) : OrganizationRead {
        override fun equals(other: Any?): Boolean = other is Answered && other.whiteLabel == whiteLabel

        override fun hashCode(): Int = whiteLabel.hashCode()

        override fun toString(): String = "OrganizationRead.Answered(whiteLabel=$whiteLabel)"
    }

    /**
     * Failed is a read that failed, a read that reached the transport's own time budget among the failures, which
     * reads as no white label.
     */
    public object Failed : OrganizationRead {
        override fun toString(): String = "OrganizationRead.Failed"
    }

    /**
     * TimedOut is a read that has gone one attempt's time budget without an answer, its further attempts going on or
     * not, which reads as no white label until a later answer says otherwise.
     */
    public object TimedOut : OrganizationRead {
        override fun toString(): String = "OrganizationRead.TimedOut"
    }

    /** NoKey is no read at all: the map's client holds no publishable key, a share token or a secret key alone. */
    public object NoKey : OrganizationRead {
        override fun toString(): String = "OrganizationRead.NoKey"
    }
}
