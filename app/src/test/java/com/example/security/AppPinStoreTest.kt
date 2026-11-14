package com.example.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AppPinStoreTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before fun clear() {
        context.getSharedPreferences("xasuus_security", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun pinCanBeSetVerifiedAndRemoved() {
        assertFalse(AppPinStore.isEnabled(context))
        AppPinStore.setPin(context, "1234")
        assertTrue(AppPinStore.isEnabled(context))
        assertTrue(AppPinStore.verify(context, "1234"))
        assertFalse(AppPinStore.verify(context, "4321"))
        val stored = context.getSharedPreferences("xasuus_security", Context.MODE_PRIVATE).all
        assertFalse(stored.values.contains("1234"))
        AppPinStore.setPin(context, null)
        assertFalse(AppPinStore.isEnabled(context))
        assertFalse(AppPinStore.verify(context, "1234"))
    }

    @Test fun replacingPinInvalidatesOldPin() {
        AppPinStore.setPin(context, "1234")
        AppPinStore.setPin(context, "5678")
        assertFalse(AppPinStore.verify(context, "1234"))
        assertTrue(AppPinStore.verify(context, "5678"))
    }
}
