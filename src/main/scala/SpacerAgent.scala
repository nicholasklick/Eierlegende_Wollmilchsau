class SpacerAgent(world:World) extends MovableAgent(world) {

  def tick() = {
    if (getOtherAgentsInVicinity(1).length > 1){
      forward(1)
      turn_left(1)    
    }
  }  
}