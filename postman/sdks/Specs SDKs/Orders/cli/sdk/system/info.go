package system

import (
	"encoding/json"
	"example.com/orders/sdk/internal/unmarshal"
)

type Info struct {
	Title       string  `json:"title" xml:"title" required:"true"`
	Version     string  `json:"version" xml:"version" required:"true"`
	Description *string `json:"description,omitempty" xml:"description,omitempty"`
}

func (i Info) String() string {
	jsonData, err := json.MarshalIndent(i, "", "  ")
	if err != nil {
		return "error converting struct: Info to string"
	}
	return string(jsonData)
}

func (i *Info) UnmarshalJSON(data []byte) error {
	if err := unmarshal.ValidateRequiredJSONKeys(data, i); err != nil {
		return err
	}
	type alias Info
	var tmp alias
	if err := json.Unmarshal(data, &tmp); err != nil {
		return err
	}
	*i = Info(tmp)
	return nil
}
