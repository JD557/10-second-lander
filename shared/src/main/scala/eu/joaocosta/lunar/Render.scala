package eu.joaocosta.lunar

import scala.collection.mutable

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
    val bufferWidth = 1 + (out.width / scale).toInt
    val bufferHeight = 1 + (out.height / scale).toInt
    val landerSprite = Plane
      .fromSurfaceWithFallback(Resources.lander.getSprite(if (!player.thrusters) 0 else 1 + frame % 2), Color(0, 0, 0))
      .translate(-16, -16)
      .rotate(player.rotation)
      .translate(23, 23)
      .toSurfaceView(46, 46)
    val fixedPlayerX = (bufferWidth - landerSprite.width) / 2
    val fixedPlayerY = (bufferHeight - landerSprite.height) / 3
    val cameraX      = (fixedPlayerX - player.x).toInt
    val cameraY      = (fixedPlayerY - player.y).toInt

    val moonSurfacePlane = 
      if (level.night) Plane.fromFunction { (x, y) =>
        val dx = player.x + 23 - x
        val dy = player.y + 23 - y
        if (dx*dx + dy*dy > (128 * 128)) Color(0, 0, 0)
        else Color(34, 32, 52)
      }
      else (Resources.moon.view.repeating)
    val buffer = moonSurfacePlane
      .clip(-cameraX, -cameraY, bufferWidth, bufferHeight)
      .toRamSurface()
    (0 until buffer.width).foreach { x =>
      val height = level.groundLine(x - cameraX) + cameraY
      buffer.fillRegion(x, 0, 1, math.min(height.toInt, bufferHeight - 1), Color(255, 0, 255))
    }
    buffer.blit(Resources.pad.getSprite(if (level.night) 1 else 0), Some(Color(255, 0, 255)))(level.padX + cameraX, level.padY + cameraY)
    buffer
      .blit(landerSprite, Some(Color(0, 0, 0)))(fixedPlayerX.toInt, fixedPlayerY.toInt)
    frame = frame + 1

    out.blit(backgroundPlane.clip(-cameraX/8, -cameraY/8, out.width, out.height))(0, 0)
    out.blit(buffer.view.scale(scale), Some(Color(255, 0, 255)))(0, 0)
  }


  def renderHud(remainingTime: Int, level: Int, speedWarning: Boolean, angleWarning: Boolean)(out: MutableSurface): Unit = {
    out.blit(Resources.hud, Some(Color(0, 0, 0)))(0, 0)

    val sec       = remainingTime / 100
    val dsec      = (remainingTime / 10) % 10
    val csec      = remainingTime        % 10
    val textStart = (out.width - 64) / 2
    out.blit(Resources.numbers.getSprite(sec + 1), Some(Color(0, 0, 0)))(textStart + 0, 16)
    out.blit(Resources.numbers.getSprite(0), Some(Color(0, 0, 0)))(textStart + 16, 16)
    out.blit(Resources.numbers.getSprite(dsec + 1), Some(Color(0, 0, 0)))(textStart + 32, 16)
    out.blit(Resources.numbers.getSprite(csec + 1), Some(Color(0, 0, 0)))(textStart + 48, 16)

    val levelTenths = level / 10
    val levelUnits = level % 10
    out.blit(Resources.numbers.getSprite(levelTenths + 1), Some(Color(0, 0, 0)))(64, 16)
    out.blit(Resources.numbers.getSprite(levelUnits + 1), Some(Color(0, 0, 0)))(80, 16)

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
    renderHud(lastState.remainingTime, lastState.level.number, lastState.landingSpeedExceeded, lastState.overRotated)(out)
    val glitchLine = util.Random.nextInt(Resources.gameover.height)
    val glitchDelta = util.Random.nextInt(10)-5
    val glitchedGameOver = Resources.gameover.view.contramap { (x, y) => 
      if (y == glitchLine) (x - glitchDelta, y)
      else (x, y)
    }.toSurfaceView(Resources.gameover.width, Resources.gameover.height)
    out.blit(glitchedGameOver, Some(Color(0, 0, 0)))((out.width - Resources.gameover.width) / 2, out.height / 2)
  }
}
