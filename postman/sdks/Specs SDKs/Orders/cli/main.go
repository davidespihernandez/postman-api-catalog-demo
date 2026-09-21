package main

import (
	_ "example.com/orders/cmd/orders"
	_ "example.com/orders/cmd/system"
	_ "example.com/orders/config"
	"example.com/orders/root"
)

func main() {
	root.Execute()
}
