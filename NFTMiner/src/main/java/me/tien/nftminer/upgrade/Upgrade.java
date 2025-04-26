package me.tien.nftminer.upgrade;

import java.util.UUID;
import org.bukkit.entity.Player;

public interface Upgrade {
    /**
     * Lấy cấp độ nâng cấp hiện tại của người chơi dựa trên UUID.
     *
     * @param uuid UUID của người chơi
     * @return Cấp độ hiện tại
     */
    int getLevel(UUID uuid);

    /**
     * Lấy cấp độ nâng cấp hiện tại của người chơi.
     *
     * @param player Người chơi
     * @return Cấp độ hiện tại
     */
    int getLevel(Player player);

    /**
     * Đặt cấp độ nâng cấp cho người chơi dựa trên UUID.
     *
     * @param uuid UUID của người chơi
     * @param level Cấp độ mới
     */
    void setLevel(UUID uuid, int level);

    /**
     * Đặt cấp độ nâng cấp cho người chơi.
     *
     * @param player Người chơi
     * @param level Cấp độ mới
     */
    void setLevel(Player player, int level);

    /**
     * Áp dụng hiệu ứng nâng cấp cho người chơi.
     *
     * @param player Người chơi
     */
    void applyEffect(Player player);

    /**
     * Lấy chi phí nâng cấp tiếp theo.
     *
     * @param player Người chơi
     * @return Chi phí nâng cấp tiếp theo
     */
    int getNextLevelCost(Player player);

    /**
     * Lấy cấp độ tối đa của nâng cấp.
     *
     * @return Cấp độ tối đa
     */
    int getMaxLevel();

    /**
     * Lấy loại nâng cấp (ví dụ: "InventoryUpgrade").
     *
     * @return Loại nâng cấp
     */
    String getType();

    /**
     * Lưu dữ liệu nâng cấp.
     */
    void saveData();

    /**
     * Lấy hiệu ứng tương ứng với cấp độ.
     *
     * @param level Cấp độ
     * @return Hiệu ứng
     */
    int getEffectLevel(int level);

    /**
     * Tải cấu hình nâng cấp từ file config.
     */
    void loadConfig();

    /**
     * Thực hiện nâng cấp cho người chơi.
     *
     * @param player Người chơi
     * @return `true` nếu nâng cấp thành công, `false` nếu thất bại
     */
    boolean upgrade(Player player);

    /**
     * Tải dữ liệu người chơi từ file config.
     *
     * @param player Người chơi
     */
    void loadPlayerData(Player player);
}