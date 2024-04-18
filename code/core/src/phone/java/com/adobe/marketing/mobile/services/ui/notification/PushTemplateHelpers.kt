/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.services.ui.notification

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.services.caching.CacheEntry
import com.adobe.marketing.mobile.services.caching.CacheExpiry
import com.adobe.marketing.mobile.services.caching.CacheService
import com.adobe.marketing.mobile.util.UrlUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * General utility functions to assist in building push template notifications.
 */

internal object PushTemplateHelpers {
    private const val SELF_TAG = "PushTemplateHelpers"
    private const val FULL_BITMAP_QUALITY = 100
    private const val DOWNLOAD_TIMEOUT = 10

    /**
     * Asset cache location to use for downloaded push template images.
     *
     * @return [String] containing the asset cache location to use for storing downloaded push template images.
     */
    private val assetCacheLocation: String?
        get() {
            val deviceInfoService = ServiceProvider.getInstance().deviceInfoService
                ?: return null
            val applicationCacheDir = deviceInfoService.applicationCacheDir
            return if ((applicationCacheDir == null)) null else (
                (
                    applicationCacheDir
                        .toString() + File.separator +
                        PushTemplateConstants.CACHE_BASE_DIR
                    ) + File.separator +
                    PushTemplateConstants.PUSH_IMAGE_CACHE
                )
        }

    private val executor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    private class DownloadImageCallable(val url: String?) :
        Callable<Bitmap?> {
        override fun call(): Bitmap? {
            var bitmap: Bitmap? = null
            var connection: HttpURLConnection? = null
            try {
                val imageUrl = URL(url)
                connection = imageUrl.openConnection() as HttpURLConnection
                connection.inputStream.use { inputStream ->
                    bitmap = BitmapFactory.decodeStream(inputStream)
                    Log.trace(
                        PushTemplateConstants.LOG_TAG,
                        SELF_TAG,
                        "Downloaded push notification image from url ($url)"
                    )
                }
            } catch (e: IOException) {
                Log.warning(
                    PushTemplateConstants.LOG_TAG,
                    SELF_TAG,
                    "Failed to download push notification image from url ($url). Exception: ${e.message}"
                )
            } finally {
                connection?.disconnect()
            }
            return bitmap
        }
    }

    /**
     * Downloads an image using the provided uri `String`. Prior to downloading, the image uri
     * is used to retrieve a [CacheResult] containing a previously cached image. If no cache
     * result is returned, a call to [download] is made to download then cache the image.
     *
     * If a valid cache result is returned then no image is downloaded. Instead, a `Bitmap`
     * is created from the cache result and returned by this method.
     *
     * @param cacheService the AEPSDK [CacheService] to use for caching or retrieving
     * downloaded image assets
     * @param uri [String] containing an image asset url
     * @return [Bitmap] containing the image referenced by the `String` uri
     */
    internal fun downloadImage(cacheService: CacheService, uri: String?): Bitmap? {
        if (assetCacheLocation.isNullOrEmpty() || uri.isNullOrEmpty()) {
            return null
        }
        val cacheResult = cacheService[assetCacheLocation!!, uri]
        if (cacheResult != null) {
            Log.trace(PushTemplateConstants.LOG_TAG, SELF_TAG, "Found cached image for $uri")
            return BitmapFactory.decodeStream(cacheResult.data)
        }
        if (!UrlUtils.isValidUrl(uri)) {
            return null
        }
        val image = download(uri) ?: return null
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Successfully download image from $uri"
        )
        // scale down the bitmap to 300dp x 200dp as we don't want to use a full
        // size image due to memory constraints
        val pushImage = scaleBitmap(image)
        // write bitmap to cache
        try {
            bitmapToInputStream(pushImage).use { bitmapInputStream ->
                cacheBitmapInputStream(
                    cacheService,
                    bitmapInputStream,
                    uri
                )
            }
        } catch (exception: IOException) {
            Log.trace(
                PushTemplateConstants.LOG_TAG,
                SELF_TAG,
                "Exception occurred creating an input stream from a bitmap: ${exception.localizedMessage}."
            )
        }
        return pushImage
    }

    /**
     * Downloads an image using the provided uri `String`. A [Future] task is created to download
     * the image using a [DownloadImageCallable]. The task is submitted to an [ExecutorService].
     * @param url [String] containing the image url to download
     * @return [Bitmap] containing the downloaded image
     */
    internal fun download(url: String?): Bitmap? {
        var bitmap: Bitmap? = null
        val executorService = executor
        val downloadTask = executorService.submit(DownloadImageCallable(url))
        try {
            bitmap = downloadTask[DOWNLOAD_TIMEOUT.toLong(), TimeUnit.SECONDS]
        } catch (e: Exception) {
            downloadTask.cancel(true)
        }
        return bitmap
    }

    /**
     * Converts a [Bitmap] into an [InputStream] to be used in caching images.
     *
     * @param bitmap [Bitmap] to be converted into an [InputStream]
     * @return an `InputStream` created from the provided bitmap
     */
    private fun bitmapToInputStream(bitmap: Bitmap): InputStream {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, FULL_BITMAP_QUALITY, byteArrayOutputStream)
        val bitmapData = byteArrayOutputStream.toByteArray()
        return ByteArrayInputStream(bitmapData)
    }

    /**
     * Writes the provided [InputStream] to the downloaded push template image [assetCacheLocation].
     *
     * @param cacheService [CacheService] the AEPSDK cache service
     * @param bitmapInputStream [InputStream] created from a download [Bitmap]
     * @param imageUri [String] containing the image uri to be used a cache key
     */
    private fun cacheBitmapInputStream(
        cacheService: CacheService,
        bitmapInputStream: InputStream,
        imageUri: String
    ) {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Caching image downloaded from $imageUri."
        )
        assetCacheLocation?.let {
            // cache push notification images for 3 days
            val cacheEntry = CacheEntry(
                bitmapInputStream,
                CacheExpiry.after(
                    PushTemplateConstants.DefaultValues.PUSH_NOTIFICATION_IMAGE_CACHE_EXPIRY_IN_MILLISECONDS
                ),
                null
            )
            cacheService[it, imageUri] = cacheEntry
        }
    }

    /**
     * Scales a downloaded [Bitmap] to a maximum width and height of 300dp x 200dp.
     * The scaling is done using a [Matrix] object to maintain the aspect ratio of the original
     * image.
     *
     * @param downloadedBitmap [Bitmap] to be scaled
     * @return [Bitmap] containing the scaled image
     */
    private fun scaleBitmap(downloadedBitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.setRectToRect(
            RectF(0f, 0f, downloadedBitmap.width.toFloat(), downloadedBitmap.height.toFloat()),
            RectF(
                0f,
                0f,
                PushTemplateConstants.DefaultValues.CAROUSEL_MAX_BITMAP_WIDTH.toFloat(),
                PushTemplateConstants.DefaultValues.CAROUSEL_MAX_BITMAP_HEIGHT.toFloat()
            ),
            Matrix.ScaleToFit.CENTER
        )
        return Bitmap.createBitmap(
            downloadedBitmap,
            0,
            0,
            downloadedBitmap.width,
            downloadedBitmap.height,
            matrix,
            true
        )
    }

    /**
     * Returns the Uri for the sound file with the given name. The sound file must be in the res/raw
     * directory. The sound file should be in format of .mp3, .wav, or .ogg
     *
     * @param soundName [String] containing the name of the sound file
     * @param context the application [Context]
     * @return the [Uri] for the sound file with the given name
     */
    internal fun getSoundUriForResourceName(
        soundName: String?,
        context: Context
    ): Uri {
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" +
                context.packageName +
                "/raw/" +
                soundName
        )
    }

    @Throws(NotificationConstructionFailedException::class)
    internal fun fallbackToBasicNotification(
        context: Context,
        trackerActivityClass: Class<out Activity>?,
        broadcastReceiverClass: Class<out BroadcastReceiver>?,
        pushTemplate: CarouselPushTemplate,
        downloadedImageUris: List<String?>
    ): NotificationCompat.Builder {
        Log.trace(
            PushTemplateConstants.LOG_TAG,
            SELF_TAG,
            "Only %d image(s) for the carousel notification were downloaded while at least %d" +
                " were expected. Building a basic push notification instead.",
            downloadedImageUris.size,
            PushTemplateConstants.DefaultValues.CAROUSEL_MINIMUM_IMAGE_COUNT
        )

        val modifiedDataMap = pushTemplate.messageData
        if (downloadedImageUris.isNotEmpty()) {
            // use the first downloaded image (if available) for the basic template notification
            modifiedDataMap[PushTemplateConstants.PushPayloadKeys.IMAGE_URL] =
                downloadedImageUris[0].toString()
        }
        val basicPushTemplate = BasicPushTemplate(modifiedDataMap)
        return BasicTemplateNotificationBuilder.construct(
            context,
            basicPushTemplate,
            trackerActivityClass,
            broadcastReceiverClass
        )
    }
}
