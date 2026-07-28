package rehdpanda.utilitygolems;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("utility-golems.json").toFile();
    private static final File OLD_CONFIG_FILE = FabricLoader.getInstance().getGameDir().resolve("utility_golems.json").toFile();
    private static GolemConfig config;

    public static class GolemStats {
        /**
         * Max health of the golem.
         */
        public double maxHealth;
        /**
         * Movement speed of the golem.
         */
        public double movementSpeed;
        /**
         * Base attack damage of the golem.
         */
        public double attackDamage = 0.5;
        /**
         * Distance at which the golem will follow its target.
         */
        public double followRange = 16.0;
        /**
         * Resistance to knockback (0.0 to 1.0).
         */
        public double knockbackResistance = 0.0;
        /**
         * Base armor value of the golem.
         */
        public double armor = 0.0;
        /**
         * Base armor toughness of the golem.
         */
        public double armorToughness = 0.0;
        /**
         * Attack speed of the golem.
         */
        public double attackSpeed = 1.0;
        /**
         * Strength of the golem's attack knockback.
         */
        public double attackKnockback = 0.0;
        /**
         * Radius in blocks within which the golem performs its primary work tasks.
         */
        public int workRadius = 16;
        /**
         * Radius in blocks within which the golem searches for chests to interact with.
         */
        public int chestSearchRadius = 16;

        public GolemStats() {}

        public GolemStats(double maxHealth, double movementSpeed) {
            this.maxHealth = maxHealth;
            this.movementSpeed = movementSpeed;
        }

        public GolemStats(double maxHealth, double movementSpeed, int workRadius) {
            this(maxHealth, movementSpeed);
            this.workRadius = workRadius;
        }

        public GolemStats(double maxHealth, double movementSpeed, double attackDamage) {
            this(maxHealth, movementSpeed);
            this.attackDamage = attackDamage;
        }
    }

    public static class GolemConfig {
        public Map<String, GolemStats> golems = new HashMap<>();

        public void initDefaults() {
            GolemStats lapis = new GolemStats(20.0, 0.25, 32);
            lapis.chestSearchRadius = 16;
            golems.put("lapis_golem", lapis);
            
            GolemStats redstone = new GolemStats(5.0, 0.3);
            redstone.workRadius = 16;
            redstone.chestSearchRadius = 16;
            golems.put("redstone_golem", redstone);
            
            GolemStats emerald = new GolemStats(30.0, 0.3, 2.0);
            emerald.followRange = 16.0;
            emerald.knockbackResistance = 0.5;
            emerald.armor = 2.0;
            emerald.armorToughness = 1.0;
            emerald.attackSpeed = 1.0;
            emerald.attackKnockback = 0.2;
            emerald.workRadius = 16;
            emerald.chestSearchRadius = 16;
            golems.put("emerald_golem", emerald);
            
            GolemStats gold = new GolemStats(20.0, 0.3);
            gold.workRadius = 16;
            gold.chestSearchRadius = 16;
            golems.put("gold_golem", gold);

            GolemStats amethyst = new GolemStats(15.0, 0.3);
            amethyst.workRadius = 16;
            amethyst.chestSearchRadius = 16;
            golems.put("amethyst_golem", amethyst);

            GolemStats netherite = new GolemStats(80.0, 0.3, 0.5);
            netherite.workRadius = 32;
            netherite.chestSearchRadius = 32;
            golems.put("netherite_golem", netherite);
            
            GolemStats ancient = new GolemStats(80.0, 0.3, 0.5);
            ancient.armor = 1.0;
            ancient.armorToughness = 0.5;
            ancient.workRadius = 32;
            ancient.chestSearchRadius = 32;
            golems.put("ancient_golem", ancient);
            
            GolemStats furnace = new GolemStats(40.0, 0.3, 0.5);
            furnace.workRadius = 16;
            furnace.chestSearchRadius = 16;
            golems.put("furnace_golem", furnace);
            
            GolemStats bamboo = new GolemStats(40.0, 0.3, 0.5);
            bamboo.workRadius = 8;
            bamboo.chestSearchRadius = 32;
            golems.put("bamboo_golem", bamboo);
            
            GolemStats diamond = new GolemStats(40.0, 0.2, 0.5);
            diamond.workRadius = 16;
            diamond.chestSearchRadius = 16;
            golems.put("diamond_golem", diamond);
            
            GolemStats sponge = new GolemStats(40.0, 0.3, 0.5);
            sponge.workRadius = 32;
            sponge.chestSearchRadius = 32;
            golems.put("sponge_golem", sponge);
            
            GolemStats deepslate = new GolemStats(40.0, 0.3, 0.5);
            deepslate.workRadius = 32;
            deepslate.chestSearchRadius = 32;
            golems.put("deepslate_golem", deepslate);
            
            GolemStats jukebox = new GolemStats(40.0, 0.3, 0.5);
            jukebox.workRadius = 16;
            jukebox.chestSearchRadius = 16;
            golems.put("jukebox_golem", jukebox);

            GolemStats lamp = new GolemStats(40.0, 0.3, 0.5);
            lamp.workRadius = 16;
            lamp.chestSearchRadius = 16;
            golems.put("lamp_golem", lamp);
            
            GolemStats netherWart = new GolemStats(40.0, 0.3, 0.5);
            netherWart.workRadius = 32;
            netherWart.chestSearchRadius = 32;
            golems.put("nether_wart_golem", netherWart);

            GolemStats smoker = new GolemStats(40.0, 0.3, 0.5);
            smoker.workRadius = 16;
            smoker.chestSearchRadius = 16;
            golems.put("smoker_golem", smoker);

            GolemStats blastFurnace = new GolemStats(40.0, 0.3, 0.5);
            blastFurnace.workRadius = 16;
            blastFurnace.chestSearchRadius = 16;
            golems.put("blast_furnace_golem", blastFurnace);

            GolemStats medic = new GolemStats(40.0, 0.3, 0.5);
            medic.workRadius = 16;
            medic.chestSearchRadius = 16;
            golems.put("medic_golem", medic);

            GolemStats cactus = new GolemStats(20.0, 0.25, 1.0);
            cactus.workRadius = 16;
            cactus.chestSearchRadius = 16;
            golems.put("cactus_golem", cactus);

            GolemStats hopper = new GolemStats(20.0, 0.25, 1.0);
            hopper.workRadius = 16;
            hopper.chestSearchRadius = 16;
            golems.put("hopper_golem", hopper);

            GolemStats honeycomb = new GolemStats(20.0, 0.25);
            honeycomb.workRadius = 16;
            honeycomb.chestSearchRadius = 16;
            golems.put("honeycomb_golem", honeycomb);

            GolemStats tintedGlass = new GolemStats(20.0, 0.25);
            tintedGlass.workRadius = 16;
            tintedGlass.chestSearchRadius = 16;
            golems.put("tinted_glass_golem", tintedGlass);
        }
    }

    public static void load() {
        if (!CONFIG_FILE.exists() && OLD_CONFIG_FILE.exists()) {
            UGInit.LOGGER.info("Migrating old config file to new location");
            if (OLD_CONFIG_FILE.renameTo(CONFIG_FILE)) {
                UGInit.LOGGER.info("Successfully migrated config file");
            } else {
                UGInit.LOGGER.error("Failed to migrate config file");
            }
        }

        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, GolemConfig.class);
            } catch (Exception e) {
                UGInit.LOGGER.error("Failed to load config, using defaults", e);
                config = new GolemConfig();
                config.initDefaults();
            }
        } else {
            config = new GolemConfig();
            config.initDefaults();
            save();
        }
        
        // Ensure all golems have entries if new ones were added in code but not in existing config file
        boolean needsResave = false;
        GolemConfig defaults = new GolemConfig();
        defaults.initDefaults();
        
        // If config was loaded from file, we might need to fill in missing fields for existing golems
        if (config != null) {
            for (String key : defaults.golems.keySet()) {
                if (!config.golems.containsKey(key)) {
                    config.golems.put(key, defaults.golems.get(key));
                    needsResave = true;
                } else {
                    // Even if the golem key exists, it might be missing fields added in later versions (like workRadius)
                    // Since we've already loaded it into memory, Gson should have populated missing fields with 0 or class defaults.
                    // By setting needsResave to true once, we ensure the file is updated with ALL current fields.
                    needsResave = true;
                }
            }
        }

        if (needsResave) {
            save();
        }
    }

    public static void save() {
        File configDir = CONFIG_FILE.getParentFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            UGInit.LOGGER.error("Failed to save config", e);
        }
    }

    public static GolemConfig getConfig() {
        if (config == null) {
            load();
        }
        return config;
    }
}
