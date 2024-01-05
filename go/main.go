package main

import (
	"compress/gzip"
	"database/sql"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"strings"
	"time"

	"github.com/gorilla/mux"
	_ "github.com/trinodb/trino-go-client/trino"
)

var db *sql.DB

type CustomTime struct {
	time.Time
}

// Item represents an item in the database
type Item struct {
	OrderKey      int     `json:"orderkey"`
	CustKey       int     `json:"custkey"`
	OrderStatus   string  `json:"orderstatus"`
	TotalPrice    float64 `json:"totalprice"`
	OrderDate     string  `json:"orderdate"`
	OrderPriority string  `json:"orderpriority"`
	Clerk         string  `json:"clerk"`
	ShipPriority  int     `json:"shippriority"`
	Comment       string  `json:"comment"`
}

func main() {
	// Setup database connection
	initDB()

	// Setup Gorilla mux router
	router := mux.NewRouter()

	// Define routes
	router.HandleFunc("/items", getItemsHandler).Methods("GET")

	// Wrap the router with gzip handler
	gzipRouter := NewGzipHandler(router)

	// Start the web server
	port := ":8080"
	fmt.Printf("Server listening on port %s...\n", port)
	log.Fatal(http.ListenAndServe(port, gzipRouter))
}

func initDB() {
	//Example - export TRINO_URL="http://user@localhost:8080?catalog=tpch&schema=sf1"
	dsn, trinoUrlExists := os.LookupEnv("TRINO_URL")
	if !trinoUrlExists {
		log.Fatal("TRINO_URL environment variable not set")
	}

	var errDB error

	db, errDB = sql.Open("trino", dsn)

	if errDB != nil {
		log.Fatal(errDB)
	}

	errPing := db.Ping()
	if errPing != nil {
		log.Fatal(errPing)
	}

	log.Println("Database initialized")
}

func getItemsHandler(w http.ResponseWriter, r *http.Request) {
	// Set response headers
	w.Header().Set("Content-Type", "application/json")

	//Use stream
	if strings.Contains(r.Header.Get("X-USER"), "stream") {
		log.Println("Streaming data to client")

		// Check if client supports gzip
		if strings.Contains(r.Header.Get("Accept-Encoding"), "gzip") {
			log.Println("GZipping response")
			gzipWriter := gzip.NewWriter(w)
			defer gzipWriter.Close()

			// Create a new response writer with the gzip writer
			gzipResponseWriter := NewGzipResponseWriter(w, gzipWriter)

			// Stream data from the database to the response
			err := streamItemsToResponse(gzipResponseWriter)
			if err != nil {
				http.Error(w, err.Error(), http.StatusInternalServerError)
				return
			}
		} else {
			// If client does not support gzip, stream data without compression
			err := streamItemsToResponse(w)
			if err != nil {
				http.Error(w, err.Error(), http.StatusInternalServerError)
				return
			}
		}
	} else {
		log.Println("Sending without streaming")
		items, err := retrieveItemsFromDB()
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		// Convert items to JSON
		jsonData, err := json.Marshal(items)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		// Write the compressed JSON response
		w.Write(jsonData)
	}
}

func streamItemsToResponse(w http.ResponseWriter) error {
	rows, err := db.Query("SELECT orderkey, custkey, orderstatus, totalprice, orderdate, orderpriority, clerk, shippriority, comment FROM orders")
	if err != nil {
		return err
	}
	defer rows.Close()

	encoder := json.NewEncoder(w)

	w.Write([]byte("["))
	defer w.Write([]byte("]"))

	firstRow := true

	for rows.Next() {
		var item Item
		err := rows.Scan(&item.OrderKey, &item.CustKey, &item.OrderStatus, &item.TotalPrice, &item.OrderDate, &item.OrderPriority, &item.Clerk, &item.ShipPriority, &item.Comment)
		if err != nil {
			return err
		}

		if !firstRow {
			w.Write([]byte(","))
		}
		firstRow = false

		// Encode and stream each item to the response
		err = encoder.Encode(item)
		if err != nil {
			return err
		}
	}

	return nil
}

func retrieveItemsFromDB() ([]Item, error) {
	rows, err := db.Query("SELECT orderkey, custkey, orderstatus, totalprice, orderdate, orderpriority, clerk, shippriority, comment FROM orders")
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var items []Item
	for rows.Next() {
		var item Item
		err := rows.Scan(&item.OrderKey, &item.CustKey, &item.OrderStatus, &item.TotalPrice, &item.OrderDate, &item.OrderPriority, &item.Clerk, &item.ShipPriority, &item.Comment)
		if err != nil {
			return nil, err
		}
		items = append(items, item)
	}

	return items, nil
}

// NewGzipHandler wraps an http.Handler and returns a new handler that adds gzip compression to the response.
func NewGzipHandler(h http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {

		// Check if client supports gzip
		if strings.Contains(r.Header.Get("Accept-Encoding"), "gzip") {
			log.Println("GZipping response")
			w.Header().Set("Content-Encoding", "gzip")
			gzipWriter := gzip.NewWriter(w)
			defer gzipWriter.Close()

			// Create a new response writer with the gzip writer
			gzipResponseWriter := NewGzipResponseWriter(w, gzipWriter)

			// Call the original handler with the gzip response writer
			h.ServeHTTP(gzipResponseWriter, r)
		} else {
			// If client does not support gzip, call the original handler as is
			h.ServeHTTP(w, r)
		}
	})
}

// NewGzipResponseWriter wraps an http.ResponseWriter and adds gzip compression to the response.
type GzipResponseWriter struct {
	http.ResponseWriter
	gzipWriter *gzip.Writer
}

func NewGzipResponseWriter(w http.ResponseWriter, gzipWriter *gzip.Writer) *GzipResponseWriter {
	return &GzipResponseWriter{
		ResponseWriter: w,
		gzipWriter:     gzipWriter,
	}
}

// Write writes compressed data to the underlying ResponseWriter.
func (grw *GzipResponseWriter) Write(data []byte) (int, error) {
	return grw.gzipWriter.Write(data)
}

// Example HTTP request to get items in JSON format:
// curl -H "Accept-Encoding: gzip" http://localhost:8080/items --output items.json.gz
