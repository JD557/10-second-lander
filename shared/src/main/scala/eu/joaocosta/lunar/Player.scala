package eu.joaocosta.lunar

case class Player(
    x: Double,
    y: Double,
    vx: Double = 0.0,
    vy: Double = 0.0,
    rotation: Double = 0.0,
    thrusters: Boolean = false
) {
  def rotateCcw = copy(rotation = rotation - Constants.rotationSpeed)
  def rotateCw  = copy(rotation = rotation + Constants.rotationSpeed)
  def thrust    = copy(thrusters = true)
  def stop      = copy(thrusters = false)

  def tick: Player = {
    val (ax, ay) =
      if (thrusters) (math.sin(rotation) * Constants.thrust, -math.cos(rotation) * Constants.thrust) else (0.0, 0.0)
    Player(
      x + vx,
      y + vy,
      vx + ax,
      vy + Constants.gravity + ay,
      rotation,
      thrusters
    )
  }
}
