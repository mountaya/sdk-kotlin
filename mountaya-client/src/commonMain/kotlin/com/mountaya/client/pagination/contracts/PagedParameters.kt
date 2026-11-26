package com.mountaya.client

/**
 * PagedParameters is the trailing builder of an operation that pages by an opaque cursor: every
 * operation the contract marks with `pagination` takes one, and a `PageWalk` reads the listing
 * through it.
 *
 * The walk replaces the builder's `cursor` and `limit` on every request and sends the rest of it as
 * the caller wrote it, filters and `sort` included, since a cursor is bound to the filters and the
 * order it was issued under. A builder is write-only to its caller, so the walk reads the cursor
 * the caller named through this class, which only the printed builders extend, the admin unit's in
 * `mountaya-admin` among them: its constructor, the cursor and the limit are `InternalMountayaApi`, which no
 * consumer reads (SPEC.md section 7).
 */
public abstract class PagedParameters
    @InternalMountayaApi
    protected constructor() {
        /** The builder's `cursor`, the position a page starts from, or null for the first page. */
        @InternalMountayaApi
        public abstract var pageCursor: String?

        /** The builder's `limit`, how many rows a page holds at most, or null where the server's default applies. */
        @InternalMountayaApi
        public abstract var pageLimit: Int?
    }
