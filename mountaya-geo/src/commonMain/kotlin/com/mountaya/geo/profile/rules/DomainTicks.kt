package com.mountaya.geo

/**
 * domainTicks is the elevations a chart's scale is labelled at, low to high: the [domain]'s floor, its middle
 * and its ceiling (`geo.profile.domain`). Three, since that is what a short plot holds without its labels
 * touching, and each states one of the frame's own bounds, so none is rounded.
 */
public fun domainTicks(domain: ElevationDomain): List<Double> =
    listOf(domain.min, (domain.min + domain.max) / 2.0, domain.max)
