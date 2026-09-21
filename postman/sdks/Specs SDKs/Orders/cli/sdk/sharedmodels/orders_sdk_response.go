package sharedmodels

import (
	"encoding/json"
	"example.com/orders/sdk/internal/clients/rest/httptransport"
	"net/http"
)

// OrdersSDKResponse is the user-facing wrapper for API responses.
// It contains the deserialized data, raw HTTP response, and metadata like headers and status code.
type OrdersSDKResponse[T any] struct {
	Data     T
	Raw      *http.Response
	Metadata OrdersSDKResponseMetadata
}

// OrdersSDKResponseMetadata contains HTTP metadata from the API response.
// Includes status code and headers for inspection and debugging.
type OrdersSDKResponseMetadata struct {
	Headers    map[string]string
	StatusCode int
}

// NewOrdersSDKResponse creates a new response wrapper from an internal transport response.
// Extracts data and metadata into a user-facing structure.
func NewOrdersSDKResponse[T any](resp *httptransport.Response[T]) *OrdersSDKResponse[T] {
	return &OrdersSDKResponse[T]{
		Data: resp.Data,
		Raw:  resp.Raw,
		Metadata: OrdersSDKResponseMetadata{
			StatusCode: resp.StatusCode,
			Headers:    resp.Headers,
		},
	}
}

// GetData returns the deserialized response data.
func (r *OrdersSDKResponse[T]) GetData() T {
	return r.Data
}

// String returns a JSON representation of the response for debugging.
// Returns an error message if JSON marshaling fails.
func (r OrdersSDKResponse[T]) String() string {
	jsonData, err := json.MarshalIndent(r, "", "  ")
	if err != nil {
		return "error converting struct: OrdersSDKResponse to string"
	}
	return string(jsonData)
}
