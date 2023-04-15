package eu.joaocosta.lunar

final case class Level(
    groundLine: Double => Double,
    padX: Int
) {
  val padY = groundLine(padX).toInt - 8
}

object Level {
  def generate(random: util.Random) = {
    val lowFreq      = 64 + random.nextInt(16)
    val lowFreqPhase = random.nextDouble() * 2 * math.Pi
    val highFreq     = 16 + random.nextInt(16)
    val maxDelta     = 25
    val constHeight  = 300
    val minHeight    = constHeight - maxDelta
    val maxHeight    = constHeight + maxDelta
    val height = { (x: Double) =>
      val raw = 30 * math.sin(x / lowFreq + lowFreqPhase) + 10 * math.sin(x / highFreq) + constHeight
      math.max(minHeight, math.min(raw, maxHeight))
    }
    val padX = {
      val candidates =
        if (random.nextBoolean) (-256 to (256 - 32) by 8)
        else (-256 to (256 - 32) by 8).reverse
      candidates.minBy { x =>
        val thisHeight = (height(x) + height(x + 32)) / 2
        math.min(thisHeight - minHeight, maxHeight - thisHeight)
      }
    }
    new Level(height, padX)
  }
}
