package shapiro.netfauxgo

abstract class PatchSpawner{
	def spawnPatch(world:World, x:Int, y:Int):Patch;
}

class DefaultPatchSpawner extends PatchSpawner {
	def spawnPatch(world:World, x:Int, y:Int) = {
		new Patch(world, x, y)
	}
}