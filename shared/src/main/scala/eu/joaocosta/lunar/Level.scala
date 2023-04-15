package eu.joaocosta.lunar

final case class Level(
    groundLine: Double => Double,
    padX: Int
) {
  val padY = groundLine(padX).toInt - 8
}
