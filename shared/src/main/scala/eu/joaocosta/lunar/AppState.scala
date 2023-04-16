package eu.joaocosta.lunar

sealed trait AppState

object AppState {
  val initial               = Loading(0, Resources.allResources)
  def startGame(level: Int) = AppState.InGame(Player(0, 0, 0, 0), Level.generate(1, util.Random), 999)
  case object Menu                                                          extends AppState
  final case class Loading(loaded: Int, remainingResouces: List[() => Any]) extends AppState
  final case class InGame(player: Player, level: Level, remainingTime: Int) extends AppState {
    private val playerSize     = 32
    private val fullPlayerSize = 46
    private val playerBorder   = (fullPlayerSize - playerSize) / 2
    private val collisionH     = fullPlayerSize - playerBorder

    val overRotated          = math.cos(player.rotation) < Constants.minCosRotation
    val landingSpeedExceeded = player.vy >= Constants.maxTouchSpeed

    val touchedPadPartial =
      player.y + collisionH >= level.padY &&
        player.x + fullPlayerSize - playerBorder >= level.padX &&
        player.x + playerBorder < level.padX + Constants.padSize
    val touchedPadFull =
      touchedPadPartial &&
        player.x + playerBorder + 5 >= level.padX &&
        player.x + fullPlayerSize - playerBorder - 5 < level.padX + Constants.padSize
    val finished = touchedPadFull && !gameOver
    val gameOver =
      remainingTime <= 0 || (touchedPadPartial && !touchedPadFull) ||
        (touchedPadFull && (landingSpeedExceeded || overRotated)) ||
        (player.y + collisionH) > level.groundLine(player.x + fullPlayerSize / 2)
  }
  final case class GameOver(lastState: InGame)                          extends AppState
  final case class Transition(from: AppState, to: AppState, t: Int = 0) extends AppState
}
