package com.whatshappening.novisad.ui.screens.map

// ── Google Maps JSON Style strings ────────────────────────────────────────────
//
// Generated to match Catppuccin Latte (light) and Mocha (dark) palettes.
// Guidelines from the spec:
//   Latte: bg #EFF1F5, roads #DCE0E8, water #CFDEE2, parks #D7E8D4, labels #5C5F77
//   Mocha: bg #1E1E2E, roads #313244, water #11111B, parks #1F3A26, labels #BAC2DE
//   Both: hide POI labels for a clean look
//
// Apply via MapProperties(mapStyleOptions = MapStyleOptions(LatteMapStyle))

val LatteMapStyle: String = """
[
  {
    "elementType": "geometry",
    "stylers": [{ "color": "#EFF1F5" }]
  },
  {
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#5C5F77" }]
  },
  {
    "elementType": "labels.text.stroke",
    "stylers": [{ "color": "#EFF1F5" }]
  },
  {
    "featureType": "road",
    "elementType": "geometry",
    "stylers": [{ "color": "#DCE0E8" }]
  },
  {
    "featureType": "road",
    "elementType": "geometry.stroke",
    "stylers": [{ "color": "#BCC0CC" }]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry",
    "stylers": [{ "color": "#CCD0DA" }]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry.stroke",
    "stylers": [{ "color": "#BCC0CC" }]
  },
  {
    "featureType": "water",
    "elementType": "geometry",
    "stylers": [{ "color": "#CFDEE2" }]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#6C6F85" }]
  },
  {
    "featureType": "poi.park",
    "elementType": "geometry",
    "stylers": [{ "color": "#D7E8D4" }]
  },
  {
    "featureType": "poi.park",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#6C6F85" }]
  },
  {
    "featureType": "poi",
    "elementType": "labels",
    "stylers": [{ "visibility": "off" }]
  },
  {
    "featureType": "transit",
    "elementType": "geometry",
    "stylers": [{ "color": "#DCE0E8" }]
  },
  {
    "featureType": "transit",
    "elementType": "labels",
    "stylers": [{ "visibility": "off" }]
  },
  {
    "featureType": "administrative",
    "elementType": "geometry.stroke",
    "stylers": [{ "color": "#BCC0CC" }]
  },
  {
    "featureType": "administrative.land_parcel",
    "elementType": "labels",
    "stylers": [{ "visibility": "off" }]
  }
]
""".trimIndent()

val MochaMapStyle: String = """
[
  {
    "elementType": "geometry",
    "stylers": [{ "color": "#1E1E2E" }]
  },
  {
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#BAC2DE" }]
  },
  {
    "elementType": "labels.text.stroke",
    "stylers": [{ "color": "#1E1E2E" }]
  },
  {
    "featureType": "road",
    "elementType": "geometry",
    "stylers": [{ "color": "#313244" }]
  },
  {
    "featureType": "road",
    "elementType": "geometry.stroke",
    "stylers": [{ "color": "#11111B" }]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry",
    "stylers": [{ "color": "#45475A" }]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry.stroke",
    "stylers": [{ "color": "#313244" }]
  },
  {
    "featureType": "water",
    "elementType": "geometry",
    "stylers": [{ "color": "#11111B" }]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#A6ADC8" }]
  },
  {
    "featureType": "poi.park",
    "elementType": "geometry",
    "stylers": [{ "color": "#1F3A26" }]
  },
  {
    "featureType": "poi.park",
    "elementType": "labels.text.fill",
    "stylers": [{ "color": "#A6ADC8" }]
  },
  {
    "featureType": "poi",
    "elementType": "labels",
    "stylers": [{ "visibility": "off" }]
  },
  {
    "featureType": "transit",
    "elementType": "geometry",
    "stylers": [{ "color": "#313244" }]
  },
  {
    "featureType": "transit",
    "elementType": "labels",
    "stylers": [{ "visibility": "off" }]
  },
  {
    "featureType": "administrative",
    "elementType": "geometry.stroke",
    "stylers": [{ "color": "#313244" }]
  },
  {
    "featureType": "administrative.land_parcel",
    "elementType": "labels",
    "stylers": [{ "visibility": "off" }]
  }
]
""".trimIndent()
