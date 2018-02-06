package eu.h2020.symbiote.monitoring;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories
public class MongoConfig extends AbstractMongoConfiguration {
    
    @Value("${monitoring.mongo.uri:#{null}}")
    private String mongoUri;
    
    @Value("${monitoring.mongo.database:symbiote-cloud-monitoring-database}")
    private String mongoDatabase;

    @Override
    protected String getDatabaseName() {
        return mongoDatabase;
    }
    
    @Override
    public Mongo mongo() throws Exception {
        return mongoClient();
    }
    
    public MongoClient mongoClient() {
        if (mongoUri != null) {
            return new MongoClient(new MongoClientURI(mongoUri));
        } else {
            return new MongoClient();
        }
    }
}