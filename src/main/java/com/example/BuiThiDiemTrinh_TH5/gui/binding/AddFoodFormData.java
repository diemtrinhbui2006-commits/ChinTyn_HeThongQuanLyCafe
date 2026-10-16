package com.example.BuiThiDiemTrinh_TH5.gui.binding;

/**
 * Dữ liệu lấy trực tiếp từ các ô nhập liệu trên màn hình gọi món.
 * Các trường dùng String để có thể nhận giá trị từ cả Swing và JavaFX.
 */
public record AddFoodFormData(
        String tableId,
        String employeeId,
        String customerId,
        String productId,
        String quantity
) {
}
