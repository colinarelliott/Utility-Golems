# Utility Golems
## "Gotta Gol 'Em All!"
### A mod for Minecraft 1.21.11
![GolemPhotoDay131](https://github.com/user-attachments/assets/171d5301-f636-4839-9d4e-d8e4cd92fa55)


#### Image Legend
- Left to Right, Row 1: Nether Wart, Redstone, Honeycomb, Gold, Stripped Bamboo, Sponge, Emerald, Cactus, Bamboo, Diamond, Lapis, Amethyst
- Left to Right, Row 2: Jukebox, Ancient, Deepslate, Netherite, Furnace, Blast Furnace, Smoker, Lamp, Medic


#### Image Legend
- Left to Right, Row 1: Nether Wart, Redstone, Gold, Stripped Bamboo, Sponge, Emerald, Bamboo, Diamond, Lapis, Amethyst
- Left to Right, Row 2: (item) Wrench, Ancient, Deepslate, Netherite, Furnace, Jukebox, Lamp, (item) Golem Spawn Egg (15 varieties)
  
#### Adds additional golems on top of the copper golem with various uses. So far:
- <code style="color : blue">Lapis Golem</code> (Digging)
  - Give the lapis golem a pickaxe and/or a shovel and it will dig then deposit items in a chest.
  - Digs down in a staircase, cardinal directions from chest. Finds and prioritizes ores.
  - Returns items to chest.
- <code style="color : grey">Netherite Golem</code> (Attack / Defend)
  - Give these golems a sword to increase their attack damage
  - Regardless, these golems will fight any hostile mobs nearby (32 block radius around chest)
  - They will follow you if you hold a sword. Return to area after.
- <code style="color : green">Emerald Golem</code> (Villager Trading Automation)
  - Give the emerald golem items to trade and it will SELL them to villagers.
  - Deposits profit into nearby chests.
  - Select items to BUY once the golem has discovered nearby villagers. Will take emeralds from chest to buy.
- <code style="color : purple">Amethyst Golem</code> (Breeding)
  - Breeds animals for the player using provided wheat or other breeding material.
- <code>Furnace Golem</code> (Smelting)
  - Contains furnace interface, portable.
  - Lights up when smelting.
  - <i>Variant</i>: <code>Blast Furnace Golem</code> (same but with Blast Furnace recipes)
  - <i>Variant</i>: <code>Smoker Golem</code> (same but with Smoker recipes)
- <code style="color : brown">Jukebox Golem</code> (Music Player)
  - Plays music discs and follows the player around
  - Contains a 9-disc playlist with shuffle and repeat options
- <code style="color : yellow">Sponge Golem</code> (Fishing)
  - Takes a fishing rod from the player and continuously fishes, places findings in nearby chest.
- <code style="color : gray">Deepslate Golem</code> (Tree Chopping)
  - Automates tree chopping if given an axe
  - Can also receive shears for getting leaves
  - Replants saplings in a 64 block radius around its chest.
- <code style="color : green">Bamboo Golem</code> (Farming)
  - Hoes the ground, plants seeds provided, harvests and deposits crops.
  - Also give them a water bucket if no water is nearby. Crops need hydration!
  - <i>Variant</i>: <code>Stripped Bamboo Golem</code> is available if you use an Axe.
- <code style="color : yellow">Gold Golem</code> (Piglin Trading)
  - Drop gold for piglins, retrieve items in return and put them in chest
- <code style="color : red">Redstone Golem</code> (Timing and Redstone Automation)
  - connect redstone elements when given redstone dust
  - activate redstone elements nearby
- <code style="color : lightblue">Diamond Golem</code> (Building)
  - Takes blocks from a chest and builds a schematic-defined structure in the world.
  - Builds small simple structures, selectable in inventory.
  - Replaces blocks as directed.
- <code style="color : lightgreen">Lamp Golem</code> (Lighting)
  - (Shift+Right Click) Turns on and off a glowing effect, illuminating the surroundings at a light level of 12.
  - Follows the player, making it useful for caving.
  - Give it a torches of any kind to have it place them in dark areas.
- <code style="color : red">Nether Wart Golem</code> (Brewing)
  - Automates potion brewing when given a brewing stand and ingredients.
  - Deposits finished potions into nearby chests.
  - Fills water bottles with water when given glass bottles.
- <code style="color : white">Medic Golem</code> (Healing)
  - Follows the player and automatically heals damaged golems when given a wrench.
  - Essential for maintaining your golem workforce and defenses.
- <code style="color : green">Cactus Golem</code> (Item Trashing)
  - Deletes items from nearby chests that match the items in its own inventory.
  - Useful for removing unwanted "trash" items from automated systems.
  - Tracks the total number of items deleted in its interface.
- <code style="color : orange">Honeycomb Golem</code> (Beekeeping)
  - Automatically harvests honey and honeycomb from nearby bee hives.
  - Needs glass bottles and shears.
  - Deposits harvested items into honeycomb chest.

### Adds a wrench item which heals golems
<img width="183" height="104" alt="Golem Wrench Crafting Recipe" src="https://github.com/user-attachments/assets/d9be85ce-094b-46c0-aa13-9ff1c00b8962" />

- 500 durability, standard tool
