package gvlfm78.plugin.OldCombatMechanics.versions.materials;

import gvlfm78.plugin.OldCombatMechanics.utilities.reflection.Reflector;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.function.Supplier;

/**
 * A material that has a version for before 1.13 and after 1.13.
 */
public class DualVersionedMaterial implements VersionedMaterial {

    private Supplier<ItemStack> oldItem;
    private Supplier<ItemStack> newItem;

    /**
     * Creates a new dual versioned material.
     *
     * @param oldItem the item supplier for the old version
     * @param newItem the item supplier for the new version
     */
    public DualVersionedMaterial(Supplier<ItemStack> oldItem, Supplier<ItemStack> newItem) {
        this.oldItem = oldItem;
        this.newItem = newItem;
    }

    /**
     * Returns a new {@link DualVersionedMaterial} based on the material names.
     *
     * @param nameOld the old name
     * @param nameNew the new name
     * @return a dual versioned material using the supplied names
     */
    public static VersionedMaterial ofMaterialNames(String nameOld, String nameNew) {
        return new DualVersionedMaterial(fromMaterial(nameOld), fromMaterial(nameNew));
    }

    private static Supplier<ItemStack> fromMaterial(String name) {
        return () -> new ItemStack(Material.matchMaterial(name));
    }

    @Override
    public ItemStack newInstance() {
        return getItemSupplier().get();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isSame(ItemStack other) {
        ItemStack baseInstance = newInstance();

        // items do not differ in more than those two things
        return baseInstance.getType() == other.getType() && baseInstance.getDurability() == other.getDurability();
    }

    private Supplier<ItemStack> getItemSupplier() {
        return Reflector.versionIsNewerOrEqualAs(1, 13, 0) ? newItem : oldItem;
    }

    @Override
    public String toString() {
        return "DualVersionedMaterial{" +
          "picked=" + (getItemSupplier() == newItem ? "new" : "old")
          + ", item=" + newInstance()
          + '}';
    }
}
