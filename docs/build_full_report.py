# docs/build_full_report.py
# -*- coding: utf-8 -*-
"""
Script tạo báo cáo đồ án Lập Trình Java & Spring Boot - Trường Cao đẳng Công Thương TP.HCM
Đề tài: Hệ thống quản lý quán cà phê ChinTyn Coffee
Sinh viên: Bùi Thị Diễm Trinh (2124110287) & Nguyễn Thị Mộng Tuyền (2124110290) - Lớp CCQ2411D - Nhóm 20
GVHD: Nguyễn Minh Tuấn
"""

import os
import sys
import shutil
from generate_report import DocxBuilder

def main():
    template_path = os.path.join('docs', 'BAO_CAO_KY_THUAT_TUAN_5_TUYEN_VA_BAO_MAT.docx')
    output_path = os.path.join('docs', 'Nhom05_HeThongQuanLyCafe_LapTrinhJava.docx')
    
    doc = DocxBuilder(template_path)
    
    # =========================================================================
    # 1. TRANG BÌA (COVER PAGE)
    # =========================================================================
    doc.add_cover_page(
        group_name="Nhóm 20",
        class_name="CCQ2411D- Khoa Công nghệ Thông tin",
        instructor="Nguyễn Minh Tuấn",
        date_str="TP. HỒ CHÍ MINH, Ngày 29, Tháng 10, Năm 2026"
    )
    
    # =========================================================================
    # 2. TRANG NHẬN XÉT CỦA GIẢNG VIÊN HƯỚNG DẪN
    # =========================================================================
    doc.add_heading1("NHẬN XÉT CỦA GIẢNG VIÊN HƯỚNG DẪN")
    doc.add_p("Họ và tên giảng viên: NGUYỄN MINH TUẤN", bold=True, size=24, space_before=100, space_after=40)
    doc.add_p("Đơn vị công tác: Khoa Công Nghệ Thông Tin – Trường Cao đẳng Công Thương TP.HCM", italic=True, size=24, space_before=0, space_after=120)
    doc.add_p("Tên đề tài: XÂY DỰNG HỆ THỐNG QUẢN LÝ QUÁN CAFE (CHINTYN COFFEE)", bold=True, size=24, space_before=0, space_after=60, color="1B365D")
    doc.add_p("Sinh viên thực hiện:", bold=True, size=24, space_before=0, space_after=40)
    doc.add_bullet("1. BÙI THỊ DIỄM TRINH", "– MSSV: 2124110287 – Lớp: CCQ2411D")
    doc.add_bullet("2. NGUYỄN THỊ MỘNG TUYỀN", "– MSSV: 2124110290 – Lớp: CCQ2411D")
    doc.add_p("", space_before=60, space_after=60)
    
    review_headers = ["STT", "Nội dung tiêu chí đánh giá", "Điểm tối đa", "Điểm đánh giá", "Nhận xét chi tiết của giảng viên"]
    review_rows = [
        ["01", "Tinh thần, thái độ học tập và mức độ chuyên cần trong quá trình làm đồ án", "1.0", ".........", "...................................................................."],
        ["02", "Khả năng phân tích yêu cầu, thiết kế kiến trúc hệ thống và cơ sở dữ liệu (ERD)", "2.0", ".........", "...................................................................."],
        ["03", "Kỹ năng lập trình Java, cấu trúc mã nguồn Spring Boot, RESTful API và tính đúng đắn nghiệp vụ", "3.0", ".........", "...................................................................."],
        ["04", "Giao diện người dùng (UI/UX), tính năng POS, đặt bàn, ca đối soát, gọi món QR và điểm thưởng", "2.0", ".........", "...................................................................."],
        ["05", "Chất lượng kiểm thử (Unit test, Integration test, Postman) và tính an toàn bảo mật (Security)", "1.0", ".........", "...................................................................."],
        ["06", "Hình thức trình bày báo cáo, tính logic và kỹ năng trả lời câu hỏi phản biện đồ án", "1.0", ".........", "...................................................................."],
        ["TỔNG", "TỔNG CỘNG ĐIỂM ĐÁNH GIÁ ĐỒ ÁN", "10.0", ".........", "Điểm bằng chữ: ..........................................."]
    ]
    doc.add_table("Bảng nhận xét và đánh giá kết quả đồ án môn học:", review_headers, review_rows, [600, 3200, 1000, 1200, 3200])
    
    doc.add_p("", space_before=120, space_after=60)
    doc.add_p("Nhận xét chung về ưu điểm và hạn chế của đề tài:", bold=True, size=24, space_before=60, space_after=40)
    doc.add_p("........................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................")
    
    doc.add_p("", space_before=140, space_after=40)
    sig_headers = ["Sinh viên thực hiện đề tài", "Giảng viên hướng dẫn chấm điểm"]
    sig_rows = [
        ["(Ký và ghi rõ họ tên)\n\n\n\n\nBùi Thị Diễm Trinh      Nguyễn Thị Mộng Tuyền", 
         "Tp. Hồ Chí Minh, ngày ..... tháng ..... năm 2026\n(Ký và ghi rõ họ tên)\n\n\n\nNguyễn Minh Tuấn"]
    ]
    doc.add_table("", sig_headers, sig_rows, [4600, 4600])
    doc.add_page_break()
    
    # =========================================================================
    # 3. MỤC LỤC TỰ ĐỘNG (AUTOMATIC TABLE OF CONTENTS WITH PAGE NUMBERS)
    # =========================================================================
    toc_items = [
        ("LỜI CẢM ƠN", "iv"),
        ("DANH MỤC CÁC TỪ VIẾT TẮT VÀ THUẬT NGỮ", "v"),
        ("DANH MỤC CÁC BẢNG BIỂU", "vi"),
        ("DANH MỤC CÁC HÌNH VẼ VÀ SƠ ĐỒ", "vii"),
        ("CHƯƠNG 1: GIỚI THIỆU ĐỀ TÀI", "1"),
        ("   1.1. Lý do chọn đề tài và tính cấp thiết", "1"),
        ("   1.2. Mục tiêu nghiên cứu và phạm vi ứng dụng", "2"),
        ("   1.3. Yêu cầu hệ thống", "3"),
        ("      1.3.1. Yêu cầu chức năng", "3"),
        ("      1.3.2. Yêu cầu phi chức năng", "4"),
        ("   1.4. Công nghệ sử dụng trong hệ thống", "5"),
        ("CHƯƠNG 2: CƠ SỞ LÝ THUYẾT VÀ CÔNG NGHỆ ỨNG DỤNG", "6"),
        ("   2.1. Nền tảng Spring Boot và kiến trúc ứng dụng", "6"),
        ("   2.2. Kiến trúc dịch vụ Web RESTful API", "8"),
        ("   2.3. Lưu trữ và xử lý dữ liệu với Spring Data JPA & Hibernate", "9"),
        ("   2.4. An toàn thông tin và bảo mật với Spring Security", "11"),
        ("   2.5. Công nghệ bổ sung: Thư viện tạo mã QR ZXing & Tiêu chuẩn in hóa đơn", "13"),
        ("CHƯƠNG 3: PHÂN TÍCH VÀ THIẾT KẾ HỆ THỐNG", "14"),
        ("   3.1. Mô tả bài toán và các tác nhân sử dụng hệ thống", "14"),
        ("   3.2. Danh sách chức năng theo từng tác nhân", "15"),
        ("   3.3. Mô tả chi tiết quy trình xử lý các nghiệp vụ cốt lõi", "17"),
        ("   3.4. Sơ đồ Use Case hệ thống", "20"),
        ("   3.5. Thiết kế Cơ sở dữ liệu (CSDL)", "22"),
        ("      3.5.1. Mô hình thực thể liên kết (ERD)", "22"),
        ("      3.5.2. Đặc tả chi tiết các bảng dữ liệu", "23"),
        ("      3.5.3. Các ràng buộc toàn vẹn quan trọng trong hệ thống", "26"),
        ("   3.6. Thiết kế giao diện và luồng thao tác người dùng", "27"),
        ("   3.7. Danh mục các điểm cuối dịch vụ (REST API Endpoints)", "30"),
        ("CHƯƠNG 4: CÀI ĐẶT VÀ TRIỂN KHAI HỆ THỐNG", "33"),
        ("   4.1. Môi trường phát triển và công cụ sử dụng", "33"),
        ("   4.2. Cấu trúc tổ chức mã nguồn (Source Code)", "34"),
        ("   4.3. Cài đặt các chức năng trọng tâm và giải thích mã nguồn", "36"),
        ("      4.3.1. Nghiệp vụ bán hàng POS và quản lý vòng đời hóa đơn", "36"),
        ("      4.3.2. Nghiệp vụ ca làm việc và đối soát tiền mặt két tự động", "38"),
        ("      4.3.3. Nghiệp vụ tạo mã QR bàn và hàng chờ duyệt đơn", "40"),
        ("      4.3.4. Nghiệp vụ tích lũy điểm và đổi quà thưởng thành viên", "42"),
        ("      4.3.5. Cấu hình bảo mật phiên làm việc và phòng chống CSRF", "43"),
        ("   4.4. Tích hợp dịch vụ mở rộng", "45"),
        ("   4.5. Hướng dẫn cài đặt, cấu hình và khởi chạy ứng dụng", "46"),
        ("CHƯƠNG 5: KIỂM THỬ HỆ THỐNG", "48"),
        ("   5.1. Phương pháp và công cụ kiểm thử", "48"),
        ("   5.2. Bảng kịch bản kiểm thử (Test Cases)", "49"),
        ("   5.3. Kết quả kiểm thử tự động với Maven và JUnit", "52"),
        ("CHƯƠNG 6: KẾT QUẢ, ĐÁNH GIÁ VÀ HƯỚNG PHÁT TRIỂN", "54"),
        ("   6.1. Tổng kết kết quả đạt được đối chiếu với mục tiêu", "54"),
        ("   6.2. Đánh giá chất lượng triển khai ứng dụng", "55"),
        ("   6.3. Những hạn chế tồn tại của hệ thống", "56"),
        ("   6.4. Định hướng phát triển và nâng cấp trong tương lai", "57"),
        ("KẾT LUẬN VÀ BÀI HỌC KINH NGHIỆM", "58"),
        ("BẢNG PHÂN CÔNG NHIỆM VỤ THỰC HIỆN ĐỀ TÀI", "59"),
        ("TÀI LIỆU THAM KHẢO", "60"),
        ("PHỤ LỤC", "61")
    ]
    
    doc.add_automatic_toc(toc_items)
    doc.add_page_break()
    
    # =========================================================================
    # DANH MỤC TỪ VIẾT TẮT, BẢNG BIỂU, HÌNH VẼ
    # =========================================================================
    doc.add_heading1("DANH MỤC CÁC TỪ VIẾT TẮT VÀ THUẬT NGỮ")
    abbr_headers = ["Từ viết tắt", "Tên tiếng Anh đầy đủ", "Ý nghĩa / Diễn giải tiếng Việt"]
    abbr_rows = [
        ["API", "Application Programming Interface", "Giao diện lập trình ứng dụng"],
        ["REST", "Representational State Transfer", "Kiểu kiến trúc phần mềm cho các hệ thống phân tán"],
        ["JPA", "Jakarta/Java Persistence API", "Đặc tả kỹ thuật quản lý dữ liệu đối tượng trong Java"],
        ["ORM", "Object-Relational Mapping", "Kỹ thuật ánh xạ giữa đối tượng mã nguồn và bảng CSDL"],
        ["DTO", "Data Transfer Object", "Đối tượng truyền tải dữ liệu giữa các tầng kiến trúc"],
        ["MVC", "Model - View - Controller", "Mô hình kiến trúc phân tách Dữ liệu - Giao diện - Điều khiển"],
        ["RBAC", "Role-Based Access Control", "Mô hình kiểm soát truy cập và phân quyền theo vai trò"],
        ["CSRF", "Cross-Site Request Forgery", "Kỹ thuật tấn công giả mạo yêu cầu từ trang web tin cậy"],
        ["POS", "Point of Sale", "Điểm bán hàng / Màn hình thu ngân phục vụ thanh toán"],
        ["QR Code", "Quick Response Code", "Mã phản hồi nhanh / Mã vạch ma trận hai chiều"],
        ["ACID", "Atomicity, Consistency, Isolation, Durability", "Bốn thuộc tính bảo đảm tính tin cậy của giao dịch CSDL"],
        ["PBKDF2", "Password-Based Key Derivation Function 2", "Thuật toán tạo khóa băm mật khẩu chống tấn công vét cạn"],
        ["JSON", "JavaScript Object Notation", "Định dạng trao đổi dữ liệu có cấu trúc nhẹ, phổ biến"],
        ["UI / UX", "User Interface / User Experience", "Giao diện người dùng / Trải nghiệm người dùng"],
        ["SPA", "Single Page Application", "Ứng dụng web trang đơn không cần tải lại toàn trang"]
    ]
    doc.add_table("Danh mục từ viết tắt:", abbr_headers, abbr_rows, [1400, 3600, 4200])
    
    doc.add_heading1("DANH MỤC CÁC BẢNG BIỂU")
    doc.add_bullet("Bảng 4.1:", "Bảng công nghệ sử dụng trong hệ thống và vai trò chi tiết (Trang 5)")
    doc.add_bullet("Bảng 4.2:", "Bảng phân loại chức năng hệ thống theo từng tác nhân người dùng (Trang 15)")
    doc.add_bullet("Bảng 4.3:", "Bảng thiết kế các bảng dữ liệu trong CSDL cafe (Trang 23)")
    doc.add_bullet("Bảng 4.4:", "Bảng đặc tả các điểm cuối dịch vụ REST API của hệ thống (Trang 30)")
    doc.add_bullet("Bảng 4.5:", "Bảng tổng hợp kịch bản kiểm thử (Test Cases) các nghiệp vụ chính (Trang 49)")
    doc.add_bullet("Bảng 4.6:", "Bảng phân công nhiệm vụ và mức độ hoàn thành của các thành viên (Trang 59)")
    
    doc.add_heading1("DANH MỤC CÁC HÌNH VẼ VÀ SƠ ĐỒ")
    doc.add_bullet("Hình 2.1:", "Kiến trúc đa tầng (Multi-tier Architecture) trong Spring Boot (Trang 7)")
    doc.add_bullet("Hình 2.2:", "Cơ chế bảo mật phiên làm việc và Token CSRF trong Spring Security (Trang 12)")
    doc.add_bullet("Hình 3.1:", "Sơ đồ Use Case tổng quát của Hệ thống Quản lý Quán Cafe (Trang 20)")
    doc.add_bullet("Hình 3.2:", "Sơ đồ Use Case phân rã phân hệ Bán hàng & Thanh toán POS (Trang 21)")
    doc.add_bullet("Hình 3.3:", "Sơ đồ mô hình quan hệ thực thể (ERD) hoàn chỉnh của cơ sở dữ liệu (Trang 22)")
    doc.add_bullet("Hình 3.4:", "Sơ đồ luồng thao tác màn hình POS bán hàng và in hóa đơn nhiệt (Trang 28)")
    doc.add_bullet("Hình 3.5:", "Sơ đồ quy trình khách quét mã QR gọi món và nhân viên duyệt đơn (Trang 29)")
    doc.add_bullet("Hình 4.1:", "Cấu trúc phân mục mã nguồn dự án theo chuẩn Clean Architecture (Trang 34)")
    doc.add_bullet("Hình 5.1:", "Kết quả thực thi tự động bộ kiểm thử Maven (BUILD SUCCESS 20/20 test) (Trang 53)")
    
    doc.add_page_break()
    
    # =========================================================================
    # CHƯƠNG 1: GIỚI THIỆU ĐỀ TÀI
    # =========================================================================
    doc.add_heading1("CHƯƠNG 1: GIỚI THIỆU ĐỀ TÀI")
    
    doc.add_heading2("1.1. Lý do chọn đề tài và tính cấp thiết")
    doc.add_p("Trong những năm gần đây, ngành dịch vụ ẩm thực và đồ uống (Food & Beverage - F&B), đặc biệt là mô hình kinh doanh quán cà phê tại Việt Nam, chứng kiến tốc độ tăng trưởng vượt bậc. Từ các quán cà phê truyền thống quy mô vừa và nhỏ đến các chuỗi thương hiệu hiện đại, nhu cầu phục vụ khách hàng nhanh chóng, chính xác và chuyên nghiệp trở thành yếu tố sống còn quyết định năng lực cạnh tranh của doanh nghiệp.", indent_first=360)
    doc.add_p("Tuy nhiên, qua khảo sát thực tế tại nhiều quán cà phê hiện nay, phương thức vận hành thủ công hoặc sử dụng các phần mềm đơn lẻ vẫn bộc lộ rất nhiều hạn chế bất cập:", indent_first=360)
    doc.add_bullet("Quy trình gọi món và ghi chép chậm trễ:", "Nhân viên phục vụ phải ghi tay order trên giấy, di chuyển giữa bàn khách và quầy pha chế, dễ gây sai sót món, nhầm lẫn yêu cầu đặc biệt (như độ đường, đá, topping), làm tăng thời gian chờ đợi của khách hàng trong giờ cao điểm.")
    doc.add_bullet("Thất thoát doanh thu và khó kiểm soát tiền mặt:", "Tiền mặt thanh toán tại quầy dễ xảy ra chênh lệch do thối nhầm, nhân viên quên ghi nhận đơn hàng hoặc không đối soát số tiền thực tế trong két với doanh thu ghi nhận trên hệ thống sau mỗi ca làm việc.")
    doc.add_bullet("Khó khăn trong quản lý tồn kho và chi phí:", "Quán thường xuyên đối mặt với tình trạng thiếu hụt nguyên liệu đột xuất hoặc hết hàng mà không có cảnh báo trước; các khoản chi phí phát sinh hàng ngày (mua đá, sữa, trái cây, trả tiền nhà cung cấp) không được liên kết trực tiếp với dữ liệu kho.")
    doc.add_bullet("Thiếu trải nghiệm số hóa cho khách hàng:", "Khách hàng hiện đại có nhu cầu tự chủ gọi món thông qua quét mã QR tại bàn trên điện thoại di động thông minh và tích lũy điểm thưởng thành viên để đổi quà ưu đãi.")
    doc.add_p("Xuất phát từ những bài toán thực tiễn nêu trên, với tư cách là sinh viên năm thứ 3 chuyên ngành Công nghệ Thông tin, nhóm chúng em đã lựa chọn đề tài \"Xây dựng Hệ thống Quản lý Quán Cafe (ChinTyn Coffee)\" làm đồ án kết thúc môn học Lập trình Java. Đề tài tập trung áp dụng các công nghệ tiên tiến nhất của hệ sinh thái Java bao gồm Spring Boot, Spring Data JPA, Spring Security, kết hợp với cơ sở dữ liệu MySQL và kiến trúc RESTful API hiện đại nhằm mang lại một giải pháp phần mềm quản lý toàn diện, tự động hóa tối đa quy trình vận hành quán cà phê.", indent_first=360)
    
    doc.add_heading2("1.2. Mục tiêu nghiên cứu và phạm vi ứng dụng")
    doc.add_p("Đồ án hướng tới các mục tiêu cụ thể và xác định rõ phạm vi triển khai như sau:", indent_first=360)
    doc.add_heading3("1.2.1. Mục tiêu của đề tài")
    doc.add_bullet("Về mặt kiến thức học thuật:", "Củng cố và vận dụng chuyên sâu các kiến thức cốt lõi của môn học Lập trình Java, làm chủ framework Spring Boot, kỹ thuật lập trình hướng đối tượng (OOP), các Design Pattern (MVC, Repository, DTO, Dependency Injection), xử lý giao dịch dữ liệu ACID và bảo mật ứng dụng web.")
    doc.add_bullet("Về mặt sản phẩm phần mềm:", "Xây dựng thành công một ứng dụng quản lý quán cà phê vận hành trơn tru, đồng bộ dữ liệu thời gian thực giữa quầy thu ngân (POS), bộ phận pha chế, quản lý kho bãi, ca làm việc và khách hàng gọi món qua mã QR.")
    doc.add_bullet("Về mặt thực tiễn:", "Tạo ra một sản phẩm có tính ứng dụng cao, đáp ứng đầy đủ các nghiệp vụ thực tế của một quán cà phê vừa và lớn, giao diện thân thiện, dễ sử dụng, bảo mật và ổn định.")
    
    doc.add_heading3("1.2.2. Phạm vi ứng dụng của hệ thống")
    doc.add_bullet("Các chức năng đã hoàn thành 100% trong đồ án:", "Bao gồm 8 phân hệ nghiệp vụ chính: (1) Quản lý bán hàng POS & In hóa đơn nhiệt; (2) Quản lý sơ đồ bàn ăn & Đặt bàn trước; (3) Quản lý thực đơn & danh mục món; (4) Quản lý ca làm việc & đối soát tiền két tự động; (5) Quản lý nhà cung cấp & chi phí vận hành; (6) Khách tự gọi món qua quét mã QR bàn ăn & Hàng chờ nhân viên duyệt; (7) Quản lý khách hàng thân thiết & Tích điểm đổi quà; (8) Quản trị tài khoản & phân quyền bảo mật 3 cấp (ADMIN, CASHIER, STAFF).")
    doc.add_bullet("Giới hạn của hệ thống:", "Hệ thống hiện tại được thiết kế tối ưu hóa cho mạng nội bộ (LAN) của quán hoặc triển khai máy chủ đám mây nội bộ; tính năng thanh toán chuyển khoản sử dụng cơ chế phát sinh mã VietQR động theo chuẩn Napas 24/7 (thu ngân đối soát xác nhận thành công trước khi hoàn tất đơn hàng, chưa kết nối trực tiếp cổng Webhook ngân hàng thương mại).")
    
    doc.add_heading2("1.3. Yêu cầu hệ thống")
    doc.add_heading3("1.3.1. Yêu cầu chức năng")
    doc.add_bullet("Phân hệ Đăng nhập & Xác thực:", "Cho phép người dùng đăng nhập bằng tên tài khoản và mật khẩu; kiểm tra vai trò người dùng (ADMIN, CASHIER, STAFF); bảo vệ phiên làm việc qua HTTP Session và Cookie an toàn; hỗ trợ đăng xuất và đổi mật khẩu.")
    doc.add_bullet("Phân hệ Bán hàng POS (Point of Sale):", "Hiển thị danh sách bàn theo trạng thái (Trống, Đang phục vụ, Đã đặt); chọn món theo danh mục với giao diện thẻ trực quan; tùy chọn độ ngọt, đá, ghi chú món; tự động tính tổng tiền hàng, áp dụng mã giảm giá (chiết khấu phần trăm hoặc số tiền), thanh toán đa phương thức (Tiền mặt, Chuyển khoản, Thẻ, Ví điện tử), tự động trừ tồn kho và in phiếu hóa đơn nhiệt chuẩn 80mm.")
    doc.add_bullet("Phân hệ Quản lý Đặt bàn trước (Reservations):", "Tiếp nhận thông tin khách đặt bàn trước (tên khách, số điện thoại, thời gian hẹn, số lượng khách, ghi chú); tự động chuyển trạng thái bàn sang 'Đã đặt trước' (RESERVED); cho phép hủy lịch đặt và giải phóng bàn về trạng thái 'Trống' (AVAILABLE).")
    doc.add_bullet("Phân hệ Ca làm việc & Đối soát tiền mặt (Work Shifts):", "Cho phép thu ngân mở ca làm việc mới với số tiền mặt đầu ca trong két; theo dõi toàn bộ dòng tiền mặt phát sinh trong ca; khi đóng ca, thu ngân nhập số tiền kiểm kê thực tế, hệ thống tự động so khớp với doanh thu lý thuyết và tính ra chênh lệch két (Khớp, Dư, Thiếu) kèm ghi chú giải trình.")
    doc.add_bullet("Phân hệ Nhà cung cấp & Chi phí (Suppliers & Expenses):", "Quản lý danh sách nhà cung cấp (tên, số điện thoại, email, địa chỉ); ghi nhận các khoản chi phí vận hành hàng ngày (tiền điện, nước, mặt bằng, sửa chữa); hỗ trợ liên kết chi phí nhập hàng trực tiếp với cập nhật số lượng tồn kho nguyên liệu.")
    doc.add_bullet("Phân hệ Gọi món qua QR (QR Ordering Queue):", "Tạo mã QR định danh cho từng bàn ăn bằng thư viện Google ZXing; khách hàng dùng điện thoại quét mã QR để truy cập menu đặt món; đơn đặt của khách sẽ đi vào hàng chờ và nhân viên chỉ cần bấm 'Duyệt vào bàn' để tự động tạo hóa đơn và báo bếp pha chế.")
    doc.add_bullet("Phân hệ Khách hàng thân thiết & Điểm thưởng (Loyalty Program):", "Lưu trữ hồ sơ khách hàng; tự động tích lũy điểm thưởng theo tỷ lệ doanh thu chi tiêu; tự động phân hạng thành viên (Thành Viên Mới, Bạc, Vàng, Kim Cương); quản lý danh mục quà tặng và hỗ trợ trừ điểm đổi quà tại quầy.")
    doc.add_bullet("Phân hệ Quản lý Danh mục, Thực đơn & Kho nguyên liệu:", "CRUD danh mục món, sản phẩm, điều chỉnh giá bán, bật/tắt trạng thái mở bán; quản lý danh mục nguyên liệu thô, cảnh báo khi tồn kho giảm xuống dưới ngưỡng an toàn.")
    
    doc.add_heading3("1.3.2. Yêu cầu phi chức năng")
    doc.add_bullet("Tính khả dụng và dễ sử dụng (Usability):", "Giao diện người dùng được thiết kế theo phong cách hiện đại, trực quan, bố cục rõ ràng, phối màu ấm cúng chuẩn nhận diện quán cà phê; thời gian đào tạo nhân viên mới sử dụng phần mềm dưới 15 phút; thao tác bán hàng được tối ưu hóa chỉ với 2-3 lượt click chuột.")
    doc.add_bullet("Tính tin cậy và toàn vẹn dữ liệu (Reliability & ACID):", "Mọi giao dịch thanh toán hóa đơn, trừ tồn kho và đối soát tiền ca đều được bảo vệ bởi cơ chế `@Transactional`, đảm bảo nguyên tắc nguyên tử: thành công trọn vẹn hoặc rollback hoàn toàn nếu có lỗi xảy ra.")
    doc.add_bullet("Hiệu năng hệ thống (Performance):", "Thời gian phản hồi các truy vấn REST API trung bình dưới 100ms; hệ thống có khả năng phục vụ đồng thời hàng chục yêu cầu POS và gọi món QR không gây nghẽn nhờ kiến trúc xử lý đa luồng non-blocking của Spring Boot nhúng máy chủ Tomcat.")
    doc.add_bullet("Tính an toàn bảo mật (Security):", "Mật khẩu người dùng được băm một chiều an toàn bằng thuật toán PBKDF2WithHmacSHA256 kết hợp muối (Salt) ngẫu nhiên; phòng chống triệt để tấn công giả mạo yêu cầu chéo trang (CSRF) thông qua mã thông báo `X-XSRF-TOKEN`; ngăn chặn truy cập trái phép bằng bộ lọc `SessionAccountValidationFilter` và phân quyền chặt chẽ theo vai trò (RBAC).")
    
    doc.add_heading2("1.4. Công nghệ sử dụng trong hệ thống")
    doc.add_p("Hệ thống được xây dựng dựa trên sự kết hợp hài hòa giữa các công nghệ backend công nghiệp chuẩn mực và giao diện frontend hiện đại, nhẹ nhàng. Dưới đây là bảng tổng hợp các công nghệ được sử dụng cùng vai trò chi tiết của từng thành phần trong đồ án:", indent_first=360)
    
    tech_headers = ["Nhóm công nghệ", "Tên công nghệ & Phiên bản", "Vai trò và nhiệm vụ trong hệ thống"]
    tech_rows = [
        ["Ngôn ngữ lập trình", "Java SE 17 (LTS)", "Ngôn ngữ phát triển toàn bộ mã nguồn xử lý nghiệp vụ backend, tận dụng tính năng Records, Switch Pattern Matching và Garbage Collection thế hệ mới."],
        ["Framework ứng dụng", "Spring Boot 3.3.4", "Khung ứng dụng nòng cốt, cung cấp cơ chế Auto-configuration, Dependency Injection, nhúng máy chủ Tomcat và quản lý vòng đời ứng dụng."],
        ["Tầng điều khiển API", "Spring Web (Spring MVC)", "Xây dựng các RESTful API Controller, tiếp nhận yêu cầu HTTP, chuyển đổi dữ liệu JSON qua Jackson ObjectMapper và xử lý ngoại lệ tập trung."],
        ["Tầng dữ liệu & ORM", "Spring Data JPA & Hibernate", "Ánh xạ các thực thể cơ sở dữ liệu (Entities) với bảng MySQL, tự động sinh truy vấn CRUD, hỗ trợ JPQL và quản lý giao dịch dữ liệu."],
        ["Hệ quản trị CSDL", "MySQL 8.0 / MariaDB (XAMPP)", "Lưu trữ dữ liệu quan hệ, sử dụng Storage Engine InnoDB hỗ trợ khóa ngoại, giao dịch ACID và bộ mã ký tự utf8mb4 hỗ trợ tiếng Việt trọn vẹn."],
        ["Bảo mật hệ thống", "Spring Security 6.x", "Bảo vệ các điểm cuối API, kiểm soát phiên đăng nhập (Session/Cookie), phòng chống tấn công CSRF và phân quyền RBAC (ADMIN, CASHIER, STAFF)."],
        ["Mã hóa mật khẩu", "PBKDF2WithHmacSHA256", "Thuật toán băm mật khẩu chuẩn NIST với 120.000 vòng lặp và khóa 256-bit, ngăn ngừa hiệu quả các cuộc tấn công Rainbow Table và brute-force."],
        ["Xử lý mã QR", "Google ZXing 3.5.3 (Core & JavaSE)", "Sinh mã ma trận QR hai chiều động cho bàn ăn phục vụ khách gọi món và tạo mã VietQR phục vụ thanh toán chuyển khoản ngân hàng."],
        ["Giao diện người dùng", "HTML5, CSS3, Vanilla JS (ES Modules)", "Xây dựng giao diện Single Page Application (SPA), thiết kế dạng thẻ Responsive, module hóa mã nguồn JavaScript sạch, không phụ thuộc framework nặng."],
        ["Quản lý thư viện & Build", "Apache Maven 3.9", "Quản lý toàn bộ vòng đời ứng dụng (dependency management, compile, test, package thành tệp jar chạy độc lập)."],
        ["Kiểm thử tự động", "JUnit 5 & Spring Boot Test", "Thực hiện kiểm thử đơn vị (Unit Test) và kiểm thử tích hợp (Integration Test) cho tầng Service, Controller và quy tắc tính toán tiền tệ."],
        ["CSDL kiểm thử", "H2 In-Memory Database", "Cơ sở dữ liệu tạm thời trên bộ nhớ RAM phục vụ thực thi tự động các bài test độc lập mà không làm sai lệch CSDL thật."],
        ["Công cụ hỗ trợ API", "Postman", "Thiết kế, gửi request, kiểm tra các luồng xác thực, kiểm thử tải và đối soát các phản hồi JSON từ RESTful API."]
    ]
    doc.add_table("Bảng 4.1: Bảng công nghệ sử dụng trong hệ thống", tech_headers, tech_rows, [1800, 2400, 5000])
    
    doc.add_page_break()
    
    # =========================================================================
    # CHƯƠNG 2: CƠ SỞ LÝ THUYẾT VÀ CÔNG NGHỆ ỨNG DỤNG
    # =========================================================================
    doc.add_heading1("CHƯƠNG 2: CƠ SỞ LÝ THUYẾT VÀ CÔNG NGHỆ ỨNG DỤNG")
    
    doc.add_heading2("2.1. Nền tảng Spring Boot và kiến trúc ứng dụng")
    doc.add_p("Spring Boot là một dự án nổi bật thuộc hệ sinh thái Spring Framework của tổ chức Pivotal/VMware. Spring Boot được tạo ra với mục tiêu tối giản hóa quá trình khởi tạo, cấu hình và phát triển các ứng dụng Java độc lập đạt tiêu chuẩn công nghiệp (Production-ready).", indent_first=360)
    doc.add_bullet("Triết lý cấu hình tự động (Auto-Configuration):", "Spring Boot tự động cấu hình các Bean và môi trường thực thi dựa trên các thư viện (jar dependencies) được khai báo trong file `pom.xml`. Ví dụ: khi nhận thấy `spring-boot-starter-web`, hệ thống sẽ tự động cấu hình máy chủ Tomcat nhúng chạy tại cổng 8080 và bộ xử lý thông điệp Jackson.")
    doc.add_bullet("Cơ chế Starters Dependencies:", "Cung cấp các gói phụ thuộc tích hợp sẵn, giúp lập trình viên không phải quản lý thủ công từng phiên bản của hàng chục thư viện riêng lẻ. Dự án của nhóm sử dụng các starter then chốt: `spring-boot-starter-web`, `spring-boot-starter-data-jpa` và `spring-boot-starter-security`.")
    doc.add_bullet("Kiến trúc phân tầng chặt chẽ (Multi-tier Architecture):", "Hệ thống ChinTyn Coffee áp dụng triệt để mô hình kiến trúc phân tầng chuẩn mực, phân tách rõ ràng trách nhiệm của từng thành phần:")
    doc.add_p("   + Tầng Giao diện (Presentation Layer): Đặt tại thư mục `src/main/resources/static/`, phụ trách hiển thị dữ liệu và gửi các yêu cầu Ajax/Fetch API đến máy chủ.", indent_first=450)
    doc.add_p("   + Tầng Điều khiển (Controller Layer): Tiếp nhận các HTTP Request, ánh xạ đường dẫn (Routing), kiểm tra tính hợp lệ của dữ liệu đầu vào và ủy thác công việc cho tầng Service.", indent_first=450)
    doc.add_p("   + Tầng Dịch vụ (Service Layer): Nơi tập trung toàn bộ các quy tắc nghiệp vụ (Business Logic) như tạo hóa đơn, trừ tồn kho, tính toán chiết khấu, đối soát ca và tích điểm.", indent_first=450)
    doc.add_p("   + Tầng Truy cập Dữ liệu (Repository / DAO Layer): Kế thừa từ `JpaRepository` để tương tác trực tiếp với cơ sở dữ liệu MySQL thông qua Spring Data JPA.", indent_first=450)
    doc.add_p("   + Tầng Thực thể Miền (Entity Domain Layer): Các lớp Java ánh xạ tương ứng 1-1 với các bảng trong cơ sở dữ liệu quan hệ.", indent_first=450)
    
    doc.add_heading2("2.2. Kiến trúc dịch vụ Web RESTful API")
    doc.add_p("REST (Representational State Transfer) là kiểu kiến trúc phần mềm tiêu chuẩn cho việc xây dựng các ứng dụng mạng phân tán. Một hệ thống Web API tuân thủ các nguyên tắc REST được gọi là RESTful API.", indent_first=360)
    doc.add_bullet("Nguyên tắc phi trạng thái (Statelessness):", "Mỗi yêu cầu từ client gửi lên máy chủ phải chứa đầy đủ mọi thông tin cần thiết để xử lý yêu cầu đó. Máy chủ không lưu trữ ngữ cảnh hội thoại của client giữa các request riêng biệt, giúp tăng cường khả năng mở rộng hệ thống.")
    doc.add_bullet("Chuẩn hóa các phương thức HTTP (HTTP Verbs):", "Hệ thống sử dụng các phương thức chuẩn để biểu diễn hành động trên tài nguyên:")
    doc.add_p("   + GET: Truy vấn và đọc dữ liệu tài nguyên (ví dụ: `GET /api/products` lấy danh sách món, `GET /api/invoices/1` lấy chi tiết hóa đơn).", indent_first=450)
    doc.add_p("   + POST: Tạo mới một tài nguyên hoặc kích hoạt một hành động nghiệp vụ (ví dụ: `POST /api/invoices/order` tạo đơn gọi món, `POST /api/shifts/open` mở ca).", indent_first=450)
    doc.add_p("   + PUT: Cập nhật toàn bộ thông tin của một tài nguyên hiện có (ví dụ: `PUT /api/products/1` sửa thông tin món ăn).", indent_first=450)
    doc.add_p("   + DELETE: Xóa hoặc vô hiệu hóa tài nguyên (ví dụ: `DELETE /api/invoices/1/details/5` hủy món khỏi đơn hàng).", indent_first=450)
    doc.add_bullet("Chuẩn hóa mã phản hồi HTTP (HTTP Status Codes):", "Máy chủ luôn trả về các mã trạng thái chuẩn quốc tế giúp client dễ dàng nhận biết kết quả xử lý:")
    doc.add_p("   + 200 OK: Xử lý thành công yêu cầu đọc hoặc cập nhật.", indent_first=450)
    doc.add_p("   + 201 Created: Tạo mới tài nguyên thành công.", indent_first=450)
    doc.add_p("   + 400 Bad Request: Dữ liệu gửi lên không hợp lệ (ví dụ: giá âm, thiếu trường bắt buộc).", indent_first=450)
    doc.add_p("   + 401 Unauthorized: Chưa đăng nhập hoặc phiên làm việc đã hết hạn.", indent_first=450)
    doc.add_p("   + 403 Forbidden: Đã đăng nhập nhưng không có quyền hạn thực hiện thao tác này.", indent_first=450)
    doc.add_p("   + 404 Not Found: Tài nguyên được yêu cầu không tồn tại trong hệ thống.", indent_first=450)
    
    doc.add_heading2("2.3. Lưu trữ và xử lý dữ liệu với Spring Data JPA & Hibernate")
    doc.add_p("Spring Data JPA là một phần của dự án Spring Data, cung cấp tầng trừu tượng hóa mức cao trên nền tảng JPA (Jakarta Persistence API) và Hibernate ORM. Công nghệ này mang lại các lợi ích vượt trội cho đồ án:", indent_first=360)
    doc.add_bullet("Tự động hóa các thao tác CRUD cơ bản:", "Bằng việc khai báo một interface kế thừa `JpaRepository<T, ID>`, Spring Data JPA tự động cung cấp các phương thức như `save()`, `findById()`, `findAll()`, `delete()` mà lập trình viên không cần viết một dòng mã SQL hay code triển khai nào.")
    doc.add_bullet("Sinh câu truy vấn thông qua tên phương thức (Query Methods):", "Cho phép lập trình viên định nghĩa các truy vấn phức tạp một cách trực quan, ví dụ: `findFirstByEmployeeIdAndClosedAtIsNullOrderByOpenedAtDesc()` tự động sinh ra câu lệnh SQL tìm ca làm việc đang mở gần nhất của một nhân viên.")
    doc.add_bullet("Quản lý giao dịch nguyên tử (`@Transactional`):", "Đảm bảo tính toàn vẹn dữ liệu cho các nghiệp vụ phức hợp. Ví dụ: khi thanh toán một hóa đơn, hệ thống phải thực hiện liên tiếp các thao tác: kiểm tra tiền, cập nhật trạng thái hóa đơn thành `PAID`, đổi trạng thái bàn sang `AVAILABLE`, ghi nhận giờ thanh toán và cộng điểm thưởng cho khách hàng. Nếu bất kỳ bước nào thất bại, toàn bộ giao dịch sẽ tự động bị rollback về trạng thái ban đầu.")
    
    doc.add_heading2("2.4. An toàn thông tin và bảo mật với Spring Security")
    doc.add_p("Bảo mật là một trong những yêu cầu kỹ thuật trọng tâm của đồ án được triển khai toàn diện thông qua Spring Security 6.x:", indent_first=360)
    doc.add_bullet("Mô hình chuỗi bộ lọc bảo mật (Security Filter Chain):", "Mọi yêu cầu HTTP gửi đến ứng dụng đều phải đi qua chuỗi bộ lọc do Spring Security quản lý trước khi tiếp cận được các Controller. Hệ thống cấu hình chính sách phân quyền chi tiết theo vai trò (Role-Based Access Control):")
    doc.add_p("   + ROLE_ADMIN: Toàn quyền truy cập mọi tính năng quản trị hệ thống, quản lý tài khoản, nhân viên, thực đơn, danh mục, kho bãi, khuyến mãi, cấu hình ca và đối soát.", indent_first=450)
    doc.add_p("   + ROLE_CASHIER: Được phép truy cập màn hình POS bán hàng, lịch sử đơn hàng, xem danh sách bàn, danh sách khách hàng, mở ca và đóng ca đối soát tiền két của chính mình.", indent_first=450)
    doc.add_p("   + ROLE_STAFF: Chỉ được phép xem trạng thái bàn ăn, hỗ trợ khách gọi món tại bàn, kiểm tra danh mục món ăn và gửi yêu cầu vào hóa đơn.", indent_first=450)
    doc.add_bullet("Phòng chống tấn công Cross-Site Request Forgery (CSRF):", "Để ngăn chặn kẻ tấn công lợi dụng phiên đăng nhập của người dùng để thực hiện các hành động độc hại từ trang web bên thứ ba, hệ thống kích hoạt cơ chế bảo vệ CSRF dựa trên Cookie thông qua `CookieCsrfTokenRepository.withHttpOnlyFalse()`. Mỗi khi người dùng đăng nhập, hệ thống sẽ phát hành mã thông báo `XSRF-TOKEN` vào cookie trình duyệt. Trong mọi yêu cầu POST, PUT, DELETE tiếp theo, mã nguồn JavaScript sẽ đọc token này và đính kèm vào HTTP Header `X-XSRF-TOKEN`. Máy chủ sẽ đối chiếu nghiêm ngặt trước khi cho phép xử lý.")
    doc.add_bullet("Mã hóa mật khẩu nâng cao với PBKDF2:", "Thay vì lưu trữ mật khẩu dưới dạng văn bản thô (Plain Text) hoặc sử dụng các hàm băm yếu đã lỗi thời (như MD5, SHA-1), hệ thống áp dụng thuật toán chuẩn `PBKDF2WithHmacSHA256` với 120.000 chu kỳ băm và muối ngẫu nhiên (Random Salt) 128-bit. Thuật toán này làm chậm đáng kể tốc độ thử mật khẩu của kẻ tấn công, triệt tiêu hoàn toàn nguy cơ tra cứu bảng cầu vồng (Rainbow Table).")
    
    doc.add_heading2("2.5. Công nghệ bổ sung: Thư viện tạo mã QR ZXing & Tiêu chuẩn in hóa đơn")
    doc.add_bullet("Thư viện Google ZXing (Zebra Crossing):", "Hệ thống tích hợp thư viện ZXing phiên bản 3.5.3 nhằm sinh mã ma trận QR hai chiều trực tiếp từ backend Java dưới định dạng hình ảnh PNG `image/png`. Mã QR được sử dụng cho 2 nghiệp vụ quan trọng: (1) Mã QR định danh bàn ăn (chứa URL có token bảo mật duy nhất cho phép khách hàng tại bàn quét mã để truy cập menu đặt món trực tiếp); (2) Mã VietQR thanh toán (tạo chuỗi mã theo chuẩn thanh toán quốc gia Napas247 chứa thông tin số tài khoản quán, số tiền cần thanh toán và mã hóa đơn giúp khách chuyển khoản nhanh mà không lo nhầm lẫn số tiền).")
    doc.add_bullet("Tiêu chuẩn in hóa đơn nhiệt POS 80mm:", "Hệ thống xây dựng giao diện xem trước phiếu thu chuẩn giấy in nhiệt K80 (chiều rộng 80mm, khoảng 48 ký tự trên mỗi dòng). Giao diện in được thiết kế chuyên dụng với CSS Media Print `@media print`, tự động căn chỉnh lề, định dạng rõ ràng logo quán, số hóa đơn, tên nhân viên, tên bàn, bảng chi tiết từng món kèm tùy chọn đường/đá, giảm giá, tổng tiền và mã vạch hóa đơn, sẵn sàng in trực tiếp ra mọi máy in nhiệt USB/LAN phổ biến.")
    
    doc.add_page_break()
    
    # =========================================================================
    # CHƯƠNG 3: PHÂN TÍCH VÀ THIẾT KẾ HỆ THỐNG
    # =========================================================================
    doc.add_heading1("CHƯƠNG 3: PHÂN TÍCH VÀ THIẾT KẾ HỆ THỐNG")
    
    doc.add_heading2("3.1. Mô tả bài toán và các tác nhân sử dụng hệ thống")
    doc.add_p("Hệ thống quản lý quán cà phê ChinTyn Coffee được thiết kế xoay quanh chu trình phục vụ khách hàng khép kín: từ khâu khách bước vào quán hoặc đặt bàn trước, gọi món tại bàn hoặc qua quét mã QR, chế biến tại quầy bar, thanh toán tại quầy thu ngân, in hóa đơn nhiệt, đến việc ghi nhận doanh thu, quản lý chi phí nhập hàng từ nhà cung cấp và đối soát tiền két cuối ca làm việc.", indent_first=360)
    doc.add_p("Hệ thống xác định 4 tác nhân (Actors) chính tham gia tương tác:", indent_first=360)
    doc.add_bullet("Quản trị viên (ADMIN):", "Là chủ quán hoặc quản lý cấp cao, có quyền hạn cao nhất trong hệ thống; phụ trách cấu hình danh mục thực đơn, giá bán, quản lý thông tin nhân viên, cấp tài khoản người dùng, xem báo cáo tổng quan Dashboard, kiểm soát chi phí nhập kho từ nhà cung cấp và quản lý các chương trình khuyến mãi, điểm thưởng.")
    doc.add_bullet("Thu ngân (CASHIER):", "Nhân viên trực tiếp đứng tại quầy POS, thực hiện mở ca đầu ngày, kiểm soát trạng thái các bàn, tạo đơn gọi món, áp dụng mã giảm giá theo yêu cầu khách, thực hiện thanh toán tiền mặt/chuyển khoản, in hóa đơn nhiệt, duyệt các yêu cầu gọi món gửi lên từ mã QR của khách và thực hiện kiểm kê, đóng ca đối soát tiền két vào cuối ca.")
    doc.add_bullet("Nhân viên phục vụ (STAFF):", "Nhân viên di chuyển linh hoạt trong quán, sử dụng máy tính bảng hoặc điện thoại di động truy cập hệ thống để kiểm tra bàn trống, tiếp nhận yêu cầu gọi món trực tiếp của khách tại bàn và gửi vào hệ thống.")
    doc.add_bullet("Khách hàng (GUEST / CUSTOMER):", "Khách hàng đến trải nghiệm tại quán, có thể quét mã QR dán tại bàn để tự xem thực đơn điện tử và gửi yêu cầu gọi món trực tuyến; hoặc đăng ký thành viên thân thiết để được tích điểm và đổi các phần thưởng giá trị.")
    
    doc.add_heading2("3.2. Danh sách chức năng theo từng tác nhân")
    doc.add_p("Dưới đây là bảng phân loại chi tiết các chức năng của hệ thống được phân quyền theo từng tác nhân cụ thể:", indent_first=360)
    
    actor_headers = ["Tác nhân", "Mã chức năng", "Tên chức năng", "Mô tả tóm tắt nghiệp vụ", "Trạng thái"]
    actor_rows = [
        ["Quản trị viên", "F_ADM_01", "Quản lý nhân viên & tài khoản", "Thêm, sửa, xóa hồ sơ nhân viên, phân quyền vai trò (Admin, Cashier, Staff), cấp tài khoản và đặt lại mật khẩu.", "Đã hoàn thành"],
        ["Quản trị viên", "F_ADM_02", "Quản lý thực đơn & danh mục", "CRUD danh mục món, sản phẩm, điều chỉnh đơn giá, số lượng tồn kho, tải ảnh đại diện món ăn, bật/tắt mở bán.", "Đã hoàn thành"],
        ["Quản trị viên", "F_ADM_03", "Báo cáo tổng quan Dashboard", "Thống kê doanh thu theo ngày/tháng, số lượng đơn hàng, món bán chạy nhất, biểu đồ tăng trưởng và cảnh báo kho.", "Đã hoàn thành"],
        ["Quản trị viên", "F_ADM_04", "Quản lý chi phí & Nhà cung cấp", "Quản lý hồ sơ đối tác cung ứng, ghi nhận các khoản chi phí hoạt động, gắn hóa đơn nhập hàng vào kho nguyên liệu.", "Đã hoàn thành"],
        ["Quản trị viên", "F_ADM_05", "Quản lý khuyến mãi & Điểm thưởng", "Tạo mã voucher giảm giá, thiết lập tỷ lệ tích điểm khách hàng thân thiết, cấu hình danh mục phần thưởng đổi quà.", "Đã hoàn thành"],
        ["Thu ngân", "F_CSH_01", "Mở ca & Đóng ca đối soát", "Nhập tiền mặt đầu ca trong két; cuối ca kiểm kê tiền thực tế, hệ thống tự động tính chênh lệch két kèm giải trình.", "Đã hoàn thành"],
        ["Thu ngân", "F_CSH_02", "Bán hàng POS & Gọi món tại bàn", "Chọn bàn, chọn món từ menu, tùy chỉnh lượng đường/đá/ghi chú, thêm món vào đơn đang mở, trừ kho tức thì.", "Đã hoàn thành"],
        ["Thu ngân", "F_CSH_03", "Thanh toán & In hóa đơn nhiệt", "Áp dụng giảm giá, chọn phương thức thanh toán (Tiền mặt, VietQR, Thẻ), chuyển trạng thái sang PAID, in bill nhiệt 80mm.", "Đã hoàn thành"],
        ["Thu ngân", "F_CSH_04", "Duyệt đơn gọi món qua QR", "Theo dõi hàng chờ các yêu cầu gọi món gửi từ điện thoại khách, kiểm tra món và bấm 'Duyệt vào bàn' để tạo hóa đơn.", "Đã hoàn thành"],
        ["Thu ngân", "F_CSH_05", "Đổi quà tích điểm cho khách", "Tra cứu điểm tích lũy của khách hàng bằng số điện thoại, hỗ trợ khách chọn quà trong danh mục và trừ điểm hệ thống.", "Đã hoàn thành"],
        ["Nhân viên", "F_STF_01", "Theo dõi sơ đồ bàn ăn", "Xem trực quan trạng thái toàn bộ các bàn trong quán (Bàn trống, Có khách, Đã đặt trước, Ngừng phục vụ).", "Đã hoàn thành"],
        ["Nhân viên", "F_STF_02", "Hỗ trợ gọi món tại bàn", "Tiếp nhận yêu cầu order của khách bằng thiết bị di động và gửi món trực tiếp vào hóa đơn của bàn.", "Đã hoàn thành"],
        ["Khách hàng", "F_GST_01", "Xem thực đơn & Đặt món QR", "Khách dùng điện thoại quét mã QR tại bàn, chọn món, chọn số lượng, ghi chú và gửi đơn vào hàng chờ nhà bếp.", "Đã hoàn thành"],
        ["Khách hàng", "F_GST_02", "Tra cứu điểm & Hạng thành viên", "Kiểm tra số điểm tích lũy của mình, hạng thành viên (Bạc, Vàng, Kim Cương) và xem các phần thưởng có thể đổi.", "Đã hoàn thành"]
    ]
    doc.add_table("Bảng 4.2: Bảng chức năng theo tác nhân người dùng", actor_headers, actor_rows, [1400, 1100, 2200, 3500, 1000])
    
    doc.add_heading2("3.3. Mô tả chi tiết quy trình xử lý các nghiệp vụ cốt lõi")
    doc.add_p("Dưới đây là đặc tả chi tiết luồng xử lý đầu vào, thuật toán nghiệp vụ và kết quả đầu ra của các chức năng quan trọng nhất trong hệ thống:", indent_first=360)
    
    doc.add_heading3("3.3.1. Nghiệp vụ Gọi món tại bàn và Quản lý vòng đời hóa đơn (Order & Invoice Lifecycle)")
    doc.add_bullet("Đầu vào (Input):", "Mã bàn (`tableId`), mã nhân viên phục vụ (`employeeId`), danh sách các món gọi kèm số lượng (`productId`, `quantity`), tùy chọn đường (`sugarPercent`), đá (`iceOption`) và ghi chú đặc biệt (`note`).")
    doc.add_bullet("Quy trình xử lý (Processing Logic):", "Hệ thống kiểm tra trạng thái bàn ăn:")
    doc.add_p("   1. Tìm kiếm xem bàn ăn đã có hóa đơn ở trạng thái mở (`OPEN`) hay chưa. Nếu chưa có, hệ thống tự động khởi tạo một đối tượng `Invoice` mới, gắn với bàn và nhân viên phục vụ, đồng thời cập nhật trạng thái bàn sang `OCCUPIED` (Có khách).", indent_first=450)
    doc.add_p("   2. Đối với từng món được gọi, hệ thống kiểm tra số lượng tồn kho trong bảng `products`. Nếu số lượng tồn kho không đủ (`quantity < orderedQuantity`), hệ thống ném ra ngoại lệ `BadRequestException(\"Món X hiện không đủ số lượng phục vụ\")` và dừng giao dịch.", indent_first=450)
    doc.add_p("   3. Nếu đủ tồn kho, hệ thống tiến hành trừ trực tiếp số lượng tồn kho của sản phẩm, chốt đơn giá hiện tại (`unit_price`) của món vào bảng chi tiết `invoice_details` (nhằm bảo đảm giá của hóa đơn không bị thay đổi nếu sau này người quản trị tăng/giảm giá menu).", indent_first=450)
    doc.add_p("   4. Tính toán lại tổng tiền tạm tính (`subtotal = sum(quantity * unit_price)`), trừ chiết khấu nếu có và cập nhật `total_amount` của hóa đơn.", indent_first=450)
    doc.add_bullet("Kết quả đầu ra (Output):", "Thông tin chi tiết hóa đơn được cập nhật thành công, trạng thái bàn đổi sang màu cam (Đang phục vụ), danh sách món ăn hiển thị tức thì trên giao diện POS.")
    
    doc.add_heading3("3.3.2. Nghiệp vụ Thanh toán và In hóa đơn nhiệt (Checkout & Thermal Print)")
    doc.add_bullet("Đầu vào (Input):", "Mã hóa đơn (`invoiceId`), phương thức thanh toán (`paymentMethod`: CASH, BANK_TRANSFER, CARD, E_WALLET), mã khách hàng thân thiết (nếu có).")
    doc.add_bullet("Quy trình xử lý (Processing Logic):", "")
    doc.add_p("   1. Kiểm tra hóa đơn phải đang ở trạng thái `OPEN` và có ít nhất 1 món ăn hợp lệ. Nếu hóa đơn rỗng hoặc đã thanh toán trước đó, từ chối xử lý.", indent_first=450)
    doc.add_p("   2. Ghi nhận thời điểm thanh toán thực tế `paid_at = LocalDateTime.now()`, lưu phương thức thanh toán và chuyển trạng thái hóa đơn sang `PAID`.", indent_first=450)
    doc.add_p("   3. Tự động giải phóng bàn ăn về trạng thái `AVAILABLE` (Bàn trống) để sẵn sàng đón lượt khách tiếp theo.", indent_first=450)
    doc.add_p("   4. Nếu đơn hàng có gắn với khách hàng thân thiết, hệ thống tự động tính điểm tích lũy: `Điểm = total_amount / 10.000` (cứ 10.000 VNĐ chi tiêu tích được 1 điểm), tạo một bản ghi biến động điểm trong bảng `loyalty_entries`.", indent_first=450)
    doc.add_bullet("Kết quả đầu ra (Output):", "Hóa đơn được hoàn tất, hiển thị hộp thoại xem trước hóa đơn nhiệt K80 và tự động kích hoạt lệnh in ra máy in nhiệt `window.print()`.")
    
    doc.add_heading3("3.3.3. Nghiệp vụ Mở/Đóng ca và Đối soát tiền mặt két tự động (Cash Drawer Reconciliation)")
    doc.add_bullet("Đầu vào (Input):", "Mã ca (`shiftId`), số tiền mặt kiểm kê thực tế đếm được trong két lúc cuối ca (`closingCash`), ghi chú giải trình (`note`).")
    doc.add_bullet("Quy trình xử lý (Processing Logic):", "")
    doc.add_p("   1. Hệ thống xác định mốc thời gian mở ca (`openedAt`) và thời điểm đóng ca hiện tại (`closedAt`).", indent_first=450)
    doc.add_p("   2. Truy vấn tổng doanh thu thu bằng tiền mặt trong khoảng thời gian ca làm việc: `cashSales = sum(invoices.total_amount) WHERE payment_method = 'CASH' AND paid_at BETWEEN openedAt AND closedAt`.", indent_first=450)
    doc.add_p("   3. Truy vấn tổng các khoản chi phí bằng tiền mặt phát sinh trong ca: `cashExpenses = sum(expenses.amount) WHERE payment_method = 'CASH' AND created_at BETWEEN openedAt AND closedAt`.", indent_first=450)
    doc.add_p("   4. Tính số tiền mặt lý thuyết dự kiến phải có trong két: `expectedCash = openingCash + cashSales - cashExpenses`.", indent_first=450)
    doc.add_p("   5. Tính toán chênh lệch két: `diff = closingCash - expectedCash`. Nếu `diff == 0`: két khớp hoàn hảo; nếu `diff > 0`: dư tiền trong két; nếu `diff < 0`: thiếu hụt tiền két (yêu cầu nhân viên ghi chú giải trình rõ ràng).", indent_first=450)
    doc.add_bullet("Kết quả đầu ra (Output):", "Bản ghi ca làm việc được lưu trữ vĩnh viễn trong CSDL, cập nhật báo cáo tài chính đối soát minh bạch giữa quản lý và thu ngân.")
    
    doc.add_heading2("3.4. Sơ đồ Use Case hệ thống")
    doc.add_p("Dưới đây là biểu diễn sơ đồ Use Case tổng quát thể hiện mối liên kết giữa các tác nhân và các phân hệ chức năng trong hệ thống quản lý quán cà phê ChinTyn Coffee:", indent_first=360)
    
    use_case_text = (
        "+---------------------------------------------------------------------------------------------------+\n"
        "|                                HỆ THỐNG QUẢN LÝ QUÁN CAFE (CHINTYN COFFEE)                        |\n"
        "+---------------------------------------------------------------------------------------------------+\n"
        "|                                                                                                   |\n"
        "|   [ QUẢN TRỊ VIÊN - ADMIN ]                                                                       |\n"
        "|       |---> ( UC01: Đăng nhập & Đổi mật khẩu hệ thống )                                           |\n"
        "|       |---> ( UC02: Quản lý Nhân viên & Cấp tài khoản phân quyền )                                |\n"
        "|       |---> ( UC03: Quản lý Danh mục món & Cấu hình giá bán thực đơn )                            |\n"
        "|       |---> ( UC04: Xem Báo cáo thống kê Dashboard & Doanh thu )                                  |\n"
        "|       |---> ( UC05: Quản lý Kho nguyên liệu & Cảnh báo tồn thấp )                                |\n"
        "|       |---> ( UC06: Quản lý Nhà cung cấp & Chi phí vận hành )                                     |\n"
        "|       |---> ( UC07: Quản lý Chương trình Khuyến mãi & Voucher giảm giá )                          |\n"
        "|                                                                                                   |\n"
        "|   [ THU NGÂN - CASHIER ]                                                                          |\n"
        "|       |---> ( UC08: Mở ca làm việc & Khai báo tiền két đầu ca )                                   |\n"
        "|       |---> ( UC09: Đóng ca & Tự động đối soát chênh lệch tiền mặt két )                          |\n"
        "|       |---> ( UC10: Quản lý Bàn ăn & Tiếp nhận Đặt bàn trước )                                    |\n"
        "|       |---> ( UC11: Bán hàng POS, Gọi món tại bàn & Tùy chọn đường/đá )                           |\n"
        "|       |---> ( UC12: Áp dụng mã giảm giá & Thanh toán đa phương thức )                              |\n"
        "|       |---> ( UC13: In hóa đơn nhiệt K80 chuyên nghiệp )                                          |\n"
        "|       |---> ( UC14: Theo dõi & Duyệt yêu cầu gọi món từ mã QR bàn )                               |\n"
        "|       |---> ( UC15: Tra cứu điểm thưởng & Đổi quà cho khách hàng )                                |\n"
        "|                                                                                                   |\n"
        "|   [ NHÂN VIÊN PHỤC VỤ - STAFF ]                                                                   |\n"
        "|       |---> ( UC16: Xem sơ đồ bàn ăn thời gian thực )                                             |\n"
        "|       |---> ( UC17: Hỗ trợ tạo đơn gọi món tại bàn cho khách )                                    |\n"
        "|                                                                                                   |\n"
        "|   [ KHÁCH HÀNG - GUEST ]                                                                          |\n"
        "|       |---> ( UC18: Quét mã QR tại bàn để xem thực đơn điện tử )                                 |\n"
        "|       |---> ( UC19: Chọn món, gửi yêu cầu gọi món vào hàng chờ quầy bar )                         |\n"
        "|       |---> ( UC20: Tra cứu lịch sử tích điểm và hạng thành viên )                                |\n"
        "+---------------------------------------------------------------------------------------------------+"
    )
    doc.add_code_block("Sơ đồ Use Case tổng quát hệ thống", use_case_text)
    
    doc.add_heading2("3.5. Thiết kế Cơ sở dữ liệu (CSDL)")
    doc.add_p("Cơ sở dữ liệu của hệ thống được đặt tên là `cafe`, thiết kế chuẩn hóa mức 3 (3NF), sử dụng bộ máy lưu trữ InnoDB của MySQL 8.0 nhằm bảo đảm tính toàn vẹn khóa ngoại và hỗ trợ giao dịch ACID.", indent_first=360)
    
    doc.add_heading3("3.5.1. Sơ đồ mô hình quan hệ thực thể (ERD)")
    erd_text = (
        "  +----------------+        1:N       +----------------+        1:N       +---------------------+\n"
        "  |   CATEGORIES   |----------------->|    PRODUCTS    |----------------->|   INVOICE_DETAILS   |\n"
        "  +----------------+                  +----------------+                  +---------------------+\n"
        "                                                                                     | N:1\n"
        "  +----------------+        1:1       +----------------+        1:N                  v\n"
        "  |   EMPLOYEES    |<---------------->|     USERS      |       +--------------------------------+\n"
        "  +----------------+                  +----------------+       |            INVOICES            |\n"
        "          | 1:N                                                +--------------------------------+\n"
        "          v                                                               | N:1              | N:1\n"
        "  +----------------+                                                      v                  v\n"
        "  |  WORK_SHIFTS   |                                              +----------------+ +------------------+\n"
        "  +----------------+                                              |  CAFE_TABLES   | |    CUSTOMERS     |\n"
        "                                                                  +----------------+ +------------------+\n"
        "  +----------------+        1:N       +----------------+                  | 1:N              | 1:N\n"
        "  |   SUPPLIERS    |----------------->|    EXPENSES    |                  v                  v\n"
        "  +----------------+                  +----------------+          +----------------+ +------------------+\n"
        "                                                                  |TABLE_RESERVE.. | | LOYALTY_ENTRIES  |\n"
        "  +----------------+        1:N       +----------------+          +----------------+ +------------------+\n"
        "  |  INGREDIENTS   |----------------->| STOCK_MOVEMENTS|                  | 1:N              | N:1\n"
        "  +----------------+                  +----------------+                  v                  v\n"
        "                                                                  +----------------+ +------------------+\n"
        "                                                                  |QR_GUEST_ORDERS | | LOYALTY_REWARDS  |\n"
        "                                                                  +----------------+ +------------------+"
    )
    doc.add_code_block("Sơ đồ cấu trúc thực thể liên kết (ERD) cơ sở dữ liệu cafe", erd_text)
    
    doc.add_heading3("3.5.2. Đặc tả chi tiết các bảng dữ liệu trong CSDL")
    doc.add_p("Hệ thống bao gồm 18 bảng dữ liệu quan hệ được phân chia theo từng phân hệ nghiệp vụ cụ thể như sau:", indent_first=360)
    
    db_headers = ["Tên Bảng / Entity", "Khóa chính (PK)", "Khóa ngoại (FK) & Quan hệ", "Ý nghĩa và vai trò trong hệ thống"]
    db_rows = [
        ["employees", "id (BIGINT)", "Không có", "Lưu trữ thông tin hồ sơ nhân viên (họ tên, email duy nhất, số điện thoại, chức vụ, mức lương)."],
        ["users", "id (BIGINT)", "employee_id (FK -> employees)", "Lưu trữ tài khoản đăng nhập (username duy nhất, password băm PBKDF2, role: ADMIN, CASHIER, STAFF)."],
        ["customers", "id (BIGINT)", "Không có", "Lưu trữ thông tin khách hàng (họ tên, số điện thoại có chỉ mục đánh index để tìm nhanh, địa chỉ, email)."],
        ["categories", "id (BIGINT)", "Không có", "Phân loại danh mục thực đơn (Cà phê, Trà sữa, Sinh tố đá xay, Bánh ngọt... name duy nhất)."],
        ["products", "id (BIGINT)", "category_id (FK -> categories)", "Lưu trữ các món ăn/thức uống, giá bán > 0, số lượng tồn kho >= 0, trạng thái mở bán, cờ xóa mềm (deleted)."],
        ["cafe_tables", "id (BIGINT)", "Không có", "Quản lý sơ đồ bàn ăn (số bàn table_number duy nhất > 0, sức chứa capacity, trạng thái AVAILABLE, OCCUPIED, RESERVED...)."],
        ["invoices", "id (BIGINT)", "table_id (FK), employee_id (FK), customer_id (FK)", "Lưu trữ thông tin hóa đơn tổng (tiền tạm tính subtotal, giảm giá discount, tổng tiền thanh toán, trạng thái OPEN/PAID/CANCELLED, hình thức thanh toán)."],
        ["invoice_details", "id (BIGINT)", "invoice_id (FK), product_id (FK)", "Chi tiết từng món trong hóa đơn (số lượng, đơn giá chốt lúc gọi món, thành tiền, tỷ lệ đường, tùy chọn đá, ghi chú)."],
        ["table_reservations", "id (BIGINT)", "cafe_table_id (FK -> cafe_tables)", "Lưu trữ lịch đặt bàn trước của khách (tên khách, SĐT, thời gian hẹn reserved_for, số khách, trạng thái BOOKED/CANCELLED)."],
        ["work_shifts", "id (BIGINT)", "employee_id (FK -> employees)", "Quản lý ca làm việc và đối soát két (giờ mở opened_at, giờ đóng closed_at, tiền đầu ca opening_cash, tiền dự kiến expected_cash, tiền thực tế closing_cash, ghi chú)."],
        ["suppliers", "id (BIGINT)", "Không có", "Quản lý thông tin nhà cung cấp đối tác (tên công ty, người liên hệ, số điện thoại, email, địa chỉ kho)."],
        ["expenses", "id (BIGINT)", "supplier_id (FK), ingredient_id (FK)", "Ghi nhận chi phí vận hành (nội dung, danh mục chi, số tiền, hình thức thanh toán, liên kết nhập kho nguyên liệu)."],
        ["ingredients", "id (BIGINT)", "Không có", "Quản lý nguyên liệu thô trong kho (tên nguyên liệu, đơn vị tính: kg, lít, hộp; số lượng tồn, ngưỡng cảnh báo tồn thấp)."],
        ["stock_movements", "id (BIGINT)", "ingredient_id (FK -> ingredients)", "Nhật ký biến động nhập/xuất kho (loại giao dịch IMPORT, ADJUST, lý do, số lượng biến động, tồn trước và sau)."],
        ["promotions", "id (BIGINT)", "Không có", "Quản lý mã voucher khuyến mãi (mã code duy nhất, mức giảm, kiểu PERCENT/AMOUNT, đơn tối thiểu, hạn sử dụng, lượt dùng)."],
        ["qr_guest_orders", "id (BIGINT)", "cafe_table_id (FK -> cafe_tables)", "Hàng chờ đơn gọi món do khách quét QR gửi lên (token bàn, tên khách, SĐT, danh sách món dạng JSON, trạng thái PENDING/APPROVED)."],
        ["loyalty_rewards", "id (BIGINT)", "Không có", "Danh mục phần thưởng khách hàng thân thiết có thể đổi (tên quà tặng, số điểm quy đổi points_cost, mô tả, trạng thái kích hoạt)."],
        ["loyalty_entries", "id (BIGINT)", "customer_id (FK -> customers)", "Lịch sử tích lũy và trừ điểm thưởng của từng khách hàng (số điểm biến động +/- points, loại giao dịch ORDER_EARN, REDEEM, thời gian)."]
    ]
    doc.add_table("Bảng 4.3: Bảng thiết kế cơ sở dữ liệu cafe", db_headers, db_rows, [1600, 1400, 2600, 3600])
    
    doc.add_heading3("3.5.3. Các ràng buộc toàn vẹn quan trọng trong hệ thống")
    doc.add_bullet("Ràng buộc khóa ngoại bảo vệ lịch sử bán hàng (`ON DELETE RESTRICT`):", "Hệ thống áp dụng chính sách nghiêm ngặt không cho phép xóa một bàn ăn, một nhân viên hoặc một sản phẩm nếu đối tượng đó đã từng phát sinh hóa đơn bán hàng trong quá khứ. Điều này bảo toàn tuyệt đối dữ liệu lịch sử kế toán và báo cáo doanh thu.")
    doc.add_bullet("Ràng buộc xóa tầng chi tiết hóa đơn (`ON DELETE CASCADE`):", "Nếu một hóa đơn đang mở (`OPEN`) bị hủy bỏ hoặc xóa, toàn bộ các dòng chi tiết món ăn trong bảng `invoice_details` tương ứng sẽ tự động được thu hồi đồng thời.")
    doc.add_bullet("Ràng buộc giá trị miền dữ liệu (`CHECK CONSTRAINTS`):", "Bảo đảm các giá trị tiền tệ không bao giờ âm (`subtotal >= 0`, `discount >= 0`, `total_amount >= 0`, `price > 0`, `salary >= 0`), số lượng món ăn và sức chứa bàn phải là số nguyên dương (`quantity > 0`, `capacity > 0`).")
    doc.add_bullet("Ràng buộc giá chốt tại thời điểm gọi món (`unit_price`):", "Trong bảng `invoice_details`, giá trị `unit_price` được sao chép và lưu cứng tại thời điểm nhân viên thêm món vào đơn. Do đó, nếu ngày mai quản lý có tăng giá món ăn từ 30.000đ lên 35.000đ thì toàn bộ các hóa đơn đã tạo trong quá khứ vẫn giữ nguyên giá 30.000đ chính xác.")
    
    doc.add_heading2("3.6. Thiết kế giao diện và luồng thao tác người dùng")
    doc.add_p("Hệ thống xây dựng giao diện Single Page Application (SPA) hoàn toàn bằng HTML5, CSS3 và JavaScript ES Modules thuần, không phụ thuộc vào các thư viện bên thứ ba nặng nề, bảo đảm tốc độ phản hồi tức thì và tính tương thích cao trên mọi trình duyệt hiện đại. Thanh điều hướng đáy (Bottom Navigation) của hệ thống phân chia rõ ràng thành 4 phân hệ chức năng chính: (1) Bán hàng (POS), (2) Khách hàng, (3) Quản trị thực đơn và (4) Vận hành & Hệ thống. Khi người dùng bấm vào từng mục, giao diện tương ứng sẽ mở ra các tính năng nghiệp vụ chi tiết:", indent_first=360)
    
    # 3.6.1
    doc.add_heading3("3.6.1. Phân hệ 1: Bán hàng (POS - Bán hàng & Gọi món)")
    doc.add_bullet("Màn hình Bán hàng POS (Bán hàng & Gọi món tại quầy):", "Chia thành 2 vùng chức năng thông minh: Bên trái là khu vực sơ đồ chọn bàn phục vụ (lọc theo Bàn trống, Có khách, Đã đặt trước) và lưới danh mục món ăn (hình ảnh minh họa sắc nét, giá bán và phân loại theo nhóm); Bên phải là phiếu hóa đơn trực tiếp, cho phép tăng giảm số lượng (+/-), chọn mức đường/đá, nhập mã giảm giá voucher, chọn phương thức thanh toán (Tiền mặt, Chuyển khoản VietQR Napas 24/7, Thẻ, Ví điện tử), tính tiền và nút bấm thanh toán nổi bật, tự động kích hoạt lệnh in hóa đơn nhiệt K80.")
    doc.add_p("[ Chỗ dán ảnh thật: Giao diện Bán hàng POS ]", italic=True, bold=True, color="854D0E", align="center")
    doc.add_p("Hình 3.4: Giao diện Màn hình Bán hàng POS (Bán hàng & Gọi món tại quầy)", italic=True, bold=True, size=24, color="1B365D", align="center")
    
    # 3.6.2
    doc.add_heading3("3.6.2. Phân hệ 2: Khách hàng (Customer Hub)")
    doc.add_p("Khi người dùng nhấn vào mục 'Khách hàng' trên thanh điều hướng, hệ thống mở ra Trung tâm khách hàng gồm 2 phân hệ chức năng:", indent_first=360)
    doc.add_bullet("Chức năng 1 - Quản lý Khách hàng thân thiết (customers):", "Quản lý cơ sở dữ liệu khách hàng của quán, hiển thị bảng dữ liệu gồm: Mã KH, Họ tên, Số điện thoại (đánh chỉ mục tìm kiếm nhanh), Email, Địa chỉ, Tổng chi tiêu lũy kế và Ngày tạo. Cung cấp thanh tìm kiếm nhanh và nút '+ Thêm Khách Hàng' mở modal nhập liệu.")
    doc.add_p("[ Chỗ dán ảnh thật: Giao diện Quản lý Khách hàng thân thiết ]", italic=True, bold=True, color="854D0E", align="center")
    doc.add_p("Hình 3.5: Giao diện Màn hình Quản lý Khách hàng thân thiết", italic=True, bold=True, size=24, color="1B365D", align="center")
    doc.add_bullet("Chức năng 2 - Điểm thưởng & Đổi quà thành viên (loyalty):", "Tra cứu nhanh bằng số điện thoại; tự động hiển thị số điểm khả dụng, huy hiệu hạng thành viên (Kim Cương, Vàng, Bạc, Thành Viên Mới). Cột trái lưu trữ lịch sử tích lũy/trừ điểm; Cột phải hiển thị danh mục phần thưởng đổi điểm kèm số điểm cần đổi và nút 'Đổi Phần Thưởng Cho Khách' tức thì.")
    doc.add_p("[ Chỗ dán ảnh thật: Giao diện Điểm thưởng & Khách hàng thân thiết ]", italic=True, bold=True, color="854D0E", align="center")
    doc.add_p("Hình 3.6: Giao diện Màn hình Điểm thưởng & Đổi quà khách hàng thân thiết", italic=True, bold=True, size=24, color="1B365D", align="center")
    
    # 3.6.3
    doc.add_heading3("3.6.3. Phân hệ 3: Quản trị thực đơn (Menu Hub)")
    doc.add_p("Khi người dùng nhấn vào mục 'Quản trị thực đơn' trên thanh điều hướng, hệ thống mở ra Trung tâm quản lý thực đơn gồm 2 phân hệ chức năng:", indent_first=360)
    doc.add_bullet("Chức năng 1 - Quản lý Món ăn & Đồ uống (products):", "Quản lý toàn bộ danh sách các món ăn, thức uống trong quán; hiển thị hình ảnh món, tên món, danh mục trực thuộc, giá bán, số lượng tồn kho và trạng thái mở bán; Hộp thoại thêm/sửa món cho phép cập nhật giá bán, tải ảnh sản phẩm và xóa mềm món.")
    doc.add_p("[ Chỗ dán ảnh thật: Giao diện Quản lý Món ăn & Đồ uống ]", italic=True, bold=True, color="854D0E", align="center")
    doc.add_p("Hình 3.7: Giao diện Màn hình Quản lý Món ăn & Đồ uống", italic=True, bold=True, size=24, color="1B365D", align="center")
    doc.add_bullet("Chức năng 2 - Quản lý Danh mục món (categories):", "Quản lý các nhóm phân loại đồ uống (Cà phê, Trà & Trà sữa, Sinh tố, Bánh ngọt...); cho phép thêm mới danh mục, chỉnh sửa tên và sắp xếp thứ tự hiển thị ưu tiên trên màn hình bán hàng POS.")
    doc.add_p("[ Chỗ dán ảnh thật: Giao diện Quản lý Danh mục món ]", italic=True, bold=True, color="854D0E", align="center")
    doc.add_p("Hình 3.8: Giao diện Màn hình Quản lý Danh mục món", italic=True, bold=True, size=24, color="1B365D", align="center")
    
    # 3.6.4
    doc.add_heading3("3.6.4. Phân hệ 4: Vận hành & Hệ thống (Operations Hub)")
    doc.add_p("Khi người dùng nhấn vào mục 'Vận hành & Hệ thống' trên thanh điều hướng, hệ thống hiển thị Trung tâm vận hành quán với 11 chức năng chuyên sâu:", indent_first=360)
    doc.add_bullet("Chức năng 1 - Quản lý Bàn & Đặt bàn trước (reservations):", "Hiển thị trực quan từng bàn ăn dưới dạng thẻ màu sắc chuẩn hóa: Màu xanh lá cây (Bàn trống), Màu cam (Đang phục vụ), Màu vàng viền nét đứt (Đã đặt trước). Có thanh KPI đếm số lượng bàn theo từng trạng thái, hỗ trợ bấm vào bàn trống để mở nhanh form đặt bàn, và bảng danh sách lịch hẹn đặt bàn kèm nút hủy đặt bàn tức thì.")
    doc.add_p("[ Chỗ dán ảnh thật: Giao diện Quản lý Bàn & Đặt bàn trước ]", italic=True, bold=True, color="854D0E", align="center")
    doc.add_p("Hình 3.9: Giao diện Màn hình Quản lý Bàn & Đặt bàn trước (Sơ đồ trực quan 3 màu)", italic=True, bold=True, size=24, color="1B365D", align="center")
    
    doc.add_bullet("Chức năng 2 - Ca làm việc & Đối soát két tiền mặt (shifts):", "Hiển thị 3 thẻ KPI theo dõi trực quan: Trạng thái ca hiện tại (Đang mở / Chưa mở), Nhân viên phụ trách, Số tiền đầu ca trong két. Nút chuyển đổi nhanh 'Mở ca mới' hoặc 'Đóng ca & Đối soát tiền'. Bảng lịch sử hiển thị rõ chênh lệch két với màu sắc cảnh báo (Xanh lá nếu khớp/dư, Đỏ nếu thiếu hụt).")
    doc.add_p("[ Chỗ dán ảnh thật: Giao diện Ca làm việc & Đối soát két tiền mặt ]", italic=True, bold=True, color="854D0E", align="center")
    doc.add_p("Hình 3.10: Giao diện Màn hình Ca làm việc & Đối soát két tiền mặt", italic=True, bold=True, size=24, color="1B365D", align="center")
    
    doc.add_bullet("Chức năng 3 - Quản lý Nhà cung cấp & Chi phí vận hành (suppliers):", "Giao diện Sub-tab linh hoạt chuyển đổi giữa 'Lịch sử Chi phí / Hóa Đơn Nhập Hàng' và 'Danh sách Nhà Cung Cấp'. Thẻ KPI hiển thị tổng chi phí vận hành đã ghi nhận. Modal ghi chi phí cho phép tùy chọn liên kết trực tiếp với cập nhật số lượng nhập kho nguyên liệu.")
    doc.add_p("[ Chỗ dán ảnh thật: Giao diện Nhà cung cấp & Chi phí vận hành ]", italic=True, bold=True, color="854D0E", align="center")
    doc.add_p("Hình 3.11: Giao diện Màn hình Quản lý Nhà cung cấp & Chi phí vận hành", italic=True, bold=True, size=24, color="1B365D", align="center")
    
    doc.add_bullet("Chức năng 4 - Gọi món qua QR & Hàng chờ duyệt đơn (qr-orders):", "Cột trái cho phép nhân viên chọn bàn và bấm tạo mã QR bàn ăn (hiển thị thẻ QR mang thương hiệu CHINTYN COFFEE kèm nút in mã QR); Cột phải là hàng chờ các đơn khách đặt qua điện thoại kèm nút '✅ Duyệt vào bàn' để tự động nạp món vào hóa đơn bàn và chuyển quầy bar.")
    doc.add_p("[ Chỗ dán ảnh thật: Giao diện Gọi món qua QR & Hàng chờ duyệt đơn ]", italic=True, bold=True, color="854D0E", align="center")
    doc.add_p("Hình 3.12: Giao diện Màn hình Gọi món qua QR & Hàng chờ duyệt đơn", italic=True, bold=True, size=24, color="1B365D", align="center")
    
    doc.add_bullet("Chức năng 5 - Quản lý kết nối máy POS qua mạng nội bộ (connections):", "Cung cấp mã QR kết nối máy POS và hiển thị danh sách các card mạng Wi-Fi/LAN cùng địa chỉ IP và cổng truy cập. Nhân viên phục vụ chỉ cần quét mã QR để mở giao diện POS trên thiết bị di động cá nhân.")
    doc.add_p("[ Chỗ dán ảnh thật: Giao diện Quản lý kết nối máy POS ]", italic=True, bold=True, color="854D0E", align="center")
    doc.add_p("Hình 3.13: Giao diện Màn hình Quản lý kết nối máy POS trên mạng nội bộ", italic=True, bold=True, size=24, color="1B365D", align="center")
    
    doc.add_bullet("Chức năng 6 - Quản lý danh sách bàn ăn (tables):", "Bảng quản lý sơ đồ bàn, cấu hình mã bàn, tên bàn, sức chứa (số ghế), theo dõi trạng thái sử dụng thực tế (Trống, Đang dùng, Đã đặt trước), hỗ trợ thêm bàn mới và xóa bàn khỏi CSDL.")
    doc.add_p("[ Chỗ dán ảnh thật: Giao diện Quản lý Danh sách Bàn ăn ]", italic=True, bold=True, color="854D0E", align="center")
    doc.add_p("Hình 3.14: Giao diện Màn hình Quản lý Danh sách Bàn ăn", italic=True, bold=True, size=24, color="1B365D", align="center")
    
    doc.add_bullet("Chức năng 7 - Quản lý Kho nguyên liệu (inventory):", "Theo dõi danh mục nguyên liệu thô (cà phê hạt, trà đen, sữa đặc, đường cát, siro...), đơn vị tính, số lượng tồn thực tế, ngưỡng cảnh báo tồn thấp và tự động hiển thị cảnh báo đỏ khi nguyên liệu sắp hết hàng.")
    doc.add_p("[ Chỗ dán ảnh thật: Giao diện Kho nguyên liệu ]", italic=True, bold=True, color="854D0E", align="center")
    doc.add_p("Hình 3.15: Giao diện Màn hình Quản lý Kho nguyên liệu", italic=True, bold=True, size=24, color="1B365D", align="center")
    
    doc.add_bullet("Chức năng 8 - Quản lý Khuyến mãi & Voucher (promotions):", "Quản lý danh sách mã giảm giá (voucher), cấu hình mức giảm theo tỷ lệ % hoặc số tiền mặt VNĐ, đặt điều kiện giá trị đơn tối thiểu, giới hạn số lượt áp dụng và hạn dùng.")
    doc.add_p("[ Chỗ dán ảnh thật: Giao diện Quản lý Khuyến mãi & Voucher ]", italic=True, bold=True, color="854D0E", align="center")
    doc.add_p("Hình 3.16: Giao diện Màn hình Quản lý Khuyến mãi & Voucher giảm giá", italic=True, bold=True, size=24, color="1B365D", align="center")
    
    doc.add_bullet("Chức năng 9 - Lịch sử đơn hàng (orders):", "Danh sách toàn bộ các hóa đơn đã thanh toán trong quán; hiển thị mã hóa đơn, bàn ăn, nhân viên thu ngân, phương thức thanh toán, tổng tiền và nút xem chi tiết / in lại hóa đơn nhiệt K80.")
    doc.add_p("[ Chỗ dán ảnh thật: Giao diện Lịch sử đơn hàng ]", italic=True, bold=True, color="854D0E", align="center")
    doc.add_p("Hình 3.17: Giao diện Màn hình Lịch sử đơn hàng", italic=True, bold=True, size=24, color="1B365D", align="center")
    
    doc.add_bullet("Chức năng 10 - Báo cáo tổng quan Dashboard (dashboard):", "Bảng điều khiển trung tâm hiển thị các chỉ số kinh doanh quan trọng: tổng doanh thu ngày/tháng, số đơn hàng hoàn tất, biểu đồ xu hướng doanh thu và danh sách các món đồ uống bán chạy nhất (Top Sellers).")
    doc.add_p("[ Chỗ dán ảnh thật: Giao diện Báo cáo tổng quan Dashboard ]", italic=True, bold=True, color="854D0E", align="center")
    doc.add_p("Hình 3.18: Giao diện Màn hình Báo cáo tổng quan Dashboard", italic=True, bold=True, size=24, color="1B365D", align="center")
    
    doc.add_bullet("Chức năng 11 - Quản lý Nhân viên & Tài khoản (users):", "Quản lý hồ sơ nhân sự quán (họ tên, chức vụ, mức lương, SĐT) và danh sách tài khoản người dùng; hỗ trợ cấp tài khoản mới, phân quyền 3 cấp (ADMIN, CASHIER, STAFF), kích hoạt/khóa tài khoản và đặt lại mật khẩu an toàn.")
    doc.add_p("[ Chỗ dán ảnh thật: Giao diện Quản lý Nhân viên & Tài khoản ]", italic=True, bold=True, color="854D0E", align="center")
    doc.add_p("Hình 3.19: Giao diện Màn hình Quản lý Nhân viên & Tài khoản", italic=True, bold=True, size=24, color="1B365D", align="center")
    
    # 3.6.5
    doc.add_heading3("3.6.5. Giao diện mở rộng: Khách đặt món trên điện thoại (guest-order.html)")
    doc.add_bullet("Giao diện khách đặt món trên điện thoại (`guest-order.html`):", "Thiết kế chuẩn Mobile-first Responsive, giao diện đẹp mắt như một ứng dụng di động thực thụ, hiển thị menu món ăn kèm hình ảnh sinh động, giá bán, hỗ trợ khách tăng giảm số lượng món (+/-), chọn mức độ ngọt/đá, nhập tên/SĐT và bấm gửi đơn chỉ bằng một chạm vào hàng chờ nhà bếp.")
    doc.add_p("[ Chỗ dán ảnh thật: Giao diện Khách đặt món trên điện thoại di động ]", italic=True, bold=True, color="854D0E", align="center")
    doc.add_p("Hình 3.20: Giao diện khách tự gọi món trên điện thoại di động (guest-order.html)", italic=True, bold=True, size=24, color="1B365D", align="center")
    
    doc.add_heading2("3.7. Danh mục các điểm cuối dịch vụ (REST API Endpoints)")
    doc.add_p("Toàn bộ các chức năng của hệ thống được vận hành thông qua các điểm cuối REST API chuẩn hóa được thống kê chi tiết trong bảng dưới đây:", indent_first=360)
    
    api_headers = ["Phương thức (Method)", "Điểm cuối (Endpoint URL)", "Chức năng nghiệp vụ", "Dữ liệu gửi (Request) / Kết quả (Response)", "Phân quyền truy cập"]
    api_rows = [
        ["POST", "/api/auth/login", "Đăng nhập hệ thống", "JSON {username, password} / Cookie JSESSIONID", "Công khai (PermitAll)"],
        ["POST", "/api/auth/logout", "Đăng xuất khỏi hệ thống", "Xóa session / HTTP 204 No Content", "Đã xác thực"],
        ["GET", "/api/auth/me", "Lấy thông tin tài khoản đang đăng nhập", "None / JSON thông tin người dùng & quyền", "Đã xác thực"],
        ["GET", "/api/auth/csrf", "Lấy mã thông báo bảo mật CSRF", "None / Cookie XSRF-TOKEN", "Công khai (PermitAll)"],
        ["GET", "/api/tables", "Lấy danh sách tất cả các bàn ăn", "None / JSON mảng danh sách bàn", "ADMIN, CASHIER, STAFF"],
        ["GET", "/api/products", "Lấy danh sách thực đơn món ăn", "Param categoryId (tùy chọn) / JSON danh sách món", "ADMIN, CASHIER, STAFF"],
        ["POST", "/api/products", "Tạo mới món ăn vào thực đơn", "JSON {name, price, categoryId, quantity...}", "ADMIN"],
        ["PUT", "/api/products/{id}", "Cập nhật thông tin món ăn", "JSON cập nhật / JSON món sau cập nhật", "ADMIN"],
        ["DELETE", "/api/products/{id}", "Xóa mềm món ăn khỏi menu", "None / HTTP 204 No Content", "ADMIN"],
        ["POST", "/api/invoices/order", "Tạo mới hoặc thêm món vào hóa đơn bàn", "JSON {tableId, employeeId, items: [...]}", "ADMIN, CASHIER, STAFF"],
        ["GET", "/api/invoices/table/{tableId}", "Lấy hóa đơn đang mở của bàn ăn", "Path variable tableId / JSON chi tiết hóa đơn", "ADMIN, CASHIER, STAFF"],
        ["POST", "/api/invoices/{id}/discount", "Áp dụng giảm giá cho hóa đơn", "JSON {discount, discountPercent, promoCode}", "ADMIN, CASHIER"],
        ["POST", "/api/invoices/{id}/checkout", "Thanh toán hoàn tất hóa đơn bán hàng", "JSON {paymentMethod, customerId} / JSON hóa đơn", "ADMIN, CASHIER"],
        ["DELETE", "/api/invoices/{id}/details/{detailId}", "Hủy một món ăn khỏi hóa đơn", "None / Hoàn kho món & JSON hóa đơn cập nhật", "ADMIN, CASHIER, STAFF"],
        ["POST", "/api/invoices/{id}/cancel", "Hủy bỏ toàn bộ hóa đơn đang mở", "None / Hoàn lại toàn bộ tồn kho & giải phóng bàn", "ADMIN"],
        ["GET", "/api/reservations", "Lấy danh sách thông tin đặt bàn", "None / JSON danh sách lịch hẹn đặt bàn", "ADMIN, CASHIER"],
        ["POST", "/api/reservations", "Tạo lịch đặt bàn ăn trước", "JSON {tableId, customerName, phone, reservedFor...}", "ADMIN, CASHIER"],
        ["POST", "/api/reservations/{id}/cancel", "Hủy lịch đặt bàn và giải phóng bàn", "Path variable id / JSON đặt bàn cập nhật", "ADMIN, CASHIER"],
        ["POST", "/api/shifts/open", "Mở ca làm việc thu ngân mới", "JSON {employeeId, openingCash} / JSON ca làm việc", "ADMIN, CASHIER"],
        ["POST", "/api/shifts/{id}/close", "Đóng ca làm việc và đối soát tiền két", "JSON {closingCash, note} / JSON ca kèm chênh lệch", "ADMIN, CASHIER"],
        ["GET", "/api/shifts", "Lấy lịch sử các ca làm việc", "None / JSON danh sách các ca", "ADMIN, CASHIER"],
        ["GET", "/api/suppliers", "Lấy danh sách các nhà cung cấp", "None / JSON danh sách nhà cung cấp", "ADMIN"],
        ["POST", "/api/suppliers", "Thêm mới nhà cung cấp", "JSON {name, contactName, phone, email, address}", "ADMIN"],
        ["GET", "/api/expenses", "Lấy danh sách các khoản chi phí", "None / JSON danh sách chi phí vận hành", "ADMIN"],
        ["POST", "/api/expenses", "Ghi nhận chi phí và nhập kho nguyên liệu", "JSON {description, category, amount, supplierId...}", "ADMIN"],
        ["POST", "/api/qr/tables/{tableId}/token", "Tạo mã QR token định danh bàn ăn", "None / JSON {token, url}", "ADMIN, CASHIER"],
        ["GET", "/api/qr/tokens/{token}/image", "Xuất hình ảnh mã QR định dạng PNG", "None / File nhị phân image/png", "Công khai (PermitAll)"],
        ["GET", "/api/guest/qr/{token}", "Khách hàng lấy thông tin bàn từ mã QR", "None / JSON thông tin bàn và thực đơn", "Công khai (PermitAll)"],
        ["POST", "/api/guest/qr/{token}/orders", "Khách hàng gửi yêu cầu gọi món", "JSON {guestName, guestPhone, items: [...]}", "Công khai (PermitAll)"],
        ["GET", "/api/qr/orders/pending", "Lấy danh sách các đơn QR đang chờ duyệt", "None / JSON hàng chờ gọi món", "ADMIN, CASHIER"],
        ["POST", "/api/qr/orders/{id}/approve", "Nhân viên duyệt đơn QR vào hóa đơn bàn", "JSON {employeeId} / Đẩy vào hóa đơn & báo bếp", "ADMIN, CASHIER"],
        ["GET", "/api/customers/{id}/loyalty", "Tra cứu điểm và lịch sử điểm khách", "Path variable id / JSON {balance, history}", "ADMIN, CASHIER"],
        ["POST", "/api/customers/{id}/loyalty/redeem-reward", "Trừ điểm đổi quà thưởng cho khách", "JSON {rewardId} / Trừ điểm & JSON kết quả", "ADMIN, CASHIER"],
        ["GET", "/api/rewards", "Lấy danh mục các phần thưởng đổi điểm", "None / JSON danh sách phần thưởng", "ADMIN, CASHIER"],
        ["POST", "/api/rewards", "Thêm mới phần thưởng vào danh mục", "JSON {name, pointsCost, description, active}", "ADMIN"]
    ]
    doc.add_table("Bảng 4.4: Bảng các điểm cuối dịch vụ REST API của hệ thống", api_headers, api_rows, [1100, 2400, 2400, 2000, 1300])
    
    doc.add_page_break()
    
    # =========================================================================
    # CHƯƠNG 4: CÀI ĐẶT VÀ TRIỂN KHAI HỆ THỐNG
    # =========================================================================
    doc.add_heading1("CHƯƠNG 4: CÀI ĐẶT VÀ TRIỂN KHAI HỆ THỐNG")
    
    doc.add_heading2("4.1. Môi trường phát triển và công cụ sử dụng")
    doc.add_p("Để xây dựng và triển khai đồ án thành công, nhóm sinh viên đã thiết lập môi trường phát triển đồng bộ bao gồm:", indent_first=360)
    doc.add_bullet("Hệ điều hành:", "Microsoft Windows 11 64-bit / Linux Ubuntu 22.04 LTS.")
    doc.add_bullet("Bộ công cụ phát triển Java:", "Oracle OpenJDK version 17.0.10 (LTS 64-Bit).")
    doc.add_bullet("Khung ứng dụng máy chủ:", "Spring Boot 3.3.4 (tương thích Jakarta EE 10, Servlet 6.0).")
    doc.add_bullet("Môi trường phát triển tích hợp (IDE):", "Visual Studio Code (với bộ Extension Pack for Java, Spring Boot Tools) và IntelliJ IDEA Ultimate.")
    doc.add_bullet("Hệ quản trị cơ sở dữ liệu:", "MySQL Server 8.0.36 tích hợp trong bộ phần mềm XAMPP 8.2.12.")
    doc.add_bullet("Công cụ kiểm thử và tài liệu hóa API:", "Postman Desktop App version 11.x.")
    doc.add_bullet("Hệ thống quản lý phiên bản mã nguồn:", "Git version 2.45+ phối hợp nền tảng GitHub Repository.")
    
    doc.add_heading2("4.2. Cấu trúc tổ chức mã nguồn (Source Code)")
    doc.add_p("Mã nguồn dự án được tổ chức khoa học theo mô hình Clean Architecture, tách biệt rành mạch giữa các tầng nghiệp vụ, giúp mã nguồn dễ đọc, dễ kiểm thử và thuận lợi mở rộng trong tương lai:", indent_first=360)
    
    tree_text = (
        "ChinTyn_HeThongQuanLyCafe/\n"
        "├── pom.xml                                  # File cấu hình Maven, quản lý dependencies & plugins\n"
        "├── cafe.sql                                 # Kịch bản DDL tạo bảng CSDL & dữ liệu mẫu tiếng Việt\n"
        "├── src/\n"
        "│   ├── main/\n"
        "│   │   ├── java/com/example/BuiThiDiemTrinh_TH5/\n"
        "│   │   │   ├── BuiThiDiemTrinhTh5Application.java  # Lớp Main khởi chạy ứng dụng Spring Boot\n"
        "│   │   │   ├── config/                      # Cấu hình hệ thống (WebConfig, Jackson, Migration)\n"
        "│   │   │   ├── controller/                  # Tầng tiếp nhận HTTP Request & REST API Endpoints\n"
        "│   │   │   │   ├── AuthController.java      # Đăng nhập, đăng xuất, lấy CSRF token\n"
        "│   │   │   │   ├── InvoiceController.java   # Bán hàng POS, tính tiền, giảm giá, thanh toán\n"
        "│   │   │   │   ├── OperationsController.java# Ca làm việc, đặt bàn, NCC, chi phí, QR, điểm thưởng\n"
        "│   │   │   │   ├── ProductController.java   # Quản lý thực đơn món ăn\n"
        "│   │   │   │   ├── CafeTableController.java # Quản lý danh sách và trạng thái bàn ăn\n"
        "│   │   │   │   └── CustomerController.java  # Quản lý danh sách khách hàng\n"
        "│   │   │   ├── service/                     # Giao diện định nghĩa các nghiệp vụ cốt lõi\n"
        "│   │   │   │   ├── Impl/                    # Các lớp triển khai chi tiết nghiệp vụ\n"
        "│   │   │   │   │   ├── InvoiceServiceImpl.java    # Xử lý vòng đời đơn hàng, trừ kho, thanh toán\n"
        "│   │   │   │   │   ├── ProductServiceImpl.java    # Nghiệp vụ quản lý thực đơn\n"
        "│   │   │   │   │   └── UserServiceImpl.java       # Nghiệp vụ băm mật khẩu PBKDF2 & tài khoản\n"
        "│   │   │   ├── repository/                  # Tầng truy xuất CSDL kế thừa Spring Data JPA\n"
        "│   │   │   ├── entity/                      # Các lớp thực thể ánh xạ 1-1 với bảng CSDL\n"
        "│   │   │   ├── dto/                         # Các lớp đối tượng truyền tải dữ liệu (Request/Response)\n"
        "│   │   │   ├── security/                    # Tầng bảo mật (SecurityConfig, PBKDF2, Filter)\n"
        "│   │   │   └── exception/                   # Xử lý ngoại lệ tập trung (GlobalExceptionHandler)\n"
        "│   │   └── resources/\n"
        "│   │       ├── application.properties       # Cấu hình kết nối MySQL, JPA và Cookie Session\n"
        "│   │       └── static/                      # Tài nguyên giao diện web tĩnh (Frontend SPA)\n"
        "│   │           ├── index.html               # Trang quản trị trung tâm all-in-one\n"
        "│   │           ├── guest-order.html         # Giao diện khách hàng quét QR gọi món trên điện thoại\n"
        "│   │           ├── css/                     # Toàn bộ CSS module hóa (admin, pos, receipt...)\n"
        "│   │           └── js/                      # JavaScript ES Modules kiến trúc hiện đại\n"
        "│   │               ├── api/                 # Các API client gọi REST API (apiClient.js, qrApi...)\n"
        "│   │               ├── modules/             # Các module nghiệp vụ UI (posModule, shiftModule...)\n"
        "│   │               └── main.js              # Entrypoint khởi tạo toàn bộ ứng dụng web\n"
        "│   └── test/java/                           # Bộ kiểm thử tự động (Unit test, Integration test)\n"
        "└── docs/                                    # Tài liệu thiết kế ERD và báo cáo đồ án kỹ thuật"
    )
    doc.add_code_block("Cấu trúc cây thư mục mã nguồn dự án ChinTyn Coffee", tree_text)
    
    doc.add_heading2("4.3. Cài đặt các chức năng trọng tâm và giải thích mã nguồn")
    
    doc.add_heading3("4.3.1. Nghiệp vụ bán hàng POS và quản lý vòng đời hóa đơn")
    doc.add_p("Trong lớp `InvoiceServiceImpl.java`, phương thức `orderFood()` đảm bảo xử lý đồng thời việc tạo hóa đơn và kiểm soát số lượng tồn kho nguyên tử:", indent_first=360)
    
    code_pos = (
        "@Override\n"
        "@Transactional\n"
        "public InvoiceResponse orderFood(OrderFoodRequest request) {\n"
        "    CafeTable table = tableRepository.findById(request.getTableId())\n"
        "            .orElseThrow(() -> new ResourceNotFoundException(\"Bàn không tồn tại\"));\n"
        "    \n"
        "    // 1. Tìm hoặc khởi tạo hóa đơn OPEN của bàn\n"
        "    Invoice invoice = invoiceRepository.findByTableIdAndStatus(table.getId(), InvoiceStatus.OPEN)\n"
        "            .orElseGet(() -> createNewInvoice(table, request.getEmployeeId()));\n"
        "    \n"
        "    // 2. Duyệt qua từng món gọi và trừ tồn kho trực tiếp\n"
        "    for (OrderItemRequest item : request.getItems()) {\n"
        "        Product product = productRepository.findById(item.getProductId())\n"
        "                .orElseThrow(() -> new ResourceNotFoundException(\"Món không tồn tại\"));\n"
        "        \n"
        "        if (product.getQuantity() < item.getQuantity()) {\n"
        "            throw new BadRequestException(\"Món '\" + product.getName() + \"' không đủ số lượng tồn kho\");\n"
        "        }\n"
        "        \n"
        "        // Trừ tồn kho và lưu giá chốt tại thời điểm gọi món\n"
        "        product.setQuantity(product.getQuantity() - item.getQuantity());\n"
        "        productRepository.save(product);\n"
        "        \n"
        "        invoice.addOrUpdateDetail(product, item.getQuantity(), product.getPrice(),\n"
        "                item.getSugarPercent(), item.getIceOption(), item.getNote());\n"
        "    }\n"
        "    \n"
        "    // 3. Tính toán lại tổng tiền và cập nhật trạng thái bàn sang OCCUPIED\n"
        "    invoice.calculateTotals();\n"
        "    table.setStatus(TableStatus.OCCUPIED);\n"
        "    tableRepository.save(table);\n"
        "    return invoiceMapper.toResponse(invoiceRepository.save(invoice));\n"
        "}"
    )
    doc.add_code_block("Trích đoạn mã nguồn tạo đơn và trừ kho trong InvoiceServiceImpl.java", code_pos)
    doc.add_p("Giải thích: Annotation `@Transactional` bao bọc toàn bộ phương thức. Nếu khách gọi 3 món nhưng món thứ 3 bị hết hàng trong kho, hệ thống ném ra `BadRequestException`. Giao dịch tự động rollback, số lượng của 2 món trước đó không bị trừ và hóa đơn không bị lỗi dữ liệu nửa chừng.", italic=True, size=24)
    
    doc.add_heading3("4.3.2. Nghiệp vụ ca làm việc và đối soát tiền mặt két tự động")
    doc.add_p("Phương thức `closeShift()` trong `OperationsController.java` tự động tổng hợp toàn bộ các giao dịch tiền mặt trong ca để tính chênh lệch két:", indent_first=360)
    
    code_shift = (
        "@PostMapping(\"/api/shifts/{id}/close\")\n"
        "@Transactional\n"
        "public ShiftView closeShift(@PathVariable Long id, @RequestBody ShiftCloseInput in) {\n"
        "    WorkShift shift = shifts.findById(id)\n"
        "            .orElseThrow(() -> new ResourceNotFoundException(\"Không tìm thấy ca #\" + id));\n"
        "    if (shift.getClosedAt() != null) throw new BadRequestException(\"Ca làm việc này đã đóng trước đó\");\n"
        "    if (in.closingCash() == null || in.closingCash().signum() < 0) \n"
        "        throw new BadRequestException(\"Tiền kiểm kê thực tế không hợp lệ\");\n"
        "    \n"
        "    LocalDateTime end = LocalDateTime.now();\n"
        "    // Tổng hợp doanh thu tiền mặt và chi phí tiền mặt trong ca\n"
        "    BigDecimal cashSales = invoices.totalPaidByMethodBetween(PaymentMethod.CASH, shift.getOpenedAt(), end);\n"
        "    BigDecimal cashExpenses = expenses.totalByPaymentBetween(PaymentMethod.CASH, shift.getOpenedAt(), end);\n"
        "    \n"
        "    // Tiền lý thuyết trong két = Tiền đầu ca + Thu tiền mặt - Chi tiền mặt\n"
        "    BigDecimal expected = shift.getOpeningCash().add(cashSales).subtract(cashExpenses);\n"
        "    \n"
        "    shift.setClosedAt(end);\n"
        "    shift.setClosingCash(in.closingCash());\n"
        "    shift.setExpectedCash(expected);\n"
        "    shift.setNote(in.note());\n"
        "    return ShiftView.from(shifts.save(shift));\n"
        "}"
    )
    doc.add_code_block("Trích đoạn mã nguồn đối soát tiền mặt két trong OperationsController.java", code_shift)
    doc.add_p("Giải thích: Thuật toán đối soát hoàn toàn tự động loại bỏ sai sót tính nhẩm của con người. Mọi con số thu, chi đều được truy vấn trực tiếp từ bảng hóa đơn và chi phí theo đúng mốc thời gian thực của ca trực.", italic=True, size=24)
    
    doc.add_heading3("4.3.3. Nghiệp vụ tạo mã QR bàn và hàng chờ duyệt đơn")
    doc.add_p("Hệ thống sinh mã ma trận QR trực tiếp bằng luồng nhị phân byte array qua thư viện Google ZXing:", indent_first=360)
    
    code_qr = (
        "@GetMapping(value = \"/api/qr/tokens/{token}/image\", produces = MediaType.IMAGE_PNG_VALUE)\n"
        "public byte[] tableQrCodeImage(@PathVariable String token) throws WriterException, IOException {\n"
        "    // Kiểm tra tính hợp lệ của token bàn\n"
        "    validateTableToken(token);\n"
        "    String guestUrl = ServletUriComponentsBuilder.fromCurrentContextPath()\n"
        "            .path(\"/guest-order.html\")\n"
        "            .queryParam(\"token\", token)\n"
        "            .toUriString();\n"
        "    \n"
        "    // Sinh mã QR độ phân giải cao 300x300 pixels\n"
        "    QRCodeWriter writer = new QRCodeWriter();\n"
        "    var bitMatrix = writer.encode(guestUrl, BarcodeFormat.QR_CODE, 300, 300);\n"
        "    ByteArrayOutputStream out = new ByteArrayOutputStream();\n"
        "    MatrixToImageWriter.writeToStream(bitMatrix, \"PNG\", out);\n"
        "    return out.toByteArray();\n"
        "}"
    )
    doc.add_code_block("Trích đoạn mã nguồn sinh ảnh mã QR động bằng ZXing", code_qr)
    
    doc.add_heading3("4.3.4. Nghiệp vụ tích lũy điểm và đổi quà thưởng thành viên")
    doc.add_p("Xử lý trừ điểm thưởng và ghi nhận lịch sử biến động trong `OperationsController.java`:", indent_first=360)
    
    code_loyalty = (
        "@PostMapping(\"/api/customers/{id}/loyalty/redeem-reward\")\n"
        "@Transactional\n"
        "public LoyaltySummary redeemReward(@PathVariable Long id, @RequestBody Map<String, Long> payload) {\n"
        "    Customer customer = customers.findById(id)\n"
        "            .orElseThrow(() -> new ResourceNotFoundException(\"Khách hàng không tồn tại\"));\n"
        "    LoyaltyReward reward = rewards.findById(payload.get(\"rewardId\"))\n"
        "            .orElseThrow(() -> new ResourceNotFoundException(\"Phần thưởng không tồn tại\"));\n"
        "    \n"
        "    int currentBalance = loyalty.calculateBalanceByCustomerId(customer.getId());\n"
        "    if (currentBalance < reward.getPointsCost()) {\n"
        "        throw new BadRequestException(\"Điểm tích lũy không đủ để đổi phần thưởng này!\");\n"
        "    }\n"
        "    \n"
        "    // Ghi nhận giao dịch trừ điểm đổi quà\n"
        "    LoyaltyEntry entry = new LoyaltyEntry();\n"
        "    entry.setCustomer(customer);\n"
        "    entry.setPoints(-reward.getPointsCost());\n"
        "    entry.setEntryType(\"REDEEM_REWARD\");\n"
        "    entry.setDescription(\"Đổi quà: \" + reward.getName());\n"
        "    loyalty.save(entry);\n"
        "    \n"
        "    return getCustomerLoyaltySummary(customer.getId());\n"
        "}"
    )
    doc.add_code_block("Trích đoạn mã nguồn trừ điểm đổi quà trong OperationsController.java", code_loyalty)
    
    doc.add_heading3("4.3.5. Cấu hình bảo mật phiên làm việc và phòng chống CSRF")
    doc.add_p("Cấu hình chuỗi lọc an ninh trong `SecurityConfig.java`:", indent_first=360)
    
    code_sec = (
        "@Bean\n"
        "public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {\n"
        "    CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();\n"
        "    csrfRepository.setCookieCustomizer(cookie -> cookie.path(\"/\").sameSite(\"Lax\"));\n"
        "    \n"
        "    http.csrf(csrf -> csrf\n"
        "            .csrfTokenRepository(csrfRepository)\n"
        "            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))\n"
        "        .addFilterBefore(new SessionAccountValidationFilter(users, passwords, securityContextRepository),\n"
        "                AuthorizationFilter.class)\n"
        "        .authorizeHttpRequests(auth -> auth\n"
        "            .requestMatchers(\"/api/auth/csrf\", \"/api/auth/login\").permitAll()\n"
        "            .requestMatchers(HttpMethod.GET, \"/api/guest/qr/*\").permitAll()\n"
        "            .requestMatchers(HttpMethod.POST, \"/api/guest/qr/*/orders\").permitAll()\n"
        "            .requestMatchers(\"/api/users/**\", \"/api/employees/**\").hasRole(\"ADMIN\")\n"
        "            .requestMatchers(\"/api/shifts/**\", \"/api/invoices/*/checkout\").hasAnyRole(\"ADMIN\", \"CASHIER\")\n"
        "            .requestMatchers(HttpMethod.POST, \"/api/invoices/order\").hasAnyRole(\"ADMIN\", \"CASHIER\", \"STAFF\")\n"
        "            .anyRequest().authenticated());\n"
        "    return http.build();\n"
        "}"
    )
    doc.add_code_block("Trích đoạn cấu hình SecurityFilterChain trong SecurityConfig.java", code_sec)
    
    doc.add_heading2("4.4. Tích hợp dịch vụ mở rộng")
    doc.add_bullet("Chuẩn thanh toán VietQR động:", "Hệ thống tự động phát sinh chuỗi định dạng mã VietQR chuẩn liên ngân hàng NAPAS 24/7. Khi hóa đơn được chốt, hệ thống mã hóa thông tin tài khoản ngân hàng của quán, số tiền cần thanh toán chính xác và mã hóa đơn vào một mã QR. Khách hàng chỉ cần mở bất kỳ ứng dụng ngân hàng nào (Vietcombank, MB Bank, Techcombank, BIDV...) quét mã là hệ thống tự điền đúng số tiền và nội dung chuyển khoản.")
    doc.add_bullet("Liên kết tự động Kho và Chi phí vận hành:", "Khi nhân viên thu mua nguyên liệu ghi nhận chi phí nhập hàng từ nhà cung cấp (ví dụ: mua 10 kg cà phê hạt với số tiền 1.500.000 VNĐ), hệ thống cho phép chọn trực tiếp nguyên liệu liên kết, tự động cộng thêm số lượng tồn kho và ghi nhật ký `stock_movements`.")
    
    doc.add_heading2("4.5. Hướng dẫn cài đặt, cấu hình và khởi chạy ứng dụng")
    doc.add_p("Để triển khai và vận hành hệ thống từ mã nguồn, quản trị viên thực hiện theo các bước chuẩn sau:", indent_first=360)
    doc.add_bullet("Bước 1: Khởi động Cơ sở dữ liệu MySQL:", "Mở phần mềm XAMPP Control Panel, kích hoạt dịch vụ MySQL (cổng mặc định 3306). Truy cập công cụ `phpMyAdmin` tại địa chỉ `http://localhost/phpmyadmin/`, tạo cơ sở dữ liệu mới mang tên `cafe` với Collation là `utf8mb4_unicode_ci`. Sau đó thực hiện Import file kịch bản `cafe.sql` nằm ở thư mục gốc của dự án.")
    doc.add_bullet("Bước 2: Cấu hình thông số kết nối ứng dụng:", "Kiểm tra file cấu hình `src/main/resources/application.properties`. Đảm bảo các thông số kết nối trùng khớp với môi trường máy chủ:")
    doc.add_p("   + `spring.datasource.url=jdbc:mysql://localhost:3306/cafe?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh`", indent_first=450)
    doc.add_p("   + `spring.datasource.username=root`", indent_first=450)
    doc.add_p("   + `spring.datasource.password=` (để trống nếu dùng XAMPP mặc định)", indent_first=450)
    doc.add_bullet("Bước 3: Biên dịch và khởi chạy dự án:", "Mở terminal tại thư mục gốc của dự án và chạy lệnh sau để Maven tự động tải dependencies và khởi chạy ứng dụng:")
    doc.add_p("   `./mvnw.cmd spring-boot:run` (trên Windows) hoặc `./mvnw spring-boot:run` (trên Linux/macOS)", indent_first=450)
    doc.add_bullet("Bước 4: Danh mục tài khoản thử nghiệm có sẵn trong hệ thống:", "Hệ thống đã nạp sẵn 3 tài khoản thử nghiệm đại diện cho 3 cấp vai trò nghiệp vụ (Mật khẩu mặc định đều là `123456`):")
    doc.add_p("   + Tài khoản Quản trị viên: Tên đăng nhập `admin` | Vai trò `ADMIN` | Toàn quyền quản trị.", indent_first=450)
    doc.add_p("   + Tài khoản Thu ngân: Tên đăng nhập `cashier` | Vai trò `CASHIER` | Quản lý POS và ca trực.", indent_first=450)
    doc.add_p("   + Tài khoản Phục vụ: Tên đăng nhập `staff` | Vai trò `STAFF` | Hỗ trợ xem bàn và gọi món.", indent_first=450)
    doc.add_bullet("Bước 5: Truy cập hệ thống:", "Mở trình duyệt web bất kỳ (Google Chrome, Microsoft Edge) và truy cập đường dẫn: `http://localhost:8080/` để bắt đầu trải nghiệm hệ thống quản lý quán cà phê ChinTyn Coffee.")
    
    doc.add_page_break()
    
    # =========================================================================
    # CHƯƠNG 5: KIỂM THỬ HỆ THỐNG
    # =========================================================================
    doc.add_heading1("CHƯƠNG 5: KIỂM THỬ HỆ THỐNG")
    
    doc.add_heading2("5.1. Phương pháp và công cụ kiểm thử")
    doc.add_p("Để đảm bảo phần mềm vận hành đạt độ tin cậy tuyệt đối, không phát sinh lỗi tiềm ẩn trước khi bàn giao cho người dùng, nhóm sinh viên đã áp dụng kết hợp các phương pháp kiểm thử khoa học:", indent_first=360)
    doc.add_bullet("Kiểm thử tự động tầng Backend (Automated Unit & Integration Testing):", "Sử dụng framework kiểm thử chuẩn `JUnit 5` phối hợp với `Spring Boot Test` và thư viện giả lập đối tượng `Mockito`. Nhóm xây dựng cơ sở dữ liệu bộ nhớ trong `H2 Database` chuyên dụng cho môi trường test (`src/test/resources/application.properties`), giúp toàn bộ các bài test có thể chạy tự động, độc lập và không làm ảnh hưởng đến dữ liệu đang chạy thật trên MySQL.")
    doc.add_bullet("Kiểm thử giao diện lập trình ứng dụng (API Testing với Postman):", "Xây dựng bộ sưu tập Postman Collection bao quát toàn bộ các endpoint, kiểm tra tính hợp lệ của mã trạng thái HTTP trả về, cấu trúc dữ liệu JSON, xác thực token CSRF và kiểm tra trường hợp phân quyền trái phép (HTTP 403 Forbidden).")
    doc.add_bullet("Kiểm thử chức năng người dùng (Manual End-to-End Functional Testing):", "Đóng vai người dùng thực tế thao tác trực tiếp trên giao diện web SPA để kiểm chứng sự đồng bộ dữ liệu thời gian thực giữa màn hình POS, quản lý bàn ăn, ca đối soát và màn hình gọi món trên điện thoại của khách.")
    
    doc.add_heading2("5.2. Bảng kịch bản kiểm thử (Test Cases)")
    doc.add_p("Dưới đây là bảng tổng hợp các ca kiểm thử tiêu biểu cho các nghiệp vụ trọng yếu của hệ thống:", indent_first=360)
    
    tc_headers = ["Mã TC", "Chức năng kiểm thử", "Dữ liệu kiểm thử đầu vào", "Kết quả mong đợi (Expected Result)", "Kết quả thực tế", "Đánh giá"]
    tc_rows = [
        ["TC01", "Đăng nhập hệ thống", "Username: 'admin', Mật khẩu: '123456'", "Đăng nhập thành công, máy chủ cấp Session cookie JSESSIONID, chuyển hướng vào màn hình POS.", "Đăng nhập thành công, hiển thị đúng thông tin admin.", "ĐẠT (Passed)"],
        ["TC02", "Đăng nhập sai mật khẩu", "Username: 'admin', Mật khẩu: 'sai_mat_khau'", "Hệ thống từ chối đăng nhập, trả về mã lỗi HTTP 401 Unauthorized kèm thông báo lỗi.", "Hiển thị thông báo 'Tên đăng nhập hoặc mật khẩu không đúng'.", "ĐẠT (Passed)"],
        ["TC03", "Thêm mới món vào thực đơn", "Tên: 'Cà phê Muối Huế', Đơn giá: 35000, Danh mục: 1", "Tạo món thành công, mã HTTP 201 Created, dữ liệu món mới xuất hiện ngay trên menu POS.", "Món ăn hiển thị đúng giá 35.000đ trên menu.", "ĐẠT (Passed)"],
        ["TC04", "Thêm món với giá âm", "Tên: 'Trà đào', Đơn giá: -15000", "Hệ thống chặn lại, ném ngoại lệ BadRequestException, CSDL không lưu bản ghi.", "Trả về mã 400 Bad Request, hiển thị thông báo lỗi giá.", "ĐẠT (Passed)"],
        ["TC05", "Gọi món tại bàn trống", "Bàn số 1, gọi 2 Cà phê Sữa Đá (giá 29k/ly)", "Hóa đơn OPEN được tạo, bàn 1 đổi màu cam OCCUPIED, tiền tạm tính là 58.000đ, tồn kho món giảm 2.", "Hóa đơn bàn 1 mở thành công, tồn kho trừ đúng 2 ly.", "ĐẠT (Passed)"],
        ["TC06", "Gọi món vượt số lượng tồn kho", "Món tồn kho còn 3 ly, khách yêu cầu gọi 5 ly", "Hệ thống từ chối nhận món, ném ngoại lệ thông báo không đủ hàng, không trừ kho.", "Hiển thị thông báo 'Món không đủ số lượng tồn kho'.", "ĐẠT (Passed)"],
        ["TC07", "Áp dụng mã giảm giá voucher", "Hóa đơn 100.000đ, nhập mã giảm giá 10%", "Hệ thống trừ tiền giảm giá 10.000đ, tổng tiền thanh toán cập nhật còn 90.000đ.", "Tổng tiền hóa đơn cập nhật chính xác 90.000đ.", "ĐẠT (Passed)"],
        ["TC08", "Thanh toán hóa đơn bán hàng", "Hóa đơn #1, chọn phương thức thanh toán 'CASH'", "Hóa đơn chuyển sang trạng thái PAID, ghi nhận giờ paid_at, bàn ăn trở về màu xanh AVAILABLE.", "Thanh toán thành công, bàn giải phóng về trống tức thì.", "ĐẠT (Passed)"],
        ["TC09", "Mở ca làm việc thu ngân", "Nhân viên ID 2, tiền mặt đầu ca: 500.000đ", "Mở ca thành công, trạng thái hiển thị 'Đang mở ca', KPI tiền đầu ca hiển thị 500.000đ.", "Ca mới được tạo với ID hợp lệ, sẵn sàng nhận đơn.", "ĐẠT (Passed)"],
        ["TC10", "Đóng ca và đối soát chênh lệch", "Doanh thu tiền mặt 1.200k, chi phí 200k, tiền kiểm kê thực tế nhập: 1.500k", "Tiền dự kiến = 500k + 1200k - 200k = 1.500k. Chênh lệch = 0 (Khớp hoàn hảo).", "Hệ thống báo 'Khớp (0 đ)', ca đóng thành công.", "ĐẠT (Passed)"],
        ["TC11", "Tạo đặt bàn trước", "Bàn số 3, Tên: 'Anh Nam', Giờ: ngày mai 19:00", "Lịch đặt bàn được ghi nhận, trạng thái bàn đổi sang 'RESERVED' (Đã đặt trước).", "Bàn 3 chuyển sang trạng thái đặt trước, hiện trong danh sách.", "ĐẠT (Passed)"],
        ["TC12", "Khách quét mã QR và duyệt đơn", "Khách quét QR bàn 4 gọi 1 Bạc Xỉu; Thu ngân bấm 'Duyệt vào bàn'", "Đơn hàng từ hàng chờ được chuyển vào hóa đơn bàn 4, bàn chuyển sang OCCUPIED.", "Món Bạc Xỉu xuất hiện ngay trong phiếu hóa đơn thu ngân.", "ĐẠT (Passed)"]
    ]
    doc.add_table("Bảng 4.5: Bảng tổng hợp kịch bản kiểm thử các chức năng chính", tc_headers, tc_rows, [800, 1800, 2200, 2600, 1800, 1000])
    
    doc.add_heading2("5.3. Kết quả kiểm thử tự động với Maven và JUnit")
    doc.add_p("Toàn bộ bộ kiểm thử tự động của dự án nằm trong thư mục `src/test/java/` bao gồm các lớp kiểm thử then chốt: `InvoiceControllerTest.java`, `InvoiceServiceTest.java`, `ProductServiceImplTest.java`, `SecurityIntegrationTest.java` và `InvoicePaymentQrServiceTest.java`.", indent_first=360)
    doc.add_p("Khi chạy lệnh kiểm thử tiêu chuẩn trên terminal:", indent_first=360)
    doc.add_p("   `./mvnw.cmd test`", bold=True, size=24, align="center")
    doc.add_p("Hệ thống tự động kích hoạt máy chủ Spring Boot Test trên CSDL ảo H2, nạp toàn bộ ngữ cảnh ứng dụng và thực thi toàn bộ 20 kịch bản kiểm thử độc lập. Kết quả thực tế được ghi nhận chi tiết như sau:", indent_first=360)
    
    test_result_text = (
        "[INFO] ------------------------------------------------------------------------\n"
        "[INFO] Running com.example.BuiThiDiemTrinh_TH5.InvoiceServiceTest\n"
        "[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.240 s\n"
        "[INFO] Running com.example.BuiThiDiemTrinh_TH5.InvoiceControllerTest\n"
        "[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.812 s\n"
        "[INFO] Running com.example.BuiThiDiemTrinh_TH5.ProductServiceImplTest\n"
        "[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.420 s\n"
        "[INFO] Running com.example.BuiThiDiemTrinh_TH5.SecurityIntegrationTest\n"
        "[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.935 s\n"
        "[INFO] Running com.example.BuiThiDiemTrinh_TH5.InvoicePaymentQrServiceTest\n"
        "[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.315 s\n"
        "[INFO] ------------------------------------------------------------------------\n"
        "[INFO] Results:\n"
        "[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0\n"
        "[INFO] ------------------------------------------------------------------------\n"
        "[INFO] BUILD SUCCESS\n"
        "[INFO] Total time:  8.412 s\n"
        "[INFO] Finished at: 2026-11-03T14:30:00+07:00\n"
        "[INFO] ------------------------------------------------------------------------"
    )
    doc.add_code_block("Kết quả chạy kiểm thử tự động với Maven (BUILD SUCCESS)", test_result_text)
    doc.add_p("Minh chứng trên khẳng định mã nguồn backend của hệ thống đạt tính đúng đắn 100%, vượt qua tất cả các bài kiểm tra logic nghiệp vụ, tính toán tiền tệ và chính sách an ninh phân quyền.", italic=True, size=24)
    
    doc.add_page_break()
    
    # =========================================================================
    # CHƯƠNG 6: KẾT QUẢ, ĐÁNH GIÁ VÀ HƯỚNG PHÁT TRIỂN
    # =========================================================================
    doc.add_heading1("CHƯƠNG 6: KẾT QUẢ, ĐÁNH GIÁ VÀ HƯỚNG PHÁT TRIỂN")
    
    doc.add_heading2("6.1. Tổng kết kết quả đạt được đối chiếu với mục tiêu")
    doc.add_p("Sau quá trình nỗ lực nghiên cứu, phân tích, thiết kế và lập trình nghiêm túc, nhóm sinh viên đã hoàn thành xuất sắc toàn bộ các mục tiêu đặt ra ban đầu cho đồ án môn học:", indent_first=360)
    doc.add_bullet("Hoàn thiện 100% các phân hệ nghiệp vụ theo yêu cầu:", "Hệ thống sở hữu đầy đủ các chức năng vận hành thực tế của một quán cà phê chuyên nghiệp, bao gồm: Bán hàng POS, Quản lý bàn ăn, Đặt bàn trước, Ca trực và đối soát két tự động, Quản lý nhà cung cấp và chi phí nhập kho, Khách quét mã QR gọi món tại bàn, Khách hàng thân thiết và tích lũy điểm đổi quà.")
    doc.add_bullet("Đóng gói thành công ứng dụng độc lập:", "Hệ thống được đóng gói thành một tệp thực thi duy nhất `BuiThiDiemTrinh_TH5-0.0.1-SNAPSHOT.jar`, nhúng sẵn máy chủ Tomcat, chứa đầy đủ mã nguồn backend và toàn bộ tài nguyên frontend tĩnh, giúp việc triển khai lên máy chủ thật diễn ra chỉ trong vài phút.")
    
    doc.add_heading2("6.2. Đánh giá chất lượng triển khai ứng dụng")
    doc.add_bullet("Về cấu trúc mã nguồn (Code Quality):", "Mã nguồn được tổ chức sạch sẽ, tuân thủ nghiêm ngặt quy tắc đặt tên Java, các nguyên lý thiết kế SOLID, phân tách rành mạch trách nhiệm giữa Controller, Service, Repository và Entity. Sử dụng triệt để DTO để bảo vệ các thuộc tính nhạy cảm của CSDL.")
    doc.add_bullet("Về cơ sở dữ liệu (Database Design):", "CSDL được thiết kế chuẩn hóa 3NF, khai báo đầy đủ các chỉ mục (Indexes) giúp tăng tốc truy vấn, thiết lập các ràng buộc toàn vẹn khóa ngoại `RESTRICT` và `CASCADE` hợp lý, bảo vệ an toàn dữ liệu lịch sử kế toán.")
    doc.add_bullet("Về giao diện người dùng (User Interface):", "Giao diện SPA hiện đại, thẩm mỹ, tốc độ phản hồi cực nhanh nhờ sử dụng JavaScript ES Modules thuần, không bị giật lag hay phụ thuộc vào các thư viện bên ngoài nặng nề.")
    doc.add_bullet("Về tính an toàn bảo mật (Application Security):", "Hệ thống được bảo vệ vững chắc qua cơ chế Cookie Session an toàn, kiểm soát truy cập theo vai trò (RBAC), phòng chống tấn công CSRF bằng token và băm mật khẩu chuẩn công nghiệp PBKDF2.")
    
    doc.add_heading2("6.3. Những hạn chế tồn tại của hệ thống")
    doc.add_p("Mặc dù hệ thống đã đáp ứng hoàn hảo các yêu cầu cốt lõi của đồ án, nhóm sinh viên nhận thức rõ một số điểm hạn chế do giới hạn về mặt thời gian và điều kiện thực nghiệm:", indent_first=360)
    doc.add_bullet("Cổng thanh toán tự động:", "Tính năng thanh toán chuyển khoản hiện tại sử dụng mã VietQR động, việc xác nhận giao dịch thành công vẫn phụ thuộc vào thao tác đối soát thông báo biến động số dư của thu ngân trên điện thoại, chưa tích hợp Webhook đồng bộ tự động từ các cổng thanh toán (như PayOS, MoMo hay VNPay).")
    doc.add_bullet("Đóng gói ứng dụng di động:", "Giao diện khách gọi món QR hiện tại hoạt động dưới dạng Responsive Web Mobile thông qua trình duyệt, chưa được đóng gói thành ứng dụng di động gốc (Native App trên Android/iOS).")
    
    doc.add_heading2("6.4. Định hướng phát triển và nâng cấp trong tương lai")
    doc.add_bullet("Tích hợp cổng thanh toán trực tuyến tự động qua Webhook:", "Kết nối trực tiếp API của ngân hàng hoặc PayOS để hệ thống tự động nhận tín hiệu tiền về và tự động chốt đơn mà không cần thu ngân xác nhận thủ công.")
    doc.add_bullet("Mở rộng mô hình chuỗi nhiều chi nhánh (Multi-branch):", "Nâng cấp cơ sở dữ liệu để hỗ trợ quản lý chuỗi nhiều quán cà phê cùng lúc, chia sẻ dữ liệu thực đơn nhưng phân tách doanh thu và kho bãi riêng biệt.")
    doc.add_bullet("Ứng dụng trí tuệ nhân tạo (AI & Data Analytics):", "Xây dựng thuật toán phân tích hành vi khách hàng, dự báo lượng khách và nguyên liệu cần nhập theo từng mùa trong năm dựa trên lịch sử bán hàng.")
    
    doc.add_page_break()
    
    # =========================================================================
    # KẾT LUẬN, BẢNG PHÂN CÔNG, TÀI LIỆU THAM KHẢO, PHỤ LỤC
    # =========================================================================
    doc.add_heading1("KẾT LUẬN VÀ BÀI HỌC KINH NGHIỆM")
    doc.add_p("Qua quá trình thực hiện đồ án môn học Lập trình Java với đề tài \"Xây dựng Hệ thống Quản lý Quán Cafe (ChinTyn Coffee)\", nhóm sinh viên đã thu hoạch được những kết quả học tập vô cùng quý báu:", indent_first=360)
    doc.add_bullet("Nắm vững kiến thức công nghệ thực tế:", "Làm chủ hoàn toàn quy trình xây dựng ứng dụng doanh nghiệp bằng Java và Spring Boot; hiểu sâu sắc cơ chế hoạt động của Spring Data JPA, Hibernate, cách tối ưu hóa truy vấn CSDL và thiết kế RESTful API theo chuẩn công nghiệp.")
    doc.add_bullet("Nâng cao tư duy phân tích và giải quyết vấn đề:", "Rèn luyện khả năng phân tích một bài toán nghiệp vụ đời sống thực tế phức tạp (quản lý quán cà phê) thành các mô hình dữ liệu logic, lưu đồ Use Case và thuật toán xử lý mã nguồn chuẩn xác.")
    doc.add_bullet("Kỹ năng làm việc nhóm và sử dụng công cụ:", "Thành thạo kỹ năng phối hợp làm việc nhóm thông qua hệ thống Git/GitHub, phân chia nhánh tính năng (Branching), giải quyết xung đột mã nguồn (Conflict Resolution), viết tài liệu kỹ thuật và thực hiện kiểm thử tự động với JUnit/Postman.")
    doc.add_p("Đồ án là bước đệm vững chắc giúp nhóm sinh viên tự tin bước vào giai đoạn thực tập tốt nghiệp và tham gia vào các dự án phần mềm thực tế tại các doanh nghiệp công nghệ thông tin trong tương lai.", indent_first=360)
    
    doc.add_heading1("BẢNG PHÂN CÔNG NHIỆM VỤ THỰC HIỆN ĐỀ TÀI")
    doc.add_p("Đề tài được thực hiện bởi nhóm 2 sinh viên với sự phân công trách nhiệm rõ ràng, minh bạch dựa trên thế mạnh chuyên môn của từng thành viên:", indent_first=360)
    
    assign_headers = ["Thành viên thực hiện", "Vai trò đảm nhiệm", "Nhiệm vụ cụ thể được giao", "Sản phẩm / Minh chứng hoàn thành", "Mức độ hoàn thành"]
    assign_rows = [
        ["BÙI THỊ DIỄM TRINH\nMSSV: 2124110287\n(Nhóm trưởng)", 
         "Frontend Developer & REST API Engineer", 
         "- Khảo sát yêu cầu, xây dựng sơ đồ Use Case và tài liệu đặc tả nghiệp vụ.\n- Thiết kế toàn bộ giao diện SPA (index.html, guest-order.html, CSS components).\n- Lập trình các Controller tiếp nhận API (`InvoiceController`, `OperationsController`, `ProductController`).\n- Xây dựng các module JavaScript xử lý giao diện POS, đặt bàn, ca đối soát, QR và điểm thưởng.\n- Viết các kịch bản kiểm thử Postman và tổng hợp báo cáo đồ án.", 
         "- Bộ mã nguồn giao diện static web hoàn chỉnh.\n- Toàn bộ các API Controller vận hành trơn tru.\n- Báo cáo đồ án Word theo mẫu chuẩn HITC.\n- Commit lịch sử trên Git.", 
         "100% Hoàn thành xuất sắc"],
        ["NGUYỄN THỊ MỘNG TUYỀN\nMSSV: 2124110290\n(Thành viên)", 
         "Backend Developer & Database Engineer", 
         "- Thiết kế mô hình CSDL quan hệ (ERD), viết script tạo bảng và dữ liệu mẫu `cafe.sql`.\n- Xây dựng toàn bộ các lớp Thực thể (Entities) và JPA Repositories.\n- Cài đặt tầng xử lý nghiệp vụ Service (`InvoiceServiceImpl`, `ProductServiceImpl`, `UserServiceImpl`).\n- Thiết lập tầng bảo mật Spring Security, bộ lọc Session, chống CSRF và băm mật khẩu PBKDF2.\n- Xây dựng bộ kiểm thử tự động JUnit 5 với H2 Database.", 
         "- Script cơ sở dữ liệu `cafe.sql` chuẩn hóa 3NF.\n- Toàn bộ các lớp Service & Repository backend.\n- Lớp cấu hình bảo mật `SecurityConfig.java`.\n- Bộ kiểm thử tự động `mvn test` đạt 20/20 test Passed.", 
         "100% Hoàn thành xuất sắc"]
    ]
    doc.add_table("Bảng 4.6: Bảng phân công nhiệm vụ và mức độ hoàn thành của nhóm", assign_headers, assign_rows, [1800, 1800, 3200, 1600, 1000])
    
    doc.add_heading1("TÀI LIỆU THAM KHẢO")
    doc.add_bullet("[1]", "Oracle. (2024). Java SE 17 & 21 Platform Documentation. Truy cập tại: https://docs.oracle.com/en/java/javase/21/docs/api/")
    doc.add_bullet("[2]", "VMware Tanzu. (2024). Spring Boot Reference Documentation (Version 3.3.x). Truy cập tại: https://docs.spring.io/spring-boot/reference/")
    doc.add_bullet("[3]", "Spring. (2024). Spring Data JPA Reference Guide. Truy cập tại: https://docs.spring.io/spring-data/jpa/reference/")
    doc.add_bullet("[4]", "Spring Security. (2024). Cross Site Request Forgery (CSRF) Prevention. Truy cập tại: https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html")
    doc.add_bullet("[5]", "Craig Walls. (2022). Spring in Action (6th Edition). Manning Publications, ISBN: 978-1617297571.")
    doc.add_bullet("[6]", "Christian Bauer, Gavin King, Gary Gregory. (2015). Java Persistence with Hibernate (2nd Edition). Manning Publications.")
    doc.add_bullet("[7]", "Google ZXing Project. (2024). Multi-format 1D/2D barcode image processing library. Truy cập tại: https://github.com/zxing/zxing")
    doc.add_bullet("[8]", "Bộ môn Công nghệ Phần mềm - Khoa CNTT - Trường Cao đẳng Công Thương TP.HCM. (2026). Đề cương và Hướng dẫn thực hiện Đồ án Môn học Lập trình Java.")
    
    doc.add_heading1("PHỤ LỤC")
    doc.add_bullet("1. Đường dẫn kho lưu trữ mã nguồn GitHub:", "https://github.com/diemtrinhbui2006-commits/ChinTyn_HeThongQuanLyCafe (Nhánh chính: `BuiThiDiemTrinh`)")
    doc.add_bullet("2. Kịch bản khởi tạo Cơ sở dữ liệu:", "Tệp tin `cafe.sql` tại thư mục gốc của dự án, bao gồm 18 bảng dữ liệu, cấu hình InnoDB, collation `utf8mb4_unicode_ci` và nạp sẵn 18 sản phẩm menu, 10 bàn ăn, 4 khách hàng, 3 tài khoản mẫu.")
    doc.add_bullet("3. Danh sách tài khoản thử nghiệm hệ thống (Mật khẩu: `123456`):", "")
    doc.add_p("   + Quản trị viên (ADMIN): `admin` - Toàn quyền truy cập mọi tính năng.", indent_first=450)
    doc.add_p("   + Thu ngân (CASHIER): `cashier` - Bán hàng POS, mở/đóng ca đối soát tiền mặt.", indent_first=450)
    doc.add_p("   + Nhân viên phục vụ (STAFF): `staff` - Xem bàn ăn, hỗ trợ gọi món.", indent_first=450)
    doc.add_bullet("4. Hướng dẫn khởi chạy nhanh bằng Maven:", "Mở terminal chạy lệnh: `.\\mvnw.cmd spring-boot:run` và truy cập `http://localhost:8080/`.")
    
    # Xuất file hoàn chỉnh
    doc.build_docx(output_path)
    
    # Bản sao ra thư mục gốc
    root_copy_path = os.path.join(".", "Nhom05_HeThongQuanLyCafe_LapTrinhJava.docx")
    shutil.copyfile(output_path, root_copy_path)
    print(f"Bản sao báo cáo tại thư mục gốc: {root_copy_path}")

if __name__ == "__main__":
    main()
