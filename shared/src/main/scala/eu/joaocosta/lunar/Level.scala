package eu.joaocosta.lunar

final case class Level(
    number: Int,
    groundLine: Double => Double,
    padX: Int,
    night: Boolean
) {
  val padY = {
    math
      .min(
        groundLine(padX),
        math.min(groundLine(padX + Constants.padSize / 2), groundLine(padX + Constants.padSize))
      )
      .toInt - 8
  }
}

object Level {
  def generate(number: Int, random: util.Random) = {
    val (hills, dificultyMod, night) = number match {
      case n if n <= 2 => (true, 0, false)
      case n if n <= 4 => (false, 0, false)
      case n if n <= 6 => (false, 1, false)
      case n if n <= 9 => (false, 2, false)
      case _           => (false, 2, true)
    }

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
        if (random.nextBoolean) (-padRange to (padRange - Constants.padSize) by 4)
        else (-padRange to (padRange - Constants.padSize) by 4).reverse
      candidates
        .filter(x => x < -64 || x > 64)
        .minBy { x =>
          val thisHeight = (height(x) + height(x + Constants.padSize / 2) + height(x + Constants.padSize)) / 3
          // math.min(thisHeight - minHeight, maxHeight - thisHeight)
          if (hills) thisHeight else -thisHeight
        }
    }
    new Level(number, height, padX, night)
  }
}
