package eu.h2020.symbiote;

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
    
    @Value("${monitoring.mongo.database:#{null}}")
    private String mongoDatabase;

    @Override
    protected String getDatabaseName() {
        if (mongoDatabase != null) {
            return mongoDatabase;
        } else {
            return "symbiote-cloud-monitoring-database";
        }
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