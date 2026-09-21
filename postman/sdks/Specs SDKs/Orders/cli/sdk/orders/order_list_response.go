package orders

import (
	"encoding/json"
	"example.com/orders/sdk/internal/unmarshal"
)

type OrderListResponse struct {
	// Orders in this page
	Data []Order `json:"data" xml:"data" required:"true"`
	// Number of orders returned
	Count int64 `json:"count" xml:"count" required:"true" min:"0"`
}

func (o OrderListResponse) String() string {
	jsonData, err := json.MarshalIndent(o, "", "  ")
	if err != nil {
		return "error converting struct: OrderListResponse to string"
	}
	return string(jsonData)
}

func (o *OrderListResponse) UnmarshalJSON(data []byte) error {
	if err := unmarshal.ValidateRequiredJSONKeys(data, o); err != nil {
		return err
	}
	type alias OrderListResponse
	var tmp alias
	if err := json.Unmarshal(data, &tmp); err != nil {
		return err
	}
	*o = OrderListResponse(tmp)
	return nil
}
