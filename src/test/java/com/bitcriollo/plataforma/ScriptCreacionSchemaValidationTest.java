package com.bitcriollo.plataforma;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:script-schema;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.platform=h2",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=file:scripts/01_creacion.sql",
        "app.jwt.secret=clave-exclusiva-de-pruebas-con-mas-de-32-bytes",
        "app.cors.allowed-origins=*"
})
class ScriptCreacionSchemaValidationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void losScriptsCreanEliminanYRecreanElEsquemaCompatibleConJpa() {
        assertThat(tablasAplicacion()).isEqualTo(20);

        ejecutarScript("scripts/03_eliminacion.sql");
        assertThat(tablasAplicacion()).isZero();

        ejecutarScript("scripts/01_creacion.sql");
        assertThat(tablasAplicacion()).isEqualTo(20);
    }

    private long tablasAplicacion() {
        return jdbc.queryForObject("select count(*) from information_schema.tables "
                + "where table_schema = 'PUBLIC' and table_type = 'BASE TABLE'", Long.class);
    }

    private void ejecutarScript(String ruta) {
        new ResourceDatabasePopulator(new FileSystemResource(ruta)).execute(jdbc.getDataSource());
    }
}
