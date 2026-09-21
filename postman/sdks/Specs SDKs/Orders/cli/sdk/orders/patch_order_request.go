package orders

import "encoding/json"

type PatchOrderRequest struct {
	CustomerID *string                  `json:"customerId,omitempty" xml:"customerId,omitempty"`
	Total      *float64                 `json:"total,omitempty" xml:"total,omitempty" min:"0"`
	Status     *PatchOrderRequestStatus `json:"status,omitempty" xml:"status,omitempty"`
	Currency   *string                  `json:"currency,omitempty" xml:"currency,omitempty" maxLength:"3" minLength:"3"`
}

func (p PatchOrderRequest) String() string {
	jsonData, err := json.MarshalIndent(p, "", "  ")
	if err != nil {
		return "error converting struct: PatchOrderRequest to string"
	}
	return string(jsonData)
}
