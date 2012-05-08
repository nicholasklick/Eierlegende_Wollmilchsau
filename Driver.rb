require 'java'

require 'akka'
java_import 'akka.actor.ActorSystem'
java_import 'shapiro.netfauxgo.SpacerAgent'
java_import 'shapiro.netfauxgo.AddAgent'
java_import 'shapiro.netfauxgo.MovableAgent'

java_import 'Driver'   #this will spawn a world
world = Driver.world
actor_system = Driver.system

class Circler < MovableAgent
  def self.create(*args)
    self.new(*args)
  end
  
  def tick
    forward 2
    turn_right 2
  end
end

(world.width * world.height / 2).times do
  circler_props = Props.create { Circler.new world }
  circler_actor = actor_system.actor_of(circler_props)
  world.manager.tell AddAgent.new circler_actor
end
