package mongodb

import (
	"context"
	"log"
	"sync"
	"time"

	"github.com/spf13/viper"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

var client *mongo.Client
var once sync.Once

func GetMongoDBClient() *mongo.Client {
	uri := viper.GetString("mongodb.uri")
    maxPoolSize := uint64(viper.GetInt64("mongodb.maxPoolSize"))
    minPoolSize := uint64(viper.GetInt64("mongodb.minPoolSize"))
    maxConnIdleTime := viper.GetInt64("mongodb.maxConnIdleTime")

	once.Do(func() {
		var err error
		clientOptions := options.Client().ApplyURI(uri).
							SetMaxPoolSize(maxPoolSize).
							SetMinPoolSize(minPoolSize).
							SetMaxConnIdleTime(time.Duration(maxConnIdleTime) * time.Minute)
											
		client, err = mongo.Connect(context.TODO(), clientOptions)
		if err != nil {
			log.Fatalf("Failed to create MongoDB client: %s", err)
		}

		err = client.Ping(context.TODO(), nil)
		if err != nil {
			log.Fatalf("Failed to connect to MongoDB: %s", err)
		}
	})
	return client
}
