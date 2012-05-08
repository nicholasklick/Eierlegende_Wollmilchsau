require 'java'

require 'hacks'
java_import 'akka.actor.ActorSystem'
java_import 'shapiro.netfauxgo.SpacerAgent'
java_import 'shapiro.netfauxgo.AddAgent'

java_import 'Driver'   #this will spawn a world
world = Driver.world
actor_system = Driver.system

class Circler < MovableAgent
  def tick
    forward 2
    turn_right 2
  end
end

class Spacer < MovableAgent
  def tick
    wiggle if get_other_agents_in_vicinity(1).length > 1
  end
  
  def wiggle
    forward 1
    turn_left 1
  end
end

(world.width * world.height / 2).times do
  circler_actor = actor_system.actor_of(Props.create { Spacer.new world })
  world.manager.tell AddAgent.new circler_actor
end
