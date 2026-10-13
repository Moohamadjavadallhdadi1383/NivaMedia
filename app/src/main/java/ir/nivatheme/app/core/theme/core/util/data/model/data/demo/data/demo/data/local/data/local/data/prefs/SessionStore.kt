
// app/src/main/java/ir/nivatheme/app/data/prefs/SessionStore.kt
package ir.nivatheme.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("niva_prefs")

@Singleton
class SessionStore @Inject constructor(@ApplicationContext ctx: Context) {
    private val ds = ctx.dataStore

    private val KEY_TOKEN = stringPreferencesKey("token")
    private val KEY_NAME = stringPreferencesKey("name")
    private val KEY_PHONE = stringPreferencesKey("phone")
    private val KEY_THEME = stringPreferencesKey("theme")
    private val KEY_WALLET = stringPreferencesKey("wallet")
    private val KEY_ENROLLED = stringPreferencesKey("enrolled")

    val isLoggedIn: Flow<Boolean> = ds.data.map { it[KEY_TOKEN] != null }
    val userName: Flow<String> = ds.data.map { it[KEY_NAME] ?: "کاربر نیوا" }
    val userPhone: Flow<String> = ds.data.map { it[KEY_PHONE] ?: "" }
    val themeMode: Flow<String> = ds.data.map { it[KEY_THEME] ?: "system" }
    val wallet: Flow<Long> = ds.data.map { (it[KEY_WALLET] ?: "120000").toLong() }
    val enrolled: Flow<Set<Long>> = ds.data.map {
        (it[KEY_ENROLLED] ?: "").split(",").filter(String::isNotEmpty).mapNotNull(String::toLongOrNull).toSet()
    }

    /* ⚠️ حالت دمو: توکن محلی ساخته می‌شود.
       در Production، توکن JWT باید از سرور (افزونه اختصاصی نیوا) دریافت شود. */
    suspend fun login(phone: String, name: String) {
        ds.edit {
            it[KEY_TOKEN] = "demo-" + UUID.randomUUID()
            it[KEY_PHONE] = phone
            it[KEY_NAME] = name.ifBlank { "کاربر نیوا" }
        }
    }

    suspend fun logout() = ds.edit { it.remove(KEY_TOKEN); it.remove(KEY_PHONE); it.remove(KEY_NAME) }
    suspend fun setTheme(mode: String) = ds.edit { it[KEY_THEME] = mode }
    suspend fun enroll(courseId: Long) {
        ds.edit { it[KEY_ENROLLED] = (enrolled.first() + courseId).joinToString(",") }
    }
    suspend fun clear() = ds.edit { it.clear() }
}
