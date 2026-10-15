package com.example.BuiThiDiemTrinh_TH5.service.Impl;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BuiThiDiemTrinh_TH5.entity.CafeTable;
import com.example.BuiThiDiemTrinh_TH5.entity.TableStatus;
import com.example.BuiThiDiemTrinh_TH5.exception.BadRequestException;
import com.example.BuiThiDiemTrinh_TH5.exception.ResourceNotFoundException;
import com.example.BuiThiDiemTrinh_TH5.repository.CafeTableRepository;
import com.example.BuiThiDiemTrinh_TH5.service.CafeTableService;

@Service
public class CafeTableServiceImpl implements CafeTableService {

    private final CafeTableRepository cafeTableRepository;

    public CafeTableServiceImpl(CafeTableRepository cafeTableRepository) {
        this.cafeTableRepository = cafeTableRepository;
    }

    @Override
    public CafeTable create(CafeTable cafeTable) {
        validate(cafeTable);
        cafeTable.setId(null);
        return cafeTableRepository.save(cafeTable);
    }

    @Override
    public List<CafeTable> getTables(String status) {
        if (status == null || status.isBlank()) {
            return cafeTableRepository.findAll();
        }
        try {
            return cafeTableRepository.findByStatus(
                    TableStatus.valueOf(status.trim().toUpperCase(Locale.ROOT))
            );
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Trạng thái bàn không hợp lệ: " + status);
        }
    }

    @Override
    public CafeTable getTableById(Long id) {
        return cafeTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bàn có id = " + id));
    }

    @Override
    @Transactional
    public CafeTable update(Long id, CafeTable cafeTable) {
        validate(cafeTable);
        CafeTable existing = getTableById(id);
        existing.setTableNumber(cafeTable.getTableNumber());
        existing.setCapacity(cafeTable.getCapacity());
        existing.setStatus(cafeTable.getStatus());
        return cafeTableRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        cafeTableRepository.delete(getTableById(id));
    }

    private void validate(CafeTable cafeTable) {
        if (cafeTable == null) {
            throw new BadRequestException("Thông tin bàn không được để trống");
        }
        if (cafeTable.getTableNumber() == null || cafeTable.getTableNumber() <= 0) {
            throw new BadRequestException("Số bàn phải lớn hơn 0");
        }
        if (cafeTable.getCapacity() == null || cafeTable.getCapacity() <= 0) {
            throw new BadRequestException("Sức chứa của bàn phải lớn hơn 0");
        }
        if (cafeTable.getStatus() == null) {
            throw new BadRequestException("Trạng thái bàn không được để trống");
        }
    }
}
