package orders

import (
	"encoding/json"
	"example.com/orders/root"
	"example.com/orders/sdk/orders"
	"fmt"
	"github.com/spf13/cobra"
	"os"
)

var updateOrderCmd = &cobra.Command{
	Use:   "update-order",
	Short: "Replace order",
	Long:  "Body schema:\n  {\n    \"customerId\": \"usr-003\",\n    \"total\": 149.99,\n    \"status\": \"<update_order_request_status>\",\n    \"currency\": \"EUR\"\n  }\n\nExamples:\n  --body '{\"customerId\":\"usr-003\",\"total\":149.99,\"status\":\"<update_order_request_status>\",\"currency\":\"EUR\"}'\n  --body-file ./body.json",
	RunE: func(cmd *cobra.Command, args []string) error {
		id, err := cmd.Flags().GetString("id")
		if err != nil {
			fmt.Fprintln(os.Stderr, err)
			return err
		}
		bodyStr, err := cmd.Flags().GetString("body")
		if err != nil {
			fmt.Fprintf(os.Stderr, "Error: failed to get body param: %v\n", err)
			return err
		}
		bodyFile, err := cmd.Flags().GetString("body-file")
		if err != nil {
			fmt.Fprintf(os.Stderr, "Error: failed to get body-file param: %v\n", err)
			return err
		}

		var bodyContent []byte
		if bodyFile != "" {
			bodyContent, err = os.ReadFile(bodyFile)
			if err != nil {
				return err
			}
		} else {
			bodyContent = []byte(bodyStr)
		}

		var requestBody orders.UpdateOrderRequest
		if len(bodyContent) > 0 {
			if err := json.Unmarshal(bodyContent, &requestBody); err != nil {
				return err
			}
		}

		client := root.CreateSdkClient()
		response, err := client.Orders.UpdateOrder(cmd.Context(), id, requestBody)
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
	updateOrderCmd.Flags().StringP("id", "", "", "Order identifier")
	_ = updateOrderCmd.MarkFlagRequired("id")
	updateOrderCmd.Flags().String("body", "", "Request body as inline JSON, e.g. '{\"customerId\":\"usr-003\",\"total\":149.99,\"status\":\"<update_order_request_status>\",\"currency\":\"EUR\"}'")
	updateOrderCmd.Flags().String("body-file", "", "Path to a file containing the request body")
	updateOrderCmd.MarkFlagsMutuallyExclusive("body", "body-file")
}
