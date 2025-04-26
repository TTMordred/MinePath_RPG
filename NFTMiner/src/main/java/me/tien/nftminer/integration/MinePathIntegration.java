package me.tien.nftminer.integration;

import me.tien.nftminer.NFTMiner;
import org.bukkit.Bukkit;

import java.math.BigDecimal;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class MinePathIntegration {
    private final NFTMiner plugin;
    private Connection connection;

    public MinePathIntegration(NFTMiner plugin) {
        this.plugin = plugin;
        initConnection();
    }

    private void initConnection() {
        try {
            String host     = plugin.getConfig().getString("minepath.host");
            int    port     = plugin.getConfig().getInt("minepath.port");
            String database = plugin.getConfig().getString("minepath.database");
            String user     = plugin.getConfig().getString("minepath.username");
            String pass     = plugin.getConfig().getString("minepath.password");

            String url = String.format(
                    "jdbc:mysql://%s:%d/%s?useSSL=false&autoReconnect=true&serverTimezone=UTC",
                    host, port, database
            );
            connection = DriverManager.getConnection(url, user, pass);
            plugin.getLogger().info("§a[MinePathIntegration] Đã kết nối đến database minepath thành công.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "[MinePathIntegration] Không thể kết nối đến database minepath", e);
        }
    }

    /** Kiểm tra xem db có sẵn sàng không */
    public boolean isMinePathAvailable() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /** Lấy số token (balance) từ bảng balance theo UUID */
    public BigDecimal getBalance(UUID uuid) {
        if (!isMinePathAvailable()) return BigDecimal.ZERO;
        String sql = "SELECT BALANCE FROM balance WHERE UUID = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("BALANCE");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE,
                    "[MinePathIntegration] Lỗi đọc balance cho " + uuid, e);
        }
        return BigDecimal.ZERO;
    }

    /** Thêm số token vào bảng balance (insert nếu chưa có, update nếu đã có) */
    public boolean addBalance(UUID uuid, BigDecimal amount) {
        if (!isMinePathAvailable() || amount.compareTo(BigDecimal.ZERO) <= 0) return false;
        BigDecimal current = getBalance(uuid);
        BigDecimal updated = current.add(amount);

        try {
            if (current.compareTo(BigDecimal.ZERO) == 0) {
                // insert mới
                String insert = "INSERT INTO balance (UUID, NAME, BALANCE) VALUES (?, ?, ?)";
                try (PreparedStatement ps = connection.prepareStatement(insert)) {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, Bukkit.getOfflinePlayer(uuid).getName());
                    ps.setBigDecimal(3, updated);
                    ps.executeUpdate();
                }
            } else {
                // update
                String update = "UPDATE balance SET BALANCE = ? WHERE UUID = ?";
                try (PreparedStatement ps = connection.prepareStatement(update)) {
                    ps.setBigDecimal(1, updated);
                    ps.setString(2, uuid.toString());
                    ps.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE,
                    "[MinePathIntegration] Lỗi cập nhật balance cho " + uuid, e);
            return false;
        }
    }

    /** Giảm số token trong bảng balance nếu đủ */
    public boolean subtractBalance(UUID uuid, BigDecimal amount) {
        if (!isMinePathAvailable() || amount.compareTo(BigDecimal.ZERO) <= 0) return false;
        BigDecimal current = getBalance(uuid);
        if (current.compareTo(amount) < 0) return false;

        BigDecimal updated = current.subtract(amount);
        String sql = "UPDATE balance SET BALANCE = ? WHERE UUID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBigDecimal(1, updated);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE,
                    "[MinePathIntegration] Lỗi trừ balance cho " + uuid, e);
            return false;
        }
    }
}
