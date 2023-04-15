package eu.joaocosta.lunar

import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.graphics._
import eu.joaocosta.minart.runtime._
import eu.joaocosta.minart.input._
import eu.joaocosta.minart.input.KeyboardInput.Key
import scala.util.chaining._

object Main {
  val canvasSettings =
    Canvas.Settings(
      width = Constants.canvasWidth,
      height = Constants.canvasHeight,
      clearColor = Color(0, 0, 0),
      scale = Some(2)
    )

  val fullScreenSettings = canvasSettings.copy(fullScreen = true, scale = None)

  val frameRate = LoopFrequency.hz60

  def toggleFullScreen(canvas: Canvas): Unit = {
    if (canvas.canvasSettings.fullScreen) canvas.changeSettings(canvasSettings)
    else canvas.changeSettings(fullScreenSettings)
  }

  val frameCounter = {
    var frameNumber: Int = 0
    var timer            = System.currentTimeMillis
    () => {
      frameNumber += 1
      if (frameNumber % 10 == 0) {
        val currTime = System.currentTimeMillis()
        val fps      = 10.0 / ((currTime - timer) / 1000.0)
        println("FPS:" + fps)
        timer = System.currentTimeMillis()
      }
    }
  }

  def main(args: Array[String]): Unit = {
    AppLoop
      .statefulRenderLoop[AppState] {
        case AppState.Loading(_, Nil) => (_) => AppState.Menu
        case AppState.Loading(loaded, loadNext :: remaining) =>
          val progress = loaded.toDouble / (loaded + remaining.size)
          (canvas: Canvas) => {
            canvas.clear()
            Render.renderLoading(progress)(canvas)
            canvas.redraw()
            loadNext()
            AppState.Loading(loaded + 1, remaining)
          }
        case AppState.Menu =>
          (canvas: Canvas) => {
            val keyboardInput = canvas.getKeyboardInput()
            if (keyboardInput.keysPressed(Key.F)) toggleFullScreen(canvas)
            canvas.clear()
            Render.renderMenu(canvas)
            canvas.redraw()
            if (keyboardInput.isDown(Key.Enter)) AppState.startGame
            else AppState.Menu
          }
        case state @ AppState.InGame(player, level, time) =>
          (canvas: Canvas) => {
            frameCounter()
            val keyboardInput = canvas.getKeyboardInput()
            if (keyboardInput.keysPressed(Key.F)) toggleFullScreen(canvas)
            canvas.clear()
            Render.renderLevel(player, level)(canvas)
            Render.renderHud(time, state.landingSpeedExceeded, state.overRotated)(canvas)
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
            val newState = AppState.InGame(newPlayer.tick, level, time - (frameRate.millis / 10).toInt)
            if (newState.gameOver) AppState.GameOver(newState)
            else if (newState.finished) AppState.startGame
            else newState
          }
        case AppState.GameOver(lastState) =>
          (canvas: Canvas) => {
            val keyboardInput = canvas.getKeyboardInput()
            if (keyboardInput.keysPressed(Key.F)) toggleFullScreen(canvas)
            canvas.clear()
            Render.renderGameOver(lastState)(canvas)
            canvas.redraw()
            if (keyboardInput.isDown(Key.Enter)) AppState.startGame
            else if (keyboardInput.isDown(Key.Escape)) AppState.Menu
            else AppState.GameOver(lastState)
          }
      }
      .configure(canvasSettings, frameRate, AppState.initial)
      .run()
  }
}
