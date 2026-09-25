package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.ElixirApplication
import com.example.MainActivity
import com.example.elixir.ElixirMultiplier
import com.example.model.ClashCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ElixirOverlayService : Service(), VisionDetectionListener {

    companion object {
        const val ACTION_START = "ACTION_START_OVERLAY_CAPTURE"
        const val ACTION_STOP = "ACTION_STOP_OVERLAY_CAPTURE"
        const val EXTRA_RESULT_CODE = "EXTRA_RESULT_CODE"
        const val EXTRA_RESULT_DATA = "EXTRA_RESULT_DATA"
        private const val CHANNEL_ID = "elixir_overlay_capture_channel"
        private const val NOTIFICATION_ID = 4001

        var isServiceRunning = false
            private set

        fun startService(context: Context, resultCode: Int, resultData: Intent) {
            val intent = Intent(context, ElixirOverlayService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_RESULT_CODE, resultCode)
                putExtra(EXTRA_RESULT_DATA, resultData)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ElixirOverlayService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var windowManager: WindowManager? = null
    private var floatingOverlayView: View? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var captureHandlerThread: HandlerThread? = null
    private var captureHandler: Handler? = null

    private var serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var stateObserverJob: Job? = null
    private lateinit var analyzer: ScreenCaptureAnalyzer

    override fun onCreate() {
        super.onCreate()
        analyzer = ScreenCaptureAnalyzer(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForegroundService()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val resultData = intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)

                startForegroundWithNotification()
                isServiceRunning = true

                if (resultCode != 0 && resultData != null) {
                    setupMediaProjection(resultCode, resultData)
                }

                if (Settings.canDrawOverlays(this)) {
                    setupFloatingOverlayView()
                }

                observeEngineState()
            }
        }
        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Elixir Pro Active")
            .setContentText("Screen Capture & Live Arena Scanner running")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Elixir Pro Companion Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Live game screen analyzer & floating overlay"
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    private fun setupMediaProjection(resultCode: Int, resultData: Intent) {
        val mpManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager ?: return
        mediaProjection = mpManager.getMediaProjection(resultCode, resultData)

        val metrics = resources.displayMetrics
        val density = metrics.densityDpi

        // Downscale capture frame resolution to 360 x 640 for lightweight, battery-efficient processing
        val captureWidth = 360
        val captureHeight = 640

        captureHandlerThread = HandlerThread("ElixirCaptureThread").apply { start() }
        captureHandler = Handler(captureHandlerThread!!.looper)

        imageReader = ImageReader.newInstance(captureWidth, captureHeight, PixelFormat.RGBA_8888, 2)
        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader?.acquireLatestImage()
            if (image != null) {
                try {
                    analyzer.analyzeFrame(image)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    image.close()
                }
            }
        }, captureHandler)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ElixirScreenCapture",
            captureWidth,
            captureHeight,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            captureHandler
        )
    }

    private fun setupFloatingOverlayView() {
        if (floatingOverlayView != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 180
        }

        // Programmatic floating pill layout
        val container = FrameLayout(this).apply {
            setBackgroundColor(0xF00B0F19.toInt())
            setPadding(24, 16, 24, 16)
        }

        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val titleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val tvTitle = TextView(this).apply {
            text = "ELIXIR"
            textSize = 9f
            setTextColor(0xFF94A3B8.toInt())
        }

        val tvMultiplier = TextView(this).apply {
            id = View.generateViewId()
            text = " 1X"
            textSize = 9f
            setTextColor(0xFFF59E0B.toInt())
        }

        titleRow.addView(tvTitle)
        titleRow.addView(tvMultiplier)

        val tvElixir = TextView(this).apply {
            id = View.generateViewId()
            text = "5.0"
            textSize = 24f
            setTextColor(0xFFFF0054.toInt())
            typeface = android.graphics.Typeface.MONOSPACE
        }

        val tvAdvantage = TextView(this).apply {
            id = View.generateViewId()
            text = "Lead: +0"
            textSize = 10f
            setTextColor(0xFF10B981.toInt())
        }

        contentLayout.addView(titleRow)
        contentLayout.addView(tvElixir)
        contentLayout.addView(tvAdvantage)

        container.addView(contentLayout)

        // Make floating overlay touch-draggable
        container.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(container, params)
                        return true
                    }
                }
                return false
            }
        })

        floatingOverlayView = container
        try {
            windowManager?.addView(container, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observeEngineState() {
        val engine = ElixirApplication.instance.engine
        stateObserverJob?.cancel()
        stateObserverJob = serviceScope.launch {
            engine.state.collectLatest { state ->
                floatingOverlayView?.let { view ->
                    val container = view as? FrameLayout ?: return@let
                    val layout = container.getChildAt(0) as? LinearLayout ?: return@let

                    val titleRow = layout.getChildAt(0) as? LinearLayout
                    val tvMultiplier = titleRow?.getChildAt(1) as? TextView
                    val tvElixir = layout.getChildAt(1) as? TextView
                    val tvAdvantage = layout.getChildAt(2) as? TextView

                    tvMultiplier?.text = " ${state.multiplier.label.take(2)}"
                    tvElixir?.text = String.format(java.util.Locale.US, "%.1f", state.opponentElixir)

                    val adv = state.elixirAdvantage
                    tvAdvantage?.text = when {
                        adv > 0 -> "+$adv Lead"
                        adv < 0 -> "$adv"
                        else -> "Even"
                    }
                    tvAdvantage?.setTextColor(
                        if (adv > 0) 0xFF10B981.toInt() else if (adv < 0) 0xFFEF4444.toInt() else 0xFF94A3B8.toInt()
                    )
                }
            }
        }
    }

    // VisionDetectionListener callbacks
    override fun onMatchStartDetected() {
        val engine = ElixirApplication.instance.engine
        if (!engine.state.value.isMatchRunning) {
            engine.startMatch()
        }
    }

    override fun onMatchEndDetected() {
        val engine = ElixirApplication.instance.engine
        if (engine.state.value.isMatchRunning) {
            engine.pauseOrResumeMatch()
        }
    }

    override fun onMultiplierDetected(multiplier: ElixirMultiplier) {
        val engine = ElixirApplication.instance.engine
        if (engine.state.value.multiplier != multiplier) {
            engine.setMultiplier(multiplier)
        }
    }

    override fun onCardDeploymentDetected(card: ClashCard) {
        val engine = ElixirApplication.instance.engine
        engine.playOpponentCard(card, source = "Live Screen Capture")
    }

    override fun onStatusUpdate(status: String) {
        val engine = ElixirApplication.instance.engine
        engine.updateVisionStatus(status)
    }

    private fun stopForegroundService() {
        isServiceRunning = false
        stateObserverJob?.cancel()
        serviceScope.cancel()

        try {
            floatingOverlayView?.let { windowManager?.removeView(it) }
        } catch (_: Exception) {}
        floatingOverlayView = null

        virtualDisplay?.release()
        virtualDisplay = null

        imageReader?.close()
        imageReader = null

        mediaProjection?.stop()
        mediaProjection = null

        captureHandlerThread?.quitSafely()
        captureHandlerThread = null

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopForegroundService()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
