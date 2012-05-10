require 'java'
java_import 'akka.actor.Props'
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

module JunkStorage
  def method_missing(name, *args)
    name_string = name.to_s
    if name_string.end_with? "="
      if args.length == 1
        self.set_junk(name_string.slice(0, name_string.length - 1), args.first)
      else raise "Value to set missing"
      end
    else
      self.get_junk(name_string).get
    end
  end
end

class Agent
  include JunkStorage
end

class Patch
  include JunkStorage
end