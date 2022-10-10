package tv.formuler.mytvonline.technic

import android.content.Context
import tv.formuler.mytvonline.technic.data.User

class TecSharedPreferences {
    companion object {
        private val TEC_SHARED_PREFERENCES_NAME = "tec_shared_preferences"
        private val LOG_IN_USER_KEY = "log_in_user"
        private val RECOMMENDS_USER_KEY = "recommends_user"

        private fun getString(context: Context, key: String, def: String): String? =
            context
                .getSharedPreferences(TEC_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getString(key, def)
        private fun setString(context: Context, key: String, value: String?): Boolean =
            context
                .getSharedPreferences(TEC_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit().putString(key, value).commit()
        private fun getStringSet(context: Context, key: String, def: Set<String>?): Set<String>? =
            context
                .getSharedPreferences(TEC_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getStringSet(key, def)
        private fun setStringSet(context: Context, key: String, value: Set<String>): Boolean =
            context
                .getSharedPreferences(TEC_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit().putStringSet(key, value).commit()

        @JvmStatic fun getLoginUser(context: Context): String? = getString(context, LOG_IN_USER_KEY, "")
//        @JvmStatic fun setLoginUser(context: Context, user: User): Boolean = setLoginUser(context, user.id)
        @JvmStatic fun setLoginUser(context: Context, userId: String?): Boolean = setString(context, LOG_IN_USER_KEY, userId)

        @JvmStatic fun getRecommendsUser(context: Context): Set<String>? = getStringSet(context, RECOMMENDS_USER_KEY, mutableSetOf())
        @JvmStatic fun setRecommendsUser(context: Context, users: Set<String>) = setStringSet(context, RECOMMENDS_USER_KEY, users)

        @JvmStatic fun isRecommendUser(context: Context, userId: String): Boolean = getRecommendsUser(context)!!.contains(userId)
        @JvmStatic fun isRecommendUser(context: Context, user: User): Boolean = isRecommendUser(context, user.id)
    }
}