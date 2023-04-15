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
    out
      .blit(landerSprite, Some(Color(0, 0, 0)))(player.x.toInt, player.y.toInt)
    frame = frame + 1
    val levelPlane = Plane
      .fromFunction((x, y) => if (y >= level.groundLine(x)) Color(255, 255, 255) else Color(0, 0, 0))
      .toSurfaceView(out.width, out.height)
    out.blit(Resources.pad, Some(Color(0, 0, 0)))(level.padX, level.padY)
    out
      .blit(levelPlane, Some(Color(0, 0, 0)))(0, 0)
  }
}
