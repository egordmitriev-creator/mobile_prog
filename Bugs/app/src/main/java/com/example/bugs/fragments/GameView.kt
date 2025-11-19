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
    private var goldenBugBitmap: Bitmap? = null
    private val paint = Paint()
    private var animator: ValueAnimator? = null
    private var isGameRunning = false

    // Увеличиваем количество жуков
    private var gameSpeed = 1.0f
    private var maxBugs = 25  // Увеличили с 10 до 25
    private var bugCreationInterval = 500L  // Уменьшили интервал создания

    // Бонусные переменные
    private var tiltBonus: TiltBonus? = null
    private var goldenBug: GoldenBug? = null
    private var isTiltModeActive = false
    private var tiltModeEndTime = 0L
    private val tiltModeDuration = 10000L
    private val tiltBonusInterval = 15000L
    private val goldenBugInterval = 20000L  // Золотой таракан каждые 20 сек
    private var lastBonusTime = 0L
    private var lastGoldenBugTime = 0L

    // Курс золота
    private var currentGoldRate: Float = 5000f

    // Сенсор и звук
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var mediaPlayer: MediaPlayer? = null

    // Переменные для наклона
    private var accelerometerValues = FloatArray(3)
    private var isAccelerometerAvailable = false

    // Инверсия наклона
    private var tiltInversionX = false

    private var onBugTappedListener: ((Int) -> Unit)? = null
    private var onMissListener: (() -> Unit)? = null
    private var onTiltBonusActivated: ((Boolean) -> Unit)? = null
    private var onGoldenBugTapped: ((Int) -> Unit)? = null

    private val bugCreationRunnable: Runnable = Runnable {
        if (isGameRunning && bugs.size < maxBugs) {
            addRandomBug()
            postDelayed(bugCreationRunnable, bugCreationInterval)
        }
    }

    private val bonusCreationRunnable: Runnable = Runnable {
        if (isGameRunning && tiltBonus == null && !isTiltModeActive) {
            createTiltBonus()
        }
        postDelayed(bonusCreationRunnable, tiltBonusInterval)
    }

    private val goldenBugCreationRunnable: Runnable = Runnable {
        if (isGameRunning && goldenBug == null) {
            createGoldenBug()
        }
        postDelayed(goldenBugCreationRunnable, goldenBugInterval)
    }

    init {
        loadBugBitmaps()
        loadTiltBonusBitmap()
        loadGoldenBugBitmap()
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
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, true) // Уменьшили размер для большего количества
            bugBitmaps.add(scaledBitmap)
        }
    }

    private fun loadTiltBonusBitmap() {
        tiltBonusBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_tilt_bonus)
        tiltBonusBitmap = Bitmap.createScaledBitmap(tiltBonusBitmap!!, 100, 100, true)
    }

    private fun loadGoldenBugBitmap() {
        goldenBugBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_golden_bug)
        goldenBugBitmap = Bitmap.createScaledBitmap(goldenBugBitmap!!, 120, 120, true)
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
        maxBugs = maxBugsCount
        bugCreationInterval = (1000 / gameSpeed).toLong() // Уменьшили интервал для большего количества жуков
    }

    fun setGoldRate(rate: Float) {
        currentGoldRate = rate
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

    fun setOnGoldenBugTapped(listener: (Int) -> Unit) {
        onGoldenBugTapped = listener
    }

    fun startGame() {
        isGameRunning = true
        bugs.clear()
        tiltBonus = null
        goldenBug = null
        isTiltModeActive = false
        lastBonusTime = System.currentTimeMillis()
        lastGoldenBugTime = System.currentTimeMillis()

        // Создаем начальных жуков
        for (i in 0 until 10) {
            addRandomBug()
        }

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                updateBugs()
                updateTiltBonus()
                updateGoldenBug()
                invalidate()
            }
            start()
        }

        postDelayed(bugCreationRunnable, bugCreationInterval)
        postDelayed(bonusCreationRunnable, tiltBonusInterval)
        postDelayed(goldenBugCreationRunnable, goldenBugInterval)

        if (isAccelerometerAvailable) {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun restartGame() {
        stopGame()
        bugs.clear()
        tiltBonus = null
        goldenBug = null
        isTiltModeActive = false
        startGame()
    }

    fun stopGame() {
        isGameRunning = false
        isTiltModeActive = false
        animator?.cancel()
        removeCallbacks(bugCreationRunnable)
        removeCallbacks(bonusCreationRunnable)
        removeCallbacks(goldenBugCreationRunnable)
        sensorManager?.unregisterListener(this)
    }

    private fun createTiltBonus() {
        if (tiltBonusBitmap == null) return

        tiltBonus = TiltBonus(
            bitmap = tiltBonusBitmap!!,
            x = Random.nextInt(100, width - 220).toFloat(),
            y = Random.nextInt(100, height - 220).toFloat(),
            creationTime = System.currentTimeMillis(),
            lifetime = 8000L
        )
    }

    private fun createGoldenBug() {
        if (goldenBugBitmap == null) return

        // Очки = курс золота / 1000 (чтобы были адекватные значения ~230 очков)
        val points = (currentGoldRate / 1000).toInt()

        goldenBug = GoldenBug(
            bitmap = goldenBugBitmap!!,
            x = Random.nextInt(100, width - 220).toFloat(),
            y = Random.nextInt(100, height - 220).toFloat(),
            speedX = (Random.nextFloat() * 8 - 4) * gameSpeed,
            speedY = (Random.nextFloat() * 8 - 4) * gameSpeed,
            creationTime = System.currentTimeMillis(),
            lifetime = 10000L,
            points = points.coerceAtLeast(100) // Минимум 100 очков
        )
    }

    private fun activateTiltMode() {
        isTiltModeActive = true
        tiltModeEndTime = System.currentTimeMillis() + tiltModeDuration
        tiltInversionX = Random.nextBoolean() // Случайная инверсия по X
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
            }
        )

        bugs.add(bug)
    }

    private fun updateBugs() {
        val iterator = bugs.iterator()
        while (iterator.hasNext()) {
            val bug = iterator.next()

            if (isTiltModeActive && isAccelerometerAvailable) {
                // Режим наклона с инверсией по горизонтали
                var tiltX = accelerometerValues[0]
                if (tiltInversionX) {
                    tiltX = -tiltX // ИНВЕРСИЯ ПО ГОРИЗОНТАЛИ
                }

                val tiltY = accelerometerValues[1]
                val tiltFactor = 1.2f // Увеличили влияние наклона

                bug.speedX += tiltX * tiltFactor
                bug.speedY += tiltY * tiltFactor

                // Ограничиваем максимальную скорость
                val maxSpeed = 20f
                val currentSpeed = sqrt(bug.speedX * bug.speedX + bug.speedY * bug.speedY)
                if (currentSpeed > maxSpeed) {
                    bug.speedX = (bug.speedX / currentSpeed) * maxSpeed
                    bug.speedY = (bug.speedY / currentSpeed) * maxSpeed
                }
            }

            // Обновляем позицию
            bug.x += bug.speedX
            bug.y += bug.speedY

            // Проверяем столкновение с границами
            if (bug.x <= 0 || bug.x >= width - bug.bitmap.width) {
                bug.speedX *= -1
                bug.x = bug.x.coerceIn(0f, (width - bug.bitmap.width).toFloat())
            }
            if (bug.y <= 0 || bug.y >= height - bug.bitmap.height) {
                bug.speedY *= -1
                bug.y = bug.y.coerceIn(0f, (height - bug.bitmap.height).toFloat())
            }

            // Удаляем жуков, которые живут слишком долго
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

    private fun updateGoldenBug() {
        goldenBug?.let { bug ->
            // Обновляем позицию золотого таракана
            bug.x += bug.speedX
            bug.y += bug.speedY

            // Отскок от границ
            if (bug.x <= 0 || bug.x >= width - bug.bitmap.width) {
                bug.speedX *= -1
                bug.x = bug.x.coerceIn(0f, (width - bug.bitmap.width).toFloat())
            }
            if (bug.y <= 0 || bug.y >= height - bug.bitmap.height) {
                bug.speedY *= -1
                bug.y = bug.y.coerceIn(0f, (height - bug.bitmap.height).toFloat())
            }

            // Проверяем время жизни
            if (System.currentTimeMillis() - bug.creationTime > bug.lifetime) {
                goldenBug = null
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.WHITE)

        // Рисуем всех жуков
        for (bug in bugs) {
            canvas.drawBitmap(bug.bitmap, bug.x, bug.y, paint)
        }

        // Рисуем золотого таракана
        goldenBug?.let { bug ->
            // Добавляем золотое свечение
            paint.color = Color.YELLOW
            paint.alpha = 100
            canvas.drawCircle(
                bug.x + bug.bitmap.width / 2,
                bug.y + bug.bitmap.height / 2,
                (bug.bitmap.width / 2 + 10).toFloat(),
                paint
            )
            paint.alpha = 255

            canvas.drawBitmap(bug.bitmap, bug.x, bug.y, paint)

            // Отображаем стоимость
            paint.color = Color.BLACK
            paint.textSize = 16f
            canvas.drawText(
                "${bug.points}₽",
                bug.x + bug.bitmap.width / 2 - 10,
                bug.y - 10,
                paint
            )
        }

        // Рисуем бонус наклона
        tiltBonus?.let { bonus ->
            canvas.drawBitmap(bonus.bitmap, bonus.x, bonus.y, paint)

            val timeLeft = (bonus.lifetime - (System.currentTimeMillis() - bonus.creationTime)) / 1000
            paint.color = Color.RED
            paint.textSize = 18f
            canvas.drawText(
                "${timeLeft}с",
                bonus.x + bonus.bitmap.width / 2 - 10,
                bonus.y - 10,
                paint
            )
        }

        // Рисуем индикатор режима наклона
        if (isTiltModeActive) {
            val timeLeft = (tiltModeEndTime - System.currentTimeMillis()) / 1000
            paint.color = if (tiltInversionX) Color.RED else Color.BLUE
            paint.textSize = 20f
            paint.textAlign = Paint.Align.CENTER
            val inversionText = if (tiltInversionX) "ИНВЕРСИЯ ВКЛ" else "НОРМАЛЬНЫЙ РЕЖИМ"
            canvas.drawText(
                "НАКЛОН: ${timeLeft}с ($inversionText)",
                width / 2f,
                60f,
                paint
            )
            paint.textAlign = Paint.Align.LEFT
        }

//        // Отображаем текущий курс золота
//        paint.color = Color.BLACK
//        paint.textSize = 16f
//        val rateText = "Курс золота: ${String.format("%.0f", currentGoldRate)}₽/унция"
//        canvas.drawText(rateText, 10f, height - 30f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && isGameRunning) {
            val x = event.x
            val y = event.y

            // Сначала проверяем золотого таракана
            goldenBug?.let { bug ->
                if (x >= bug.x && x <= bug.x + bug.bitmap.width &&
                    y >= bug.y && y <= bug.y + bug.bitmap.height) {

                    onGoldenBugTapped?.invoke(bug.points)
                    goldenBug = null
                    return true
                }
            }

            // Затем проверяем бонус наклона
            tiltBonus?.let { bonus ->
                if (x >= bonus.x && x <= bonus.x + bonus.bitmap.width &&
                    y >= bonus.y && y <= bonus.y + bonus.bitmap.height) {

                    tiltBonus = null
                    activateTiltMode()
                    return true
                }
            }

            // Затем проверяем обычных жуков
            var hitBug = false
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
        goldenBug = null
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
    var lifetime: Int = 300  // Увеличили время жизни
)

data class TiltBonus(
    val bitmap: Bitmap,
    val x: Float,
    val y: Float,
    val creationTime: Long,
    val lifetime: Long
)

data class GoldenBug(
    val bitmap: Bitmap,
    var x: Float,
    var y: Float,
    var speedX: Float,
    var speedY: Float,
    val creationTime: Long,
    val lifetime: Long,
    val points: Int
)