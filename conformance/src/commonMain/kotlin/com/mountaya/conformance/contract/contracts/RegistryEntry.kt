package com.mountaya.conformance

/**
 * RegistryEntry is one capability of the contract's `sdk/registry.yaml`, as compiled into the
 * harness: its id, the layer that owns it, whether a consumer calls it, and its fixture's path.
 */
internal class RegistryEntry(
    val id: String,
    val layer: String,
    val api: Boolean,
    val fixture: String,
)
