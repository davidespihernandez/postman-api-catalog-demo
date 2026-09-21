package sharedmodels

import (
	"example.com/orders/sdk/internal/clients/rest/httptransport"
	"net/http"
)

// OrdersSDKError wraps API errors with detailed metadata including status code, headers, and raw response.
// It implements the error interface and provides structured access to error information.
type OrdersSDKError[T any] struct {
	Err      error
	Data     *T
	Body     []byte
	Raw      *http.Response
	Metadata OrdersSDKErrorMetadata
}

// OrdersSDKErrorMetadata contains HTTP metadata associated with an error response.
type OrdersSDKErrorMetadata struct {
	Headers    map[string]string
	StatusCode int
}

// NewOrdersSDKError creates a new OrdersSDKError from an internal transport error.
// It extracts error details, body, status code, and headers into a user-facing error structure.
func NewOrdersSDKError[T any](transportError *httptransport.ErrorResponse[T]) *OrdersSDKError[T] {
	return &OrdersSDKError[T]{
		Err:  transportError.GetError(),
		Data: transportError.Data,
		Body: transportError.GetBody(),
		Raw:  transportError.Raw,
		Metadata: OrdersSDKErrorMetadata{
			StatusCode: transportError.GetStatusCode(),
			Headers:    transportError.GetHeaders(),
		},
	}
}

// Error implements the error interface, returning the error message string.
func (e *OrdersSDKError[T]) Error() string {
	if e == nil || e.Err == nil {
		return ""
	}
	return e.Err.Error()
}

// Unwrap returns the underlying error, enabling errors.Is and errors.As to traverse the chain.
func (e *OrdersSDKError[T]) Unwrap() error {
	return e.Err
}

// GetData returns the deserialized error response data.
// Returns nil if unmarshaling failed or the response body was empty.
func (e *OrdersSDKError[T]) GetData() *T {
	return e.Data
}

// GetBody returns the raw response body bytes from the error response.
// Returns nil if no response body was received.
func (e *OrdersSDKError[T]) GetBody() []byte {
	return e.Body
}
