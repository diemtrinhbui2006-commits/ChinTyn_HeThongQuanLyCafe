# docs/generate_report.py
# Generator for complete Word document according to HITC guidelines with Automatic TOC & Page Numbers

import os
import zipfile
import html

def escape_xml(s):
    if s is None:
        return ""
    return (str(s)
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace('"', "&quot;")
            .replace("'", "&apos;"))

class DocxBuilder:
    def __init__(self, template_docx_path):
        self.template_path = template_docx_path
        self.body_elements = []
        self.rels_image = False
        self.logo_path = "docs/hitc_logo_clean.png"
        
    def add_page_break(self):
        self.body_elements.append('<w:p><w:r><w:br w:type="page"/></w:r></w:p>')

    def add_p(self, text, bold=False, italic=False, align="both", size=26, color=None, space_before=60, space_after=60, line_spacing=312, indent_first=0):
        t_escaped = escape_xml(text)
        rpr_parts = [
            '<w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman" w:cs="Times New Roman"/>',
            f'<w:sz w:val="{size}"/>',
            f'<w:szCs w:val="{size}"/>'
        ]
        if bold:
            rpr_parts.append('<w:b/><w:bCs/>')
        if italic:
            rpr_parts.append('<w:i/><w:iCs/>')
        if color:
            rpr_parts.append(f'<w:color w:val="{color}"/>')
            
        rpr = "".join(rpr_parts)
        
        ppr_parts = [
            f'<w:jc w:val="{align}"/>',
            f'<w:spacing w:before="{space_before}" w:after="{space_after}" w:line="{line_spacing}" w:lineRule="auto"/>'
        ]
        if indent_first > 0:
            ppr_parts.append(f'<w:ind w:firstLine="{indent_first}"/>')
            
        ppr = "".join(ppr_parts)
        
        xml = f'<w:p><w:pPr>{ppr}</w:pPr><w:r><w:rPr>{rpr}</w:rPr><w:t xml:space="preserve">{t_escaped}</w:t></w:r></w:p>'
        self.body_elements.append(xml)

    def add_bullet(self, bold_prefix, text, size=26):
        b_escaped = escape_xml(bold_prefix)
        t_escaped = escape_xml(text)
        xml = (
            f'<w:p><w:pPr>'
            f'<w:jc w:val="both"/>'
            f'<w:spacing w:before="40" w:after="40" w:line="300" w:lineRule="auto"/>'
            f'<w:ind w:left="450" w:hanging="250"/>'
            f'</w:pPr>'
            f'<w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="{size}"/></w:rPr><w:t>• </w:t></w:r>'
            f'<w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:b/><w:bCs/><w:sz w:val="{size}"/></w:rPr><w:t xml:space="preserve">{b_escaped} </w:t></w:r>'
            f'<w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="{size}"/></w:rPr><w:t xml:space="preserve">{t_escaped}</w:t></w:r>'
            f'</w:p>'
        )
        self.body_elements.append(xml)

    def add_heading1(self, text):
        t = escape_xml(text)
        xml = (
            f'<w:p><w:pPr>'
            f'<w:pStyle w:val="Heading1"/>'
            f'<w:outlineLvl w:val="0"/>'
            f'<w:jc w:val="left"/>'
            f'<w:spacing w:before="400" w:after="160" w:line="360" w:lineRule="auto"/>'
            f'<w:keepNext/>'
            f'</w:pPr>'
            f'<w:r><w:rPr>'
            f'<w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/>'
            f'<w:b/><w:bCs/><w:sz w:val="32"/><w:szCs w:val="32"/>'
            f'<w:color w:val="1B365D"/>'
            f'</w:rPr>'
            f'<w:t>{t}</w:t></w:r>'
            f'</w:p>'
        )
        self.body_elements.append(xml)

    def add_heading2(self, text):
        t = escape_xml(text)
        xml = (
            f'<w:p><w:pPr>'
            f'<w:pStyle w:val="Heading2"/>'
            f'<w:outlineLvl w:val="1"/>'
            f'<w:jc w:val="left"/>'
            f'<w:spacing w:before="280" w:after="120" w:line="320" w:lineRule="auto"/>'
            f'<w:keepNext/>'
            f'</w:pPr>'
            f'<w:r><w:rPr>'
            f'<w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/>'
            f'<w:b/><w:bCs/><w:sz w:val="28"/><w:szCs w:val="28"/>'
            f'<w:color w:val="2C3E50"/>'
            f'</w:rPr>'
            f'<w:t>{t}</w:t></w:r>'
            f'</w:p>'
        )
        self.body_elements.append(xml)

    def add_heading3(self, text):
        t = escape_xml(text)
        xml = (
            f'<w:p><w:pPr>'
            f'<w:pStyle w:val="Heading3"/>'
            f'<w:outlineLvl w:val="2"/>'
            f'<w:jc w:val="left"/>'
            f'<w:spacing w:before="200" w:after="80" w:line="300" w:lineRule="auto"/>'
            f'<w:keepNext/>'
            f'</w:pPr>'
            f'<w:r><w:rPr>'
            f'<w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/>'
            f'<w:b/><w:bCs/><w:i/><w:iCs/><w:sz w:val="26"/><w:szCs w:val="26"/>'
            f'<w:color w:val="34495E"/>'
            f'</w:rPr>'
            f'<w:t>{t}</w:t></w:r>'
            f'</w:p>'
        )
        self.body_elements.append(xml)

    def add_code_block(self, title, code_text):
        if title:
            self.add_p(f"Đoạn mã: {title}", italic=True, size=22, space_before=140, space_after=40, color="555555")
        
        lines = code_text.strip().split("\n")
        escaped_lines = [escape_xml(l) for l in lines]
        code_p_list = []
        for l in escaped_lines:
            code_p_list.append(
                f'<w:p><w:pPr>'
                f'<w:jc w:val="left"/>'
                f'<w:spacing w:before="0" w:after="0" w:line="240" w:lineRule="auto"/>'
                f'</w:pPr>'
                f'<w:r><w:rPr>'
                f'<w:rFonts w:ascii="Consolas" w:hAnsi="Consolas" w:cs="Consolas"/>'
                f'<w:sz w:val="20"/><w:szCs w:val="20"/>'
                f'<w:color w:val="24292E"/>'
                f'</w:rPr>'
                f'<w:t xml:space="preserve">{l if l else " "}</w:t></w:r></w:p>'
            )
        
        cell_xml = "".join(code_p_list)
        table_xml = (
            f'<w:tbl>'
            f'<w:tblPr>'
            f'<w:tblW w:w="9200" w:type="dxa"/>'
            f'<w:jc w:val="center"/>'
            f'<w:tblBorders>'
            f'<w:top w:val="single" w:sz="4" w:space="0" w:color="CBD5E1"/>'
            f'<w:left w:val="single" w:sz="18" w:space="0" w:color="0284C7"/>'
            f'<w:bottom w:val="single" w:sz="4" w:space="0" w:color="CBD5E1"/>'
            f'<w:right w:val="single" w:sz="4" w:space="0" w:color="CBD5E1"/>'
            f'</w:tblBorders>'
            f'<w:tblCellMar><w:top w:w="120" w:type="dxa"/><w:bottom w:w="120" w:type="dxa"/><w:left w:w="180" w:type="dxa"/><w:right w:w="180" w:type="dxa"/></w:tblCellMar>'
            f'</w:tblPr>'
            f'<w:tr>'
            f'<w:tc>'
            f'<w:tcPr>'
            f'<w:tcW w:w="9200" w:type="dxa"/>'
            f'<w:shd w:val="clear" w:color="auto" w:fill="F8FAFC"/>'
            f'</w:tcPr>'
            f'{cell_xml}'
            f'</w:tc>'
            f'</w:tr>'
            f'</w:tbl>'
        )
        self.body_elements.append(table_xml)
        self.add_p("", space_before=40, space_after=40)

    def add_table(self, title, headers, rows, col_widths=None):
        if title:
            self.add_p(title, bold=True, size=24, align="left", space_before=160, space_after=60, color="1B365D")
            
        num_cols = len(headers)
        if not col_widths:
            default_w = 9200 // num_cols
            col_widths = [default_w] * num_cols
            
        tbl_w = sum(col_widths)
        
        # Build Header Row
        header_cells = []
        for i, h in enumerate(headers):
            w = col_widths[i]
            h_escaped = escape_xml(h)
            header_cells.append(
                f'<w:tc>'
                f'<w:tcPr>'
                f'<w:tcW w:w="{w}" w:type="dxa"/>'
                f'<w:shd w:val="clear" w:color="auto" w:fill="1B365D"/>'
                f'<w:vAlign w:val="center"/>'
                f'<w:tcMar><w:top w:w="120" w:type="dxa"/><w:bottom w:w="120" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tcMar>'
                f'</w:tcPr>'
                f'<w:p><w:pPr><w:jc w:val="center"/><w:spacing w:before="60" w:after="60"/></w:pPr>'
                f'<w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:b/><w:bCs/><w:sz w:val="22"/><w:szCs w:val="22"/><w:color w:val="FFFFFF"/></w:rPr>'
                f'<w:t>{h_escaped}</w:t></w:r></w:p>'
                f'</w:tc>'
            )
        header_row_xml = f'<w:tr><w:trPr><w:tblHeader/><w:cantSplit/></w:trPr>{"".join(header_cells)}</w:tr>'
        
        # Build Data Rows
        data_rows_xml = []
        for r_idx, row in enumerate(rows):
            row_cells = []
            bg_fill = "F8FAFC" if (r_idx % 2 == 1) else "FFFFFF"
            for c_idx, val in enumerate(row):
                w = col_widths[c_idx]
                val_escaped = escape_xml(val)
                align = "center" if (len(str(val)) <= 8 or c_idx == 0) else "left"
                row_cells.append(
                    f'<w:tc>'
                    f'<w:tcPr>'
                    f'<w:tcW w:w="{w}" w:type="dxa"/>'
                    f'<w:shd w:val="clear" w:color="auto" w:fill="{bg_fill}"/>'
                    f'<w:vAlign w:val="center"/>'
                    f'<w:tcMar><w:top w:w="100" w:type="dxa"/><w:bottom w:w="100" w:type="dxa"/><w:left w:w="120" w:type="dxa"/><w:right w:w="120" w:type="dxa"/></w:tcMar>'
                    f'</w:tcPr>'
                    f'<w:p><w:pPr><w:jc w:val="{align}"/><w:spacing w:before="40" w:after="40"/><w:line w:line="260" w:lineRule="auto"/></w:pPr>'
                    f'<w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="22"/><w:szCs w:val="22"/></w:rPr>'
                    f'<w:t xml:space="preserve">{val_escaped}</w:t></w:r></w:p>'
                    f'</w:tc>'
                )
            data_rows_xml.append(f'<w:tr><w:trPr><w:cantSplit/></w:trPr>{"".join(row_cells)}</w:tr>')
            
        tbl_xml = (
            f'<w:tbl>'
            f'<w:tblPr>'
            f'<w:tblW w:w="{tbl_w}" w:type="dxa"/>'
            f'<w:jc w:val="center"/>'
            f'<w:tblBorders>'
            f'<w:top w:val="single" w:sz="6" w:space="0" w:color="1B365D"/>'
            f'<w:left w:val="single" w:sz="4" w:space="0" w:color="CBD5E1"/>'
            f'<w:bottom w:val="single" w:sz="6" w:space="0" w:color="1B365D"/>'
            f'<w:right w:val="single" w:sz="4" w:space="0" w:color="CBD5E1"/>'
            f'<w:insideH w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>'
            f'<w:insideV w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>'
            f'</w:tblBorders>'
            f'</w:tblPr>'
            f'{header_row_xml}'
            f'{"".join(data_rows_xml)}'
            f'</w:tbl>'
        )
        self.body_elements.append(tbl_xml)
        self.add_p("", space_before=40, space_after=60)

    def add_cover_page(self, group_name="Nhóm 20", class_name="CCQ2411D- Khoa Công nghệ Thông tin", instructor="Nguyễn Minh Tuấn", date_str="TP. HỒ CHÍ MINH, Ngày 29, Tháng 10, Năm 2026"):
        self.add_p("TRƯỜNG CAO ĐẲNG CÔNG THƯƠNG TP. HỒ CHÍ MINH", bold=True, size=28, align="center", space_before=100, space_after=40, color="1B365D")
        self.add_p("KHOA CÔNG NGHỆ THÔNG TIN", bold=True, size=26, align="center", space_before=20, space_after=80, color="1B365D")
        self.add_p("-----***-----", bold=True, size=24, align="center", space_before=0, space_after=140, color="64748B")
        
        # Logo HITC image
        logo_xml = (
            '<w:p><w:pPr><w:jc w:val="center"/><w:spacing w:before="80" w:after="160"/></w:pPr>'
            '<w:r><w:drawing>'
            '<wp:inline distT="0" distB="0" distL="0" distR="0">'
            '<wp:extent cx="1500000" cy="1500000"/>'
            '<wp:effectExtent l="0" t="0" r="0" b="0"/>'
            '<wp:docPr id="1" name="Logo HITC"/>'
            '<wp:cNvGraphicFramePr><a:graphicFrameLocks xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" noChangeAspect="1"/></wp:cNvGraphicFramePr>'
            '<a:graphic xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main">'
            '<a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">'
            '<pic:pic xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">'
            '<pic:nvPicPr><pic:cNvPr id="1" name="hitc_logo.png"/><pic:cNvPicPr/></pic:nvPicPr>'
            '<pic:blipFill>'
            '<a:blip r:embed="rIdLogo"/>'
            '<a:stretch><a:fillRect/></a:stretch>'
            '</pic:blipFill>'
            '<pic:spPr>'
            '<a:xfrm><a:off x="0" y="0"/><a:ext cx="1500000" cy="1500000"/></a:xfrm>'
            '<a:prstGeom prst="rect"><a:avLst/></a:prstGeom>'
            '</pic:spPr>'
            '</pic:pic>'
            '</a:graphicData>'
            '</a:graphic>'
            '</wp:inline>'
            '</w:drawing></w:r></w:p>'
        )
        self.body_elements.append(logo_xml)
        self.rels_image = True
        
        self.add_p("BÁO CÁO ĐỒ ÁN MÔN HỌC", bold=True, size=28, align="center", space_before=160, space_after=60, color="2C3E50")
        self.add_p("HỌC PHẦN: LẬP TRÌNH JAVA & CÔNG NGHỆ SPRING BOOT", bold=True, size=24, align="center", space_before=0, space_after=180, color="2C3E50")
        
        self.add_p("ĐỀ TÀI:", bold=True, size=26, align="center", space_before=40, space_after=40, color="D97706")
        self.add_p("XÂY DỰNG HỆ THỐNG QUẢN LÝ QUÁN CAFE", bold=True, size=34, align="center", space_before=40, space_after=40, color="1B365D")
        self.add_p("CHINTYN COFFEE MANAGEMENT SYSTEM", bold=True, size=26, align="center", space_before=20, space_after=280, color="D97706")
        
        info_rows = [
            ["Sinh viên thực hiện 1:", "Bùi Thị Diễm Trinh", "MSSV: 2124110287"],
            ["Sinh viên thực hiện 2:", "Nguyễn Thị Mộng Tuyền", "MSSV: 2124110290"],
            ["Lớp chuyên ngành:", class_name, ""],
            ["Nhóm thực hiện:", group_name, ""],
            ["Giảng viên hướng dẫn:", instructor, ""]
        ]
        
        info_cells = []
        for r in info_rows:
            label, name, mssv = r
            row_xml = (
                f'<w:tr><w:trPr><w:cantSplit/></w:trPr>'
                f'<w:tc><w:tcPr><w:tcW w:w="3000" w:type="dxa"/></w:tcPr>'
                f'<w:p><w:pPr><w:jc w:val="left"/><w:spacing w:before="50" w:after="50"/></w:pPr>'
                f'<w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:b/><w:sz w:val="24"/><w:szCs w:val="24"/></w:rPr>'
                f'<w:t>{escape_xml(label)}</w:t></w:r></w:p></w:tc>'
                f'<w:tc><w:tcPr><w:tcW w:w="3800" w:type="dxa"/></w:tcPr>'
                f'<w:p><w:pPr><w:jc w:val="left"/><w:spacing w:before="50" w:after="50"/></w:pPr>'
                f'<w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:b/><w:sz w:val="24"/><w:szCs w:val="24"/><w:color w:val="1B365D"/></w:rPr>'
                f'<w:t>{escape_xml(name)}</w:t></w:r></w:p></w:tc>'
                f'<w:tc><w:tcPr><w:tcW w:w="2200" w:type="dxa"/></w:tcPr>'
                f'<w:p><w:pPr><w:jc w:val="left"/><w:spacing w:before="50" w:after="50"/></w:pPr>'
                f'<w:r><w:rPr><w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/><w:sz w:val="24"/><w:szCs w:val="24"/></w:rPr>'
                f'<w:t>{escape_xml(mssv)}</w:t></w:r></w:p></w:tc>'
                f'</w:tr>'
            )
            info_cells.append(row_xml)
            
        info_table_xml = (
            f'<w:tbl>'
            f'<w:tblPr>'
            f'<w:tblW w:w="9000" w:type="dxa"/>'
            f'<w:jc w:val="center"/>'
            f'<w:tblBorders>'
            f'<w:top w:val="none"/><w:left w:val="none"/><w:bottom w:val="none"/><w:right w:val="none"/>'
            f'<w:insideH w:val="none"/><w:insideV w:val="none"/>'
            f'</w:tblBorders>'
            f'</w:tblPr>'
            f'{"".join(info_cells)}'
            f'</w:tbl>'
        )
        self.body_elements.append(info_table_xml)
        
        self.add_p("", space_before=300, space_after=100)
        self.add_p(date_str, bold=True, size=24, align="center", space_before=100, space_after=40, color="64748B")
        self.add_page_break()

    def add_automatic_toc(self, toc_items):
        r"""
        Creates an official Word Table of Contents SDT block with field code:
        TOC \o "1-3" \h \z \u
        and pre-rendered items formatted with dot leaders to page numbers.
        When opened in Word, Word treats this as a dynamic TOC that updates automatically!
        """
        toc_entries_xml = []
        for title, page in toc_items:
            t_escaped = escape_xml(title.strip())
            p_escaped = escape_xml(str(page).strip())
            
            # Determine heading level from title prefix
            if title.strip().startswith("CHƯƠNG") or title.strip() in ["LỜI CẢM ƠN", "DANH MỤC CÁC TỪ VIẾT TẮT VÀ THUẬT NGỮ", "DANH MỤC CÁC BẢNG BIỂU", "DANH MỤC CÁC HÌNH VẼ VÀ SƠ ĐỒ", "KẾT LUẬN VÀ BÀI HỌC KINH NGHIỆM", "BẢNG PHÂN CÔNG NHIỆM VỤ THỰC HIỆN ĐỀ TÀI", "TÀI LIỆU THAM KHẢO", "PHỤ LỤC"]:
                style_val = "TOC1"
                left_ind = 0
                font_bold = True
                font_color = "1B365D"
            elif title.strip().startswith(("1.", "2.", "3.", "4.", "5.", "6.")) and len(title.strip().split()[0].split('.')) == 2:
                style_val = "TOC2"
                left_ind = 360
                font_bold = False
                font_color = "2C3E50"
            else:
                style_val = "TOC3"
                left_ind = 720
                font_bold = False
                font_color = "475569"
                
            entry_xml = (
                f'<w:p>'
                f'<w:pPr>'
                f'<w:pStyle w:val="{style_val}"/>'
                f'<w:tabs><w:tab w:val="right" w:leader="dot" w:pos="9350"/></w:tabs>'
                f'<w:spacing w:before="30" w:after="30" w:line="280" w:lineRule="auto"/>'
                + (f'<w:ind w:left="{left_ind}"/>' if left_ind > 0 else '') +
                f'</w:pPr>'
                f'<w:r>'
                f'<w:rPr>'
                f'<w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/>'
                + ('<w:b/><w:bCs/>' if font_bold else '') +
                f'<w:sz w:val="23"/><w:szCs w:val="23"/>'
                f'<w:color w:val="{font_color}"/>'
                f'</w:rPr>'
                f'<w:t>{t_escaped}</w:t>'
                f'</w:r>'
                f'<w:r><w:tab/></w:r>'
                f'<w:r>'
                f'<w:rPr>'
                f'<w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/>'
                + ('<w:b/><w:bCs/>' if font_bold else '') +
                f'<w:sz w:val="23"/><w:szCs w:val="23"/>'
                f'<w:color w:val="{font_color}"/>'
                f'</w:rPr>'
                f'<w:t>{p_escaped}</w:t>'
                f'</w:r>'
                f'</w:p>'
            )
            toc_entries_xml.append(entry_xml)

        sdt_xml = (
            f'<w:sdt>'
            f'<w:sdtPr>'
            f'<w:docPartObj>'
            f'<w:docPartGallery w:val="Table of Contents"/>'
            f'<w:docPartUnique/>'
            f'</w:docPartObj>'
            f'</w:sdtPr>'
            f'<w:sdtContent>'
            f'<w:p>'
            f'<w:pPr>'
            f'<w:pStyle w:val="TOCHeading"/>'
            f'<w:jc w:val="left"/>'
            f'<w:spacing w:before="360" w:after="160"/>'
            f'</w:pPr>'
            f'<w:r>'
            f'<w:rPr>'
            f'<w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman"/>'
            f'<w:b/><w:bCs/><w:sz w:val="32"/><w:szCs w:val="32"/>'
            f'<w:color w:val="1B365D"/>'
            f'</w:rPr>'
            f'<w:t>MỤC LỤC TỰ ĐỘNG</w:t>'
            f'</w:r>'
            f'</w:p>'
            # Field begin
            f'<w:p>'
            f'<w:pPr><w:pStyle w:val="TOC1"/></w:pPr>'
            f'<w:r>'
            f'<w:fldChar w:fldCharType="begin"/>'
            f'<w:instrText xml:space="preserve"> TOC \\o "1-3" \\h \\z \\u </w:instrText>'
            f'<w:fldChar w:fldCharType="separate"/>'
            f'</w:r>'
            f'</w:p>'
            # Pre-rendered content for instant view
            f'{"".join(toc_entries_xml)}'
            # Field end
            f'<w:p>'
            f'<w:r><w:fldChar w:fldCharType="end"/></w:r>'
            f'</w:p>'
            f'</w:sdtContent>'
            f'</w:sdt>'
        )
        self.body_elements.append(sdt_xml)

    def build_docx(self, output_path):
        with open(self.logo_path, "rb") as f:
            logo_bytes = f.read()
            
        full_body_content = "".join(self.body_elements)
        
        # Standard page section properties with <w:titlePg/> so cover page has NO page number
        sect_pr = (
            '<w:sectPr>'
            '<w:headerReference w:type="default" r:id="rId9"/>'
            '<w:footerReference w:type="default" r:id="rId10"/>'
            '<w:titlePg/>'  # Different first page (bìa không hiện số trang)
            '<w:pgSz w:w="11906" w:h="16838"/>'
            '<w:pgMar w:top="1134" w:right="1134" w:bottom="1134" w:left="1417" w:header="720" w:footer="720" w:gutter="0"/>'
            '<w:cols w:space="720"/>'
            '<w:docGrid w:linePitch="360"/>'
            '</w:sectPr>'
        )
        
        doc_xml = (
            '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            '<w:document xmlns:wpc="http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas" '
            'xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" '
            'xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" '
            'xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math" '
            'xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing" '
            'xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" '
            'xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture" '
            'xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main" '
            'xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml">'
            f'<w:body>{full_body_content}{sect_pr}</w:body>'
            '</w:document>'
        )
        
        # Dynamic footer with page number: "Trang <PAGE>" and subject name
        dynamic_footer_xml = (
            '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            '<w:ftr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">'
            '<w:p>'
            '<w:pPr>'
            '<w:pStyle w:val="Footer"/>'
            '<w:pBdr>'
            '<w:top w:val="single" w:sz="4" w:space="4" w:color="CBD5E1"/>'
            '</w:pBdr>'
            '<w:tabs>'
            '<w:tab w:val="right" w:pos="9350"/>'
            '</w:tabs>'
            '<w:spacing w:before="60" w:after="0"/>'
            '</w:pPr>'
            '<w:r>'
            '<w:rPr>'
            '<w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman" w:cs="Times New Roman"/>'
            '<w:sz w:val="18"/>'
            '<w:szCs w:val="18"/>'
            '<w:color w:val="64748B"/>'
            '</w:rPr>'
            '<w:t>Đề tài: Quản lý quán Cafe (ChinTyn Coffee) - Nhóm 20</w:t>'
            '</w:r>'
            '<w:r>'
            '<w:rPr>'
            '<w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman" w:cs="Times New Roman"/>'
            '<w:sz w:val="18"/>'
            '<w:szCs w:val="18"/>'
            '<w:color w:val="1B365D"/>'
            '</w:rPr>'
            '<w:tab/>'
            '<w:t xml:space="preserve">Trang </w:t>'
            '</w:r>'
            '<w:fldSimple w:instr="PAGE">'
            '<w:r>'
            '<w:rPr>'
            '<w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman" w:cs="Times New Roman"/>'
            '<w:b/><w:bCs/>'
            '<w:sz w:val="18"/>'
            '<w:szCs w:val="18"/>'
            '<w:color w:val="1B365D"/>'
            '</w:rPr>'
            '<w:t>1</w:t>'
            '</w:r>'
            '</w:fldSimple>'
            '</w:p>'
            '</w:ftr>'
        )
        
        # Professional academic header
        dynamic_header_xml = (
            '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
            '<w:hdr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">'
            '<w:p>'
            '<w:pPr>'
            '<w:pStyle w:val="Header"/>'
            '<w:pBdr>'
            '<w:bottom w:val="single" w:sz="4" w:space="4" w:color="CBD5E1"/>'
            '</w:pBdr>'
            '<w:tabs>'
            '<w:tab w:val="right" w:pos="9350"/>'
            '</w:tabs>'
            '<w:spacing w:before="0" w:after="80"/>'
            '</w:pPr>'
            '<w:r>'
            '<w:rPr>'
            '<w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman" w:cs="Times New Roman"/>'
            '<w:i/><w:iCs/>'
            '<w:sz w:val="18"/>'
            '<w:szCs w:val="18"/>'
            '<w:color w:val="64748B"/>'
            '</w:rPr>'
            '<w:t>Trường Cao đẳng Công Thương TP.HCM - Khoa CNTT</w:t>'
            '</w:r>'
            '<w:r>'
            '<w:rPr>'
            '<w:rFonts w:ascii="Times New Roman" w:hAnsi="Times New Roman" w:cs="Times New Roman"/>'
            '<w:i/><w:iCs/>'
            '<w:sz w:val="18"/>'
            '<w:szCs w:val="18"/>'
            '<w:color w:val="64748B"/>'
            '</w:rPr>'
            '<w:tab/>'
            '<w:t>Báo cáo Đồ án: Lập trình Java &amp; Spring Boot</w:t>'
            '</w:r>'
            '</w:p>'
            '</w:hdr>'
        )

        with zipfile.ZipFile(self.template_path, 'r') as zin, zipfile.ZipFile(output_path, 'w', compression=zipfile.ZIP_DEFLATED) as zout:
            for item in zin.infolist():
                if item.filename == 'word/document.xml':
                    zout.writestr(item, doc_xml.encode('utf-8'))
                elif item.filename == 'word/footer1.xml':
                    zout.writestr(item, dynamic_footer_xml.encode('utf-8'))
                elif item.filename == 'word/header1.xml':
                    zout.writestr(item, dynamic_header_xml.encode('utf-8'))
                elif item.filename == 'word/settings.xml':
                    text = zin.read(item.filename).decode('utf-8')
                    # Ensure updateFields is enabled so Word updates TOC on open
                    if 'w:updateFields' not in text:
                        text = text.replace('</w:settings>', '<w:updateFields w:val="true"/></w:settings>')
                    zout.writestr(item, text.encode('utf-8'))
                elif item.filename == '[Content_Types].xml':
                    text = zin.read(item.filename).decode('utf-8')
                    if 'Extension="png"' not in text:
                        text = text.replace('</Types>', '<Default Extension="png" ContentType="image/png"/></Types>')
                    zout.writestr(item, text.encode('utf-8'))
                elif item.filename == 'word/_rels/document.xml.rels':
                    text = zin.read(item.filename).decode('utf-8')
                    if 'Id="rIdLogo"' not in text:
                        text = text.replace('</Relationships>', '<Relationship Id="rIdLogo" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="media/hitc_logo.png"/></Relationships>')
                    zout.writestr(item, text.encode('utf-8'))
                else:
                    zout.writestr(item, zin.read(item.filename))
            zout.writestr('word/media/hitc_logo.png', logo_bytes)
            
        print(f"Báo cáo hoàn chỉnh đã được xuất thành công tại: {output_path}")

print("DocxBuilder updated successfully with Auto-TOC and Page Numbering!")
