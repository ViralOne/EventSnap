package com.eventsnap.android.core.data.settings

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.ProviderException

/**
 * Guards the launch-crash fix: a settings file the device can no longer decrypt must be discarded,
 * not allowed to take down the app during Koin startup.
 */
class OpenEncryptedPrefsTest {
    private val prefs = mock<SharedPreferences>()

    @Test
    fun `returns the prefs when they open normally, without discarding anything`() {
        var discarded = false

        val opened = openEncryptedPrefs(create = { prefs }, discardUnreadable = { discarded = true })

        assertThat(opened).isSameInstanceAs(prefs)
        assertThat(discarded).isFalse()
    }

    @Test
    fun `discards the file and retries when it cannot be decrypted`() {
        var attempts = 0
        var discarded = false

        val opened =
            openEncryptedPrefs(
                create = {
                    attempts++
                    // AEADBadTagException (the real failure) is a GeneralSecurityException.
                    if (attempts == 1) throw GeneralSecurityException("bad tag") else prefs
                },
                discardUnreadable = { discarded = true },
            )

        assertThat(opened).isSameInstanceAs(prefs)
        assertThat(discarded).isTrue()
        assertThat(attempts).isEqualTo(2)
    }

    @Test
    fun `recovers from an unchecked keystore failure too`() {
        var attempts = 0

        val opened =
            openEncryptedPrefs(
                create = {
                    attempts++
                    if (attempts == 1) throw ProviderException("keystore provider failed") else prefs
                },
                discardUnreadable = {},
            )

        assertThat(opened).isSameInstanceAs(prefs)
    }

    @Test(expected = IOException::class)
    fun `a failure that survives the retry propagates`() {
        openEncryptedPrefs(create = { throw IOException("disk gone") }, discardUnreadable = {})
    }
}
