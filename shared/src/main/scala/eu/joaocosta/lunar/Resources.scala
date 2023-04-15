package eu.joaocosta.lunar

import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.graphics.image._
import eu.joaocosta.minart.runtime._

object Resources {
  lazy val lander = SpriteSheet(
    Image.loadBmpImage(Resource("assets/lander.bmp")).get,
    32,
    32
  )
  lazy val pad   = Image.loadBmpImage(Resource("assets/pad.bmp")).get
  lazy val space = Image.loadBmpImage(Resource("assets/space.bmp")).get

  val allResources: List[() => Any] = List(
    () => lander,
    () => pad,
    () => space
  )
}
