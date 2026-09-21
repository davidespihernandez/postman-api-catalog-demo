package system

import "encoding/json"

type Servers struct {
	URL         *string `json:"url,omitempty" xml:"url,omitempty"`
	Description *string `json:"description,omitempty" xml:"description,omitempty"`
}

func (s Servers) String() string {
	jsonData, err := json.MarshalIndent(s, "", "  ")
	if err != nil {
		return "error converting struct: Servers to string"
	}
	return string(jsonData)
}
