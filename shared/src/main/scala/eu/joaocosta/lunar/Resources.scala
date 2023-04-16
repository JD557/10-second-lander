package eu.joaocosta.lunar

import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.audio.sound._
import eu.joaocosta.minart.graphics.image._
import eu.joaocosta.minart.runtime._

object Resources {
  lazy val lander = SpriteSheet(
    Image.loadBmpImage(Resource("assets/lander.bmp")).get,
    32,
    32
  )
  lazy val numbers = SpriteSheet(
    Image.loadBmpImage(Resource("assets/numbers.bmp")).get,
    16,
    16
  )
  lazy val warnings = SpriteSheet(
    Image.loadBmpImage(Resource("assets/warnings.bmp")).get,
    38,
    12
  )
  lazy val pad = SpriteSheet(
    Image.loadBmpImage(Resource("assets/pad.bmp")).get,
    Constants.padSize,
    8
  )
  lazy val space    = Image.loadBmpImage(Resource("assets/space.bmp")).get
  lazy val hud      = Image.loadBmpImage(Resource("assets/hud.bmp")).get
  lazy val gameover = Image.loadBmpImage(Resource("assets/gameover.bmp")).get
  lazy val menu     = Image.loadBmpImage(Resource("assets/menu.bmp")).get
  lazy val moon     = Image.loadBmpImage(Resource("assets/moon.bmp")).get

  lazy val beep         = Sound.loadWavClip(Resource("assets/beep.wav")).get
  lazy val gameoverBeep = Sound.loadWavClip(Resource("assets/gameover-beep.wav")).get
  lazy val transition   = Sound.loadWavClip(Resource("assets/transition.wav")).get
  lazy val jet          = Sound.loadWavClip(Resource("assets/jet.wav")).get

  val allResources: List[() => Any] = List(
    () => lander,
    () => numbers,
    () => pad,
    () => space,
    () => hud,
    () => gameover,
    () => menu,
    () => moon,
    () => beep,
    () => gameoverBeep,
    () => transition,
    () => jet
  )
}
