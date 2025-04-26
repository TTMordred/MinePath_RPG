# Hướng dẫn thêm NFT mới vào plugin

## Giới thiệu

Plugin NFT đã được cập nhật để cho phép thêm NFT mới mà không cần biên dịch lại plugin. Bạn chỉ cần tạo file metadata mới trong thư mục `plugins/NFTPlugin/metadata/` và khởi động lại máy chủ.

## Cách thêm NFT mới

1. **Tạo file metadata**: Tạo file JSON mới trong thư mục `plugins/NFTPlugin/metadata/`
2. **Đặt tên file**: Tên file sẽ là khóa thành tựu, ví dụ: `diamond_sword.json`
3. **Cấu trúc file**: File JSON phải có cấu trúc như sau:

```json
{
  "name": "Diamond Sword of Power",
  "symbol": "DSWD",
  "description": "A powerful diamond sword infused with ancient magic",
  "image": "URL_TO_IMAGE",
  "attributes": [
    {
      "trait_type": "Type",
      "value": "Weapon"
    },
    {
      "trait_type": "Rarity",
      "value": "Epic"
    }
  ],
  "quest": {
    "type": "HOLD_NAMED_ITEM_INSTANT",
    "target": "DIAMOND_SWORD",
    "target_name": "Sword of Power",
    "duration": 0,
    "description": "Hold a diamond sword named 'Sword of Power'"
  }
}
```

4. **Khởi động lại máy chủ**: Khởi động lại máy chủ để plugin tải các file metadata mới

## Các trường quan trọng

### Trường cơ bản
- `name`: Tên NFT
- `symbol`: Ký hiệu NFT (tối đa 10 ký tự)
- `description`: Mô tả NFT
- `image`: URL hình ảnh NFT

### Trường quest
- `type`: Loại thành tựu (hiện tại chỉ hỗ trợ `HOLD_NAMED_ITEM_INSTANT`)
- `target`: Loại vật phẩm Minecraft (ví dụ: `DIAMOND_SWORD`, `BLAZE_ROD`, `PAPER`)
- `target_name`: Tên vật phẩm cần cầm để đạt thành tựu
- `duration`: Thời gian cần cầm vật phẩm (đặt 0 để kích hoạt ngay lập tức)
- `description`: Mô tả cách đạt thành tựu

### Trường reward (tùy chọn)
- `item`: Loại vật phẩm phần thưởng
- `name`: Tên vật phẩm phần thưởng (hỗ trợ mã màu với ký tự §)
- `lore`: Mô tả vật phẩm phần thưởng (danh sách các dòng, hỗ trợ mã màu)
- `enchantments`: Enchantment cho vật phẩm phần thưởng (danh sách các enchantment theo định dạng `NAME:LEVEL`)
- `unbreakable`: Vật phẩm có bất khả phá hủy không
- `glowing`: Vật phẩm có phát sáng không
- `custom_model_data`: ID model tùy chỉnh cho vật phẩm

## Ví dụ

### Great Light (Blaze Rod)
```json
{
  "name": "Great Light",
  "symbol": "GRTL",
  "description": "A mystical wand containing the power of the great light",
  "image": "https://example.com/great_light.png",
  "quest": {
    "type": "HOLD_NAMED_ITEM_INSTANT",
    "target": "BLAZE_ROD",
    "target_name": "Great Light",
    "duration": 0,
    "description": "Hold a blaze rod named 'Great Light'"
  }
}
```

### Ancient Scroll (Paper)
```json
{
  "name": "Ancient Scroll",
  "symbol": "ASCR",
  "description": "A mysterious scroll containing ancient knowledge",
  "image": "https://example.com/ancient_scroll.png",
  "quest": {
    "type": "HOLD_NAMED_ITEM_INSTANT",
    "target": "PAPER",
    "target_name": "Ancient Scroll",
    "duration": 0,
    "description": "Hold a paper named 'Ancient Scroll'"
  }
}
```

### Diamond Sword of Power
```json
{
  "name": "Diamond Sword of Power",
  "symbol": "DSWD",
  "description": "A powerful diamond sword infused with ancient magic",
  "image": "https://example.com/diamond_sword.png",
  "quest": {
    "type": "HOLD_NAMED_ITEM_INSTANT",
    "target": "DIAMOND_SWORD",
    "target_name": "Sword of Power",
    "duration": 0,
    "description": "Hold a diamond sword named 'Sword of Power'"
  }
}
```

## Lưu ý

- Tên file phải kết thúc bằng `.json`
- Tên file sẽ được sử dụng làm khóa thành tựu
- Nếu có lỗi khi tải file metadata, kiểm tra log của máy chủ
- Nếu thành tựu đã được đăng ký từ config.yml, file metadata sẽ bị bỏ qua
- Các loại vật phẩm phải là tên hợp lệ trong Minecraft (ví dụ: `DIAMOND_SWORD`, `BLAZE_ROD`, `PAPER`)
