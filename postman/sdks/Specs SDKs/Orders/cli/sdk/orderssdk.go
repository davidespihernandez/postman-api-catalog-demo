package orderssdk

import (
	"example.com/orders/sdk/internal/clients/rest/hooks"
	"example.com/orders/sdk/internal/configmanager"
	"example.com/orders/sdk/orders"
	"example.com/orders/sdk/system"
	"time"
)

// OrdersSDK is the main SDK client that provides access to all service endpoints.
// It manages configuration, authentication, and service instances with centralized settings.
type OrdersSDK struct {
	System  *system.Service
	Orders  *orders.Service
	manager *configmanager.ConfigManager
}

func NewOrdersSDK(config Config) *OrdersSDK {
	system := system.NewService()
	orders := orders.NewService()

	manager := configmanager.NewConfigManager(config)
	hook := hooks.NewDefaultHook()
	system.WithConfigManager(manager)
	orders.WithConfigManager(manager)
	system.WithHook(hook)
	orders.WithHook(hook)

	return &OrdersSDK{
		System:  system,
		Orders:  orders,
		manager: manager,
	}
}

func (o *OrdersSDK) SetBaseURL(baseURL string) {
	o.manager.SetBaseURL(baseURL)
}

func (o *OrdersSDK) SetTimeout(timeout time.Duration) {
	o.manager.SetTimeout(timeout)
}

// SetEnvironment configures the SDK to use the specified environment's base URL.
func (o *OrdersSDK) SetEnvironment(environment Environment) {
	o.manager.SetBaseURL(string(environment))
}

// c029837e0e474b76bc487506e8799df5e3335891efe4fb02bda7a1441840310c
