package com.festive.poster.models

import com.google.gson.annotations.SerializedName

data class GetPosterViewConfigResult(
    @SerializedName("templatePacks")
    val templatePacks: FestivePosterSectionModel?,
    @SerializedName("todayPick")
    val todayPick: FestivePosterSectionModel?,
    @SerializedName("allTemplates")
    val allTemplates: FestivePosterSectionModel,
)