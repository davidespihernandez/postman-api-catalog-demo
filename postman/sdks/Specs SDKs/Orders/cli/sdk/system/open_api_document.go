package system

import (
	"encoding/json"
	"example.com/orders/sdk/internal/unmarshal"
)

// OpenAPI 3.0 document
type OpenAPIDocument struct {
	Openapi    string    `json:"openapi" xml:"openapi" required:"true"`
	Info       Info      `json:"info" xml:"info" required:"true"`
	Servers    []Servers `json:"servers,omitempty" xml:"servers,omitempty"`
	Paths      any       `json:"paths" xml:"paths" required:"true"`
	Components any       `json:"components,omitempty" xml:"components,omitempty"`
}

func (o OpenAPIDocument) String() string {
	jsonData, err := json.MarshalIndent(o, "", "  ")
	if err != nil {
		return "error converting struct: OpenAPIDocument to string"
	}
	return string(jsonData)
}

func (o *OpenAPIDocument) UnmarshalJSON(data []byte) error {
	if err := unmarshal.ValidateRequiredJSONKeys(data, o); err != nil {
		return err
	}
	type alias OpenAPIDocument
	var tmp alias
	if err := json.Unmarshal(data, &tmp); err != nil {
		return err
	}
	*o = OpenAPIDocument(tmp)
	return nil
}
