package kafka

import (
	"context"
	"encoding/json"
	"log"
	"user-service/pkg/mongodb"

	"github.com/IBM/sarama"
	"github.com/spf13/viper"
)

func GetConsumer() sarama.Consumer {
	config := sarama.NewConfig()
	config.Consumer.Return.Errors = true

	brokers := viper.GetStringSlice("kafka.brokers")

	consumer, err := sarama.NewConsumer(brokers, config)
	if err != nil {
		log.Panic("Failed to create Kafka consumer:", err)
	}

	return consumer
}

func RunOAuth2Consumer() {
	consumer := GetConsumer()

	oauth2Consumer, err := consumer.ConsumePartition("oauth2-data", 0, sarama.OffsetNewest)
	if err != nil {
		log.Panic("Failed to consume Kafka partition:", err)
	}
	defer func() {
		if err := oauth2Consumer.Close(); err != nil {
			log.Panic("Failed to close Kafka consumer partition:", err)
		}
	}()

	go func() {
		client := mongodb.GetMongoDBClient()

		db := client.Database(viper.GetString("mongodb.database"))
		collection := db.Collection("user")

        for msg := range oauth2Consumer.Messages() {
			var oauth2Msg OAuth2Message
            if err := json.Unmarshal(msg.Value, &oauth2Msg); err != nil {
				log.Printf("Error decoding message: %s", err)
				continue
			}
	
			if _, err := collection.InsertOne(context.Background(), oauth2Msg); err != nil {
                log.Printf("Failed to insert message into MongoDB: %s", err)
            }

			log.Printf("User inserted: %+v", oauth2Msg.OAuth2Id)
        }
    }()
}