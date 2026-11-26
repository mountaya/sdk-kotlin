package com.mountaya.geo

/**
 * isWatermarkKeptClear answers whether nothing of a map's own may lay over the Mountaya watermark's frame as the read
 * of its organization stands, [read] (`geo.organization.watermark`): the frame [watermarkWidth] wide and
 * [watermarkHeight] tall, its top-left corner [watermarkInset] from the top-left corner of the map's safe area.
 *
 * The frame is held from the moment the map is drawn, while the read is waiting too, so a control stack, a panel, a
 * readout or a marker's label kept clear of it does not move as the mark lands, and is released only once the read
 * answers that the organization holds white label, which draws no mark. A read that failed, one that has waited one
 * attempt's time budget, and the read a client holding no publishable key never makes all hold it, since each draws
 * the mark. A host reads it off `MapEngine.organizationRead` to keep its own views clear of the frame as the SDK keeps
 * its own, as `sdk-dart`'s Geo publishes the rule.
 */
public fun isWatermarkKeptClear(read: OrganizationRead): Boolean =
    !(read is OrganizationRead.Answered && read.whiteLabel)
