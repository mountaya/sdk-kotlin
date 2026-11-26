plugins {
    id("mountaya.root")
}

mountaya {
    // A name the naming law refuses and this repository keeps, each valued by its sentence.
    namingExemptions.put(
        "MountayaException",
        "A bare `Exception` would shadow `kotlin.Exception`, imported into every Kotlin file by default (SPEC.md section 5).",
    )
    namingExemptions.put(
        "onResponse",
        "The contract's own name for the hook told of every answer (sdk/SPEC.md section 2), which the Swift SDK keeps too.",
    )
    namingExemptions.put(
        "NoKey",
        "A state of a map's read of its organization, the contract's `no-key`, which every SDK names alike, never a " +
            "null object standing in for a key (SPEC.md section 8).",
    )

    // A main file the layout law would hold to a test of its own, each valued by why it has none.
    untestedFiles.put(
        "mountaya-client/commonMain/com/mountaya/client/calls/contracts/OperationSender.kt",
        "An interface with no behaviour of its own: the generated surface's tests drive it through a scripted sender.",
    )
    untestedFiles.put(
        "mountaya-elements/commonMain/com/mountaya/elements/map/contracts/ElevationReader.kt",
        "An interface with no behaviour of its own: the readout's tests drive it through a reader that holds each tile.",
    )
    untestedFiles.put(
        "mountaya-client/commonMain/com/mountaya/client/modules/contracts/InternalMountayaApi.kt",
        "An annotation with no behaviour of its own: the compiler holds its opt-in, and the ABI dumps leave out what it marks.",
    )

    // A file the layout law would hold to its module's package, each valued by why it declares another. A
    // contract and the rule named after it, `ProfileTotals` and `profileTotals`, share a file name in the
    // `contracts` and `rules` of one topic, so their tests at the mirrored paths share a class name, which one
    // package holds once: the contract's test keeps the package its directory spells.
    listOf(
        "terrain/contracts/AspectCardinalTest.kt",
        "layers/contracts/HoveredItineraryTest.kt",
        "layers/contracts/LayersPanelPressTest.kt",
        "layers/contracts/PoiPanelRowsTest.kt",
        "layers/contracts/TimeSliderReadoutTest.kt",
        "layers/contracts/TimeSliderReleaseTest.kt",
        "layers/contracts/TimeSliderWindowTest.kt",
        "profile/contracts/AmenityFilterTest.kt",
        "profile/contracts/ProfileCharacteristicsTest.kt",
        "profile/contracts/ProfileReadoutTest.kt",
        "profile/contracts/ProfileTotalsTest.kt",
        "profile/contracts/SegmentRowCellTest.kt",
        "profile/contracts/WaypointRowCellTest.kt",
        "search/contracts/SearchListStateTest.kt",
        "units/contracts/PreferencePressTest.kt",
    ).forEach { test ->
        packageExemptions.put(
            "mountaya-geo/commonTest/com/mountaya/geo/$test",
            "Its class name is the test's of the rule named after its contract, and one package holds a class name once.",
        )
    }

    // Every `@Suppress`, `@SuppressLint` and `@SuppressWarnings` in the repository, each valued by the sentence that
    // justifies it.
    suppressions.put(
        "mountaya-navigation/androidMain/com/mountaya/navigation/adapter/engines/" +
            "SpeechNavigationVoice.kt: @Suppress(\"DEPRECATION\")",
        "Below API 26, which the modules' floor reaches, the audio focus is taken and let go by the one call " +
            "the platform has there, which API 26 deprecates for the request the voice makes from that level up.",
    )
    suppressions.put(
        "mountaya-navigation/androidMain/com/mountaya/navigation/seams/engines/" +
            "DeviceLocationSource.kt: @Suppress(\"DEPRECATION\")",
        "Below API 33, which the modules' floor reaches, the app's own package is read by the one call the " +
            "platform has there, which API 33 deprecates for the flags object the source passes from that level up.",
    )
    suppressions.put(
        "mountaya-elements/androidMain/com/mountaya/elements/renderer/components/" +
            "WatermarkView.kt: @SuppressLint(\"ViewConstructor\")",
        "The view is built in code alone, by the renderer with the mark it draws, and never inflated from a layout, " +
            "so the constructors a layout inflater calls, which Android Lint asks every view for, would build nothing.",
    )

    // A scope is a named absence, never a skip: each is valued by the title of the SPEC.md
    // section 11 bullet that says this SDK does not carry it, and the coverage gate holds the
    // two to each other.
    scopedLayers.put("studio", "No studio layer.")
    scopedCapabilities.put("transport.browser.secret-key", "No browser refusal for the secret key.")

    // SPEC.md section 9's table: the module that holds the runners of each layer's entries, on
    // each side of the registry's `api` flag. A module is named by the project name it takes in
    // settings.gradle.kts, before it is listed there: the coverage gate then lists its entries
    // as having no runner, under its name, rather than as owned by nobody. A layer or a flag the
    // registry adds fails the gate until it is named here. Every studio entry is scoped, and a
    // scoped entry is held to its sentence rather than to a module, so the studio row is the
    // table's and decides nothing; studio has no `api: false` entry to name a module for.
    apiRunnerModules.putAll(
        mapOf(
            "transport" to "conformance",
            "geo" to "conformance",
            "map" to "conformance",
            "studio" to "conformance",
        ),
    )
    internalRunnerModules.putAll(
        mapOf(
            "transport" to "mountaya-client",
            "geo" to "mountaya-geo",
            "map" to "mountaya-elements",
        ),
    )
}
