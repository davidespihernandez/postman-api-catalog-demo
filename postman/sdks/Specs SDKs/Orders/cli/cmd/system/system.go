package system

import (
	"example.com/orders/root"
	"github.com/spf13/cobra"
)

var Cmd = &cobra.Command{
	Use: "system",
}

func init() {
	Cmd.AddCommand(getHealthCmd)
	Cmd.AddCommand(getOpenApiCmd)
	root.AddCommand(Cmd)
}
