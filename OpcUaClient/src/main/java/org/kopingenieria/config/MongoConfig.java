package org.kopingenieria.config;


import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableMongoRepositories(basePackages = "com.industrial.communication.repository")
public class MongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public MongoClient mongoClient() {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoUri))
                .applyToConnectionPoolSettings(builder ->
                        builder.maxSize(100)
                                .minSize(5)
                                .maxWaitTime(2000, TimeUnit.MILLISECONDS))
                .applyToSocketSettings(builder ->
                        builder.connectTimeout(3000, TimeUnit.MILLISECONDS))
                .retryWrites(true)
                .retryReads(true)
                .build();

        return MongoClients.create(settings);
    }

    @Bean
    public MappingMongoConverter mappingMongoConverter(
            MongoDatabaseFactory databaseFactory,
            MongoCustomConversions customConversions) {
        MappingMongoConverter converter =
                new MappingMongoConverter(
                        new DefaultDbRefResolver(databaseFactory),
                        new MongoMappingContext()
                );
        converter.setCustomConversions(customConversions);
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return converter;
    }
}
