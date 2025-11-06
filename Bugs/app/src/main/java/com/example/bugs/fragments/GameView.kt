package com.example.bugs.fragments

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.bugs.R
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.math.sqrt

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), SensorEventListener {

    private val bugs = mutableListOf<Bug>()
    private val bugBitmaps = mutableListOf<Bitmap>()
    private var tiltBonusBitmap: Bitmap? = null
    private val paint = Paint()
    private var animator: ValueAnimator? = null
    private var isGameRunning = false

    private var gameSpeed = 1.0f
    private var maxBugs = 25
    private var bugCreationInterval = 500L
    private var initialBugsCount = 15


    private var tiltBonus: TiltBonus? = null
    private var isTiltModeActive = false
    private var tiltModeEndTime = 0L
    private val tiltModeDuration = 10000L
    private val tiltBonusInterval = 15000L
    private var lastBonusTime = 0L

    // Сенсор и звук
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var mediaPlayer: MediaPlayer? = null

    // Переменные для наклона
    private var accelerometerValues = FloatArray(3)
    private var isAccelerometerAvailable = false
    private var tiltInversionX = -1f

    private var onBugTappedListener: ((Int) -> Unit)? = null
    private var onMissListener: (() -> Unit)? = null
    private var onTiltBonusActivated: ((Boolean) -> Unit)? = null

    private val bugCreationRunnable: Runnable = Runnable {
        if (isGameRunning && bugs.size < maxBugs) {
            // Создаем сразу несколько жуков за раз
            val bugsToCreate = Random.nextInt(1, 4) // 1-3 жука за раз
            repeat(bugsToCreate) {
                if (bugs.size < maxBugs) {
                    addRandomBug()
                }
            }
            postDelayed(bugCreationRunnable, bugCreationInterval)
        }
    }

    private val bonusCreationRunnable: Runnable = Runnable {
        if (isGameRunning && tiltBonus == null && !isTiltModeActive) {
            createTiltBonus()
        }
        postDelayed(bonusCreationRunnable, tiltBonusInterval)
    }

    init {
        loadBugBitmaps()
        loadTiltBonusBitmap()
        setupSensors()
        setupSound()
        paint.isAntiAlias = true
    }

    private fun loadBugBitmaps() {
        val bugDrawables = listOf(
            R.drawable.bug1, R.drawable.bug2, R.drawable.bug3,
            R.drawable.bug4, R.drawable.bug5
        )

        bugDrawables.forEach { drawableId ->
            val bitmap = BitmapFactory.decodeResource(resources, drawableId)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, true)
            bugBitmaps.add(scaledBitmap)
        }
    }

    private fun loadTiltBonusBitmap() {
        tiltBonusBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_tilt_bonus)
        tiltBonusBitmap = Bitmap.createScaledBitmap(tiltBonusBitmap!!, 100, 100, true)
    }

    private fun setupSensors() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        isAccelerometerAvailable = accelerometer != null
    }

    private fun setupSound() {
        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.scream)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playScreamSound() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.seekTo(0)
                }
                it.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setGameSettings(speed: Int, maxBugsCount: Int) {
        gameSpeed = speed / 50.0f
        maxBugs = maxBugsCount * 2  // Удваиваем количество от настроек
        bugCreationInterval = (1000 / gameSpeed).toLong() // Быстрее создание
    }

    fun setOnBugTappedListener(listener: (Int) -> Unit) {
        onBugTappedListener = listener
    }

    fun setOnMissListener(listener: () -> Unit) {
        onMissListener = listener
    }

    fun setOnTiltBonusActivated(listener: (Boolean) -> Unit) {
        onTiltBonusActivated = listener
    }

    fun startGame() {
        isGameRunning = true
        bugs.clear()
        tiltBonus = null
        isTiltModeActive = false
        lastBonusTime = System.currentTimeMillis()

        // Создаем начальное количество жуков
        repeat(initialBugsCount) {
            addRandomBug()
        }

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                updateBugs()
                updateTiltBonus()
                invalidate()
            }
            start()
        }

        postDelayed(bugCreationRunnable, bugCreationInterval)
        postDelayed(bonusCreationRunnable, tiltBonusInterval)

        if (isAccelerometerAvailable) {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun restartGame() {
        stopGame()
        bugs.clear()
        tiltBonus = null
        isTiltModeActive = false
        startGame()
    }

    fun stopGame() {
        isGameRunning = false
        isTiltModeActive = false
        animator?.cancel()
        removeCallbacks(bugCreationRunnable)
        removeCallbacks(bonusCreationRunnable)
        sensorManager?.unregisterListener(this)
    }

    private fun createTiltBonus() {
        if (tiltBonusBitmap == null) return

        tiltBonus = TiltBonus(
            bitmap = tiltBonusBitmap!!,
            x = Random.nextInt(100, width - 200).toFloat(),
            y = Random.nextInt(100, height - 200).toFloat(),
            creationTime = System.currentTimeMillis(),
            lifetime = 8000L
        )
    }

    private fun activateTiltMode() {
        isTiltModeActive = true
        tiltModeEndTime = System.currentTimeMillis() + tiltModeDuration
        playScreamSound()
        onTiltBonusActivated?.invoke(true)

        postDelayed({
            isTiltModeActive = false
            onTiltBonusActivated?.invoke(false)
        }, tiltModeDuration)
    }

    private fun addRandomBug() {
        if (bugBitmaps.isEmpty() || !isGameRunning) return

        val randomBitmap = bugBitmaps[Random.nextInt(bugBitmaps.size)]
        val bugType = Random.nextInt(3)

        val bug = Bug(
            bitmap = randomBitmap,
            x = Random.nextInt(0, width - randomBitmap.width).toFloat(),
            y = Random.nextInt(0, height - randomBitmap.height).toFloat(),
            speedX = (Random.nextFloat() * 8 - 4) * gameSpeed,
            speedY = (Random.nextFloat() * 8 - 4) * gameSpeed,
            type = bugType,
            points = when (bugType) {
                0 -> 10
                1 -> 15
                2 -> 25
                else -> 10
            },
            lifetime = 250
        )

        bugs.add(bug)
    }

    private fun updateBugs() {
        val iterator = bugs.iterator()
        while (iterator.hasNext()) {
            val bug = iterator.next()

            if (isTiltModeActive && isAccelerometerAvailable) {
                val tiltFactor = 0.8f
                bug.speedX += accelerometerValues[0] * tiltFactor * tiltInversionX
                bug.speedY += accelerometerValues[1] * tiltFactor

                val maxSpeed = 12f
                val currentSpeed = sqrt(bug.speedX * bug.speedX + bug.speedY * bug.speedY)
                if (currentSpeed > maxSpeed) {
                    bug.speedX = (bug.speedX / currentSpeed) * maxSpeed
                    bug.speedY = (bug.speedY / currentSpeed) * maxSpeed
                }
            }

            bug.x += bug.speedX
            bug.y += bug.speedY


            if (bug.x <= 0) {
                bug.speedX = bug.speedX.absoluteValue
                bug.x = 1f
            } else if (bug.x >= width - bug.bitmap.width) {
                bug.speedX = -bug.speedX.absoluteValue
                bug.x = (width - bug.bitmap.width - 1).toFloat()
            }

            if (bug.y <= 0) {
                bug.speedY = bug.speedY.absoluteValue
                bug.y = 1f
            } else if (bug.y >= height - bug.bitmap.height) {
                bug.speedY = -bug.speedY.absoluteValue
                bug.y = (height - bug.bitmap.height - 1).toFloat()
            }

            bug.lifetime--
            if (bug.lifetime <= 0 && bug.type != 2) {
                iterator.remove()
            }
        }
    }

    private fun updateTiltBonus() {
        tiltBonus?.let { bonus ->
            if (System.currentTimeMillis() - bonus.creationTime > bonus.lifetime) {
                tiltBonus = null
            }
        }

        if (isTiltModeActive && System.currentTimeMillis() > tiltModeEndTime) {
            isTiltModeActive = false
            onTiltBonusActivated?.invoke(false)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.WHITE)

        // Рисуем счетчик жуков
//        paint.color = Color.BLACK
//        paint.textSize = 16f
//        canvas.drawText("Жуков: ${bugs.size}/$maxBugs", 10f, 30f, paint)

        // Рисуем всех жуков
        for (bug in bugs) {
            canvas.drawBitmap(bug.bitmap, bug.x, bug.y, paint)
        }

        // Рисуем бонус
        tiltBonus?.let { bonus ->
            canvas.drawBitmap(bonus.bitmap, bonus.x, bonus.y, paint)

            val timeLeft = (bonus.lifetime - (System.currentTimeMillis() - bonus.creationTime)) / 1000
            paint.color = Color.RED
            paint.textSize = 16f
            canvas.drawText(
                "${timeLeft}с",
                bonus.x + bonus.bitmap.width / 2 - 10,
                bonus.y - 5,
                paint
            )
        }

        // Рисуем индикатор режима наклона
        if (isTiltModeActive) {
            val timeLeft = (tiltModeEndTime - System.currentTimeMillis()) / 1000
            paint.color = Color.BLUE
            paint.textSize = 20f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(
                "РЕЖИМ НАКЛОНА: ${timeLeft}с",
                width / 2f,
                50f,
                paint
            )
            paint.textAlign = Paint.Align.LEFT
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && isGameRunning) {
            val x = event.x
            val y = event.y
            var hitBug = false

            tiltBonus?.let { bonus ->
                if (x >= bonus.x && x <= bonus.x + bonus.bitmap.width &&
                    y >= bonus.y && y <= bonus.y + bonus.bitmap.height) {

                    tiltBonus = null
                    activateTiltMode()
                    return true
                }
            }

            val iterator = bugs.iterator()
            while (iterator.hasNext()) {
                val bug = iterator.next()

                if (x >= bug.x && x <= bug.x + bug.bitmap.width &&
                    y >= bug.y && y <= bug.y + bug.bitmap.height) {

                    iterator.remove()
                    onBugTappedListener?.invoke(bug.points)
                    hitBug = true
                    break
                }
            }

            if (!hitBug) {
                onMissListener?.invoke()
            }
        }
        return true
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values.copyOf()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bugs.clear()
        tiltBonus = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopGame()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

data class Bug(
    val bitmap: Bitmap,
    var x: Float,
    var y: Float,
    var speedX: Float,
    var speedY: Float,
    val type: Int = 0,
    val points: Int = 10,
    var lifetime: Int = 250
)

data class TiltBonus(
    val bitmap: Bitmap,
    val x: Float,
    val y: Float,
    val creationTime: Long,
    val lifetime: Long
)