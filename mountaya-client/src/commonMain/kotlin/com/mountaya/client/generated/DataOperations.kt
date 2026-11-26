// GENERATED FILE. DO NOT EDIT.
//
// Printed by `./gradlew generateClient` from sdk/model.json of mountaya/schemas, contract
// v0.9.0, model digest f9ded6dd4ca71cb8f89c6974c42adec77509ee9a19e5afd01c31b6ea9dcc33ea.
// Every name below is the contract's, verbatim, but an enum member's, printed as its words
// (SPEC.md section 3). A shape that is wrong here is a defect in the contract's description,
// and the next print overwrites any edit.

package com.mountaya.client.generated

/** DataOperations is each Data API operation with its full field tree, which a caller's selection is held to. */
internal object DataOperations {
    /** `analyzeGeometry`, with its full field tree. */
    val analyzeGeometry: com.mountaya.client.DataOperation =
        com.mountaya.client.DataOperation(
            name = "analyzeGeometry",
            inputType = "AnalyzeGeometryInput!",
            selection =
                com.mountaya.client.FieldSelection {
                    field("analysis") {
                        field("elevation") {
                            field("altitude") {
                                field("max")
                                field("min")
                            }
                            field("ascent")
                            field("descent")
                            field("gradient") {
                                field("max")
                                field("min")
                            }
                        }
                        field("elevations")
                        field("slopeInfo") {
                            field("fromIndex")
                            field("toIndex")
                            field("value")
                        }
                        field("speedInfo") {
                            field("fromIndex")
                            field("toIndex")
                            field("value")
                        }
                        field("surfaceInfo") {
                            field("fromIndex")
                            field("toIndex")
                            field("value")
                        }
                        field("trailDifficultyInfo") {
                            field("fromIndex")
                            field("toIndex")
                            field("value")
                        }
                        field("wayTypeInfo") {
                            field("fromIndex")
                            field("toIndex")
                            field("value")
                        }
                        field("advisories") {
                            field("tollway")
                            field("restrictedAccess")
                            field("tollwayInfo") {
                                field("fromIndex")
                                field("toIndex")
                                field("value")
                            }
                            field("roadAccessInfo") {
                                field("fromIndex")
                                field("toIndex")
                                field("values")
                            }
                        }
                    }
                    field("duration")
                },
        )

    /** `directions`, with its full field tree. */
    val directions: com.mountaya.client.DataOperation =
        com.mountaya.client.DataOperation(
            name = "directions",
            inputType = "DirectionsInput!",
            selection =
                com.mountaya.client.FieldSelection {
                    field("routes") {
                        field("distance")
                        field("duration")
                        field("elevation") {
                            field("altitude") {
                                field("max")
                                field("min")
                            }
                            field("ascent")
                            field("descent")
                            field("gradient") {
                                field("max")
                                field("min")
                            }
                        }
                        field("geometry")
                        field("analysis") {
                            field("elevation") {
                                field("altitude") {
                                    field("max")
                                    field("min")
                                }
                                field("ascent")
                                field("descent")
                                field("gradient") {
                                    field("max")
                                    field("min")
                                }
                            }
                            field("elevations")
                            field("slopeInfo") {
                                field("fromIndex")
                                field("toIndex")
                                field("value")
                            }
                            field("speedInfo") {
                                field("fromIndex")
                                field("toIndex")
                                field("value")
                            }
                            field("surfaceInfo") {
                                field("fromIndex")
                                field("toIndex")
                                field("value")
                            }
                            field("trailDifficultyInfo") {
                                field("fromIndex")
                                field("toIndex")
                                field("value")
                            }
                            field("wayTypeInfo") {
                                field("fromIndex")
                                field("toIndex")
                                field("value")
                            }
                            field("advisories") {
                                field("tollway")
                                field("restrictedAccess")
                                field("tollwayInfo") {
                                    field("fromIndex")
                                    field("toIndex")
                                    field("value")
                                }
                                field("roadAccessInfo") {
                                    field("fromIndex")
                                    field("toIndex")
                                    field("values")
                                }
                            }
                        }
                        field("avoidance")
                        field("summary")
                        field("steps") {
                            field("fromIndex")
                            field("toIndex")
                            field("type")
                            field("name")
                            field("distance")
                            field("duration")
                            field("exitNumber")
                        }
                    }
                },
        )

    /** `exportItinerary`, with its full field tree. */
    val exportItinerary: com.mountaya.client.DataOperation =
        com.mountaya.client.DataOperation(
            name = "exportItinerary",
            inputType = "ExportItineraryInput!",
            selection =
                com.mountaya.client.FieldSelection {
                    field("url")
                    field("filename")
                    field("format")
                    field("expiresAt")
                },
        )

    /** `geocode`, with its full field tree. */
    val geocode: com.mountaya.client.DataOperation =
        com.mountaya.client.DataOperation(
            name = "geocode",
            inputType = "GeocodeInput!",
            selection =
                com.mountaya.client.FieldSelection {
                    field("count")
                    field("places") {
                        field("osm") {
                            field("id")
                            field("type")
                            field("key")
                            field("value")
                        }
                        field("name")
                        field("label")
                        field("layer")
                        field("category")
                        field("point")
                        field("bounds")
                        field("country")
                        field("countryName")
                        field("region")
                        field("county")
                        field("city")
                        field("postcode")
                        field("street")
                        field("houseNumber")
                    }
                },
        )

    /** `isochrones`, with its full field tree. */
    val isochrones: com.mountaya.client.DataOperation =
        com.mountaya.client.DataOperation(
            name = "isochrones",
            inputType = "IsochronesInput!",
            selection =
                com.mountaya.client.FieldSelection {
                    field("isochrones") {
                        field("value")
                        field("area")
                        field("reachFactor")
                        field("center")
                        field("geometry")
                    }
                },
        )

    /** `itinerary`, with its full field tree. */
    val itinerary: com.mountaya.client.DataOperation =
        com.mountaya.client.DataOperation(
            name = "itinerary",
            inputType = "ItineraryInput!",
            selection =
                com.mountaya.client.FieldSelection {
                    field("id")
                    field("name")
                    field("description")
                    field("activitySlug")
                    field("distance")
                    field("ascent")
                    field("isPrivate")
                    field("organizationId")
                    field("segments") {
                        field("id")
                        field("name")
                        field("description")
                        field("position")
                        field("groupId")
                        field("distance")
                        field("duration")
                        field("ascent")
                        field("descent")
                        field("altitude") {
                            field("max")
                            field("min")
                        }
                        field("gradient") {
                            field("max")
                            field("min")
                        }
                        field("geometry")
                    }
                    field("waypoints") {
                        field("id")
                        field("name")
                        field("description")
                        field("position")
                        field("point")
                        field("amenities")
                        field("timing") {
                            field("cutoffAt")
                            field("stopDuration")
                        }
                        field("contact") {
                            field("website")
                            field("phone")
                            field("email")
                            field("address")
                            field("openingHours")
                            field("bookingUrl")
                        }
                        field("media") {
                            field("photos") {
                                field("url")
                                field("caption")
                            }
                            field("links") {
                                field("label")
                                field("url")
                            }
                        }
                        field("properties") {
                            field("key")
                            field("label")
                            field("value")
                            field("format")
                        }
                    }
                    field("groups") {
                        field("id")
                        field("name")
                        field("color")
                        field("speed")
                    }
                    field("segmentCount")
                    field("waypointCount")
                    field("groupCount")
                },
        )

    /** `matrix`, with its full field tree. */
    val matrix: com.mountaya.client.DataOperation =
        com.mountaya.client.DataOperation(
            name = "matrix",
            inputType = "MatrixInput!",
            selection =
                com.mountaya.client.FieldSelection {
                    field("durations")
                    field("distances")
                },
        )

    /** `pois`, with its full field tree. */
    val pois: com.mountaya.client.DataOperation =
        com.mountaya.client.DataOperation(
            name = "pois",
            inputType = "PoisInput!",
            selection =
                com.mountaya.client.FieldSelection {
                    field("count")
                    field("pois") {
                        field("osm") {
                            field("id")
                            field("type")
                            field("key")
                            field("value")
                        }
                        field("name")
                        field("category")
                        field("point")
                        field("elevation")
                        field("tags") {
                            field("key")
                            field("value")
                        }
                        field("distanceMeters")
                        field("country")
                        field("countryName")
                        field("region")
                        field("city")
                    }
                },
        )

    /** `reverseGeocode`, with its full field tree. */
    val reverseGeocode: com.mountaya.client.DataOperation =
        com.mountaya.client.DataOperation(
            name = "reverseGeocode",
            inputType = "ReverseGeocodeInput!",
            selection =
                com.mountaya.client.FieldSelection {
                    field("point")
                    field("places") {
                        field("osm") {
                            field("id")
                            field("type")
                            field("key")
                            field("value")
                        }
                        field("name")
                        field("label")
                        field("layer")
                        field("category")
                        field("point")
                        field("bounds")
                        field("country")
                        field("countryName")
                        field("region")
                        field("county")
                        field("city")
                        field("postcode")
                        field("street")
                        field("houseNumber")
                    }
                    field("count")
                },
        )

    /** `search`, with its full field tree. */
    val search: com.mountaya.client.DataOperation =
        com.mountaya.client.DataOperation(
            name = "search",
            inputType = "SearchInput!",
            selection =
                com.mountaya.client.FieldSelection {
                    field("query")
                    field("language")
                    field("targets") {
                        field("places")
                        field("pois")
                        field("ways")
                        field("itineraries")
                        field("collections")
                        field("zones")
                    }
                    field("places") {
                        field("count")
                        field("places") {
                            field("osm") {
                                field("id")
                                field("type")
                                field("key")
                                field("value")
                            }
                            field("name")
                            field("label")
                            field("layer")
                            field("category")
                            field("point")
                            field("bounds")
                            field("country")
                            field("countryName")
                            field("region")
                            field("county")
                            field("city")
                            field("postcode")
                            field("street")
                            field("houseNumber")
                        }
                    }
                    field("pois") {
                        field("count")
                        field("pois") {
                            field("osm") {
                                field("id")
                                field("type")
                                field("key")
                                field("value")
                            }
                            field("name")
                            field("category")
                            field("point")
                            field("elevation")
                            field("tags") {
                                field("key")
                                field("value")
                            }
                            field("distanceMeters")
                            field("country")
                            field("countryName")
                            field("region")
                            field("city")
                        }
                    }
                    field("ways") {
                        field("count")
                        field("ways") {
                            field("osm") {
                                field("id")
                                field("type")
                                field("key")
                                field("value")
                            }
                            field("name")
                            field("highway")
                            field("surface")
                            field("sacScale")
                            field("point")
                            field("bounds")
                            field("coordinates")
                            field("activityCompatible")
                            field("country")
                            field("countryName")
                            field("region")
                            field("city")
                        }
                        field("activityApplied")
                    }
                    field("itineraries") {
                        field("count")
                        field("itineraries") {
                            field("id")
                            field("name")
                            field("description")
                            field("distance")
                            field("ascent")
                            field("activitySlug")
                            field("start")
                            field("bounds")
                            field("isPrivate")
                            field("organizationId")
                            field("score")
                            field("matchedField")
                        }
                    }
                    field("collections") {
                        field("count")
                        field("collections") {
                            field("id")
                            field("name")
                            field("description")
                            field("count") {
                                field("itineraries")
                                field("zones")
                            }
                            field("bounds")
                            field("isPrivate")
                            field("organizationId")
                            field("score")
                            field("matchedField")
                        }
                    }
                    field("zones") {
                        field("count")
                        field("zones") {
                            field("id")
                            field("name")
                            field("description")
                            field("level")
                            field("mode")
                            field("point")
                            field("bounds")
                            field("isPrivate")
                            field("organizationId")
                            field("score")
                            field("matchedField")
                        }
                    }
                    field("errors") {
                        field("target")
                        field("code")
                        field("message")
                    }
                },
        )

    /** `suggestions`, with its full field tree. */
    val suggestions: com.mountaya.client.DataOperation =
        com.mountaya.client.DataOperation(
            name = "suggestions",
            inputType = "SuggestionsInput!",
            selection =
                com.mountaya.client.FieldSelection {
                    field("routes") {
                        field("distance")
                        field("duration")
                        field("elevation") {
                            field("altitude") {
                                field("max")
                                field("min")
                            }
                            field("ascent")
                            field("descent")
                            field("gradient") {
                                field("max")
                                field("min")
                            }
                        }
                        field("geometry")
                        field("analysis") {
                            field("elevation") {
                                field("altitude") {
                                    field("max")
                                    field("min")
                                }
                                field("ascent")
                                field("descent")
                                field("gradient") {
                                    field("max")
                                    field("min")
                                }
                            }
                            field("elevations")
                            field("slopeInfo") {
                                field("fromIndex")
                                field("toIndex")
                                field("value")
                            }
                            field("speedInfo") {
                                field("fromIndex")
                                field("toIndex")
                                field("value")
                            }
                            field("surfaceInfo") {
                                field("fromIndex")
                                field("toIndex")
                                field("value")
                            }
                            field("trailDifficultyInfo") {
                                field("fromIndex")
                                field("toIndex")
                                field("value")
                            }
                            field("wayTypeInfo") {
                                field("fromIndex")
                                field("toIndex")
                                field("value")
                            }
                            field("advisories") {
                                field("tollway")
                                field("restrictedAccess")
                                field("tollwayInfo") {
                                    field("fromIndex")
                                    field("toIndex")
                                    field("value")
                                }
                                field("roadAccessInfo") {
                                    field("fromIndex")
                                    field("toIndex")
                                    field("values")
                                }
                            }
                        }
                        field("summary")
                        field("steps") {
                            field("fromIndex")
                            field("toIndex")
                            field("type")
                            field("name")
                            field("distance")
                            field("duration")
                            field("exitNumber")
                        }
                        field("seed")
                    }
                },
        )

    /** `terrainGrid`, with its full field tree. */
    val terrainGrid: com.mountaya.client.DataOperation =
        com.mountaya.client.DataOperation(
            name = "terrainGrid",
            inputType = "TerrainGridInput!",
            selection =
                com.mountaya.client.FieldSelection {
                    field("count")
                    field("coveredCount")
                    field("zoom")
                    field("resolutionMeters")
                    field("cols")
                    field("rows")
                    field("cells") {
                        field("point")
                        field("elevation")
                        field("slope")
                        field("aspect")
                        field("aspectOctant")
                        field("steepnessBand")
                        field("distanceMeters")
                    }
                },
        )

    /** `terrainProfile`, with its full field tree. */
    val terrainProfile: com.mountaya.client.DataOperation =
        com.mountaya.client.DataOperation(
            name = "terrainProfile",
            inputType = "TerrainProfileInput!",
            selection =
                com.mountaya.client.FieldSelection {
                    field("count")
                    field("coveredCount")
                    field("zoom")
                    field("resolutionMeters")
                    field("samples") {
                        field("point")
                        field("elevation")
                        field("slope")
                        field("aspect")
                        field("aspectOctant")
                        field("steepnessBand")
                        field("distanceMeters")
                    }
                },
        )

    /** `terrainSamples`, with its full field tree. */
    val terrainSamples: com.mountaya.client.DataOperation =
        com.mountaya.client.DataOperation(
            name = "terrainSamples",
            inputType = "TerrainSamplesInput!",
            selection =
                com.mountaya.client.FieldSelection {
                    field("count")
                    field("coveredCount")
                    field("zoom")
                    field("resolutionMeters")
                    field("samples") {
                        field("point")
                        field("elevation")
                        field("slope")
                        field("aspect")
                        field("aspectOctant")
                        field("steepnessBand")
                        field("distanceMeters")
                    }
                },
        )

    /** Every Data API operation, in the model's order. */
    val all: kotlin.collections.List<com.mountaya.client.DataOperation> =
        kotlin.collections.listOf(
            analyzeGeometry,
            directions,
            exportItinerary,
            geocode,
            isochrones,
            itinerary,
            matrix,
            pois,
            reverseGeocode,
            search,
            suggestions,
            terrainGrid,
            terrainProfile,
            terrainSamples,
        )
}
