# NFT Lootbox System

## Rarity Tiers

NFTs trong hệ thống được phân loại thành 5 cấp độ hiếm với tỉ lệ xuất hiện khác nhau:

1. 🍃 **Common** (60%): NFT phổ biến, buff nhỏ
2. 🍀 **Rare** (25%): NFT hiếm, buff trung bình
3. 🔥 **Epic** (10%): NFT rất hiếm, buff mạnh
4. 💎 **Legendary** (4%): NFT cực hiếm, buff rất mạnh
5. 🌀 **Mythic** (1%): NFT hiếm nhất, buff cực mạnh

## NFT Categories & Drop Rates

Mỗi cấp độ hiếm sẽ có các loại NFT khác nhau với tỉ lệ xuất hiện riêng. Dưới đây là chi tiết:

### 🍃 Common Tier (60%)

| NFT Type | Drop Rate | Description |
|----------|-----------|-------------|
| Lucky Charm I | 60% | +1% tỉ lệ vật phẩm NFT khi đào |
| Explosion Pickaxe I | 20% | Khai thác theo vùng 3x3 |
| Laser Pickaxe I | 20% | Khai thác sâu 2 block |

### 🍀 Rare Tier (25%)

| NFT Type | Drop Rate | Description |
|----------|-----------|-------------|
| Lucky Charm II | 40% | +2% tỉ lệ vật phẩm NFT khi đào |
| Explosion Pickaxe II | 30% | Khai thác theo vùng 4x4 |
| Laser Pickaxe II | 30% | Khai thác sâu 3 block |

### 🔥 Epic Tier (10%)

| NFT Type | Drop Rate | Description |
|----------|-----------|-------------|
| Lucky Charm V | 40% | +5% tỉ lệ vật phẩm NFT khi đào |
| Explosion Pickaxe III | 30% | Khai thác theo vùng 5x5 |
| Laser Pickaxe III | 30% | Khai thác sâu 4 block |

### 💎 Legendary Tier (4%)

| NFT Type | Drop Rate | Description |
|----------|-----------|-------------|
| Lucky Charm X | 40% | +10% tỉ lệ vật phẩm NFT khi đào |
| Explosion Pickaxe IV | 30% | Khai thác theo vùng 6x6 |
| Laser Pickaxe IV | 30% | Khai thác sâu 5 block |

### 🌀 Mythic Tier (1%)

| NFT Type | Drop Rate | Description |
|----------|-----------|-------------|
| Lucky Charm XX | 40% | +20% tỉ lệ vật phẩm NFT khi đào |
| Explosion Pickaxe V | 30% | Khai thác theo vùng 7x7 |
| Laser Pickaxe V | 30% | Khai thác sâu 6 block |

## Hệ thống Lootbox

Người chơi có thể nhận NFT thông qua hệ thống lootbox với các mức giá khác nhau:

### Basic Lootbox (500 coins)
- Common: 80%
- Rare: 15% 
- Epic: 4%
- Legendary: 0.9%
- Mythic: 0.1%

### Premium Lootbox (1500 coins)
- Common: 50%
- Rare: 35%
- Epic: 10%
- Legendary: 4%
- Mythic: 1%

### Ultimate Lootbox (3000 coins)
- Common: 30%
- Rare: 40%
- Epic: 20%
- Legendary: 8%
- Mythic: 2%

## Hệ thống Fusion

Người chơi có thể kết hợp 3 NFT cùng loại để có cơ hội nhận được 1 NFT cấp cao hơn:

### Lucky Charm Fusion
- 3x Lucky Charm I → Lucky Charm II (70% thành công)
- 3x Lucky Charm II → Lucky Charm V (50% thành công)
- 3x Lucky Charm V → Lucky Charm X (30% thành công)
- 3x Lucky Charm X → Lucky Charm XX (10% thành công)

### Explosion Pickaxe Fusion
- 3x Explosion Pickaxe I → Explosion Pickaxe II (70% thành công)
- 3x Explosion Pickaxe II → Explosion Pickaxe III (50% thành công)
- 3x Explosion Pickaxe III → Explosion Pickaxe IV (30% thành công)
- 3x Explosion Pickaxe IV → Explosion Pickaxe V (10% thành công)

### Laser Pickaxe Fusion
- 3x Laser Pickaxe I → Laser Pickaxe II (70% thành công)
- 3x Laser Pickaxe II → Laser Pickaxe III (50% thành công)
- 3x Laser Pickaxe III → Laser Pickaxe IV (30% thành công)
- 3x Laser Pickaxe IV → Laser Pickaxe V (10% thành công)

## Hệ thống Event & Limited Edition NFTs

Trong các sự kiện đặc biệt, server sẽ phát hành các NFT Limited Edition với tỉ lệ drop cực kỳ thấp (0.1-0.5%) nhưng có chỉ số cực mạnh, ví dụ:

- 🌋 **Volcanic Pickaxe** (0.3%): Kết hợp Explosion Pickaxe V + Laser Pickaxe V + tự động nấu quặng
- 🍀 **Ultimate Lucky Charm** (0.2%): +30% tỉ lệ vật phẩm hiếm + 10% kinh nghiệm
- ⚡ **Quantum Pickaxe** (0.1%): Khai thác tức thì + 50% tỉ lệ vật phẩm hiếm khi sét đánh

## Lưu ý triển khai

1. Tất cả tỉ lệ phần trăm có thể được điều chỉnh để cân bằng gameplay
2. Các NFT hiếm nên có visual effect đặc biệt để tăng sự hấp dẫn
3. Nên có hệ thống showcase để người chơi có thể khoe NFT của mình
4. Nên có leaderboard để hiển thị người chơi có bộ sưu tập NFT giá trị nhất
5. Nên có hệ thống trading để người chơi có thể trao đổi NFT với nhau

## Tỉ lệ thực tế khi mở Lootbox

Dưới đây là tỉ lệ thực tế để nhận được một NFT cụ thể khi mở lootbox:

### Basic Lootbox (500 coins)

| NFT | Tier Rate | Item Rate | Final Rate |
|-----|-----------|-----------|------------|
| Lucky Charm I | 80% | 60% | 48% |
| Explosion Pickaxe I | 80% | 20% | 16% |
| Laser Pickaxe I | 80% | 20% | 16% |
| Lucky Charm II | 15% | 40% | 6% |
| Explosion Pickaxe II | 15% | 30% | 4.5% |
| Laser Pickaxe II | 15% | 30% | 4.5% |
| Lucky Charm V | 4% | 40% | 1.6% |
| Explosion Pickaxe III | 4% | 30% | 1.2% |
| Laser Pickaxe III | 4% | 30% | 1.2% |
| Lucky Charm X | 0.9% | 40% | 0.36% |
| Explosion Pickaxe IV | 0.9% | 30% | 0.27% |
| Laser Pickaxe IV | 0.9% | 30% | 0.27% |
| Lucky Charm XX | 0.1% | 40% | 0.04% |
| Explosion Pickaxe V | 0.1% | 30% | 0.03% |
| Laser Pickaxe V | 0.1% | 30% | 0.03% |

### Premium Lootbox (1500 coins)

| NFT | Tier Rate | Item Rate | Final Rate |
|-----|-----------|-----------|------------|
| Lucky Charm I | 50% | 60% | 30% |
| Explosion Pickaxe I | 50% | 20% | 10% |
| Laser Pickaxe I | 50% | 20% | 10% |
| Lucky Charm II | 35% | 40% | 14% |
| Explosion Pickaxe II | 35% | 30% | 10.5% |
| Laser Pickaxe II | 35% | 30% | 10.5% |
| Lucky Charm V | 10% | 40% | 4% |
| Explosion Pickaxe III | 10% | 30% | 3% |
| Laser Pickaxe III | 10% | 30% | 3% |
| Lucky Charm X | 4% | 40% | 1.6% |
| Explosion Pickaxe IV | 4% | 30% | 1.2% |
| Laser Pickaxe IV | 4% | 30% | 1.2% |
| Lucky Charm XX | 1% | 40% | 0.4% |
| Explosion Pickaxe V | 1% | 30% | 0.3% |
| Laser Pickaxe V | 1% | 30% | 0.3% |

### Ultimate Lootbox (3000 coins)

| NFT | Tier Rate | Item Rate | Final Rate |
|-----|-----------|-----------|------------|
| Lucky Charm I | 30% | 60% | 18% |
| Explosion Pickaxe I | 30% | 20% | 6% |
| Laser Pickaxe I | 30% | 20% | 6% |
| Lucky Charm II | 40% | 40% | 16% |
| Explosion Pickaxe II | 40% | 30% | 12% |
| Laser Pickaxe II | 40% | 30% | 12% |
| Lucky Charm V | 20% | 40% | 8% |
| Explosion Pickaxe III | 20% | 30% | 6% |
| Laser Pickaxe III | 20% | 30% | 6% |
| Lucky Charm X | 8% | 40% | 3.2% |
| Explosion Pickaxe IV | 8% | 30% | 2.4% |
| Laser Pickaxe IV | 8% | 30% | 2.4% |
| Lucky Charm XX | 2% | 40% | 0.8% |
| Explosion Pickaxe V | 2% | 30% | 0.6% |
| Laser Pickaxe V | 2% | 30% | 0.6% |
