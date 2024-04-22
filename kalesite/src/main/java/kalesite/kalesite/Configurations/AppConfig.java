package kalesite.kalesite.Configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class AppConfig {

    @Bean
    public DataSource dataSource() {

        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName("org.postgresql.Driver");

        //dataSource.setUrl("jdbc:postgresql://localhost:5432/kalesite");
        dataSource.setUrl("jdbc:postgresql://159.89.23.51:5433/kale");
        //dataSource.setUsername("postgres");
        //dataSource.setPassword("bestuser");
        dataSource.setUsername("kale");
        dataSource.setPassword("!1234567A@kale");

        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {

        return new JdbcTemplate(dataSource);
    }
}