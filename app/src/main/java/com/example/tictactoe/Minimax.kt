package com.example.tictactoe

data class Move(var row: Int, var col: Int)

public class Minimax {
    private var ai : Char = 'O'
    private var player : Char = 'X'

    public fun findBestMove(board: Array<CharArray>, aiInput: Char, playerInput: Char): Move {
        this.ai = aiInput
        this.player = playerInput

        var bestVal = Int.MIN_VALUE
        val bestMove = Move(-1, -1)

        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[i][j] == ' ') {
                    board[i][j] = this.ai
                    val moveVal = minimax(board, 0, false)
                    board[i][j] = ' '

                    if (moveVal > bestVal) {
                        bestMove.row = i
                        bestMove.col = j
                        bestVal = moveVal
                    }
                }
            }
        }
        return bestMove
    }

    private fun minimax(board: Array<CharArray>, depth: Int, isMax: Boolean): Int {
        val score = evaluate(board)

        if (score == 10) return score
        if (score == -10) return score
        if (!areMovesLeft(board)) return 0

        if (isMax) {
            var best = Int.MIN_VALUE
            for (i in 0 until 3) {
                for (j in 0 until 3) {
                    if (board[i][j] == ' ') {
                        board[i][j] = this.ai
                        best = maxOf(best, minimax(board, depth + 1, false))
                        board[i][j] = ' '
                    }
                }
            }
            return best
        } else {
            var best = Int.MAX_VALUE
            for (i in 0 until 3) {
                for (j in 0 until 3) {
                    if (board[i][j] == ' ') {
                        board[i][j] = this.player
                        best = minOf(best, minimax(board, depth + 1, true))
                        board[i][j] = ' '
                    }
                }
            }
            return best
        }

    }

    private fun areMovesLeft(board: Array<CharArray>): Boolean {
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[i][j] == ' ') {
                    return true
                }
            }
        }
        return false
    }

    private fun evaluate(board: Array<CharArray>): Int {
        for (row in 0 until 3) {
            if (board[row][0] == board[row][1] && board[row][1] == board[row][2]) {
                if (board[row][0] == this.ai) return 10
                else if (board[row][0] == this.player) return -10
            }
        }

        for (col in 0 until 3) {
            if (board[0][col] == board[1][col] && board[1][col] == board[2][col]) {
                if (board[0][col] == this.ai) return 10
                else if (board[0][col] == this.player) return -10
            }
        }

        if (board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            if (board[0][0] == this.ai) return 10
            else if (board[0][0] == this.player) return -10
        }

        if (board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            if (board[0][2] == this.ai) return 10
            else if (board[0][2] == this.player) return -10
        }

        return 0
    }
}