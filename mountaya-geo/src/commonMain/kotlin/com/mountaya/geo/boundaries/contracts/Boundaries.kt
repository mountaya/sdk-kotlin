package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * Boundaries is one of an organization's geographic boundaries, a `name` and a `bbox` written
 * `[west, south, east, north]` in degrees: the generated `RestSchema.Boundaries`, named from Geo,
 * never a copy of it.
 */
public typealias Boundaries = RestSchema.Boundaries
