package com.example.tictactoe

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.media.SoundPool
import kotlin.random.Random

class TicTacToeActivity : AppCompatActivity() {
    private lateinit var gameView: TicTacToeView
    private var isSmart = false
    private val minimax = Minimax()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isSmart = intent.getBooleanExtra(getString(R.string.is_smart), false)
        gameView = TicTacToeView(this)
        setContentView(gameView)
    }

    override fun onDestroy() {
        super.onDestroy()
        gameView.releaseSoundPool()
    }

    inner class TicTacToeView(context: Context) : View(context) {
        private var board = Array(3) { CharArray(3) { ' ' } }
        private var player: Char = 'X'
        private var ai: Char = 'O'
        private var currentPlayer: Char = 'X'
        private var gameOver = false
        private var transition = false
        private var winningPositions: List<Pair<Int, Int>>? = null
        private var animationProgress: Float = 0f
        private var soundPool: SoundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .build()
        private var xSound: Int = 0
        private var oSound: Int = 0
        private var lineSound: Int = 0
        private var winSound: Int = 0
        private var loseSound: Int = 0
        private lateinit var xScaledBitmap: Bitmap
        private lateinit var oScaledBitmap: Bitmap

        init {
            xSound = soundPool.load(context, R.raw.x, 1)
            oSound = soundPool.load(context, R.raw.circle, 1)
            lineSound = soundPool.load(context, R.raw.line, 1)
            winSound = soundPool.load(context, R.raw.fanfare, 1)
            loseSound = soundPool.load(context, R.raw.foghorn, 1)
        }

        private fun initializeBitmaps() {
            val xBitmap = BitmapFactory.decodeResource(resources, R.drawable.ximage)
            val oBitmap = BitmapFactory.decodeResource(resources, R.drawable.oimage)

            val cellWidth = width / 3
            val cellHeight = height / 3

            xScaledBitmap = Bitmap.createScaledBitmap(xBitmap, cellWidth / 4, cellHeight / 4, false)
            oScaledBitmap = Bitmap.createScaledBitmap(oBitmap, cellWidth / 4, cellHeight / 4, false)

            xBitmap.recycle()
            oBitmap.recycle()
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            initializeBitmaps()
        }

        private val paint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 5f
            color = Color.BLACK
        }

        private val winningLinePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 20f
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            drawBoard(canvas)
            drawMarks(canvas)

            if (gameOver && winningPositions != null) {
                val cellWidth = width / 3f
                val cellHeight = height / 3f
                winningLinePaint.color = if (checkWinner().second == 'X') Color.RED else Color.BLUE

                val startX = winningPositions!![0].second * cellWidth + cellWidth / 2
                val startY = winningPositions!![0].first * cellHeight + cellHeight / 2
                val endX = winningPositions!![2].second * cellWidth + cellWidth / 2
                val endY = winningPositions!![2].first * cellHeight + cellHeight / 2

                canvas.drawLine(
                    startX,
                    startY,
                    startX + (endX - startX) * animationProgress,
                    startY + (endY - startY) * animationProgress,
                    winningLinePaint
                )
            }
        }

        private fun drawBoard(canvas: Canvas) {
            val cellWidth = width / 3f
            val cellHeight = height / 3f

            for (i in 1 until 3) {
                canvas.drawLine(i * cellWidth, 0f,
                    i * cellWidth, height.toFloat(), paint)
                canvas.drawLine(0f, i * cellHeight,
                    width.toFloat(), i * cellHeight, paint)
            }
        }

        private fun drawMarks(canvas: Canvas) {
            val cellWidth = width / 3f
            val cellHeight = height / 3f

            for (i in 0 until 3) {
                for (j in 0 until 3) {
                    val mark = board[i][j]
                    if (mark != ' ') {
                        val left = j * cellWidth
                        val top = i * cellHeight
                        val right = (j + 1) * cellWidth
                        val bottom = (i + 1) * cellHeight

                        val bitmap = if (mark == 'X') xScaledBitmap else oScaledBitmap
                        canvas.drawBitmap(bitmap, null, RectF(left + cellWidth / 4,
                            top + cellHeight / 4, right - cellWidth / 4,
                            bottom - cellHeight / 4), null)
                    }
                }
            }
        }

        /*
        private fun drawX(canvas: Canvas, x: Float, y: Float, cellWidth: Float, cellHeight: Float) {
            val offset = cellWidth / 4
            val paint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 50f
                color = Color.WHITE
            }

            canvas.drawLine(x - offset, y - offset, x + offset, y + offset, paint)
            canvas.drawLine(x - offset, y + offset, x + offset, y - offset, paint)

            paint.apply {
                style = Paint.Style.STROKE
                strokeWidth = 20f
                color = Color.RED
            }

            canvas.drawLine(x - offset, y - offset, x + offset, y + offset, paint)
            canvas.drawLine(x - offset, y + offset, x + offset, y - offset, paint)
        }

        private fun drawO(canvas: Canvas, x: Float, y: Float, cellWidth: Float, cellHeight: Float) {
            val offset = cellWidth / 4
            val paint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 50f
                color = Color.WHITE
            }

            canvas.drawCircle(x, y, offset, paint)

            paint.apply {
                style = Paint.Style.STROKE
                strokeWidth = 20f
                color = Color.BLUE
            }

            canvas.drawCircle(x, y, offset, paint)
        }
        */

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (transition) {
                return true
            }

            if (event.action == MotionEvent.ACTION_DOWN && !gameOver && currentPlayer == player) {
                val x = event.x
                val y = event.y
                val cellWidth = width / 3f
                val cellHeight = height / 3f
                val row = (y / cellHeight).toInt()
                val col = (x / cellWidth).toInt()

                if (row in 0..2 && col in 0..2 && board[row][col] == ' ') {
                    board[row][col] = currentPlayer
                    invalidate()
                    if (currentPlayer == 'X') {
                        BackgroundPlayer.playSound("x")
                    } else {
                        BackgroundPlayer.playSound("circle")
                    }
                    if (!checkGameOver()) {
                        currentPlayer = ai
                        postDelayed({
                            aiMove()
                        }, 1000)
                    }
                }
            }
            return true
        }

        private fun aiMove() {
            if (isSmart) {
                val bestMove = minimax.findBestMove(board, ai, player)
                board[bestMove.row][bestMove.col] = ai
            } else {
                var isValidMove = false
                while (!isValidMove) {
                    val row = Random.nextInt(3)
                    val col = Random.nextInt(3)
                    if (board[row][col] == ' ') {
                        board[row][col] = ai
                        isValidMove = true
                    }
                }
            }
            invalidate()
            checkGameOver()
            currentPlayer = player
        }

        private fun checkGameOver(): Boolean {
            val (positions, winner) = checkWinner()
            if (winner != null) {
                gameOver = true
                winningPositions = positions
                showToast(winner)
                return true
            }

            if (isBoardFull()) {
                gameOver = true
                showToast(null)
                return true
            }

            return false
        }

        private fun animateLine() {
            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.duration = 1000
            animator.addUpdateListener { animation ->
                animationProgress = animation.animatedValue as Float
                invalidate()
            }
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    BackgroundPlayer.playSound("line")
                }
            })
            animator.start()
        }

        private fun checkWinner(): Pair<List<Pair<Int, Int>>?, Char?> {
            // Check rows
            for (i in 0 until 3) {
                if (board[i][0] != ' ' && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                    return Pair(listOf(Pair(i, 0), Pair(i, 1), Pair(i, 2)), board[i][0])
                }
            }

            // Check columns
            for (j in 0 until 3) {
                if (board[0][j] != ' ' && board[0][j] == board[1][j] && board[1][j] == board[2][j]) {
                    return Pair(listOf(Pair(0, j), Pair(1, j), Pair(2, j)), board[0][j])
                }
            }

            // Check diagonals
            if (board[0][0] != ' ' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
                return Pair(listOf(Pair(0, 0), Pair(1, 1), Pair(2, 2)), board[0][0])
            }
            if (board[0][2] != ' ' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
                return Pair(listOf(Pair(0, 2), Pair(1, 1), Pair(2, 0)), board[0][2])
            }

            return Pair(null, null)
        }

        private fun isBoardFull(): Boolean {
            for (i in 0 until 3) {
                for (j in 0 until 3) {
                    if (board[i][j] == ' ') {
                        return false
                    }
                }
            }
            return true
        }

        private fun showToast(winner: Char?) {
            transition = true

            val message = when (winner) {
                player -> {
                    BackgroundPlayer.playSound("fanfare")
                    context.getString(R.string.you_win)
                }
                ai -> {
                    BackgroundPlayer.playSound("foghorn")
                    context.getString(R.string.you_lose)
                }
                else -> context.getString(R.string.tie)
            }

            val toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()

            if (message != context.getString(R.string.tie)) {
                animateLine()
            }

            postDelayed({
                showGameOverDialog()
            }, 3000)
        }

        private fun showGameOverDialog() {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Game Over")
            builder.setMessage("Do you want to go again or go back to the main menu?")
            builder.setPositiveButton("Go Again") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                resetGame()
                invalidate()
            }
            builder.setNegativeButton("Main Menu") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
                (context as Activity).finish()
            }
            builder.setCancelable(false)
            builder.show()
        }

        private fun showPlayerToast(c: Char) {
            val msg = if (c == 'X') "You are X, you go first!" else "You are O"
            val toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()

            postDelayed({
                invalidate()
            }, 1000)
        }

        private fun resetGame() {
            postDelayed({
                board = Array(3) { CharArray(3) { ' ' } }
                player = if (player == 'X') 'O' else 'X'
                ai = if (player == 'X') 'O' else 'X'
                showPlayerToast(player)
                currentPlayer = 'X'
                gameOver = false
                winningPositions = null
                animationProgress = 0f

                if (currentPlayer == ai) {
                    postDelayed({
                        aiMove()
                        transition = false
                    }, 1000)
                } else {
                    transition = false
                }

                invalidate()
            }, 1000)
        }

        fun releaseSoundPool() {
            soundPool.release()
        }
    }
}