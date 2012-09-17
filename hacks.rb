require 'java'
require 'jruby/scala_support'

java_import 'akka.actor.Props'
java_import 'akka.actor.ActorRef'
java_import 'shapiro.netfauxgo.Agent'
java_import 'shapiro.netfauxgo.Patch'
java_import 'shapiro.netfauxgo.MovableAgent'

class Props
  def self.[](*args, &block)
    options = args.last.is_a?(Hash) && args.pop
    creator = (args.first.is_a?(Class) && args.first) || (options && options[:creator]) || block
    raise ArgumentError, %(No creator specified) unless creator
    props = new
    props = props.with_creator(creator)
    props
  end

  class << self
    alias_method :create, :[]
  end
end

module DataStorage
  def self.included(base)
    base.extend ClassMethods
  end
  
  module ClassMethods
    def stm_attr_accessor(*args)
      args.each do |arg|
        send :define_method, arg.to_s do
          get_property arg.to_s
        end
        
        send :define_method, (arg.to_s + "=") do |new_value|
          set_property arg.to_s, new_value
        end
      end
    end
  end
end

module AgentSpawning
  def spawn(klass)
    new_actor = world.system.actor_of(Props.create { klass.new world })
    world.manager.tell AddAgent.new new_actor
    new_actor
  end
end


class RubyMovableAgent < MovableAgent
  include DataStorage
  include DatabaseSync
  include AgentSpawning
  
  def get_patches_in_neighborhood(radius)
    super(radius).from_scala
  end
  
  def get_other_agents_in_vicinity(radius)
    super(radius).from_scala
  end
end

class RubyPatch < Patch
  include DataStorage
  include DatabaseSync
  include AgentSpawning
end

#class ActorRef
  
  #the below are all commented out because I'm not sure yet how to get the world that corresponds to an ActorRef. 
  
  # def x
  #   actor_data = world.get_actor_data(self.path)
  #   actor_data.get_position[0]
  # end
  
  # def method_missing(name, *args)  #be able to seamlessly look up the properties of other agents
  #   name_string = name.to_s
  #   actor_data = world.get_actor_data(self.path)
  #   if name_string.end_with? "="
  #     if args.length == 1
  #       actor_data.set_property(name_string.slice(0, name_string.length - 1), args.first)
  #     else raise "Value to set missing"
  #     end
  #   else
  #     actor_data.get_property(name_string).get
  #   end
  # end
#end