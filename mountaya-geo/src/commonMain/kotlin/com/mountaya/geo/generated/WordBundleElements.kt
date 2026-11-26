// GENERATED FILE. DO NOT EDIT.
//
// Printed by `./gradlew :mountaya-geo:generateElements` from sdk/elements/words.json of
// mountaya/schemas, contract v0.9.0, model digest
// f9ded6dd4ca71cb8f89c6974c42adec77509ee9a19e5afd01c31b6ea9dcc33ea.
// A value that is wrong here is a defect in the contract's table, and the next print overwrites
// any edit.

package com.mountaya.geo.generated

import com.mountaya.client.generated.RestSchema
import com.mountaya.geo.WordBundle
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/** The elements' words in the language `en`. */
private val wordsEn: JsonObject =
    buildJsonObject {
        putJsonObject("amenities") {
            put("accommodation", "Accommodation")
            put("assistance", "Personal assistance")
            put("cable-car", "Cable car")
            put("camping", "Camping")
            put("drinks", "Drinks")
            put("drop-bag", "Drop bag")
            put("first-aid", "First aid")
            put("food", "Food")
            put("hot-meal", "Hot meal")
            put("luggage-storage", "Luggage storage")
            put("medical", "Medical post")
            put("mobile-signal", "Mobile signal")
            put("parking", "Parking")
            put("pet-friendly", "Pets allowed")
            put("picnic", "Picnic area")
            put("power-charging", "Charging point")
            put("public-transport", "Public transport")
            put("rest-area", "Rest area")
            put("shelter", "Shelter")
            put("shop", "Shop")
            put("showers", "Showers")
            put("shuttle", "Shuttle")
            put("snacks", "Snacks")
            put("timing-checkpoint", "Timing checkpoint")
            put("toilets", "Toilets")
            put("viewpoint", "Viewpoint")
            put("waste", "Waste disposal")
            put("water", "Water")
            put("wheelchair-access", "Wheelchair access")
            put("wifi", "Wi-Fi")
        }
        putJsonObject("controls") {
            put("close", "Close")
            put("compass", "Reset bearing to north")
            put("directions", "Directions")
            put("download", "Download")
            put("geolocate", "Show my location")
            put("layers", "Layers")
            put("pois", "Points of interest")
            put("preferences", "Preferences")
            put("zoomIn", "Zoom in")
            put("zoomOut", "Zoom out")
        }
        putJsonObject("layers") {
            put("enrichment", "Enrichment")
            put("locked", "Locked")
            put("none", "None")
            put("overlays", "Overlays")
            put("terrain", "Base map")
            put("time", "Time")
            putJsonObject("timeDaysAgo") {
                put("one", "{count} day ago")
                put("other", "{count} days ago")
            }
            putJsonObject("timeDaysAhead") {
                put("one", "in {count} day")
                put("other", "in {count} days")
            }
            put("timeHint", "Choose the time for time-aware layers.")
            putJsonObject("timeHoursAgo") {
                put("one", "{count} hour ago")
                put("other", "{count} hours ago")
            }
            putJsonObject("timeHoursAhead") {
                put("one", "in {count} hour")
                put("other", "in {count} hours")
            }
            put("timeKeyboard", "Use \u2190 \u2192 to adjust by the hour.")
            put("timeMinus2d", "-2d")
            put("timeNote", "Times shown in your local time zone.")
            put("timeNow", "Now")
            put("timePlus2d", "+2d")
            put("timePlus5d", "+5d")
            put("timeUnavailable", "The current time could not be read, so the time can't be changed.")
        }
        putJsonObject("pois") {
            put("categories", "Categories")
            put("displayOnMap", "Show on the map")
            put("zoomWarning", "Zoom in to see points of interest.")
        }
        putJsonObject("preferences") {
            put("celsius", "Celsius")
            put("degrees", "Degrees")
            put("fahrenheit", "Fahrenheit")
            put("imperial", "Imperial")
            put("language", "Language")
            put("length", "Distance")
            put("metric", "Metric")
            put("pace", "Travel rate")
            put("perDistance", "Pace")
            put("perTime", "Speed")
            put("percentage", "Percent")
            put("slope", "Slope")
            put("temperature", "Temperature")
        }
        putJsonObject("profile") {
            put("amenities", "Services along the itinerary")
            put("ascent", "Ascent")
            put("bands", "Along the itinerary")
            put("chart", "Elevation along the itinerary")
            put("cutoff", "Cut-off")
            put("cutoffs", "Cut-off times")
            put("descent", "Descent")
            put("difficulty", "Difficulty")
            put("distance", "Distance")
            put("duration", "Duration")
            put("elevation", "Elevation")
            put("empty", "This itinerary carries no elevation data.")
            put("grade", "Grade")
            put("navigator", "Itinerary overview")
            put("rangeEnd", "End of the range shown")
            put("rangeStart", "Start of the range shown")
            put("sections", "Sections")
            putJsonObject("slope") {
                put("ascent", "Climb")
                put("descent", "Descent")
                put("flat", "Flat")
                put("steepAscent", "Steep climb")
                put("steepDescent", "Steep descent")
            }
            put("title", "Elevation profile")
            put("unlabelledBand", "Unnamed section")
            put("unlabelledWaypoint", "Unnamed waypoint")
            put("waypoints", "Waypoints")
        }
        putJsonObject("readout") {
            put("altitude", "Altitude")
            put("aspect", "Aspect")
            putJsonObject("aspects") {
                put("E", "East")
                put("N", "North")
                put("NE", "North-East")
                put("NW", "North-West")
                put("S", "South")
                put("SE", "South-East")
                put("SW", "South-West")
                put("W", "West")
            }
            put("coordinates", "Coordinates")
            put("copied", "Copied")
            put("copyCoordinates", "Copy coordinates")
            put("overlayRange", "Between {min} and {max}")
            put("overlayTime", "As of {moment}")
            put("slope", "Slope")
            put("zone", "Zone")
            putJsonObject("zoneLevels") {
                put("danger", "Danger")
                put("info", "Info")
                put("resolved", "Resolved")
                put("warning", "Warning")
            }
            put("zoneWindowFrom", "Active from {start}")
            put("zoneWindowRange", "Active {start} to {end}")
            put("zoneWindowUntil", "Active until {end}")
            put("zoomInForDetails", "Zoom in to see more details.")
        }
        putJsonObject("search") {
            put("categoryAll", "All")
            put("categoryItineraries", "Itineraries")
            put("categoryPlaces", "Places")
            put("categoryZones", "Zones")
            put("clear", "Clear the search")
            put("emptyDescription", "Try a different name, or move the map.")
            put("emptyTitle", "Nothing found")
            put("error", "The search could not be completed.")
            put("label", "Search")
            put("placeholder", "Search for a place or an itinerary")
            putJsonObject("resultCount") {
                put("one", "{count} result")
                put("other", "{count} results")
            }
            put("retry", "Try again")
            put("viewportFallback", "No results here, so we searched everywhere.")
            put("viewportScope", "Search this area only")
        }
        putJsonObject("table") {
            put("altitude", "Altitude")
            put("amenities", "Services")
            put("amenityFilter", "Filter by service")
            put("ascent", "Climbed")
            put("cutoff", "Cut-off")
            put("descent", "Lost")
            put("difficulty", "Difficulty")
            put("distance", "Distance")
            put("duration", "Duration")
            put("elevation", "Elevation")
            put("empty", "This itinerary has no segments to state.")
            put("loading", "Reading the itinerary\u2026")
            put("profile", "Itinerary profile")
            put("properties", "Custom")
            put("segment", "Segment")
            put("title", "Itinerary table")
            put("total", "Total")
            put("waypoint", "Waypoint")
            put("waypointEmpty", "This itinerary has no waypoints to state.")
            put("waypointFilterEmpty", "No waypoint offers every service you picked.")
            put("waypointTitle", "Waypoints along the itinerary")
        }
        putJsonObject("tooltips") {
            putJsonObject("compass") {
                put("description", "Point the map north again.")
                put("title", "Compass")
            }
            putJsonObject("directions") {
                put("description", "Open turn-by-turn directions for this itinerary.")
                put("title", "Directions")
            }
            putJsonObject("download") {
                put("description", "Export this itinerary as a file.")
                put("title", "Download")
            }
            putJsonObject("geolocate") {
                put("deniedDescription", "Location is blocked. Allow it in your settings.")
                put("description", "Centre the map on where you are.")
                put("title", "My location")
            }
            putJsonObject("layers") {
                put("description", "Choose a base map and overlays.")
                put("title", "Layers")
            }
            putJsonObject("pois") {
                put("description", "Show huts, water, shelter and more.")
                put("title", "Points of interest")
            }
            putJsonObject("preferences") {
                put("description", "Units, language and appearance.")
                put("title", "Preferences")
            }
            putJsonObject("zoomIn") {
                put("description", "See more detail.")
                put("title", "Zoom in")
            }
            putJsonObject("zoomOut") {
                put("description", "See more ground.")
                put("title", "Zoom out")
            }
        }
        putJsonObject("watermark") {
            put("label", "Visit Mountaya")
        }
        putJsonObject("waypointCard") {
            put("address", "Address")
            put("amenities", "Services")
            put("booking", "Booking page")
            put("contact", "Contact")
            put("cutoff", "Cut-off time")
            put("email", "Email")
            put("empty", "Nothing recorded yet")
            put("emptyHint", "Services, opening hours, photos and your own fields all show here.")
            put("media", "Photos and links")
            put("openingHours", "Opening hours")
            put("phone", "Phone")
            put("properties", "Custom")
            put("stop", "Planned stop")
            putJsonObject("summary") {
                put("cutoffAt", "Cut-off {moment}")
                put("openingHours", "Open {hours}")
                put("stop", "{elapsed} stop")
            }
            put("timing", "Timing")
            put("website", "Website")
        }
    }

/** The elements' words in the language `fr`. */
private val wordsFr: JsonObject =
    buildJsonObject {
        putJsonObject("amenities") {
            put("accommodation", "H\u00e9bergement")
            put("assistance", "Assistance personnelle")
            put("cable-car", "Remont\u00e9e m\u00e9canique")
            put("camping", "Camping")
            put("drinks", "Boissons")
            put("drop-bag", "Sac de d\u00e9lestage")
            put("first-aid", "Premiers secours")
            put("food", "Nourriture")
            put("hot-meal", "Repas chaud")
            put("luggage-storage", "Consigne")
            put("medical", "Poste m\u00e9dical")
            put("mobile-signal", "R\u00e9seau mobile")
            put("parking", "Stationnement")
            put("pet-friendly", "Animaux accept\u00e9s")
            put("picnic", "Aire de pique-nique")
            put("power-charging", "Borne de recharge")
            put("public-transport", "Transports en commun")
            put("rest-area", "Aire de repos")
            put("shelter", "Abri")
            put("shop", "Commerce")
            put("showers", "Douches")
            put("shuttle", "Navette")
            put("snacks", "En-cas")
            put("timing-checkpoint", "Point de chronom\u00e9trage")
            put("toilets", "Toilettes")
            put("viewpoint", "Point de vue")
            put("waste", "Poubelles")
            put("water", "Eau")
            put("wheelchair-access", "Acc\u00e8s fauteuil roulant")
            put("wifi", "Wi-Fi")
        }
        putJsonObject("controls") {
            put("close", "Fermer")
            put("compass", "R\u00e9orienter vers le nord")
            put("directions", "Indications")
            put("download", "T\u00e9l\u00e9charger")
            put("geolocate", "Afficher ma position")
            put("layers", "Calques")
            put("pois", "Points d'int\u00e9r\u00eat")
            put("preferences", "Pr\u00e9f\u00e9rences")
            put("zoomIn", "Zoom avant")
            put("zoomOut", "Zoom arri\u00e8re")
        }
        putJsonObject("layers") {
            put("enrichment", "Enrichissement")
            put("locked", "Verrouill\u00e9")
            put("none", "Aucun")
            put("overlays", "Surcouches")
            put("terrain", "Terrain")
            put("time", "Heure")
            putJsonObject("timeDaysAgo") {
                put("one", "il y a {count} jour")
                put("other", "il y a {count} jours")
            }
            putJsonObject("timeDaysAhead") {
                put("one", "dans {count} jour")
                put("other", "dans {count} jours")
            }
            put("timeHint", "Choisissez l'heure des calques temporels.")
            putJsonObject("timeHoursAgo") {
                put("one", "il y a {count} heure")
                put("other", "il y a {count} heures")
            }
            putJsonObject("timeHoursAhead") {
                put("one", "dans {count} heure")
                put("other", "dans {count} heures")
            }
            put("timeKeyboard", "Utilisez \u2190 \u2192 pour ajuster heure par heure.")
            put("timeMinus2d", "-2 j")
            put("timeNote", "Heures dans votre fuseau horaire local.")
            put("timeNow", "Maintenant")
            put("timePlus2d", "+2 j")
            put("timePlus5d", "+5 j")
            put("timeUnavailable", "L'heure actuelle n'a pas pu \u00eatre lue, l'heure ne peut donc pas \u00eatre modifi\u00e9e.")
        }
        putJsonObject("pois") {
            put("categories", "Cat\u00e9gories")
            put("displayOnMap", "Afficher sur la carte")
            put("zoomWarning", "Zoomez pour voir les points d'int\u00e9r\u00eat.")
        }
        putJsonObject("preferences") {
            put("celsius", "Celsius")
            put("degrees", "Degr\u00e9s")
            put("fahrenheit", "Fahrenheit")
            put("imperial", "Imp\u00e9rial")
            put("language", "Langue")
            put("length", "Distance")
            put("metric", "M\u00e9trique")
            put("pace", "Rythme de d\u00e9placement")
            put("perDistance", "Allure")
            put("perTime", "Vitesse")
            put("percentage", "Pourcentage")
            put("slope", "Pente")
            put("temperature", "Temp\u00e9rature")
        }
        putJsonObject("profile") {
            put("amenities", "Services le long de l'itin\u00e9raire")
            put("ascent", "D\u00e9nivel\u00e9 +")
            put("bands", "Conditions du parcours")
            put("chart", "Courbe altim\u00e9trique")
            put("cutoff", "Barri\u00e8re")
            put("cutoffs", "Barri\u00e8res horaires")
            put("descent", "D\u00e9nivel\u00e9 -")
            put("difficulty", "Difficult\u00e9")
            put("distance", "Distance")
            put("duration", "Dur\u00e9e")
            put("elevation", "Altitude")
            put("empty", "Ce parcours ne comporte pas de donn\u00e9es d'altitude.")
            put("grade", "Pente")
            put("navigator", "Vue d'ensemble du parcours")
            put("rangeEnd", "Fin de la plage affich\u00e9e")
            put("rangeStart", "D\u00e9but de la plage affich\u00e9e")
            put("sections", "Sections")
            putJsonObject("slope") {
                put("ascent", "Mont\u00e9e")
                put("descent", "Descente")
                put("flat", "Plat")
                put("steepAscent", "Mont\u00e9e raide")
                put("steepDescent", "Descente raide")
            }
            put("title", "Profil altim\u00e9trique")
            put("unlabelledBand", "Section sans nom")
            put("unlabelledWaypoint", "Emplacement sans nom")
            put("waypoints", "Emplacements")
        }
        putJsonObject("readout") {
            put("altitude", "Altitude")
            put("aspect", "Orientation")
            putJsonObject("aspects") {
                put("E", "Est")
                put("N", "Nord")
                put("NE", "Nord-Est")
                put("NW", "Nord-Ouest")
                put("S", "Sud")
                put("SE", "Sud-Est")
                put("SW", "Sud-Ouest")
                put("W", "Ouest")
            }
            put("coordinates", "Coordonn\u00e9es")
            put("copied", "Copi\u00e9")
            put("copyCoordinates", "Copier les coordonn\u00e9es")
            put("overlayRange", "Entre {min} et {max}")
            put("overlayTime", "Au {moment}")
            put("slope", "Pente")
            put("zone", "Zone")
            putJsonObject("zoneLevels") {
                put("danger", "Danger")
                put("info", "Info")
                put("resolved", "R\u00e9solu")
                put("warning", "Avertissement")
            }
            put("zoneWindowFrom", "Active \u00e0 partir du {start}")
            put("zoneWindowRange", "Active du {start} au {end}")
            put("zoneWindowUntil", "Active jusqu\u2019au {end}")
            put("zoomInForDetails", "Zoomez pour voir plus de d\u00e9tails.")
        }
        putJsonObject("search") {
            put("categoryAll", "Tout")
            put("categoryItineraries", "Itin\u00e9raires")
            put("categoryPlaces", "Lieux")
            put("categoryZones", "Zones")
            put("clear", "Effacer la recherche")
            put("emptyDescription", "Essayez un autre mot-cl\u00e9 ou \u00e9largissez la zone.")
            put("emptyTitle", "Aucun r\u00e9sultat")
            put("error", "Service de recherche injoignable. R\u00e9essayez.")
            put("label", "Rechercher")
            put("placeholder", "Cherchez un sentier, un itin\u00e9raire, un lieu\u2026")
            putJsonObject("resultCount") {
                put("one", "{count} r\u00e9sultat")
                put("other", "{count} r\u00e9sultats")
            }
            put("retry", "R\u00e9essayer")
            put("viewportFallback", "Aucun r\u00e9sultat dans la vue, voici les plus proches.")
            put("viewportScope", "Chercher dans cette zone")
        }
        putJsonObject("table") {
            put("altitude", "Altitude")
            put("amenities", "Services")
            put("amenityFilter", "Filtrer par service")
            put("ascent", "D\u00e9nivel\u00e9 positif")
            put("cutoff", "Barri\u00e8re horaire")
            put("descent", "D\u00e9nivel\u00e9 n\u00e9gatif")
            put("difficulty", "Difficult\u00e9")
            put("distance", "Distance")
            put("duration", "Dur\u00e9e")
            put("elevation", "D\u00e9nivel\u00e9")
            put("empty", "Cet itin\u00e9raire n'a aucun segment \u00e0 pr\u00e9senter.")
            put("loading", "Lecture de l'itin\u00e9raire\u2026")
            put("profile", "Profil de l\u2019itin\u00e9raire")
            put("properties", "Personnalis\u00e9")
            put("segment", "Segment")
            put("title", "Tableau de l'itin\u00e9raire")
            put("total", "Total")
            put("waypoint", "Emplacement")
            put("waypointEmpty", "Cet itin\u00e9raire n'a aucun emplacement \u00e0 pr\u00e9senter.")
            put("waypointFilterEmpty", "Aucun emplacement ne propose tous les services choisis.")
            put("waypointTitle", "Emplacements le long de l'itin\u00e9raire")
        }
        putJsonObject("tooltips") {
            putJsonObject("compass") {
                put("description", "Cliquez pour r\u00e9orienter vers le nord.")
                put("title", "Boussole")
            }
            putJsonObject("directions") {
                put("description", "Ouvrir les indications d\u00e9taill\u00e9es de cet itin\u00e9raire.")
                put("title", "Indications")
            }
            putJsonObject("download") {
                put("description", "Exporter cet itin\u00e9raire dans un fichier.")
                put("title", "T\u00e9l\u00e9charger")
            }
            putJsonObject("geolocate") {
                put("deniedDescription", "Localisation bloqu\u00e9e\u00a0: autorisez-la dans vos r\u00e9glages.")
                put("description", "Centrer la carte sur votre position.")
                put("title", "Ma position")
            }
            putJsonObject("layers") {
                put("description", "Choisir un terrain et des surcouches.")
                put("title", "Calques")
            }
            putJsonObject("pois") {
                put("description", "Afficher refuges, points d'eau, abris et plus.")
                put("title", "Points d'int\u00e9r\u00eat publics")
            }
            putJsonObject("preferences") {
                put("description", "Unit\u00e9s, langue et affichage.")
                put("title", "Pr\u00e9f\u00e9rences")
            }
            putJsonObject("zoomIn") {
                put("description", "Se rapprocher de la carte.")
                put("title", "Zoom avant")
            }
            putJsonObject("zoomOut") {
                put("description", "S'\u00e9loigner de la carte.")
                put("title", "Zoom arri\u00e8re")
            }
        }
        putJsonObject("watermark") {
            put("label", "D\u00e9couvrir Mountaya")
        }
        putJsonObject("waypointCard") {
            put("address", "Adresse")
            put("amenities", "Services")
            put("booking", "Page de r\u00e9servation")
            put("contact", "Contact")
            put("cutoff", "Heure de barri\u00e8re")
            put("email", "E-mail")
            put("empty", "Rien d\u2019enregistr\u00e9 pour l\u2019instant")
            put("emptyHint", "Services, horaires, photos et vos propres champs s\u2019affichent ici.")
            put("media", "Photos et liens")
            put("openingHours", "Horaires d\u2019ouverture")
            put("phone", "T\u00e9l\u00e9phone")
            put("properties", "Personnalis\u00e9")
            put("stop", "Arr\u00eat pr\u00e9vu")
            putJsonObject("summary") {
                put("cutoffAt", "Barri\u00e8re {moment}")
                put("openingHours", "Ouvert {hours}")
                put("stop", "{elapsed} d\u2019arr\u00eat")
            }
            put("timing", "Horaires")
            put("website", "Site web")
        }
    }

/**
 * elementsWordBundle is the words the elements put on screen, by section, as the contract's words
 * table holds them: English, which every language falls back to, and the translations it carries
 * (fr). A word is a string, a template a string holding `{name}` placeholders, and a counted word
 * an object of the forms CLDR's plural rules name, `other` among them. `WordBundle.elements`
 * publishes it, and a consumer's own words are folded over it by `resolveBundle`.
 */
internal val elementsWordBundle: WordBundle =
    WordBundle(
        english = wordsEn,
        translations =
            mapOf(
                RestSchema.Language("fr") to wordsFr,
            ),
    )
