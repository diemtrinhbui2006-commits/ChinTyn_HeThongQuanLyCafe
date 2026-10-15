package com.example.BuiThiDiemTrinh_TH5.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.BuiThiDiemTrinh_TH5.entity.CafeTable;
import com.example.BuiThiDiemTrinh_TH5.service.CafeTableService;

@RestController
@RequestMapping("/api/tables")
public class CafeTableController {

    private final CafeTableService cafeTableService;

    public CafeTableController(CafeTableService cafeTableService) {
        this.cafeTableService = cafeTableService;
    }

    @GetMapping
    public List<CafeTable> getTables(@RequestParam(required = false) String status) {
        return cafeTableService.getTables(status);
    }

    @GetMapping("/{id}")
    public CafeTable getTableById(@PathVariable Long id) {
        return cafeTableService.getTableById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CafeTable createTable(@RequestBody CafeTable cafeTable) {
        return cafeTableService.create(cafeTable);
    }

    @PutMapping("/{id}")
    public CafeTable updateTable(@PathVariable Long id, @RequestBody CafeTable cafeTable) {
        return cafeTableService.update(id, cafeTable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTable(@PathVariable Long id) {
        cafeTableService.delete(id);
    }
}
