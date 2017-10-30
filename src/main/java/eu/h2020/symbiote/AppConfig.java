package eu.h2020.symbiote;

import com.mongodb.MongoClient;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories
public class AppConfig extends AbstractMongoConfiguration {
    

    @Override
    protected String getDatabaseName() {
        return "symbiote-cloud-monitoring-database";
    }

    @Override
    public MongoClient mongo() throws Exception {
        return new MongoClient();
    }
}