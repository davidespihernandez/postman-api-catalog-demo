package system

import (
	"encoding/json"
	"example.com/orders/root"
	"fmt"
	"github.com/spf13/cobra"
)

var getHealthCmd = &cobra.Command{
	Use:   "get-health",
	Short: "Health check",
	RunE: func(cmd *cobra.Command, args []string) error {

		client := root.CreateSdkClient()
		response, err := client.System.GetHealth(cmd.Context())
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
