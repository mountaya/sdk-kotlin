package com.mountaya.geo

import com.mountaya.client.generated.RestSchema

/**
 * trailDifficultyRank is the rank of [difficulty] within its scale, the `trailDifficultyRank`
 * `geo/profile-characteristics.json` declares: the SAC hiking scale from `hiking`, 1, to
 * `difficult-alpine-hiking`, 6, and the mountain-biking scale from `mtb-0`, 0, to `mtb-6`, 6.
 *
 * Null for `unknown`, a way that carries no difficulty, and for a grade the contract adds after this
 * build: neither has a place on the range.
 */
public fun trailDifficultyRank(difficulty: RestSchema.TrailDifficultyType): Int? =
    when (difficulty) {
        RestSchema.TrailDifficultyType.Hiking -> 1
        RestSchema.TrailDifficultyType.MountainHiking -> 2
        RestSchema.TrailDifficultyType.DemandingMountainHiking -> 3
        RestSchema.TrailDifficultyType.AlpineHiking -> 4
        RestSchema.TrailDifficultyType.DemandingAlpineHiking -> 5
        RestSchema.TrailDifficultyType.DifficultAlpineHiking -> 6
        RestSchema.TrailDifficultyType.Mtb0 -> 0
        RestSchema.TrailDifficultyType.Mtb1 -> 1
        RestSchema.TrailDifficultyType.Mtb2 -> 2
        RestSchema.TrailDifficultyType.Mtb3 -> 3
        RestSchema.TrailDifficultyType.Mtb4 -> 4
        RestSchema.TrailDifficultyType.Mtb5 -> 5
        RestSchema.TrailDifficultyType.Mtb6 -> 6
        else -> null
    }
