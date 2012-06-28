require 'java'
java_import 'akka.actor.Props'
java_import 'akka.actor.ActorRef'
java_import 'shapiro.netfauxgo.Agent'


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
  def method_missing(name, *args)
    name_string = name.to_s
    if name_string.end_with? "="
      if args.length == 1
        self.set_property(name_string.slice(0, name_string.length - 1), args.first)
      else raise "Value to set missing"
      end
    else
      self.get_property(name_string).get
    end
  end  
end


class Agent
  include DataStorage
end

class Patch
  include DataStorage
end

class ActorRef
  
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
end