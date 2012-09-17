class DeerMartenPatch < RubyPatch  
  Tree_fields = [ :num_2_inch_diameter_trees, :num_4_inch_diameter_trees, 
                  :num_6_inch_diameter_trees,  :num_8_inch_diameter_trees, 
                  :num_10_inch_diameter_trees, :num_12_inch_diameter_trees, 
                  :num_14_inch_diameter_trees, :num_16_inch_diameter_trees, 
                  :num_18_inch_diameter_trees, :num_20_inch_diameter_trees, 
                  :num_22_inch_diameter_trees, :num_24_inch_diameter_trees ]
                  
  Max_vole_population = 13.9
                  
  stm_attr_accessor :vole_population, :marten, :marten_scent_age, :tree_density, 
                    :tree_type, *Tree_fields
                    
  sync_fields :tree_density, :tree_type, *Tree_fields

  def initialize(world, world_id, x, y)
    super world, x, y
    correspondent = ResourceTile.where(:world_id => world_id, :x => x, :y => y).first
    use_correspondent correspondent
    sync_from_db
    
    self.vole_population = Max_vole_population / 2
    self.marten = nil
    self.marten_scent_age = nil
    #puts "Patch (#{self.object_id}) initialized with :num_14_inch_diameter_trees = #{num_14_inch_diameter_trees}" if num_14_inch_diameter_trees > 0
  end

  def tick
    grow_vole_population
    smelly_stuff
    #sync_to_db  #slow to do this every tick
  end

  def grow_vole_population
    self.vole_population *= 1.01
  end

  def smelly_stuff
    unless self.marten_scent_age.nil?
      self.marten_scent_age += 1
      if self.marten_scent_age > 14
        self.marten_scent_age = nil
        self.marten = nil
      end
    end
  end
end
