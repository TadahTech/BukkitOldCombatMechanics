package gvlfm78.plugin.OldCombatMechanics.module;

import gvlfm78.plugin.OldCombatMechanics.OCMMain;
import gvlfm78.plugin.OldCombatMechanics.utilities.Messenger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

import static gvlfm78.plugin.OldCombatMechanics.versions.materials.MaterialRegistry.ENCHANTED_GOLDEN_APPLE;

/**
 * Customize the golden apple effects.
 */
public class ModuleGoldenApple extends Module {

    private List<PotionEffect> enchantedGoldenAppleEffects, goldenAppleEffects;
    private ShapedRecipe enchantedAppleRecipe;

    public ModuleGoldenApple(OCMMain plugin) {
        super(plugin, "old-golden-apples");
    }

    @SuppressWarnings("deprecated")
    @Override
    public void reload() {
        enchantedGoldenAppleEffects = getPotionEffects("napple");
        goldenAppleEffects = getPotionEffects("gapple");

        try {
            enchantedAppleRecipe = new ShapedRecipe(
              new NamespacedKey(plugin, "MINECRAFT"),
              ENCHANTED_GOLDEN_APPLE.newInstance()
            );
        } catch (NoClassDefFoundError e) {
            enchantedAppleRecipe = new ShapedRecipe(ENCHANTED_GOLDEN_APPLE.newInstance());
        }
        enchantedAppleRecipe
          .shape("ggg", "gag", "ggg")
          .setIngredient('g', Material.GOLD_BLOCK)
          .setIngredient('a', Material.APPLE);

        registerCrafting();
    }

    private void registerCrafting() {
        if (isEnabled() && module().getBoolean("enchanted-golden-apple-crafting")) {
            if (Bukkit.getRecipesFor(ENCHANTED_GOLDEN_APPLE.newInstance()).size() > 0) {
                return;
            }
            Bukkit.addRecipe(enchantedAppleRecipe);
            Messenger.debug("Added napple recipe");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareItemCraft(PrepareItemCraftEvent e) {
        if (e.getInventory() == null) {
            return;
        }

        ItemStack item = e.getInventory().getResult();
        if (item == null) {
            return; // This should never ever ever ever run. If it does then you probably screwed something up.
        }

        if (ENCHANTED_GOLDEN_APPLE.isSame(item)) {

            World world = e.getView().getPlayer().getWorld();

            if (isSettingEnabled("no-conflict-mode")) {
                return;
            }

            if (!isEnabled(world)) {
                e.getInventory().setResult(null);
            } else if (isEnabled(world) && !isSettingEnabled("enchanted-golden-apple-crafting")) {
                e.getInventory().setResult(null);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemConsume(PlayerItemConsumeEvent e) {
        if (e.getItem() == null) {
            return;
        }

        if (e.getItem().getType() != Material.GOLDEN_APPLE && !ENCHANTED_GOLDEN_APPLE.isSame(e.getItem())) {
            return;
        }

        if (!isEnabled(e.getPlayer().getWorld()) || !isSettingEnabled("old-potion-effects")) {
            return;
        }

        e.setCancelled(true);

        ItemStack originalItem = e.getItem();

        ItemStack item = e.getItem();

        Player p = e.getPlayer();
        PlayerInventory inv = p.getInventory();

        //Hunger level
        int foodLevel = p.getFoodLevel();
        foodLevel = foodLevel + 4 > 20 ? 20 : foodLevel + 4;

        item.setAmount(item.getAmount() - 1);

        p.setFoodLevel(foodLevel);

        // Saturation
        // Gapple and Napple saturation is 9.6
        float saturation = p.getSaturation() + 9.6f;
        // "The total saturation never gets higher than the total number of hunger points"
        if (saturation > foodLevel) {
            saturation = foodLevel;
        }

        p.setSaturation(saturation);

        if (ENCHANTED_GOLDEN_APPLE.isSame(item)) {
            applyEffects(p, enchantedGoldenAppleEffects);
        } else {
            applyEffects(p, goldenAppleEffects);
        }

        if (item.getAmount() <= 0) {
            item = null;
        }

        ItemStack mainHand = inv.getItemInMainHand();
        ItemStack offHand = inv.getItemInOffHand();

        if (mainHand.equals(originalItem)) {
            inv.setItemInMainHand(item);
        } else if (offHand.equals(originalItem)) {
            inv.setItemInOffHand(item);
        } else if (mainHand.getType() == Material.GOLDEN_APPLE || ENCHANTED_GOLDEN_APPLE.isSame(mainHand)) {
            inv.setItemInMainHand(item);
        }
        // The bug occurs here, so we must check which hand has the apples
        // A player can't eat food in the offhand if there is any in the main hand
        // On this principle if there are gapples in the mainhand it must be that one, else it's the offhand
    }

    private List<PotionEffect> getPotionEffects(String apple) {
        List<PotionEffect> appleEffects = new ArrayList<>();

        ConfigurationSection sect = module().getConfigurationSection(apple + "-effects");
        for (String key : sect.getKeys(false)) {
            int duration = sect.getInt(key + ".duration");
            int amplifier = sect.getInt(key + ".amplifier");

            PotionEffectType type = PotionEffectType.getByName(key);
            Objects.requireNonNull(type, String.format("Invalid potion effect type '%s'!", key));

            PotionEffect fx = new PotionEffect(type, duration, amplifier);
            appleEffects.add(fx);
        }
        return appleEffects;
    }

    private void applyEffects(LivingEntity target, List<PotionEffect> effects) {
        for (PotionEffect effect : effects) {
            OptionalInt maxActiveAmplifier = target.getActivePotionEffects().stream()
              .filter(potionEffect -> potionEffect.getType() == effect.getType())
              .mapToInt(PotionEffect::getAmplifier)
              .max();

            // the active one is stronger, so do not apply the weaker one
            if (maxActiveAmplifier.orElse(-1) > effect.getAmplifier()) {
                continue;
            }

            // remove it, as the active one is weaker
            maxActiveAmplifier.ifPresent(ignored -> target.removePotionEffect(effect.getType()));

            target.addPotionEffect(effect);
        }
    }
}
