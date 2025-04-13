# SolanaLogin Web Server

Đây là thành phần máy chủ web của dự án SolanaLogin, xử lý xác thực ví Solana cho plugin Minecraft.

## Tính năng

- Xác thực ví Solana thông qua chữ ký blockchain
- Hỗ trợ kết nối qua Phantom browser extension
- Tạo và xử lý mã QR cho ứng dụng Phantom mobile
- Tích hợp với mạng Solana devnet
- API RESTful để tương tác với plugin Minecraft

## Cài đặt

1. Cài đặt Node.js (phiên bản 14 hoặc cao hơn)
2. Cài đặt các phụ thuộc:
   ```bash
   npm install
   ```
3. Khởi động máy chủ:
   ```bash
   npm start
   ```

Máy chủ sẽ chạy trên cổng 3000 theo mặc định. Bạn có thể thay đổi bằng cách đặt biến môi trường `PORT`.

## API Endpoints

- `GET /login` - Trang đăng nhập
- `GET /api/qr` - Tạo mã QR cho kết nối mobile
- `POST /api/verify` - Xác minh chữ ký ví
- `GET /status` - Kiểm tra trạng thái kết nối
- `GET /phantom-redirect` - Xử lý redirect từ Phantom

## Trang kiểm tra và debug

Máy chủ web bao gồm một số trang kiểm tra để giúp debug các vấn đề kết nối ví:

1. **Test Flow**: `/test-flow.html`
   Truy cập http://localhost:3000/test-flow.html
   - Kiểm tra toàn bộ quy trình kết nối ví
   - Tạo session, kết nối ví và xác minh kết nối

2. **Simple Connect**: `/simple-connect.html`
   - Kiểm tra kết nối cơ bản với Phantom extension
   - Kiểm tra ký tin nhắn

3. **Simple QR**: `/simple-qr.html`
   - Kiểm tra tạo và quét mã QR
   - Kiểm tra deep link cho Phantom mobile

4. **Simple Redirect**: `/simple-redirect.html`
   - Kiểm tra xử lý redirect từ Phantom
   - Kiểm tra các tham số URL

## Cấu hình

Đảm bảo URL trong tệp `config.yml` của plugin Minecraft khớp với URL nơi máy chủ web này đang chạy:

```yaml
web-server:
  enabled: true
  url: "http://localhost:3000"  # Thay đổi thành URL của máy chủ của bạn
  port: 3000
  qr-code-timeout: 300
  check-interval: 5
```

## Cập nhật mới nhất (Version 1.2)

- Chuyển sang sử dụng mạng Solana devnet
- Cải thiện xử lý kết nối ví Phantom
- Sửa lỗi "Missing required parameters" khi xác minh chữ ký
- Đơn giản hóa định dạng deep link cho Phantom wallet
- Thêm chế độ phát triển (development mode) để dễ dàng debug
- Cải thiện xử lý lỗi và logging
- Thêm các trang kiểm tra đơn giản để debug kết nối ví
