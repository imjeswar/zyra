

package iad1tya.echo.music.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import iad1tya.echo.music.extensions.toEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.properties.ReadOnlyProperty

private val Context.rawDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private val secureDataStoreMap = java.util.WeakHashMap<Context, DataStore<Preferences>>()

val Context.dataStore: DataStore<Preferences>
    get() = synchronized(secureDataStoreMap) {
        secureDataStoreMap.getOrPut(this) { SecureDataStore(rawDataStore) }
    }

class SecureDataStore(private val delegate: DataStore<Preferences>) : DataStore<Preferences> {
    
    companion object {
        private val SENSITIVE_KEYS = setOf(
            "spotify_sp_dc",
            "spotify_sp_key",
            "spotify_access_token",
            "discord_token",
            "discord_refresh_token",
            "innerTubeCookie"
        )
        
        private fun isSensitive(key: Preferences.Key<*>): Boolean {
            return key.name in SENSITIVE_KEYS
        }
    }

    override val data: Flow<Preferences> = delegate.data.map { preferences ->
        val mutablePreferences = preferences.toMutablePreferences()
        var modified = false
        for ((key, value) in preferences.asMap()) {
            if (isSensitive(key) && value is String) {
                val decrypted = Cryptography.decrypt(value)
                @Suppress("UNCHECKED_CAST")
                mutablePreferences[key as Preferences.Key<String>] = decrypted
                modified = true
            }
        }
        if (modified) mutablePreferences.toPreferences() else preferences
    }

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        return delegate.updateData { preferences ->
            // 1. Decrypt raw preferences so transform function gets decrypted values
            val mutableDecrypted = preferences.toMutablePreferences()
            for ((key, value) in preferences.asMap()) {
                if (isSensitive(key) && value is String) {
                    @Suppress("UNCHECKED_CAST")
                    mutableDecrypted[key as Preferences.Key<String>] = Cryptography.decrypt(value)
                }
            }
            
            // 2. Run the transform function on decrypted preferences
            val transformed = transform(mutableDecrypted.toPreferences())
            
            // 3. Encrypt the sensitive keys back for physical storage
            val mutableEncrypted = transformed.toMutablePreferences()
            for ((key, value) in transformed.asMap()) {
                if (isSensitive(key) && value is String) {
                    @Suppress("UNCHECKED_CAST")
                    mutableEncrypted[key as Preferences.Key<String>] = Cryptography.encrypt(value)
                }
            }
            
            mutableEncrypted.toPreferences()
        }
    }
}


operator fun <T> DataStore<Preferences>.get(key: Preferences.Key<T>): T? =
    runBlocking(Dispatchers.IO) {
        data.first()[key]
    }

fun <T> DataStore<Preferences>.get(
    key: Preferences.Key<T>,
    defaultValue: T,
): T =
    runBlocking(Dispatchers.IO) {
        data.first()[key] ?: defaultValue
    }

fun <T> preference(
    context: Context,
    key: Preferences.Key<T>,
    defaultValue: T,
) = ReadOnlyProperty<Any?, T> { _, _ -> context.dataStore[key] ?: defaultValue }

inline fun <reified T : Enum<T>> enumPreference(
    context: Context,
    key: Preferences.Key<String>,
    defaultValue: T,
) = ReadOnlyProperty<Any?, T> { _, _ -> context.dataStore[key].toEnum(defaultValue) }

@Composable
fun <T> rememberPreference(
    key: Preferences.Key<T>,
    defaultValue: T,
): MutableState<T> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val state =
        remember {
            context.dataStore.data
                .map { it[key] ?: defaultValue }
                .distinctUntilChanged()
        }.collectAsState(context.dataStore[key] ?: defaultValue)

    return remember {
        object : MutableState<T> {
            override var value: T
                get() = state.value
                set(value) {
                    coroutineScope.launch {
                        context.dataStore.edit {
                            it[key] = value
                        }
                    }
                }

            override fun component1() = value

            override fun component2(): (T) -> Unit = { value = it }
        }
    }
}

@Composable
inline fun <reified T : Enum<T>> rememberEnumPreference(
    key: Preferences.Key<String>,
    defaultValue: T,
): MutableState<T> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val initialValue = context.dataStore[key].toEnum(defaultValue = defaultValue)
    val state =
        remember {
            context.dataStore.data
                .map { it[key].toEnum(defaultValue = defaultValue) }
                .distinctUntilChanged()
        }.collectAsState(initialValue)

    return remember {
        object : MutableState<T> {
            override var value: T
                get() = state.value
                set(value) {
                    coroutineScope.launch {
                        context.dataStore.edit {
                            it[key] = value.name
                        }
                    }
                }

            override fun component1() = value

            override fun component2(): (T) -> Unit = { value = it }
        }
    }
}
