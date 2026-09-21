package system

import (
	"encoding/json"
	"example.com/orders/sdk/internal/unmarshal"
)

type HealthResponse struct {
	Status  HealthResponseStatus `json:"status" xml:"status" required:"true"`
	Service string               `json:"service" xml:"service" required:"true"`
	Version string               `json:"version" xml:"version" required:"true"`
}

func (h HealthResponse) String() string {
	jsonData, err := json.MarshalIndent(h, "", "  ")
	if err != nil {
		return "error converting struct: HealthResponse to string"
	}
	return string(jsonData)
}

func (h *HealthResponse) UnmarshalJSON(data []byte) error {
	if err := unmarshal.ValidateRequiredJSONKeys(data, h); err != nil {
		return err
	}
	type alias HealthResponse
	var tmp alias
	if err := json.Unmarshal(data, &tmp); err != nil {
		return err
	}
	*h = HealthResponse(tmp)
	return nil
}
