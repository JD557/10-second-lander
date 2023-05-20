package eu.joaocosta.lunar

import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.audio._
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

  val frameRate = LoopFrequency.hz30

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

  def renderAppState(appState: AppState)(out: MutableSurface): Unit = appState match {
    case AppState.Loading(loaded, remaining) =>
      val progress = loaded.toDouble / (loaded + remaining.size)
      Render.renderLoading(progress)(out)
    case AppState.Menu =>
      Render.renderMenu(out)
    case state @ AppState.InGame(player, level, time) =>
      Render.renderLevel(player, level)(out)
      Render.renderHud(time, state.level.number, state.landingSpeedExceeded, state.overRotated)(out)
    case AppState.GameOver(lastState) =>
      Render.renderGameOver(lastState)(out)
    case AppState.Transition(from, to, t) =>
      val halfTransition = Constants.transitionMillis / 2
      val halfHeight     = out.height / 2
      if (t < halfTransition) {
        renderAppState(from)(out)
        // TODO Find out the bug here
        // out.fillRegion(0, 0, out.width, out.height * t / Constants.transitionMillis, Color(0, 0, 0))
        val transitionSize = math.min(out.height * t / halfTransition / 2, halfHeight).toInt
        out.fillRegion(
          0,
          halfHeight - transitionSize,
          out.width,
          transitionSize,
          Color(0, 0, 0)
        )
        out.fillRegion(
          0,
          halfHeight,
          out.width,
          transitionSize,
          Color(0, 0, 0)
        )
      } else {
        renderAppState(to)(out)
        val transitionSize =
          math.min(out.height * (Constants.transitionMillis - t) / halfTransition / 2, halfHeight).toInt
        out.fillRegion(
          0,
          halfHeight - transitionSize,
          out.width,
          transitionSize,
          Color(0, 0, 0)
        )
        out.fillRegion(
          0,
          halfHeight,
          out.width,
          transitionSize,
          Color(0, 0, 0)
        )

      }
  }

  def main(args: Array[String]): Unit = {
    AppLoop
      .statefulAppLoop[AppState] {
        case AppState.Loading(_, Nil) => (_) => AppState.Menu
        case state @ AppState.Loading(loaded, loadNext :: remaining) =>
          (canvas: Canvas) => {
            canvas.clear()
            renderAppState(state)(canvas)
            canvas.redraw()
            loadNext()
            AppState.Loading(loaded + 1, remaining)
          }
        case state @ AppState.Menu =>
          (system: Canvas with AudioPlayer) => {
            val keyboardInput = system.getKeyboardInput()
            if (keyboardInput.keysPressed(Key.F)) toggleFullScreen(system)
            system.clear()
            renderAppState(state)(system)
            system.redraw()
            if (keyboardInput.isDown(Key.Enter)) {
              system.play(Resources.transition, 1)
              AppState.Transition(state, AppState.startGame(1))
            } else AppState.Menu
          }
        case state @ AppState.InGame(player, level, time) =>
          (system: Canvas with AudioPlayer) => {
            // frameCounter()
            val keyboardInput = system.getKeyboardInput()
            if (keyboardInput.keysPressed(Key.F)) toggleFullScreen(system)
            system.clear()
            renderAppState(state)(system)
            system.redraw()
            val newPlayer = player
              .pipe(p =>
                if (keyboardInput.isDown(Key.Left)) p.rotateCcw
                else if (keyboardInput.isDown(Key.Right)) p.rotateCw
                else p
              )
              .pipe(p =>
                if (keyboardInput.isDown(Key.Space) || keyboardInput.isDown(Key.Up)) {
                  if (!system.isPlaying(2)) {
                    system.play(Resources.jet.repeating, 2)
                  }
                  p.thrust
                } else {
                  system.stop(2)
                  p.stop
                }
              )
            val newTime  = time - (frameRate.millis / 10).toInt
            val newState = AppState.InGame(newPlayer.tick, level, newTime)
            if (newTime <= 500 && time / 100 != newTime / 100) system.play(Resources.beep, 1)
            if (newState.gameOver) {
              system.play(Resources.gameoverBeep, 1)
              AppState.GameOver(newState)
            } else if (newState.finished) {
              system.play(Resources.transition, 1)
              AppState.Transition(state, AppState.startGame(level.number + 1))
            } else newState
          }
        case state @ AppState.GameOver(lastState) =>
          (system: Canvas with AudioPlayer) => {
            val keyboardInput = system.getKeyboardInput()
            if (keyboardInput.keysPressed(Key.F)) toggleFullScreen(system)
            system.clear()
            renderAppState(state)(system)
            system.redraw()
            if (keyboardInput.isDown(Key.Enter)) {
              system.play(Resources.transition, 1)
              AppState.Transition(state, AppState.startGame(1))
            } else if (keyboardInput.isDown(Key.Escape)) {
              system.play(Resources.transition, 1)
              AppState.Transition(state, AppState.Menu)
            } else state
          }
        case state @ AppState.Transition(from, to, t) =>
          (canvas: Canvas) => {
            canvas.clear()
            renderAppState(state)(canvas)
            canvas.redraw()
            if (t >= Constants.transitionMillis) to
            else AppState.Transition(from, to, t + frameRate.millis.toInt)
          }
      }
      .configure((canvasSettings, AudioPlayer.Settings()), frameRate, AppState.initial)
      .run()
  }
}
