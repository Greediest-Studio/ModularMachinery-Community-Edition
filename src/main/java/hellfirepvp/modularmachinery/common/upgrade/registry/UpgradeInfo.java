package hellfirepvp.modularmachinery.common.upgrade.registry;

import hellfirepvp.modularmachinery.common.upgrade.MachineUpgrade;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpgradeInfo {
    private final List<ItemStack>      matches  = new ArrayList<>();
    private final List<MachineUpgrade> upgrades = new ArrayList<>();
    private final Map<Integer, List<MachineUpgrade>> fixedUpgrades = new HashMap<>();

    public UpgradeInfo(List<ItemStack> matches) {
        this.matches.addAll(matches);
    }

    public UpgradeInfo() {

    }

    public boolean matches(ItemStack stack) {
        for (final ItemStack match : matches) {
            if (ItemStack.areItemsEqual(match, stack)) {
                return true;
            }
        }

        return false;
    }

    public UpgradeInfo addMatch(ItemStack match) {
        matches.add(match);
        return this;
    }

    public UpgradeInfo addUpgrade(MachineUpgrade upgrade) {
        upgrades.add(upgrade);
        return this;
    }

    public UpgradeInfo addFixedUpgrade(ItemStack match, MachineUpgrade upgrade) {
        addMatch(match);
        fixedUpgrades.computeIfAbsent(match.getItemDamage(), key -> new ArrayList<>()).add(upgrade);
        return this;
    }

    public List<MachineUpgrade> getUpgrades(ItemStack stack) {
        List<MachineUpgrade> matched = new ArrayList<>(upgrades);
        List<MachineUpgrade> fixed = fixedUpgrades.get(stack.getItemDamage());
        if (fixed != null) {
            matched.addAll(fixed);
        }
        return matched;
    }

    public List<ItemStack> getMatches() {
        return matches;
    }

    public List<MachineUpgrade> getUpgrades() {
        return upgrades;
    }
}
