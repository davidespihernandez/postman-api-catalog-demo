package orders

import (
	"encoding/json"
	"example.com/orders/root"
	"example.com/orders/sdk/orders"
	"fmt"
	"github.com/spf13/cobra"
	"os"
)

var createOrderCmd = &cobra.Command{
	Use:   "create-order",
	Short: "Create order",
	Long:  "Body schema:\n  {\n    \"customerId\": \"usr-002\",\n    \"total\": 99.5,\n    \"status\": \"<create_order_request_status>\",\n    \"currency\": \"USD\"\n  }\n\nExamples:\n  --body '{\"customerId\":\"usr-002\",\"total\":99.5,\"status\":\"<create_order_request_status>\",\"currency\":\"USD\"}'\n  --body-file ./body.json",
	RunE: func(cmd *cobra.Command, args []string) error {
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

		var requestBody orders.CreateOrderRequest
		if len(bodyContent) > 0 {
			if err := json.Unmarshal(bodyContent, &requestBody); err != nil {
				return err
			}
		}

		client := root.CreateSdkClient()
		response, err := client.Orders.CreateOrder(cmd.Context(), requestBody)
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
	createOrderCmd.Flags().String("body", "", "Request body as inline JSON, e.g. '{\"customerId\":\"usr-002\",\"total\":99.5,\"status\":\"<create_order_request_status>\",\"currency\":\"USD\"}'")
	createOrderCmd.Flags().String("body-file", "", "Path to a file containing the request body")
	createOrderCmd.MarkFlagsMutuallyExclusive("body", "body-file")
}
