package com.mountaya.client

/**
 * NamedException is a failure that declares the contract's name of its class as data, such as
 * `PreconditionFailedError`, which is what [isMountayaException] and [isApiException] read.
 *
 * Every failure the client reports is one, through [MountayaException]. A class of another module's
 * may be one too: a failure that crossed a boundary the client's own classes did not, rebuilt on the
 * far side by code of its own, declares the name it was raised under, and the two predicates
 * recognise it by that name as they recognise one of the client's own (`transport.errors.structural`).
 * An `is MountayaException` check still tells the client's own apart.
 */
public interface NamedException {
    /** The contract's name for the failure's class, such as `PreconditionFailedError`. */
    public val name: String
}
