package eu.joaocosta.lunar

final case class Level(
    number: Int,
    groundLine: Double => Double,
    padX: Int
) {
  val padY = groundLine(padX).toInt - 8
}

object Level {
  def generate(number: Int, random: util.Random) = {
    val dificultyMod = math.min(number / 4, 2).toInt

    val padRange = 256 + 64 * dificultyMod

    val lowFreq      = 64 + random.nextInt(16)
    val lowFreqPhase = random.nextDouble() * 2 * math.Pi
    val lowFreqAmp   = 30 + 10 * dificultyMod
    val highFreq     = 16 + random.nextInt(16)
    val highFreqAmp  = 10 + 10 * dificultyMod

    val maxDelta    = (lowFreqAmp + highFreqAmp) / 2 + 5
    val constHeight = 250
    val minHeight   = constHeight - maxDelta
    val maxHeight   = constHeight + maxDelta

    val height = { (x: Double) =>
      val raw = lowFreqAmp * math.sin(x / lowFreq + lowFreqPhase) + highFreqAmp * math.sin(x / highFreq) + constHeight
      math.max(minHeight, math.min(raw, maxHeight))
    }
    val padX = {
      val candidates =
        if (random.nextBoolean) (-padRange to (padRange - 32) by 8)
        else (-padRange to (padRange - 32) by 8).reverse
      candidates.minBy { x =>
        val thisHeight = (height(x) + height(x + 32)) / 2
        math.min(thisHeight - minHeight, maxHeight - thisHeight)
      }
    }
    new Level(number, height, padX)
  }
}
