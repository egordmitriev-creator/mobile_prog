package com.example.bugs.fragments

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.bugs.R
import kotlin.random.Random

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bugs = mutableListOf<Bug>()
    private val bugBitmaps = mutableListOf<Bitmap>()
    private val paint = Paint()
    private var animator: ValueAnimator? = null
    private var isGameRunning = false

    private var gameSpeed = 1.0f
    private var maxBugs = 10
    private var bugCreationInterval = 1000L // 1 секунда

    private var onBugTappedListener: ((Int) -> Unit)? = null
    private var onMissListener: (() -> Unit)? = null

    private val bugCreationRunnable: Runnable = Runnable {
        if (isGameRunning && bugs.size < maxBugs) {
            addRandomBug()
            postDelayed(bugCreationRunnable, bugCreationInterval)
        }
    }

    init {
        // Загружаем изображения жуков
        loadBugBitmaps()
        paint.isAntiAlias = true
    }

    private fun loadBugBitmaps() {
        val bugDrawables = listOf(
            R.drawable.bug1, R.drawable.bug2, R.drawable.bug3,
            R.drawable.bug4, R.drawable.bug5
        )

        bugDrawables.forEach { drawableId ->
            val bitmap = BitmapFactory.decodeResource(resources, drawableId)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true)
            bugBitmaps.add(scaledBitmap)
        }
    }

    fun setGameSettings(speed: Int, maxBugsCount: Int) {
        gameSpeed = speed / 50.0f // Нормализуем скорость
        maxBugs = maxBugsCount
        bugCreationInterval = (2000 / gameSpeed).toLong() // Интервал зависит от скорости
    }

    fun setOnBugTappedListener(listener: (Int) -> Unit) {
        onBugTappedListener = listener
    }

    fun setOnMissListener(listener: () -> Unit) {
        onMissListener = listener
    }

    fun startGame() {
        isGameRunning = true
        bugs.clear()

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            ValueAnimator.setFrameDelay(1000)
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                updateBugs()
                invalidate()
            }
            start()
        }

        postDelayed(bugCreationRunnable, bugCreationInterval)
    }
    fun restartGame() {
        stopGame()
        bugs.clear()
        startGame()
    }
    fun stopGame() {
        isGameRunning = false
        animator?.cancel()
        removeCallbacks(bugCreationRunnable)
    }

    private fun addRandomBug() {
        if (bugBitmaps.isEmpty() || !isGameRunning) return

        val randomBitmap = bugBitmaps[Random.nextInt(bugBitmaps.size)]
        val bugType = Random.nextInt(3) // 0 - обычный, 1 - быстрый, 2 - бонусный

        val bug = Bug(
            bitmap = randomBitmap,
            x = Random.nextInt(0, width - randomBitmap.width).toFloat(),
            y = Random.nextInt(0, height - randomBitmap.height).toFloat(),
            speedX = (Random.nextFloat() * 10 - 5) * gameSpeed,
            speedY = (Random.nextFloat() * 10 - 5) * gameSpeed,
            type = bugType,
            points = when (bugType) {
                0 -> 10  // Обычный жук
                1 -> 15  // Быстрый жук
                2 -> 25  // Бонусный жук
                else -> 10
            }
        )

        bugs.add(bug)
    }

    private fun updateBugs() {
        val iterator = bugs.iterator()
        while (iterator.hasNext()) {
            val bug = iterator.next()

            // Обновляем позицию
            bug.x += bug.speedX
            bug.y += bug.speedY

            // Проверяем столкновение с границами
            if (bug.x <= 0 || bug.x >= width - bug.bitmap.width) {
                bug.speedX *= -1
            }
            if (bug.y <= 0 || bug.y >= height - bug.bitmap.height) {
                bug.speedY *= -1
            }

            // Удаляем жуков, которые живут слишком долго
            bug.lifetime--
            if (bug.lifetime <= 0 && bug.type != 2) {
                iterator.remove()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Рисуем фон
        canvas.drawColor(Color.WHITE)

        // Рисуем всех жуков
        for (bug in bugs) {
            canvas.drawBitmap(bug.bitmap, bug.x, bug.y, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && isGameRunning) {
            val x = event.x
            val y = event.y
            var hitBug = false

            val iterator = bugs.iterator()
            while (iterator.hasNext()) {
                val bug = iterator.next()

                // Проверяем попадание по жуку
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bugs.clear()
    }
}

data class Bug(
    val bitmap: Bitmap,
    var x: Float,
    var y: Float,
    var speedX: Float,
    var speedY: Float,
    val type: Int = 0, // 0 - обычный, 1 - быстрый, 2 - бонусный
    val points: Int = 10,
    var lifetime: Int = 200 // Время жизни в кадрах
)