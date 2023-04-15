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
  def renderLevel(player: Player, level: Level)(out: MutableSurface): Unit = {
    out.blit(backgroundPlane.toSurfaceView(out.width, out.height))(0, 0)

    val landerSprite = Plane
      .fromSurfaceWithFallback(Resources.lander.getSprite(if (!player.thrusters) 0 else 1 + frame % 2), Color(0, 0, 0))
      .translate(-16, -16)
      .rotate(player.rotation)
      .translate(24, 24)
      .toSurfaceView(48, 48)
    val fixedPlayerX = (out.width - landerSprite.width) / 2
    val fixedPlayerY = (out.height - landerSprite.height) / 3
    val cameraX      = (fixedPlayerX - player.x).toInt
    val cameraY      = (fixedPlayerY - player.y).toInt
    out
      .blit(landerSprite, Some(Color(0, 0, 0)))(fixedPlayerX, fixedPlayerY)
    frame = frame + 1

    val levelPlane = Plane
      .fromFunction((x, y) => if (y >= level.groundLine(x)) Color(255, 255, 255) else Color(0, 0, 0))
      .translate(cameraX, cameraY)
      .toSurfaceView(out.width, out.height)
    out.blit(Resources.pad, Some(Color(0, 0, 0)))(level.padX + cameraX, level.padY + cameraY)
    out
      .blit(levelPlane, Some(Color(0, 0, 0)))(0, 0)
  }

  def renderGameOver(lastState: AppState.InGame)(out: MutableSurface): Unit = {
    val buffer = new RamSurface(out.width, out.height, Color(0, 0, 0))
    def blur(surface: SurfaceView): SurfaceView = surface.coflatMap { surface =>
      val p1 = surface.getPixelOrElse(0, 0, Color(0, 0, 0))
      val p2 = surface.getPixelOrElse(0, 1, Color(0, 0, 0))
      val p3 = surface.getPixelOrElse(1, 0, Color(0, 0, 0))
      val p4 = surface.getPixelOrElse(1, 1, Color(0, 0, 0))
      Color(
        p1.r / 4 + p2.r / 4 + p3.r / 4 + p4.r / 4,
        p1.g / 4 + p2.g / 4 + p3.g / 4 + p4.g / 4,
        p1.b / 4 + p2.b / 4 + p3.b / 4 + p4.b / 4
      )
    }
    renderLevel(lastState.player, lastState.level)(buffer)
    val blurred = blur(blur(buffer.view).precompute)
    out.blit(blurred)(0, 0)
  }
}
