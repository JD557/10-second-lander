package eu.joaocosta.lunar

sealed trait AppState

object AppState {
  val initial   = Loading(0, Resources.allResources)
  def startGame = AppState.InGame(Player(0, 0, 0, 0), Level.generate(util.Random), 999)
  final case class Loading(loaded: Int, remainingResouces: List[() => Any]) extends AppState
  final case class InGame(player: Player, level: Level, remainingTime: Int) extends AppState {
    val touchedPad =
      player.x >= level.padX && player.x + 48 < level.padX + 128 && player.y + 32 >= level.padY
    val finished = touchedPad && !gameOver
    val gameOver =
      remainingTime <= 0 ||
        (touchedPad && player.vy >= Constants.maxTouchSpeed) ||
        (touchedPad && math.cos(player.rotation) < Constants.minCosRotation) ||
        !touchedPad && player.y + 32 > level.groundLine(player.x)
  }
  final case class GameOver(lastState: InGame) extends AppState
}
