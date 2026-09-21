package orders

import (
	"encoding/json"
	"example.com/orders/root"
	"fmt"
	"github.com/spf13/cobra"
)

var listOrdersCmd = &cobra.Command{
	Use:   "list-orders",
	Short: "List orders",
	RunE: func(cmd *cobra.Command, args []string) error {

		client := root.CreateSdkClient()
		response, err := client.Orders.ListOrders(cmd.Context())
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
