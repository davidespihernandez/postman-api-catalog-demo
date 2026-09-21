package orders

import (
	"example.com/orders/root"
	"github.com/spf13/cobra"
)

var Cmd = &cobra.Command{
	Use: "orders",
}

func init() {
	Cmd.AddCommand(listOrdersCmd)
	Cmd.AddCommand(createOrderCmd)
	Cmd.AddCommand(getOrderCmd)
	Cmd.AddCommand(updateOrderCmd)
	Cmd.AddCommand(patchOrderCmd)
	Cmd.AddCommand(deleteOrderCmd)
	root.AddCommand(Cmd)
}
