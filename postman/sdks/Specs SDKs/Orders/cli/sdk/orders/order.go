package orders

import (
	"encoding/json"
	"example.com/orders/sdk/internal/unmarshal"
)

type Order struct {
	// Unique order identifier
	ID string `json:"id" xml:"id" required:"true"`
	// ID of the customer who placed the order
	CustomerID string `json:"customerId" xml:"customerId" required:"true"`
	// Order lifecycle status
	Status OrderStatus `json:"status" xml:"status" required:"true"`
	// Order total amount
	Total float64 `json:"total" xml:"total" required:"true" min:"0"`
	// ISO 4217 currency code
	Currency string `json:"currency" xml:"currency" required:"true" maxLength:"3" minLength:"3"`
}

func (o Order) String() string {
	jsonData, err := json.MarshalIndent(o, "", "  ")
	if err != nil {
		return "error converting struct: Order to string"
	}
	return string(jsonData)
}

func (o *Order) UnmarshalJSON(data []byte) error {
	if err := unmarshal.ValidateRequiredJSONKeys(data, o); err != nil {
		return err
	}
	type alias Order
	var tmp alias
	if err := json.Unmarshal(data, &tmp); err != nil {
		return err
	}
	*o = Order(tmp)
	return nil
}
