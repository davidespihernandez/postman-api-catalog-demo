package system

import (
	"context"
	restClient "example.com/orders/sdk/internal/clients/rest"
	"example.com/orders/sdk/internal/clients/rest/hooks"
	"example.com/orders/sdk/internal/clients/rest/httptransport"
	"example.com/orders/sdk/internal/configmanager"
	"example.com/orders/sdk/orderssdkconfig"
	"example.com/orders/sdk/sharedmodels"
	"time"
)

// Service provides methods to interact with System-related API endpoints.
// It uses a configuration manager for settings and supports custom hooks for request/response interception.
type Service struct {
	manager          *configmanager.ConfigManager
	hook             hooks.Hook
	getHealthConfig  []orderssdkconfig.RequestOption
	getOpenAPIConfig []orderssdkconfig.RequestOption
}

func NewService() *Service {
	return &Service{
		manager: configmanager.NewConfigManager(orderssdkconfig.Config{}),
	}
}

// WithConfigManager sets the configuration manager for this service.
// Returns the service instance for method chaining.
func (api *Service) WithConfigManager(manager *configmanager.ConfigManager) *Service {
	api.manager = manager
	return api
}

// WithHook sets a custom hook for request/response interception.
// Returns the service instance for method chaining.
func (api *Service) WithHook(hook hooks.Hook) *Service {
	api.hook = hook
	return api
}

func (api *Service) config() *orderssdkconfig.Config {
	return api.manager.GetSystem()
}

func (api *Service) getHook() hooks.Hook {
	return api.hook
}

func (api *Service) SetBaseURL(baseURL string) {
	config := api.config()
	config.SetBaseURL(baseURL)
}

func (api *Service) SetTimeout(timeout time.Duration) {
	config := api.config()
	config.SetTimeout(timeout)
}

// SetGetHealthConfig sets method-level configuration for GetHealth.
// Options are applied to every future call to GetHealth and take
// precedence over service-level config. Per-call options still take highest precedence.
func (api *Service) SetGetHealthConfig(opts ...orderssdkconfig.RequestOption) *Service {
	api.getHealthConfig = opts
	return api
}

// SetGetOpenAPIConfig sets method-level configuration for GetOpenAPI.
// Options are applied to every future call to GetOpenAPI and take
// precedence over service-level config. Per-call options still take highest precedence.
func (api *Service) SetGetOpenAPIConfig(opts ...orderssdkconfig.RequestOption) *Service {
	api.getOpenAPIConfig = opts
	return api
}

func (api *Service) GetHealth(ctx context.Context, opts ...orderssdkconfig.RequestOption) (*HealthResponse, error) {
	config := *api.config()
	for _, opt := range api.getHealthConfig {
		opt(&config)
	}
	for _, opt := range opts {
		opt(&config)
	}

	httpRequest := httptransport.NewRequestBuilder().WithContext(ctx).
		WithMethod("GET").
		WithPath("/health").
		WithConfig(config).
		WithContentType(httptransport.ContentTypeJSON).
		WithResponseContentType(httptransport.ContentTypeJSON).
		WithSecuritySchemes(nil).
		Build()

	httpClient := restClient.NewRestClient[HealthResponse, []byte](config, api.getHook())
	resp, err := httpClient.Call(*httpRequest)
	if err != nil {
		return nil, sharedmodels.NewOrdersSDKError[[]byte](err)
	}

	return &resp.Data, nil
}

func (api *Service) GetOpenAPI(ctx context.Context, opts ...orderssdkconfig.RequestOption) (*OpenAPIDocument, error) {
	config := *api.config()
	for _, opt := range api.getOpenAPIConfig {
		opt(&config)
	}
	for _, opt := range opts {
		opt(&config)
	}

	httpRequest := httptransport.NewRequestBuilder().WithContext(ctx).
		WithMethod("GET").
		WithPath("/openapi.json").
		WithConfig(config).
		WithContentType(httptransport.ContentTypeJSON).
		WithResponseContentType(httptransport.ContentTypeJSON).
		WithSecuritySchemes(nil).
		Build()

	httpClient := restClient.NewRestClient[OpenAPIDocument, []byte](config, api.getHook())
	resp, err := httpClient.Call(*httpRequest)
	if err != nil {
		return nil, sharedmodels.NewOrdersSDKError[[]byte](err)
	}

	return &resp.Data, nil
}
