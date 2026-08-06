package com.example.game

import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import com.example.R
import android.content.Context

@RunWith(RobolectricTestRunner::class)
class ImageTest {
    @Test
    fun testImage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val options = BitmapFactory.Options().apply { inMutable = true }
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img_timoteo_white_vip_1785984795061, options)
        println("Has alpha: ${bitmap.hasAlpha()}")
        println("Config: ${bitmap.config}")
    }
}
