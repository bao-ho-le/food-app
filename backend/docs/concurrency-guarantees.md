# Concurrency guarantees

Phạm vi tài liệu này: những gì hệ thống đảm bảo và KHÔNG đảm bảo dưới truy cập đồng thời, dựa
trên bằng chứng thực nghiệm từ Phase 6 (`StockOversellConcurrencyTest`,
`DishLockOrderingConcurrencyTest`, `OrderStatusConcurrencyTest`, `RefreshTokenConcurrencyTest`,
`IdempotencyConcurrencyTest`, `CartConcurrencyTest`, `DefaultAddressConcurrencyTest`) chạy
trên MySQL 8.4 thật (Testcontainers), không phải suy luận từ đọc code.

## Phần A -- Những gì hệ thống đảm bảo

### A.1 Không bán vượt tồn kho
**Cơ chế:** `DishRepository.findByIdForUpdate` (`SELECT ... FOR UPDATE`, pessimistic write
lock) khoá từng dòng dish trước khi kiểm tra và trừ kho; ràng buộc `CHECK (stock_quantity >=
0)` (migration V3) là hàng rào cuối ở tầng database.
**Test chứng minh:** `StockOversellConcurrencyTest` (3 item) -- tồn kho 1 với 2 khách tranh
nhau, tồn kho 5 với 5 khách đồng thời, và đặt hàng đồng thời với nhập kho. Cả ba đều xanh.

### A.2 Không deadlock khi hai đơn có tập dish giao nhau
**Cơ chế:** `OrderServiceImpl.lockDishesForUpdate` sort id dish tăng dần trước khi khoá, buộc
mọi transaction khoá theo cùng một thứ tự.
**Test chứng minh:** `DishLockOrderingConcurrencyTest` -- hai khách có giỏ hàng chứa cùng hai
dish nhưng thêm vào giỏ theo thứ tự ngược nhau, đặt hàng đồng thời. Cả hai hoàn tất trong
< 15s, không có `DeadlockLoserDataAccessException`/lock timeout.

### A.3 Cập nhật trạng thái đơn hàng không mất update
**Cơ chế:** `Order.@Version` (optimistic locking) -- `saveAndFlush` ném
`ObjectOptimisticLockingFailureException` nếu version đã bị đổi bởi transaction khác, được ánh
xạ thành `409 ORDER_STATUS_CONFLICT`.
**Test chứng minh:** `OrderStatusConcurrencyTest` (2 item) -- admin chuyển PREPARING đồng thời
khách huỷ; hai admin cùng chuyển PREPARING. Cả hai case: đúng 1 thắng (200), đúng 1 thua (409
ORDER_STATUS_CONFLICT), tồn kho khớp với trạng thái cuối.

### A.4 Một refresh token chỉ dùng được đúng một lần
**Cơ chế:** `RefreshTokenRepository.revokeIfActive` -- `UPDATE ... WHERE revoked_at IS NULL`
nguyên tử ở tầng database, số dòng bị ảnh hưởng quyết định ai thắng.
**Test chứng minh:** `RefreshTokenConcurrencyTest` -- hai request refresh đồng thời cùng một
token. Đúng 1 nhận 200 (token mới), đúng 1 nhận 401 REFRESH_TOKEN_REUSED. Không có trường hợp
cả hai cùng thành công.

### A.5 Idempotency-Key: đúng một thao tác nghiệp vụ được thực thi
**Cơ chế:** `IdempotencyKeyStore.tryReserve` dùng khoá chính `idempotency_key` +
`@Transactional(REQUIRES_NEW)` để chỉ một request "giữ" được key.
**Test chứng minh:** `IdempotencyConcurrencyTest` -- dù mã lỗi phía request thua có đúng đặc tả
hay không (xem Phần C), bất biến **đúng 1 đơn được tạo trong database, kho chỉ bị trừ đúng 1
lần** luôn giữ vững qua nhiều lần chạy. Request phát lại sau đó (item 2) nhận đúng lại response
đã lưu, không tạo đơn thứ hai.

## Phần B -- Những gì hệ thống KHÔNG đảm bảo

### B.1 Idempotency và sự cố tiến trình (KHÔNG có test tự động -- xem Phần C cho phần CÓ test được)

Cơ chế idempotency chạy qua **ba transaction rời rạc**: đặt chỗ khoá (`REQUIRES_NEW`) -- thực
hiện nghiệp vụ (transaction riêng) -- ghi nhận kết quả (`REQUIRES_NEW`). Nếu tiến trình chết
sau khi đơn hàng đã commit nhưng trước khi kết quả được ghi nhận, khoá sẽ kẹt ở trạng thái
`IN_PROGRESS`: mọi lần thử lại đều nhận 409 dù đơn đã tồn tại. Sau 48 giờ, tác vụ dọn dẹp xoá
khoá đó và một lần thử lại sẽ tạo **đơn thứ hai**.

Hệ thống vì vậy đảm bảo **at-most-once trong 48 giờ khi không có sự cố tiến trình**, **không**
đảm bảo exactly-once. Kịch bản này không có test tự động (không thể mô phỏng sự cố tiến trình
một cách đáng tin cậy trong test), nhưng phải được ghi lại ở đây vì một giới hạn không được
tuyên bố sẽ bị hiểu nhầm thành một đảm bảo.

### B.2 Mã lỗi khi thua tranh chấp Idempotency-Key (CÓ test, xác nhận đỏ -- xem Phần C.1)

Đặc tả kỳ vọng request thua nhận `409 REQUEST_IN_PROGRESS`. Thực tế đo được: request thua nhận
`500 SOMETHING_WENT_WRONG` do `UnexpectedRollbackException`. Bất biến nghiệp vụ cốt lõi (đúng 1
đơn, kho trừ đúng 1 lần) vẫn giữ vững -- đây là lỗi về **chất lượng phản hồi cho client**
(client nhận lỗi hệ thống thay vì lỗi nghiệp vụ có thể xử lý), không phải lỗi mất tiền.

### B.3 Giỏ hàng: một dish có thể có nhiều dòng nếu thêm đồng thời (CÓ test, xác nhận đỏ -- xem Phần C.2)

Không có ràng buộc `UNIQUE (user_id, dish_id)` trên bảng `user_dish`, và
`UserDishServiceImpl.addUserDish` không có `@Transactional` bao đoạn find-rồi-save. Hai lần
thêm cùng dish đồng thời tạo hai dòng riêng thay vì cộng dồn một dòng. Hệ quả xa hơn: bất kỳ
lời gọi tiếp theo nào dùng `findByUser_IdAndDish_Id` (kỳ vọng tối đa 1 kết quả, ví dụ thêm dish
đó vào giỏ thêm lần nữa) sẽ ném `IncorrectResultSizeDataAccessException` -- 500 không tự phục
hồi cho cặp (user, dish) đó. `GET /user-dishes` (dùng `findAllByUser_Id`, trả `List`) KHÔNG bị
ảnh hưởng vì không đi qua truy vấn giả định-đơn-dòng đó.

### B.4 Địa chỉ mặc định: race về mặt code là có thật, dù chưa quan sát được qua HTTP (xem Phần C.3)

`AddressServiceImpl` không có `@Transactional` bao ngoài `updateAddress`; "bỏ mặc định cũ" và
"đặt mặc định mới" là hai lần `save()` rời nhau, không có ràng buộc CSDL nào chặn 2 địa chỉ
cùng `is_default = true`. Đọc code xác nhận khả năng còn 0 hoặc 2 địa chỉ mặc định là có thật.
Test `DefaultAddressConcurrencyTest` (2 luồng thật qua HTTP, 6 lần chạy) không tái hiện được
kết quả sai -- xem Phần C.3 để biết vì sao đây KHÔNG được coi là "đã chứng minh an toàn".

## Phần C -- Kết quả thí nghiệm của Phase 6

### C.1 IdempotencyConcurrencyTest -- giả thuyết 500 được XÁC NHẬN

- **Dự đoán:** request thua tranh chấp Idempotency-Key nhận 500 thay vì 409, do
  `IdempotencyKeyStore.tryReserve` bắt `DataIntegrityViolationException` bên trong chính
  transaction `REQUIRES_NEW` của nó rồi `return` bình thường từ một transaction đã bị đánh dấu
  rollback-only.
- **Quan sát thực tế:** đúng như dự đoán. HTTP status: **500**. Exception ở đỉnh stack trace:
  **`org.springframework.transaction.UnexpectedRollbackException: Transaction silently rolled
  back because it has been marked as rollback-only`**, ném ra từ
  `IdempotencyKeyStore$$SpringCGLIB$$0.tryReserve`, lan tới `GlobalExceptionHandler`'s catch-all
  `Exception` handler (body: `{"error":"SOMETHING_WENT_WRONG", ...}`).
- **Trạng thái database sau khi chạy:** đúng **1** đơn hàng, kho bị trừ đúng **1 lần** -- bất
  biến quan trọng nhất (không mất tiền) **giữ vững** dù mã lỗi sai.
- **Tái hiện:** ổn định 100% qua 3 lần chạy liên tiếp (không chập chờn).
- **Item 2 (replay sau khi cả hai request đã ổn định):** xanh -- request thứ ba với cùng key
  nhận lại đúng response 201 đã lưu của lần thành công, không tạo đơn thứ hai. Điều này đúng vì
  luồng của request THẮNG không đi qua nhánh lỗi ở trên; chỉ luồng THUA bị ảnh hưởng.

**Kết luận:** giả thuyết 500 **được xác nhận**, không phải suy luận sai. Đây là defect thật,
cần một thay đổi riêng (ngoài phạm vi Phase 6) để `tryReserve` không nuốt exception làm hỏng
trạng thái transaction của chính nó.

### C.2 CartConcurrencyTest -- dự đoán trùng dòng được XÁC NHẬN

- **Dự đoán:** hai request thêm cùng dish đồng thời tạo 2 dòng `user_dish` thay vì 1 dòng cộng
  dồn.
- **Quan sát thực tế:** đúng như dự đoán. Database có **2 dòng**, mỗi dòng `quantity = 1` (thay
  vì 1 dòng `quantity = 2`).
- **Hệ quả nghiệp vụ:** `GET /api/v1/user-dishes` sau đó **vẫn trả về 200 bình thường** (dùng
  `findAllByUser_Id`, không phụ thuộc giả định đơn-dòng) -- phần "đọc lại giỏ hàng vẫn hoạt
  động" của đặc tả **đúng**, không đỏ. Rủi ro 500 không tự phục hồi (từ
  `findByUser_IdAndDish_Id`) chỉ treo lơ lửng cho lần **thêm tiếp** cùng dish đó hoặc bất kỳ
  thao tác nào khác đi qua đúng truy vấn (user_id, dish_id) đó -- không được test riêng trong
  Phase 6 vì nằm ngoài 1 coverage item đã định phạm vi, nhưng được ghi lại ở đây làm rõ chính
  xác điều kiện kích hoạt 500.
- **Tái hiện:** ổn định 100% qua 3 lần chạy liên tiếp.

**Kết luận:** defect được xác nhận đúng như audit trước đã đọc code, ở đúng phần "trùng dòng".

### C.3 DefaultAddressConcurrencyTest -- KHÔNG tái hiện được qua HTTP, dù cơ chế bảo vệ vẫn thiếu

- **Dự đoán:** 0 hoặc 2 địa chỉ mặc định sau khi hai request đồng thời đặt mặc định cho hai địa
  chỉ khác nhau.
- **Quan sát thực tế:** đúng **1** địa chỉ mặc định sau mỗi lần chạy, ổn định qua **6 lần chạy
  liên tiếp** (kể cả khi chạy riêng lẻ lẫn trong cả bộ suite).
- **Vì sao KHÔNG kết luận "đã an toàn":** `AddressServiceImpl.updateAddress` không có
  `@Transactional` bao ngoài -- mỗi lời gọi repository (`findByIdAndUser_Id`,
  `findByUser_IdAndIsDefault`, hai `save()`) là một transaction auto-commit riêng, rất ngắn.
  Cửa sổ race thật sự tồn tại về mặt code (đọc "địa chỉ mặc định hiện tại" rồi ghi, không khoá)
  nhưng **hẹp hơn nhiều bậc độ lớn** so với race trong `IdempotencyConcurrencyTest` (giữ một
  transaction `REQUIRES_NEW` cả một lượt flush) hay `CartConcurrencyTest` (toàn bộ
  find-rồi-save nằm gọn trong một request không có gì chặn giữa). Với chỉ 2 luồng và độ trễ
  JDBC/HTTP cục bộ, khả năng hai request thực sự chồng lấn đúng vào cửa sổ vài câu lệnh SQL đó
  là thấp. Đây là giới hạn thực nghiệm của kiểm thử race qua hộp đen (black-box), không phải
  bằng chứng cơ chế đã an toàn -- **race điều kiện (0 hoặc 2 mặc định) vẫn có thể xảy ra trong
  production dưới tải cao hơn hoặc độ trễ mạng khác**. Test được giữ nguyên (xanh), không bị
  nới lỏng hay đánh dấu `@Disabled`; phát hiện này được ghi lại thay vì bị che giấu.

**Kết luận:** dự đoán từ đọc code (thiếu `@Transactional`, thiếu ràng buộc CSDL) vẫn đúng về
mặt cơ chế, nhưng thí nghiệm cụ thể trong Phase 6 không đủ mạnh để buộc race này lộ ra qua HTTP.
Không có test nào bị coi là chập chờn (flaky) ở đây -- kết quả nhất quán GREEN qua toàn bộ 6 lần
chạy, không phải lúc xanh lúc đỏ.

## Tổng kết bảng kết quả

| Test class | Item | Kết quả | Ghi chú |
|---|---|---|---|
| StockOversellConcurrencyTest | 1, 2, 3 | XANH | Đúng như kỳ vọng |
| DishLockOrderingConcurrencyTest | 1 | XANH | Đúng như kỳ vọng |
| OrderStatusConcurrencyTest | 1, 2 | XANH | Đúng như kỳ vọng |
| RefreshTokenConcurrencyTest | 1 | XANH | Đúng như kỳ vọng |
| IdempotencyConcurrencyTest | 1 | **ĐỎ** | 500 UnexpectedRollbackException thay vì 409 -- xác nhận Phần C.1. Bất biến "1 đơn" vẫn giữ |
| IdempotencyConcurrencyTest | 2 | XANH | Replay hoạt động đúng |
| CartConcurrencyTest | 1 | **ĐỎ** | 2 dòng thay vì 1 -- xác nhận Phần C.2. GET sau đó vẫn OK |
| DefaultAddressConcurrencyTest | 1 | XANH | Dự đoán đỏ KHÔNG tái hiện qua HTTP -- xem Phần C.3, không phải bằng chứng đã an toàn |
