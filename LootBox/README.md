# NFT Lootbox Plugin

NFT Lootbox là plugin Minecraft cho phép người chơi mua và mở các lootbox để nhận NFT từ NFT Plugin.

## Tính năng

- Hệ thống lootbox NFT với 3 cấp độ: Basic, Premium và Ultimate
- Tỷ lệ rơi đồ khác nhau cho mỗi cấp độ lootbox
- Tích hợp với NFT Plugin để mint NFT cho người chơi
- Hỗ trợ nhiều loại NFT với các hiệu ứng khác nhau
- Giao diện mở lootbox trực quan với hiệu ứng quay số

## Yêu cầu

- Minecraft Server 1.18+
- [Vault](https://www.spigotmc.org/resources/vault.34315/) (để xử lý tiền tệ)
- [NFT Plugin](https://github.com/yourusername/NFT-Plugin) (để mint và quản lý NFT)

## Cài đặt

1. Tải file JAR mới nhất từ [Releases](https://github.com/yourusername/NFT-Lootbox/releases)
2. Đặt file JAR vào thư mục `plugins` của server Minecraft
3. Khởi động lại server
4. Chỉnh sửa file `config.yml` theo nhu cầu của bạn

## Cấu hình

Plugin sử dụng file `config.yml` để cấu hình. Dưới đây là các tùy chọn cấu hình:

```yaml
database:
  url: jdbc:mysql://localhost:3306/minepath
  user: root
  password: yourpassword
prices:
  # Giá của các loại NFT lootbox
  basic_nft: 500
  premium_nft: 1500
  ultimate_nft: 3000

# Cấu hình NFT Lootbox
nft_lootbox:
  # Đường dẫn đến thư mục NFT Plugin
  plugin_path: "path/to/NFT-Plugin"

  # Tỷ lệ rơi đồ cho mỗi cấp độ lootbox
  basic:
    common: 80
    rare: 15
    epic: 4
    legendary: 0.9
    mythic: 0.1
  
  premium:
    common: 50
    rare: 35
    epic: 10
    legendary: 4
    mythic: 1
  
  ultimate:
    common: 30
    rare: 40
    epic: 20
    legendary: 8
    mythic: 2
  
  # Các loại NFT và tỷ lệ rơi trong mỗi cấp độ
  tiers:
    common:
      lucky_charm_1: 60
      explosion_pickaxe_1: 20
      laser_pickaxe_1: 20
    
    rare:
      lucky_charm_2: 40
      explosion_pickaxe_2: 30
      laser_pickaxe_2: 30
    
    epic:
      lucky_charm_5: 40
      explosion_pickaxe_3: 30
      laser_pickaxe_3: 30
    
    legendary:
      lucky_charm_10: 40
      explosion_pickaxe_4: 30
      laser_pickaxe_4: 30
    
    mythic:
      lucky_charm_20: 40
      explosion_pickaxe_5: 30
      laser_pickaxe_5: 30
```

## Lệnh

- `/nftlootbox <type> <amount>` - Mua NFT lootbox
  - `type`: Loại lootbox (basic_nft, premium_nft, ultimate_nft)
  - `amount`: Số lượng lootbox muốn mua
  - Ví dụ: `/nftlootbox basic_nft 1`

## Quyền

- `nftlootbox.use` - Cho phép sử dụng lệnh `/nftlootbox`
- `nftlootbox.admin` - Cho phép sử dụng các lệnh quản trị (nếu có)

## Cách sử dụng

1. Mua NFT lootbox bằng lệnh `/nftlootbox <type> <amount>`
2. Cầm lootbox trong tay và nhấp chuột phải để mở
3. Xem hiệu ứng quay số và nhận NFT ngẫu nhiên
4. Kiểm tra NFT đã nhận trong kho đồ NFT bằng lệnh `/nftinv`

## Tích hợp với NFT Plugin

Plugin này tích hợp với NFT Plugin để mint NFT cho người chơi. Khi người chơi mở lootbox, plugin sẽ:

1. Chọn ngẫu nhiên một cấp độ (common, rare, epic, legendary, mythic) dựa trên tỷ lệ của loại lootbox
2. Chọn ngẫu nhiên một NFT trong cấp độ đó dựa trên tỷ lệ của từng NFT
3. Sử dụng lệnh `/mintnft` để mint NFT cho người chơi
4. Hiển thị thông báo cho người chơi về NFT họ đã nhận được

## Xây dựng từ mã nguồn

Dự án sử dụng Maven để quản lý dependencies và build:

```bash
mvn clean package
```

File JAR sẽ được tạo trong thư mục `target/`.

## Giấy phép

Plugin này được phân phối dưới giấy phép [MIT License](LICENSE).

## Liên hệ

Nếu bạn có bất kỳ câu hỏi hoặc đề xuất nào, vui lòng liên hệ:

- Email: your.email@example.com
- Discord: YourDiscordUsername#1234
- GitHub: [github.com/yourusername](https://github.com/yourusername)
