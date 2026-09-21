package configmanager

import (
	"example.com/orders/sdk/orderssdkconfig"
	"time"
)

// ConfigManager manages configuration across all services with synchronized updates.
// Provides centralized configuration management and OAuth token handling for multiple services.
type ConfigManager struct {
	system orderssdkconfig.Config
	orders orderssdkconfig.Config
}

// NewConfigManager creates a new configuration manager with the provided config and optional OAuth token service.
// Initializes service-specific configs and sets up OAuth token management if enabled.
func NewConfigManager(config orderssdkconfig.Config) *ConfigManager {
	return &ConfigManager{
		system: config,
		orders: config,
	}
}

// SetBaseURL updates the BaseURL configuration parameter across all services.
// Changes are applied synchronously to all registered service configurations.
func (c *ConfigManager) SetBaseURL(baseURL string) {
	c.system.SetBaseURL(baseURL)
	c.orders.SetBaseURL(baseURL)
}

// SetTimeout updates the Timeout configuration parameter across all services.
// Changes are applied synchronously to all registered service configurations.
func (c *ConfigManager) SetTimeout(timeout time.Duration) {
	c.system.SetTimeout(timeout)
	c.orders.SetTimeout(timeout)
}

// SetRetryConfig updates the retry configuration across all services.
// Changes are applied synchronously to all registered service configurations.
func (c *ConfigManager) SetRetryConfig(retry orderssdkconfig.RetryConfig) {
	c.system.SetRetryConfig(retry)
	c.orders.SetRetryConfig(retry)
}

// GetSystem returns the configuration for the System service.
// Returns a pointer to the service-specific config for use in API calls.
func (c *ConfigManager) GetSystem() *orderssdkconfig.Config {
	return &c.system
}

// GetOrders returns the configuration for the Orders service.
// Returns a pointer to the service-specific config for use in API calls.
func (c *ConfigManager) GetOrders() *orderssdkconfig.Config {
	return &c.orders
}

// GetBaseURL returns the currently configured base URL.
// All services share the same base URL; this reads it from the first service's config.
func (c *ConfigManager) GetBaseURL() string {
	return c.system.BaseURL
}
