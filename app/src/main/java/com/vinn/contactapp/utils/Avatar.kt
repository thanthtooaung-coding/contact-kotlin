package com.vinn.contactapp.utils

import android.content.Context

data class Avatar(val resName: String)

object AvatarStore {

    val avatars = listOf(
        Avatar("ic_avatar_1"),
        Avatar("ic_avatar_2"),
        Avatar("ic_avatar_3"),
        Avatar("ic_avatar_4"),
        Avatar("ic_avatar_5"),
        Avatar("ic_avatar_6")
    )

    fun getAvatarResourceId(context: Context, resName: String): Int {
        return context.resources.getIdentifier(resName, "drawable", context.packageName)
    }
}
