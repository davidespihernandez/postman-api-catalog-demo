package system

import (
	"encoding/json"
	"example.com/orders/root"
	"fmt"
	"github.com/spf13/cobra"
)

var getOpenApiCmd = &cobra.Command{
	Use:   "get-open-api",
	Short: "OpenAPI specification",
	RunE: func(cmd *cobra.Command, args []string) error {

		client := root.CreateSdkClient()
		response, err := client.System.GetOpenAPI(cmd.Context())
		if err != nil {
			return err
		}

		jsonData, err := json.MarshalIndent(response, "", "  ")
		if err != nil {
			fmt.Println(response)
		} else {
			fmt.Println(string(jsonData))
		}

		return nil
	},
}

func init() {
}
