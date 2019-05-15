package gvlfm78.plugin.OldCombatMechanics.utilities;

import gvlfm78.plugin.OldCombatMechanics.utilities.potions.GenericPotionDurations;
import gvlfm78.plugin.OldCombatMechanics.utilities.potions.PotionDurations;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Various utilities for making it easier to work with {@link org.bukkit.configuration.Configuration Configurations}.
 *
 * @author Rayzr522
 * @see org.bukkit.configuration.file.YamlConfiguration
 * @see org.bukkit.configuration.ConfigurationSection
 */
public class ConfigUtils {

    /**
     * Safely loads all doubles from a configuration section, reading both double and integer values.
     *
     * @param section The section from which to load the doubles.
     * @return The map of doubles.
     */
    public static Map<String, Double> loadDoubleMap(ConfigurationSection section){
        Objects.requireNonNull(section, "section cannot be null!");

        return section.getKeys(false).stream()
                .filter(((Predicate<String>) section::isDouble).or(section::isInt))
                .collect(Collectors.toMap(key -> key, section::getDouble));
    }

    /**
     * Loads the list of {@link Material Materials} with the given key from a configuration section.
     * Safely ignores non-matching materials.
     *
     * @param section The section from which to load the material list.
     * @param key     The key of the material list.
     * @return The loaded material list, or an empty list if there is no list at the given key.
     */
    public static List<Material> loadMaterialList(ConfigurationSection section, String key){
        Objects.requireNonNull(section, "section cannot be null!");
        Objects.requireNonNull(key, "key cannot be null!");

        if(!section.isList(key)){
            return new ArrayList<>();
        }

        return section.getStringList(key).stream()
                .map(Material::matchMaterial)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Gets potion duration values from config
     * @param section The section from which to load the duration values
     * @return HashMap of PotionType and PotionDurations
     */
    public static HashMap<PotionType, PotionDurations> loadPotionDurationsList(ConfigurationSection section){
        Objects.requireNonNull(section, "section cannot be null!");
        HashMap<PotionType, PotionDurations> durationsHashMap = new HashMap<>();
        ConfigurationSection durationsSection = section.getConfigurationSection("potion-durations");

            for (String potionName : durationsSection.getKeys(false)) {
                ConfigurationSection potionSection = durationsSection.getConfigurationSection(potionName);
                ConfigurationSection drinkable = potionSection.getConfigurationSection("drinkable");
                ConfigurationSection splash = potionSection.getConfigurationSection("splash");

                potionName = potionName.toUpperCase(Locale.ROOT);

                try {
                    PotionType potionType = PotionType.valueOf(potionName);
                    durationsHashMap.put(potionType, new PotionDurations(getGenericDurations(drinkable), getGenericDurations(splash)));

                } catch (IllegalArgumentException e){ //In case the potion doesn't exist in the version running on the server
                    Messenger.debug("Skipping loading " + potionName + " potion");
                }
            }

        return durationsHashMap;
    }

    private static GenericPotionDurations getGenericDurations(ConfigurationSection section){
        return new GenericPotionDurations(section.getInt("base"), section.getInt("II"), section.getInt("extended"));
    }
}
