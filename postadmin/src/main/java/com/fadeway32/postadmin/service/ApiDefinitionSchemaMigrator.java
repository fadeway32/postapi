package com.fadeway32.postadmin.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Component
@Order(-100)
public class ApiDefinitionSchemaMigrator implements CommandLineRunner {
    private final JdbcTemplate jdbcTemplate;

    public ApiDefinitionSchemaMigrator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("ALTER TABLE pa_api_definition ADD COLUMN IF NOT EXISTS version VARCHAR(32) NOT NULL DEFAULT 'v1'");
        jdbcTemplate.update("UPDATE pa_api_definition SET version = 'v1' WHERE version IS NULL OR version = ''");
        dropApiDefinitionUniqueConstraints();
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uk_pa_api_definition_tenant_code_version "
                + "ON pa_api_definition (tenant_id, api_code, version)");
    }

    private void dropApiDefinitionUniqueConstraints() {
        for (String indexName : findLegacyApiCodeUniqueIndexes()) {
            dropConstraintBackedIndex(indexName);
        }
        executeIgnoringFailure("ALTER TABLE pa_api_definition DROP CONSTRAINT IF EXISTS CONSTRAINT_4");
        executeIgnoringFailure("ALTER TABLE pa_api_definition DROP CONSTRAINT IF EXISTS CONSTRAINT_35");
        executeIgnoringFailure("ALTER TABLE pa_api_definition DROP CONSTRAINT IF EXISTS UK_PA_API_DEFINITION_TENANT_CODE");
        executeIgnoringFailure("ALTER TABLE pa_api_definition DROP CONSTRAINT IF EXISTS uk_pa_api_definition_tenant_code");
    }

    private Set<String> findLegacyApiCodeUniqueIndexes() {
        return jdbcTemplate.execute((ConnectionCallback<Set<String>>) connection -> {
            Set<String> names = new LinkedHashSet<String>();
            collectLegacyApiCodeUniqueIndexes(connection.getMetaData(), null, "pa_api_definition", names);
            collectLegacyApiCodeUniqueIndexes(connection.getMetaData(), "PUBLIC", "pa_api_definition", names);
            collectLegacyApiCodeUniqueIndexes(connection.getMetaData(), "PUBLIC", "PA_API_DEFINITION", names);
            return names;
        });
    }

    private void collectLegacyApiCodeUniqueIndexes(DatabaseMetaData metaData,
                                                  String schema,
                                                  String table,
                                                  Set<String> names) throws SQLException {
        Map<String, TreeMap<Short, String>> columnsByIndex = new LinkedHashMap<String, TreeMap<Short, String>>();
        ResultSet indexes = metaData.getIndexInfo(null, schema, table, true, false);
        try {
            while (indexes.next()) {
                if (indexes.getBoolean("NON_UNIQUE")) {
                    continue;
                }
                String indexName = indexes.getString("INDEX_NAME");
                String columnName = indexes.getString("COLUMN_NAME");
                short ordinal = indexes.getShort("ORDINAL_POSITION");
                if (indexName == null || columnName == null) {
                    continue;
                }
                TreeMap<Short, String> columns = columnsByIndex.get(indexName);
                if (columns == null) {
                    columns = new TreeMap<Short, String>();
                    columnsByIndex.put(indexName, columns);
                }
                columns.put(ordinal, columnName.toLowerCase(Locale.ENGLISH));
            }
        } finally {
            indexes.close();
        }

        for (Map.Entry<String, TreeMap<Short, String>> entry : columnsByIndex.entrySet()) {
            List<String> columns = new ArrayList<String>(entry.getValue().values());
            if (columns.equals(Arrays.asList("tenant_id", "api_code"))) {
                names.add(entry.getKey());
            }
        }
    }

    private void dropConstraintBackedIndex(String indexName) {
        int indexToken = indexName.indexOf("_INDEX");
        if (indexToken > 0) {
            executeIgnoringFailure("ALTER TABLE pa_api_definition DROP CONSTRAINT IF EXISTS " + indexName.substring(0, indexToken));
        }
        executeIgnoringFailure("ALTER TABLE pa_api_definition DROP CONSTRAINT IF EXISTS " + indexName);
        executeIgnoringFailure("DROP INDEX IF EXISTS " + indexName);
        executeIgnoringFailure("DROP INDEX IF EXISTS PUBLIC." + indexName);
    }

    private void executeIgnoringFailure(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (RuntimeException ignored) {
            // Best-effort migration for existing H2 databases with generated constraint names.
        }
    }
}
