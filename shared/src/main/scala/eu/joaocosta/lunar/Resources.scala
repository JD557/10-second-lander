package eu.joaocosta.lunar

import eu.joaocosta.minart.backend.defaults._
import eu.joaocosta.minart.audio.sound._
import eu.joaocosta.minart.graphics.image._
import eu.joaocosta.minart.runtime._

object Resources {
  lazy val lander = SpriteSheet(
    Image.loadQoiImage(Resource("assets/lander.qoi")).get,
    32,
    32
  )
  lazy val numbers = SpriteSheet(
    Image.loadQoiImage(Resource("assets/numbers.qoi")).get,
    16,
    16
  )
  lazy val warnings = SpriteSheet(
    Image.loadQoiImage(Resource("assets/warnings.qoi")).get,
    38,
    12
  )
  lazy val pad = SpriteSheet(
    Image.loadQoiImage(Resource("assets/pad.qoi")).get,
    Constants.padSize,
    8
  )
  lazy val space    = Image.loadQoiImage(Resource("assets/space.qoi")).get
  lazy val hud      = Image.loadQoiImage(Resource("assets/hud.qoi")).get
  lazy val gameover = Image.loadQoiImage(Resource("assets/gameover.qoi")).get
  lazy val menu     = Image.loadQoiImage(Resource("assets/menu.qoi")).get
  lazy val moon     = Image.loadQoiImage(Resource("assets/moon.qoi")).get

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
