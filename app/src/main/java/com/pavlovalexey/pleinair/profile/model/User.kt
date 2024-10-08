package com.pavlovalexey.pleinair.profile.model

import com.google.firebase.firestore.GeoPoint

data class User(
    val userId: String = "",
    val name: String = "",
    val description: String? = null,
    val selectedArtStyles: List<String> = emptyList(),
    val profileImageUrl: String = "",
    val location: GeoPoint? = null,
    val locationName: String = "",
    val isOnline: Boolean? = null
)