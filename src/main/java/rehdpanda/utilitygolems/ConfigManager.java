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
    private static final File CONFIG_FILE = FabricLoader.getInstance().getGameDir().resolve("utility_golems.json").toFile();
    private static GolemConfig config;

    public static class GolemStats {
        public double maxHealth;
        public double movementSpeed;
        public double attackDamage = 0.5;
        public double followRange = 16.0;
        public double knockbackResistance = 0.0;
        public double armor = 0.0;
        public double armorToughness = 0.0;
        public double attackSpeed = 1.0;
        public double attackKnockback = 0.0;

        public GolemStats() {}

        public GolemStats(double maxHealth, double movementSpeed) {
            this.maxHealth = maxHealth;
            this.movementSpeed = movementSpeed;
        }

        public GolemStats(double maxHealth, double movementSpeed, double attackDamage) {
            this(maxHealth, movementSpeed);
            this.attackDamage = attackDamage;
        }
    }

    public static class GolemConfig {
        public Map<String, GolemStats> golems = new HashMap<>();

        public void initDefaults() {
            golems.put("lapis_golem", new GolemStats(20.0, 0.25));
            golems.put("redstone_golem", new GolemStats(5.0, 0.3));
            
            GolemStats emerald = new GolemStats(30.0, 0.3, 2.0);
            emerald.followRange = 16.0;
            emerald.knockbackResistance = 0.5;
            emerald.armor = 2.0;
            emerald.armorToughness = 1.0;
            emerald.attackSpeed = 1.0;
            emerald.attackKnockback = 0.2;
            golems.put("emerald_golem", emerald);
            
            golems.put("gold_golem", new GolemStats(20.0, 0.3));
            golems.put("amethyst_golem", new GolemStats(15.0, 0.3));
            golems.put("netherite_golem", new GolemStats(80.0, 0.3, 0.5));
            
            GolemStats ancient = new GolemStats(80.0, 0.3, 0.5);
            ancient.armor = 1.0;
            ancient.armorToughness = 0.5;
            golems.put("ancient_golem", ancient);
            
            golems.put("furnace_golem", new GolemStats(40.0, 0.3, 0.5));
            golems.put("bamboo_golem", new GolemStats(40.0, 0.3, 0.5));
            golems.put("diamond_golem", new GolemStats(40.0, 0.2, 0.5));
            golems.put("sponge_golem", new GolemStats(40.0, 0.3, 0.5));
            golems.put("deepslate_golem", new GolemStats(40.0, 0.3, 0.5));
            golems.put("jukebox_golem", new GolemStats(40.0, 0.3, 0.5));
            golems.put("lamp_golem", new GolemStats(40.0, 0.3, 0.5));
            golems.put("nether_wart_golem", new GolemStats(40.0, 0.3, 0.5));
            golems.put("smoker_golem", new GolemStats(40.0, 0.3, 0.5));
            golems.put("blast_furnace_golem", new GolemStats(40.0, 0.3, 0.5));
            golems.put("medic_golem", new GolemStats(40.0, 0.3, 0.5));
            golems.put("cactus_golem", new GolemStats(20.0, 0.25, 1.0));
            golems.put("hopper_golem", new GolemStats(20.0, 0.25, 1.0));
            golems.put("honeycomb_golem", new GolemStats(20.0, 0.25));
        }
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                config = GSON.fromJson(reader, GolemConfig.class);
            } catch (IOException e) {
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
        for (String key : defaults.golems.keySet()) {
            if (!config.golems.containsKey(key)) {
                config.golems.put(key, defaults.golems.get(key));
                needsResave = true;
            }
        }
        if (needsResave) {
            save();
        }
    }

    public static void save() {
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
