java_import 'scala.None'

class DeerMartenPatch < RubyPatch  
  Tree_fields = [ :num_2_inch_diameter_trees, :num_4_inch_diameter_trees, 
                  :num_6_inch_diameter_trees,  :num_8_inch_diameter_trees, 
                  :num_10_inch_diameter_trees, :num_12_inch_diameter_trees, 
                  :num_14_inch_diameter_trees, :num_16_inch_diameter_trees, 
                  :num_18_inch_diameter_trees, :num_20_inch_diameter_trees, 
                  :num_22_inch_diameter_trees, :num_24_inch_diameter_trees ]
                  
  Max_vole_population = 13.9
  Land_cover_class_codes_that_can_have_voles = [41, 42, 43, 90]

  stm_attr_accessor :vole_population, :marten, :marten_scent_age, :tree_density, 
                    *Tree_fields, :tree_type, :land_cover_class, :landcover_class_code
                    
  sync_fields :tree_density, :tree_type, *Tree_fields, :landcover_class_code

  def initialize(world, world_id, x, y)
    super world, x, y
    correspondent = ResourceTile.where(:world_id => world_id, :x => x, :y => y).first
    use_correspondent correspondent
    sync_from_db
    self.land_cover_class = correspondent.land_cover_type.to_sym
    if Land_cover_class_codes_that_can_have_voles.include? landcover_class_code
      self.vole_population = Max_vole_population 
    else
      self.vole_population = 0
    end
    self.marten = None
    self.marten_scent_age = None
    
    # puts "Initialized #{self.class.to_s} at (#{x},#{y}). It has the following STM vole population: #{vole_population}"
  end

  def tick
    grow_vole_population
    smelly_stuff
  end

  def grow_vole_population
    if Land_cover_class_codes_that_can_have_voles.include? landcover_class_code
      new_pop = self.vole_population * 1.1 + 0.5
      self.vole_population = [new_pop, Max_vole_population].min
    end
  end

  def smelly_stuff
    unless self.marten_scent_age.nil? or self.marten_scent_age == None
      self.marten_scent_age += 1
      if self.marten_scent_age > 7
        self.marten_scent_age = None
        self.marten = None
      end
    end
  end
  
  COLORS = { :deciduous        => [ 93, 169, 109],
             :cultivated_crops => [184, 110,  52],
             :forested_wetland => [180, 216, 235],
             :mixed            => [188, 203, 152],
             :coniferous       => [  0, 102,  56],
             :developed_open_space => [229, 204, 206],
             :emergent_herbaceous_wetland => [ 96, 166, 191],
             :pasture_hay => [225, 214, 88],
             :developed_low_intensity => [231, 148, 131],
             :open_water => [ 56, 112, 159],
             :developed_high_intensity => [186,  0,  5],
             :barren => [180, 175, 165],
             :developed_medium_intensity => [255,  0,  8],
             :grassland_herbaceous => [238, 235, 208],
             :shrub_scrub => [214, 185, 136],
             :excluded => [0, 0, 0]}
               
end
