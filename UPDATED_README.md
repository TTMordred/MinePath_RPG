# SolanaLogin - Minecraft Solana Wallet Authentication

SolanaLogin là một plugin Minecraft cho phép người chơi đăng nhập bằng ví Solana, tích hợp blockchain vào trải nghiệm chơi game. Dự án này bao gồm một plugin Spigot và một máy chủ web để xử lý xác thực ví.

## Tính năng

- Đăng nhập Minecraft bằng ví Solana (Phantom)
- Hỗ trợ hai phương thức xác thực:
  - Kết nối trực tiếp qua Phantom browser extension
  - Quét mã QR bằng ứng dụng Phantom trên điện thoại
- Xác thực an toàn thông qua chữ ký blockchain
- Tích hợp với mạng Solana devnet
- Hỗ trợ phiên bản Minecraft "cracked"

## Cấu trúc dự án

Dự án được chia thành hai phần chính:

1. **Plugin Minecraft (thư mục `src/`)**: Plugin Spigot xử lý các lệnh trong game và tương tác với máy chủ web.
2. **Máy chủ web (thư mục `web-server/`)**: Máy chủ Express.js xử lý xác thực ví Solana và tạo mã QR.

## Yêu cầu

- Java 8 hoặc cao hơn
- Máy chủ Minecraft Spigot/Paper
- Node.js 14 hoặc cao hơn
- Ví Phantom (extension hoặc ứng dụng di động)

## Cài đặt

### Plugin Minecraft

1. Biên dịch plugin hoặc tải tệp JAR từ phần Releases
2. Đặt tệp JAR vào thư mục `plugins` của máy chủ Minecraft
3. Khởi động lại máy chủ
4. Cấu hình plugin trong tệp `plugins/SolanaLogin/config.yml`

### Máy chủ web

1. Di chuyển đến thư mục `web-server`
2. Cài đặt các phụ thuộc:
   ```bash
   npm install
   ```
3. Khởi động máy chủ:
   ```bash
   npm start
   ```

## Cấu hình

### Plugin Minecraft (config.yml)

```yaml
# Cài đặt máy chủ web
web-server:
  enabled: true  # Bật/tắt máy chủ web cho đăng nhập QR code
  url: "http://localhost:3000"  # URL của máy chủ web
  port: 3000  # Cổng của máy chủ web
  qr-code-timeout: 300  # Thời gian hết hạn của mã QR (giây)
  check-interval: 5  # Thời gian kiểm tra trạng thái kết nối (giây)

# Cài đặt Solana
solana:
  network: "devnet"  # Mạng Solana (devnet, testnet, mainnet-beta)
  rpc-url: "https://api.devnet.solana.com"  # URL RPC của Solana

# Cài đặt bảo mật
security:
  max-login-attempts: 5  # Số lần đăng nhập tối đa trước khi tạm khóa
  login-timeout: 300  # Thời gian tạm khóa sau khi đăng nhập thất bại (giây)
  session-timeout: 3600  # Thời gian hết hạn phiên đăng nhập (giây)

# Cài đặt cơ sở dữ liệu
database:
  type: "sqlite"  # Loại cơ sở dữ liệu (sqlite, mysql)
  file: "database.db"  # Tên tệp SQLite
```

### Máy chủ web (.env hoặc biến môi trường)

```
PORT=3000  # Cổng máy chủ web
NODE_ENV=development  # Môi trường (development, production)
```

## Sử dụng

### Lệnh trong game

- `/connectwallet` - Mở giao diện kết nối ví
- `/login` - Đăng nhập bằng ví đã kết nối
- `/logout` - Đăng xuất khỏi phiên hiện tại
- `/walletinfo` - Hiển thị thông tin ví đã kết nối

### Quy trình đăng nhập

#### Phương thức 1: Kết nối qua Phantom Extension

1. Sử dụng lệnh `/connectwallet` trong Minecraft
2. Nhấp vào liên kết được hiển thị trong trò chơi
3. Trên trang web, nhấp vào "Connect with Phantom"
4. Xác nhận kết nối trong extension Phantom
5. Ký tin nhắn xác thực
6. Quay lại Minecraft và sử dụng lệnh `/login`

#### Phương thức 2: Quét mã QR bằng Phantom Mobile

1. Sử dụng lệnh `/connectwallet qr` trong Minecraft
2. Nhấp vào liên kết được hiển thị trong trò chơi
3. Quét mã QR bằng ứng dụng Phantom trên điện thoại
4. Xác nhận kết nối và ký tin nhắn trong ứng dụng Phantom
5. Quay lại Minecraft và sử dụng lệnh `/login`

## Xử lý sự cố

### Vấn đề kết nối ví

- **Lỗi "Missing required parameters"**: Đảm bảo URL đăng nhập chứa đầy đủ các tham số session, nonce và player.
- **Không thể kết nối với Phantom**: Kiểm tra xem Phantom extension đã được cài đặt và đăng nhập.
- **Mã QR không hoạt động**: Đảm bảo ứng dụng Phantom đã được cập nhật lên phiên bản mới nhất.

### Vấn đề máy chủ web

- **Máy chủ web không khởi động**: Kiểm tra xem Node.js đã được cài đặt và các phụ thuộc đã được cài đặt đúng cách.
- **Lỗi CORS**: Đảm bảo URL trong config.yml khớp với URL thực tế của máy chủ web.

### Vấn đề plugin

- **Plugin không tải**: Kiểm tra phiên bản Java và Spigot/Paper.
- **Lệnh không hoạt động**: Kiểm tra quyền của người chơi và cấu hình plugin.

## Trang kiểm tra và debug

Máy chủ web bao gồm một số trang kiểm tra để giúp debug các vấn đề kết nối ví:

1. **Test Flow**: `http://localhost:3000/test-flow.html`
   - Kiểm tra toàn bộ quy trình kết nối ví
   - Tạo session, kết nối ví và xác minh kết nối

2. **Simple Connect**: `http://localhost:3000/simple-connect.html`
   - Kiểm tra kết nối cơ bản với Phantom extension
   - Kiểm tra ký tin nhắn

3. **Simple QR**: `http://localhost:3000/simple-qr.html`
   - Kiểm tra tạo và quét mã QR
   - Kiểm tra deep link cho Phantom mobile

4. **Simple Redirect**: `http://localhost:3000/simple-redirect.html`
   - Kiểm tra xử lý redirect từ Phantom
   - Kiểm tra các tham số URL

## Changelog

### Version 1.2 (Cập nhật mới nhất)

- Chuyển sang sử dụng mạng Solana devnet
- Cải thiện xử lý kết nối ví Phantom
- Sửa lỗi "Missing required parameters" khi xác minh chữ ký
- Đơn giản hóa định dạng deep link cho Phantom wallet
- Thêm chế độ phát triển (development mode) để dễ dàng debug
- Cải thiện xử lý lỗi và logging
- Thêm các trang kiểm tra đơn giản để debug kết nối ví

### Version 1.1

- Thêm hỗ trợ kết nối ví qua mã QR và browser extension
- Thêm thành phần máy chủ web cho xác thực ví Solana
- Cải thiện cấu trúc mã trong các lớp lệnh
- Cải thiện logging với kiểm tra cấp độ để tăng hiệu suất
- Thêm kiểm tra null và xử lý lỗi phù hợp
- Sửa rò rỉ bộ nhớ tiềm ẩn trong kết nối cơ sở dữ liệu
- Cải thiện xác thực ví cho ví Phantom

### Version 1.0

- Phát hành ban đầu
- Chức năng kết nối ví cơ bản
- Tích hợp cơ sở dữ liệu SQLite
- Lệnh đăng nhập và đăng ký
- Tính năng bảo mật cơ bản

## Phát triển

### Yêu cầu phát triển

- JDK 8 hoặc cao hơn
- Maven
- Node.js và npm
- IDE Java (IntelliJ IDEA, Eclipse)

### Biên dịch plugin

```bash
mvn clean package
```

### Chạy máy chủ web trong chế độ phát triển

```bash
cd web-server
npm run dev
```

### Cấu trúc mã nguồn

- `src/main/java/com/nftlogin/walletlogin/` - Mã nguồn Java cho plugin
  - `commands/` - Các lệnh trong game
  - `database/` - Xử lý cơ sở dữ liệu
  - `session/` - Quản lý phiên đăng nhập
  - `utils/` - Tiện ích
- `web-server/` - Mã nguồn máy chủ web
  - `public/` - Tệp tĩnh (HTML, CSS, JS)
  - `server.js` - Mã nguồn máy chủ Express

## Đóng góp

Đóng góp luôn được chào đón! Vui lòng làm theo các bước sau:

1. Fork dự án
2. Tạo nhánh tính năng (`git checkout -b feature/amazing-feature`)
3. Commit thay đổi (`git commit -m 'Add some amazing feature'`)
4. Push lên nhánh (`git push origin feature/amazing-feature`)
5. Mở Pull Request

## Giấy phép

Dự án này được cấp phép theo giấy phép MIT - xem tệp [LICENSE](LICENSE) để biết chi tiết.

## Liên hệ

Nếu bạn có bất kỳ câu hỏi hoặc đề xuất nào, vui lòng mở một issue trên GitHub.

---

Dự án được phát triển cho Solana Hackathon 2025.
