package orders

import (
	"encoding/json"
	"example.com/orders/sdk/internal/unmarshal"
)

type CreateOrderRequest struct {
	// Customer placing the order
	CustomerID string  `json:"customerId" xml:"customerId" required:"true"`
	Total      float64 `json:"total" xml:"total" required:"true" min:"0"`
	// Initial order status
	Status *CreateOrderRequestStatus `json:"status,omitempty" xml:"status,omitempty"`
	// ISO 4217 currency code
	Currency *string `json:"currency,omitempty" xml:"currency,omitempty" maxLength:"3" minLength:"3"`
}

func (c CreateOrderRequest) String() string {
	jsonData, err := json.MarshalIndent(c, "", "  ")
	if err != nil {
		return "error converting struct: CreateOrderRequest to string"
	}
	return string(jsonData)
}

func (c *CreateOrderRequest) UnmarshalJSON(data []byte) error {
	if err := unmarshal.ValidateRequiredJSONKeys(data, c); err != nil {
		return err
	}
	type alias CreateOrderRequest
	var tmp alias
	if err := json.Unmarshal(data, &tmp); err != nil {
		return err
	}
	*c = CreateOrderRequest(tmp)
	return nil
}
