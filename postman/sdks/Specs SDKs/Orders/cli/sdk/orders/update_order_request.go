package orders

import (
	"encoding/json"
	"example.com/orders/sdk/internal/unmarshal"
)

type UpdateOrderRequest struct {
	CustomerID string                   `json:"customerId" xml:"customerId" required:"true"`
	Total      float64                  `json:"total" xml:"total" required:"true" min:"0"`
	Status     UpdateOrderRequestStatus `json:"status" xml:"status" required:"true"`
	Currency   *string                  `json:"currency,omitempty" xml:"currency,omitempty" maxLength:"3" minLength:"3"`
}

func (u UpdateOrderRequest) String() string {
	jsonData, err := json.MarshalIndent(u, "", "  ")
	if err != nil {
		return "error converting struct: UpdateOrderRequest to string"
	}
	return string(jsonData)
}

func (u *UpdateOrderRequest) UnmarshalJSON(data []byte) error {
	if err := unmarshal.ValidateRequiredJSONKeys(data, u); err != nil {
		return err
	}
	type alias UpdateOrderRequest
	var tmp alias
	if err := json.Unmarshal(data, &tmp); err != nil {
		return err
	}
	*u = UpdateOrderRequest(tmp)
	return nil
}
