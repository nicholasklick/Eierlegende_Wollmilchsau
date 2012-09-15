require 'yaml'
require 'active_record'

dbconfig = YAML.load(File.read('config/database.yml'))
ActiveRecord::Base.establish_connection dbconfig["development"]

require 'db_models/world'
require 'db_models/megatile'
require 'db_models/resource_tile'

