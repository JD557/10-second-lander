package eu.joaocosta.lunar

object Constants {
  val canvasWidth  = 480
  val canvasHeight = 270

  val transitionMillis = 500

  // Multiplied by 2 due to 30 FPS cap
  val rotationSpeed  = 0.05 * 2
  val gravity        = 0.025 * 2
  val thrust         = 0.1 * 2
  val maxTouchSpeed  = 1.25 * 2
  val minCosRotation = 0.90

  val padSize = 64

  val minRelDist = 1.0
  val maxRelDist = 1.5
}
