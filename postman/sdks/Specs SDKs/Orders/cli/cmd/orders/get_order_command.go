package orders

import (
	"encoding/json"
	"example.com/orders/root"
	"fmt"
	"github.com/spf13/cobra"
	"os"
)

var getOrderCmd = &cobra.Command{
	Use:   "get-order",
	Short: "Get order by ID",
	RunE: func(cmd *cobra.Command, args []string) error {
		id, err := cmd.Flags().GetString("id")
		if err != nil {
			fmt.Fprintln(os.Stderr, err)
			return err
		}

		client := root.CreateSdkClient()
		response, err := client.Orders.GetOrder(cmd.Context(), id)
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
	getOrderCmd.Flags().StringP("id", "", "", "Order identifier")
	_ = getOrderCmd.MarkFlagRequired("id")
}
