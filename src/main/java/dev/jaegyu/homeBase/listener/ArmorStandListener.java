package dev.jaegyu.homeBase.listener;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.EulerAngle;

import java.util.List;

public class ArmorStandListener implements Listener {

    private static final List<EulerAngle[]> POSES = List.of(
            pose(0,-15,0,  0,0,0,   -10,0,-10, -15,0,10,  -1,0,-1, 1,0,1),   // default
            pose(0,0,0,    0,0,0,   0,0,0,     0,0,0,     0,0,0,   0,0,0),   // no_pose
            pose(15,0,0,   0,0,2,   -30,15,15, -60,-20,-10, -1,0,-1, 1,0,1), // solemn
            pose(-5,0,0,   0,0,2,   10,0,-5,   -60,20,-10,  -3,-3,-3, 3,3,3),// athena
            pose(-15,0,0,  0,0,-2,  20,0,-10,  -110,50,0,  5,-3,-3, -5,3,3), // brandish
            pose(-15,0,0,  0,0,0,   -110,35,0, -110,-35,0, 5,-3,-3, -5,3,3), // honor
            pose(-15,0,0,  0,0,0,   -110,-35,0,-110,35,0,  5,-3,-3, -5,3,3), // entertain
            pose(0,0,0,    0,0,0,   10,0,-5,   -70,-40,0,  -1,0,-1, 1,0,1),  // salute
            pose(16,20,0,  0,0,0,   4,8,237,   246,0,89,   -14,-18,-16, 8,20,4), // riposte
            pose(-10,0,-5, 0,0,0,   -105,0,0,  -100,0,0,   7,0,0,  -46,0,0), // zombie
            pose(-5,18,0,  0,22,0,  8,0,-114,  0,84,111,   -111,55,0, 0,23,-13),  // cancan_a
            pose(-10,-20,0,0,-18,0, 0,0,-112,  8,90,111,   0,0,13,  -119,-42,0),  // cancan_b
            pose(-4,67,0,  0,8,0,   16,32,-8,  -99,63,0,   0,-75,-8, 4,63,8)  // hero
    );

    private final NamespacedKey poseKey;

    public ArmorStandListener(JavaPlugin plugin) {
        poseKey = new NamespacedKey(plugin, "armor_stand_pose");
    }

    @EventHandler
    public void onArmorStandInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand stand)) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();

        if (!stand.hasArms()) {
            // Must be holding a stick to opt in
            if (held.getType() != Material.STICK) return;
            event.setCancelled(true);
            stand.setArms(true);
            applyPose(stand, 0);
            // Consume one stick
            if (held.getAmount() > 1) {
                held.setAmount(held.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            }
            return;
        }

        // Has arms — shift + right click cycles pose
        if (player.isSneaking()) {
            event.setCancelled(true);
            int current = stand.getPersistentDataContainer()
                    .getOrDefault(poseKey, PersistentDataType.INTEGER, 0);
            int next = (current + 1) % POSES.size();
            applyPose(stand, next);
            return;
        }

        // Not sneaking — fall through to vanilla
    }

    private void applyPose(ArmorStand stand, int index) {
        EulerAngle[] pose = POSES.get(index);
        stand.setHeadPose(pose[0]);
        stand.setBodyPose(pose[1]);
        stand.setLeftArmPose(pose[2]);
        stand.setRightArmPose(pose[3]);
        stand.setLeftLegPose(pose[4]);
        stand.setRightLegPose(pose[5]);
        stand.getPersistentDataContainer().set(poseKey, PersistentDataType.INTEGER, index);
    }

    private static EulerAngle[] pose(
            double hx, double hy, double hz,
            double bx, double by, double bz,
            double lax, double lay, double laz,
            double rax, double ray, double raz,
            double llx, double lly, double llz,
            double rlx, double rly, double rlz) {
        return new EulerAngle[]{
                rad(hx, hy, hz), rad(bx, by, bz),
                rad(lax, lay, laz), rad(rax, ray, raz),
                rad(llx, lly, llz), rad(rlx, rly, rlz)
        };
    }

    private static EulerAngle rad(double x, double y, double z) {
        return new EulerAngle(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
    }
}