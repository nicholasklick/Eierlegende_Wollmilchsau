require 'chunky_png'

java_import 'shapiro.netfauxgo.TickReporter'

class PNGReporter
  def initialize(world, world_id)
    @world = world
    @world_id = world_id
    @width = world.width
    @height = world.height
    @tick_count = 1
    
    @job_id = Time.now.to_i.to_s
    @dir_name = File.join("output", "#{@world_id}", @job_id, "tick_images")
    puts "Writing snapshots to #{@dir_name}"
    FileUtils.mkdir_p(@dir_name)
  end
  
  
  def output_file_name(critter_count)
    file_name = "world_tick_%05d_#{critter_count}.png" % @tick_count 
    File.join(@dir_name, file_name)
  end
  
  def tickComplete(snapshot)
    canvas = ChunkyPNG::Image.new(@width, @height)
    buffer = Array.new(@width) { Array.new(@height) { Hash.new }}
    marten_color = ChunkyPNG::Color(0,255,0)
    number_of_critters = 0
    
    snapshot.values.foreach do |actor_data|
      klass = actor_data.klass
      position = actor_data.get_position
      x, y = position._1, position._2 #unwrap from scala tuple
      if x.nan? or y.nan?
         next
       end
      x = [[0, x.floor].max, @width-1].min
      y = [[0, y.floor].max, @height-1].min
      if klass.include? "Patch"
        vole_population = actor_data.get_property("vole_population")
        scent_age = actor_data.get_property("marten_scent_age")
        if scent_age == None
          scentiness = 0
        else
          scentiness = (255 * (14 - scent_age)/14.0).to_i
        end
        
        voleness = ((vole_population/DeerMartenPatch::Max_vole_population) * 128).floor
        cover_class_sym = actor_data.get_property("land_cover_class").to_sym
        ground_color = ChunkyPNG::Color(*DeerMartenPatch::COLORS[cover_class_sym])
        #ground_color = ChunkyPNG::Color(0,0,0)
        combined_color = ChunkyPNG::Color::interpolate_quick(ChunkyPNG::Color(255,255,255), ground_color, voleness)  
        combined_color = ChunkyPNG::Color::interpolate_quick(ChunkyPNG::Color(72,61,139), combined_color, scentiness)          
        buffer[x][y][:background] = combined_color
      elsif klass.include? "Agent"
        number_of_critters+=1
        energy = [actor_data.get_property("energy"), 0].max
        buffer[x][y][:foreground] = ChunkyPNG::Color::interpolate_quick( marten_color, ChunkyPNG::Color(0,0,0), (128 * energy/Marten::Max_Energy).floor)
      else
        raise "Got an agent of klass #{klass} and I don't know what to do with it!"
      end
    end
    
    @width.times do |x|
      @height.times do |y|
        if buffer[x][y].has_key? :foreground
          canvas[x,y] = ChunkyPNG::Color::interpolate_quick(buffer[x][y][:foreground], buffer[x][y][:background],  200)
        else 
          canvas[x,y] = buffer[x][y][:background]
        end
      end
    end
    canvas.save output_file_name(number_of_critters)
    @tick_count += 1
  end

  
end

# class GraphicalTickReporter 
#   include Cheri::Swing
# 
#   def initialize(world)
#     @world = world
#     @width = world.width
#     @height = world.height
#     
#     @f = swing.frame("Egg Laying Pig") { label 'Tick Reporter ready!!!!!!!'}
#     
#     @f.pack
#     @f.visible = true
#   end
#   
#   def tickComplete(snapshot)
#     puts "There are #{snapshot.size} things in the current snapshot"
#   end
# end