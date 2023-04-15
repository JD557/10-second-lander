package eu.joaocosta.lunar

sealed trait AppState

object AppState {
  val initial = Loading(0, Resources.allResources)
  final case class Loading(loaded: Int, remainingResouces: List[() => Any]) extends AppState
  final case class InGame(player: Player, level: Level)                     extends AppState
}
