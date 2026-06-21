package com.comstorss.toolbox

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.SystemClock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Collections
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

private const val SPEED_TEST_DURATION_MS = 10_000L
private const val SPEED_TEST_SAMPLE_MS = 300L
private const val SPEED_TEST_PARALLEL_CALLS = 4

class NetworkSpeedTestService(private val context: Context) {
    private val activeCalls = Collections.synchronizedList(mutableListOf<Call>())
    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(1500, TimeUnit.MILLISECONDS)
        .readTimeout(1500, TimeUnit.MILLISECONDS)
        .callTimeout(SPEED_TEST_DURATION_MS + 1500, TimeUnit.MILLISECONDS)
        .build()

    private val candidateUrls = listOf(
        "https://mirrors.tuna.tsinghua.edu.cn/ubuntu-releases/22.04.5/ubuntu-22.04.5-desktop-amd64.iso",
        "https://mirrors.bfsu.edu.cn/ubuntu-releases/22.04.5/ubuntu-22.04.5-desktop-amd64.iso",
        "https://mirrors.huaweicloud.com/ubuntu-releases/22.04.5/ubuntu-22.04.5-desktop-amd64.iso",
        "https://mirrors.hit.edu.cn/ubuntu-releases/22.04.5/ubuntu-22.04.5-desktop-amd64.iso"
    )

    fun cancel() {
        val calls = synchronized(activeCalls) { activeCalls.toList().also { activeCalls.clear() } }
        calls.forEach { it.cancel() }
    }

    fun networkKind(): NetworkKind = try {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return NetworkKind.Unknown
        val network = manager.activeNetwork ?: return NetworkKind.None
        val caps = manager.getNetworkCapabilities(network) ?: return NetworkKind.Unknown
        when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkKind.Wifi
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkKind.Mobile
            else -> NetworkKind.Unknown
        }
    } catch (_: SecurityException) {
        NetworkKind.Unknown
    } catch (_: Throwable) {
        NetworkKind.Unknown
    }

    suspend fun runDownloadTest(onSample: (SpeedTestSample) -> Unit): Result<SpeedTestResult> = withContext(Dispatchers.IO) {
        runCatching {
            val network = networkKind()
            require(network != NetworkKind.None) { "\u5f53\u524d\u65e0\u7f51\u7edc\uff0c\u8bf7\u8fde\u63a5\u540e\u518d\u6d4b\u8bd5" }
            val totalBytes = AtomicLong(0L)
            val startTime = SystemClock.elapsedRealtime()
            val deadline = startTime + SPEED_TEST_DURATION_MS
            var maxMbps = 0.0

            coroutineScope {
                val sampler = launch {
                    var lastBytes = 0L
                    var lastTime = startTime
                    while (SystemClock.elapsedRealtime() < deadline) {
                        delay(SPEED_TEST_SAMPLE_MS)
                        val now = SystemClock.elapsedRealtime()
                        val bytes = totalBytes.get()
                        val deltaBytes = (bytes - lastBytes).coerceAtLeast(0L)
                        val deltaMs = (now - lastTime).coerceAtLeast(1L)
                        val currentMbps = bytesToMbps(deltaBytes, deltaMs)
                        val elapsed = (now - startTime).coerceAtLeast(1L)
                        val averageMbps = bytesToMbps(bytes, elapsed)
                        maxMbps = maxOf(maxMbps, currentMbps)
                        onSample(SpeedTestSample(currentMbps, maxMbps, averageMbps, elapsed, network))
                        lastBytes = bytes
                        lastTime = now
                    }
                }

                val deadlineCanceller = launch {
                    delay(SPEED_TEST_DURATION_MS)
                    this@NetworkSpeedTestService.cancel()
                }

                try {
                    (0 until SPEED_TEST_PARALLEL_CALLS).map { worker ->
                        async { downloadUntilDeadline(worker, deadline, totalBytes) }
                    }.awaitAll()
                } finally {
                    deadlineCanceller.cancel()
                    sampler.cancelAndJoin()
                    this@NetworkSpeedTestService.cancel()
                }
            }

            val elapsedMs = (SystemClock.elapsedRealtime() - startTime).coerceAtLeast(1L)
            val bytes = totalBytes.get()
            require(bytes > 0L) { "\u6d4b\u901f\u670d\u52a1\u6682\u65f6\u4e0d\u53ef\u7528\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5" }
            val averageMbps = bytesToMbps(bytes, elapsedMs)
            SpeedTestResult(maxMbps, averageMbps, elapsedMs, network, ratingFor(averageMbps))
        }
    }

    private suspend fun downloadUntilDeadline(worker: Int, deadline: Long, totalBytes: AtomicLong) {
        val buffer = ByteArray(256 * 1024)
        var attempt = worker
        while (SystemClock.elapsedRealtime() < deadline) {
            currentCoroutineContext().ensureActive()
            val url = candidateUrls[attempt % candidateUrls.size]
            attempt += SPEED_TEST_PARALLEL_CALLS
            val request = Request.Builder()
                .url(cacheBustedUrl(url, "$worker-${SystemClock.elapsedRealtime()}"))
                .header("Cache-Control", "no-cache")
                .header("User-Agent", "Mozilla/5.0 Android Toolbox SpeedTest")
                .build()
            val call = trackedCall(request)
            try {
                call.execute().use { response ->
                    if (!response.isSuccessful) error("HTTP ${response.code}")
                    val body = response.body ?: error("empty body")
                    body.byteStream().use { input ->
                        while (SystemClock.elapsedRealtime() < deadline) {
                            currentCoroutineContext().ensureActive()
                            val read = input.read(buffer)
                            if (read <= 0) break
                            totalBytes.addAndGet(read.toLong())
                        }
                    }
                }
            } catch (error: Throwable) {
                if (error is CancellationException) throw error
                delay(120)
            } finally {
                finishCall(call)
            }
        }
    }

    private fun trackedCall(request: Request): Call {
        val call = client.newCall(request)
        activeCalls.add(call)
        return call
    }

    private fun finishCall(call: Call) {
        activeCalls.remove(call)
    }

    private fun cacheBustedUrl(url: String, token: String): String {
        if (!url.contains("__down")) return url
        val separator = if (url.contains('?')) '&' else '?'
        return "$url${separator}toolbox_speed=$token"
    }

    private fun bytesToMbps(bytes: Long, millis: Long): Double {
        if (bytes <= 0L || millis <= 0L) return 0.0
        return bytes * 8.0 / millis / 1000.0
    }

    private fun ratingFor(mbps: Double): SpeedRating = when {
        mbps >= 100.0 -> SpeedRating.Excellent
        mbps >= 50.0 -> SpeedRating.Good
        mbps >= 15.0 -> SpeedRating.Normal
        else -> SpeedRating.Slow
    }
}