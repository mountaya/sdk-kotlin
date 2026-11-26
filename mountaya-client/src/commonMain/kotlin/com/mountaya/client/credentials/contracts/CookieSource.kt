package com.mountaya.client

/**
 * CookieSource answers the `Cookie` header a request to [url] carries for a share-link visitor,
 * or null where it carries none.
 *
 * It does not suspend, for the reason a session token's function does not: a map's request hook
 * reads it on a thread of its own. Where the visitor's cookies live, a WebView's store or the
 * shared cookie storage, is the app's to know, and the client reads none on its own.
 */
public typealias CookieSource = (url: Url) -> String?
