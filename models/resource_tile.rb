class ResourceTile < ActiveRecord::Base
  belongs_to :megatile
  belongs_to :world
end

class LandTile < ResourceTile 
end

class WaterTile < ResourceTile 
end
  