package com.example.BuiThiDiemTrinh_TH5.config;

import javax.sql.DataSource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Replaces the legacy product-only uniqueness after Hibernate adds variant columns. */
@Component
public class InvoiceVariantSchemaMigration implements ApplicationRunner {
    private final DataSource dataSource;
    public InvoiceVariantSchemaMigration(DataSource dataSource) { this.dataSource = dataSource; }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (var connection = dataSource.getConnection()) {
            boolean legacyIndex = false;
            try (var indexes = connection.getMetaData().getIndexInfo(connection.getCatalog(), null, "invoice_details", true, false)) {
                while (indexes.next()) {
                    if ("uk_invoice_detail_invoice_product".equalsIgnoreCase(indexes.getString("INDEX_NAME"))) legacyIndex = true;
                }
            }
            if (legacyIndex) {
                try (var statement = connection.createStatement()) {
                    statement.execute("ALTER TABLE invoice_details DROP INDEX uk_invoice_detail_invoice_product");
                }
            }
        }
    }
}
