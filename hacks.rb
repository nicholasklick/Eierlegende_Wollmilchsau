require 'java'
java_import 'akka.actor.Props'
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

class MovableAgent
  def self.create(*args)
    self.new(*args)
  end
end