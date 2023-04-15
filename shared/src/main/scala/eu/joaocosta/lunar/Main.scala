package eu.joaocosta.lunar

import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.graphics._
import eu.joaocosta.minart.runtime._
import eu.joaocosta.minart.input._
import eu.joaocosta.minart.input.KeyboardInput.Key
import scala.util.chaining._

object Main {
  val canvasSettings =
    Canvas.Settings(width = Constants.canvasWidth, height = Constants.canvasHeight, clearColor = Color(0, 0, 0))

  def main(args: Array[String]): Unit = {
    AppLoop
      .statefulRenderLoop[AppState] {
        case AppState.Loading(_, Nil) => (_) => AppState.startGame
        case AppState.Loading(loaded, loadNext :: remaining) =>
          val progress = loaded.toDouble / (loaded + remaining.size)
          (canvas: Canvas) => {
            canvas.clear()
            Render.renderLoading(progress)(canvas)
            canvas.redraw()
            loadNext()
            AppState.Loading(loaded + 1, remaining)
          }
        case AppState.InGame(player, level) =>
          (canvas: Canvas) => {
            val keyboardInput = canvas.getKeyboardInput()
            canvas.clear()
            Render.renderLevel(player, level)(canvas)
            canvas.redraw()
            val newPlayer = player
              .pipe(p =>
                if (keyboardInput.isDown(Key.Left)) p.rotateCcw
                else if (keyboardInput.isDown(Key.Right)) p.rotateCw
                else p
              )
              .pipe(p =>
                if (keyboardInput.isDown(Key.Space) || keyboardInput.isDown(Key.Up)) p.thrust
                else p.stop
              )
            val newState = AppState.InGame(newPlayer.tick, level)
            if (newState.gameOver) AppState.GameOver(newState)
            else if (newState.finished) AppState.startGame
            else newState
          }
        case AppState.GameOver(lastState) =>
          (canvas: Canvas) => {
            val keyboardInput = canvas.getKeyboardInput()
            canvas.clear()
            Render.renderGameOver(lastState)(canvas)
            canvas.redraw()
            if (keyboardInput.isDown(Key.Enter)) AppState.startGame
            else AppState.GameOver(lastState)
          }
      }
      .configure(canvasSettings, LoopFrequency.hz60, AppState.initial)
      .run()
  }
}
