package com.mountaya.elements

import com.mountaya.geo.ElevationSource
import com.mountaya.geo.ElevationTile

/**
 * ElevationReader is what a map's readout reads its terrain through (`map.readout.open`): the elevation source the
 * layers catalogue names, read once its TileJSON document answers, and each tile of it at an address, read into its
 * pixels. `TileElevationReader` reads both through the client's tile transport; a test scripts its own.
 */
internal interface ElevationReader {
    /** The elevation source, or null where the catalogue or its document names none a readout can read. */
    suspend fun source(): ElevationSource?

    /** The tile at [address], its pixels read; it throws where the tile cannot be read. */
    suspend fun tile(address: ElevationTile.Address): ElevationTile
}
