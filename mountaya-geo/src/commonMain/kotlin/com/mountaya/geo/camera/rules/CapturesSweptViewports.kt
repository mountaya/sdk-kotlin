package com.mountaya.geo

/**
 * capturesSweptViewports answers whether a recording that captures [captured] captures exactly the
 * viewports its sweep jumped through, [swept], so the recorder needs no warm pass of its own.
 *
 * It does where both hold the same number of frames and each frame of one is the same pose as the
 * frame of the other at its index, every field compared exactly, as [CameraPose] compares: a
 * clearance pass that lowered one frame's pitch or zoom shows ground the sweep never framed. Two
 * sweeps of no frame capture each other.
 */
public fun capturesSweptViewports(
    swept: List<CameraPose>,
    captured: List<CameraPose>,
): Boolean = swept.size == captured.size && swept.indices.all { swept[it] == captured[it] }
