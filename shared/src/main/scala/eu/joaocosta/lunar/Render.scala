package eu.joaocosta.lunar

import eu.joaocosta.minart.graphics._

object Render {
  lazy val backgroundPlane = Resources.space.view.repeating

  def renderLoading(progress: Double)(out: MutableSurface): Unit = {
    out.fillRegion(
      10,
      Constants.canvasHeight - 20,
      Constants.canvasWidth - 20,
      10,
      Color(255, 255, 255)
    )
    out.fillRegion(
      10 + 2,
      Constants.canvasHeight - 20 + 2,
      Constants.canvasWidth - 20 - 4,
      10 - 4,
      Color(0, 0, 0)
    )
    out.fillRegion(
      10 + 3,
      Constants.canvasHeight - 20 + 3,
      (progress * (Constants.canvasWidth - 20 - 6)).toInt,
      10 - 6,
      Color(255, 255, 255)
    )
  }

  private var frame = 0
  def renderMenu(out: MutableSurface): Unit = {
    out.blit(backgroundPlane.clip(frame, 0, out.width, out.height))(0, 0)
    out.blit(Resources.menu, Some(Color(0, 0, 0)))(0, 0)
    frame = frame + 1
  }

  def renderLevel(player: Player, level: Level)(out: MutableSurface): Unit = {
    val distanceX = math.abs(player.x - level.padX)
    val distanceY = math.abs(player.y - level.padY)
    val relDistX = distanceX / (out.width / 2)
    val relDistY = distanceY / (out.height / 2)
    val scale = 1.0 / math.max(Constants.minRelDist, math.min(math.max(relDistX, relDistY), Constants.maxRelDist))
    renderLevelScaled(player, level, scale)(out)
  }


  def renderLevelScaled(player: Player, level: Level, scale: Double = 1.0)(out: MutableSurface): Unit = {
    val buffer = new RamSurface(1 + (out.width / scale).toInt, 1 + (out.height / scale).toInt, Color(0, 0, 0))
    val landerSprite = Plane
      .fromSurfaceWithFallback(Resources.lander.getSprite(if (!player.thrusters) 0 else 1 + frame % 2), Color(0, 0, 0))
      .translate(-16, -16)
      .rotate(player.rotation)
      .translate(23, 23)
      .toSurfaceView(46, 46)
    val fixedPlayerX = (buffer.width - landerSprite.width) / 2
    val fixedPlayerY = (buffer.height - landerSprite.height) / 3
    val cameraX      = (fixedPlayerX - player.x).toInt
    val cameraY      = (fixedPlayerY - player.y).toInt

    buffer
      .blit(landerSprite, Some(Color(0, 0, 0)))(fixedPlayerX.toInt, fixedPlayerY.toInt)
    frame = frame + 1

    val levelPlane = Resources.moon.view.repeating
      .flatMap((c: Color) => (x: Int, y: Int) => if (y >= level.groundLine(x)) c else Color(0, 0, 0))
      .clip(-cameraX, -cameraY, buffer.width, buffer.height)
    buffer.blit(Resources.pad, Some(Color(0, 0, 0)))(level.padX + cameraX, level.padY + cameraY)
    buffer
      .blit(levelPlane, Some(Color(0, 0, 0)))(0, 0)

    out.blit(backgroundPlane.clip(-cameraX/8, -cameraY/8, out.width, out.height))(0, 0)
    out.blit(buffer.view.scale(scale), Some(Color(0, 0, 0)))(0, 0)
  }


  def renderHud(remainingTime: Int, speedWarning: Boolean, angleWarning: Boolean)(out: MutableSurface): Unit = {
    out.blit(Resources.hud, Some(Color(0, 0, 0)))(0, 0)
    val sec       = remainingTime / 100
    val dsec      = (remainingTime / 10) % 10
    val csec      = remainingTime        % 10
    val textStart = (out.width - 64) / 2
    out.blit(Resources.numbers.getSprite(sec + 1), Some(Color(0, 0, 0)))(textStart + 0, 16)
    out.blit(Resources.numbers.getSprite(0), Some(Color(0, 0, 0)))(textStart + 16, 16)
    out.blit(Resources.numbers.getSprite(dsec + 1), Some(Color(0, 0, 0)))(textStart + 32, 16)
    out.blit(Resources.numbers.getSprite(csec + 1), Some(Color(0, 0, 0)))(textStart + 48, 16)
    out.blit(Resources.warnings.getSprite(if (speedWarning) 1 else 0, 1), Some(Color(0, 0, 0)))(432, 227)
    out.blit(Resources.warnings.getSprite(if (angleWarning) 1 else 0, 0), Some(Color(0, 0, 0)))(432, 243)
  }

  def renderGameOver(lastState: AppState.InGame)(out: MutableSurface): Unit = {
    val buffer = new RamSurface(out.width, out.height, Color(0, 0, 0))
    renderLevel(lastState.player, lastState.level)(buffer)
    out.blit(buffer.view.map(c => 
        val gray = math.max(math.max(c.r, c.g), c.b)
        Color.grayscale(gray / 16)
      ))(0, 0)
    renderHud(lastState.remainingTime, lastState.landingSpeedExceeded, lastState.overRotated)(out)
    val glitchLine = util.Random.nextInt(Resources.gameover.height)
    val glitchDelta = util.Random.nextInt(10)-5
    val glitchedGameOver = Resources.gameover.view.contramap { (x, y) => 
      if (y == glitchLine) (x - glitchDelta, y)
      else (x, y)
    }.toSurfaceView(Resources.gameover.width, Resources.gameover.height)
    out.blit(glitchedGameOver, Some(Color(0, 0, 0)))((out.width - Resources.gameover.width) / 2, out.height / 2)
  }
}
