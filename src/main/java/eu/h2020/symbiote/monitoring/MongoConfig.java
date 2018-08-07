package eu.h2020.symbiote.monitoring;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Collection;
import java.util.Collections;

@Configuration
@EnableMongoRepositories
public class MongoConfig extends AbstractMongoConfiguration {
    
    @Value("${spring.data.mongodb.host:localhost}")
    private String mongoHost;

    @Value("${monitoring.mongo.database:symbiote-cloud-monitoring-database}")
    private String mongoDatabase;

    @Override
    protected String getDatabaseName() {
        return mongoDatabase;
    }

    @Override
    public Mongo mongo() {
        return new MongoClient();
    }

    @Override
    protected Collection<String> getMappingBasePackages() { return Collections.singletonList("com.oreilly.springdata.mongodb"); }

    @Bean
    @Override
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(new MongoClient(mongoHost), getDatabaseName());
    }

}