package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"time"
	"user-service/pkg/kafka"
	"user-service/pkg/mongodb"

	"github.com/gorilla/mux"
	"github.com/spf13/viper"
	"go.mongodb.org/mongo-driver/bson"
)

func init() {
	viper.SetConfigFile("config.yml")
	if err := viper.ReadInConfig(); err != nil {
		log.Fatal("Error loading config file:", err)
	}

    kafka.RunOAuth2Consumer()
}

func main() {
    r := mux.NewRouter()

    r.HandleFunc("/user-service/api/user/{oAuth2Id}", getUserHandler).Methods("GET")

	fmt.Printf("user-service http server starts at ... %s\n", time.Now())
    if err := http.ListenAndServe(":8001", r); err != nil {
        fmt.Println("Failed to start server:", err)
    }
}

func getUserHandler(w http.ResponseWriter, r *http.Request) {
    vars := mux.Vars(r)
    oAuth2Id := vars["oAuth2Id"]

    client := mongodb.GetMongoDBClient()

	db := client.Database(viper.GetString("mongodb.database"))
	collection := db.Collection("user")

    var user kafka.OAuth2Message
    if err := collection.FindOne(context.Background(), bson.M{"oAuth2Id": oAuth2Id}).Decode(&user); err != nil {
        w.WriteHeader(http.StatusNotFound)
        fmt.Fprintf(w, "User with OAuth2Id '%s' not found", oAuth2Id)
        return
    }

    w.Header().Set("Content-Type", "application/json")
    if err := json.NewEncoder(w).Encode(user); err != nil {
        log.Fatalf("Error encoding response: %s", err)
    }

}
