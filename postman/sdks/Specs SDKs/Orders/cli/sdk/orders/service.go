package orders

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

// Service provides methods to interact with Orders-related API endpoints.
// It uses a configuration manager for settings and supports custom hooks for request/response interception.
type Service struct {
	manager           *configmanager.ConfigManager
	hook              hooks.Hook
	listOrdersConfig  []orderssdkconfig.RequestOption
	createOrderConfig []orderssdkconfig.RequestOption
	getOrderConfig    []orderssdkconfig.RequestOption
	updateOrderConfig []orderssdkconfig.RequestOption
	patchOrderConfig  []orderssdkconfig.RequestOption
	deleteOrderConfig []orderssdkconfig.RequestOption
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
	return api.manager.GetOrders()
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

// SetListOrdersConfig sets method-level configuration for ListOrders.
// Options are applied to every future call to ListOrders and take
// precedence over service-level config. Per-call options still take highest precedence.
func (api *Service) SetListOrdersConfig(opts ...orderssdkconfig.RequestOption) *Service {
	api.listOrdersConfig = opts
	return api
}

// SetCreateOrderConfig sets method-level configuration for CreateOrder.
// Options are applied to every future call to CreateOrder and take
// precedence over service-level config. Per-call options still take highest precedence.
func (api *Service) SetCreateOrderConfig(opts ...orderssdkconfig.RequestOption) *Service {
	api.createOrderConfig = opts
	return api
}

// SetGetOrderConfig sets method-level configuration for GetOrder.
// Options are applied to every future call to GetOrder and take
// precedence over service-level config. Per-call options still take highest precedence.
func (api *Service) SetGetOrderConfig(opts ...orderssdkconfig.RequestOption) *Service {
	api.getOrderConfig = opts
	return api
}

// SetUpdateOrderConfig sets method-level configuration for UpdateOrder.
// Options are applied to every future call to UpdateOrder and take
// precedence over service-level config. Per-call options still take highest precedence.
func (api *Service) SetUpdateOrderConfig(opts ...orderssdkconfig.RequestOption) *Service {
	api.updateOrderConfig = opts
	return api
}

// SetPatchOrderConfig sets method-level configuration for PatchOrder.
// Options are applied to every future call to PatchOrder and take
// precedence over service-level config. Per-call options still take highest precedence.
func (api *Service) SetPatchOrderConfig(opts ...orderssdkconfig.RequestOption) *Service {
	api.patchOrderConfig = opts
	return api
}

// SetDeleteOrderConfig sets method-level configuration for DeleteOrder.
// Options are applied to every future call to DeleteOrder and take
// precedence over service-level config. Per-call options still take highest precedence.
func (api *Service) SetDeleteOrderConfig(opts ...orderssdkconfig.RequestOption) *Service {
	api.deleteOrderConfig = opts
	return api
}

func (api *Service) ListOrders(ctx context.Context, opts ...orderssdkconfig.RequestOption) (*OrderListResponse, error) {
	config := *api.config()
	for _, opt := range api.listOrdersConfig {
		opt(&config)
	}
	for _, opt := range opts {
		opt(&config)
	}

	httpRequest := httptransport.NewRequestBuilder().WithContext(ctx).
		WithMethod("GET").
		WithPath("/orders").
		WithConfig(config).
		WithContentType(httptransport.ContentTypeJSON).
		WithResponseContentType(httptransport.ContentTypeJSON).
		WithSecuritySchemes(nil).
		Build()

	httpClient := restClient.NewRestClient[OrderListResponse, []byte](config, api.getHook())
	resp, err := httpClient.Call(*httpRequest)
	if err != nil {
		return nil, sharedmodels.NewOrdersSDKError[[]byte](err)
	}

	return &resp.Data, nil
}

func (api *Service) CreateOrder(ctx context.Context, createOrderRequest CreateOrderRequest, opts ...orderssdkconfig.RequestOption) (*Order, error) {
	config := *api.config()
	for _, opt := range api.createOrderConfig {
		opt(&config)
	}
	for _, opt := range opts {
		opt(&config)
	}

	httpRequest := httptransport.NewRequestBuilder().WithContext(ctx).
		WithMethod("POST").
		WithPath("/orders").
		WithConfig(config).
		WithBody(createOrderRequest).
		AddHeader("CONTENT-TYPE", "application/json").
		WithContentType(httptransport.ContentTypeJSON).
		WithResponseContentType(httptransport.ContentTypeJSON).
		WithSecuritySchemes(nil).
		Build()

	httpClient := restClient.NewRestClient[Order, []byte](config, api.getHook())
	resp, err := httpClient.Call(*httpRequest)
	if err != nil {
		return nil, sharedmodels.NewOrdersSDKError[[]byte](err)
	}

	return &resp.Data, nil
}

func (api *Service) GetOrder(ctx context.Context, id string, opts ...orderssdkconfig.RequestOption) (*Order, error) {
	config := *api.config()
	for _, opt := range api.getOrderConfig {
		opt(&config)
	}
	for _, opt := range opts {
		opt(&config)
	}

	httpRequest := httptransport.NewRequestBuilder().WithContext(ctx).
		WithMethod("GET").
		WithPath("/orders/{id}").
		WithConfig(config).
		AddPathParam("id", id).
		WithContentType(httptransport.ContentTypeJSON).
		WithResponseContentType(httptransport.ContentTypeJSON).
		WithSecuritySchemes(nil).
		Build()

	httpClient := restClient.NewRestClient[Order, []byte](config, api.getHook())
	resp, err := httpClient.Call(*httpRequest)
	if err != nil {
		return nil, sharedmodels.NewOrdersSDKError[[]byte](err)
	}

	return &resp.Data, nil
}

func (api *Service) UpdateOrder(ctx context.Context, id string, updateOrderRequest UpdateOrderRequest, opts ...orderssdkconfig.RequestOption) (*Order, error) {
	config := *api.config()
	for _, opt := range api.updateOrderConfig {
		opt(&config)
	}
	for _, opt := range opts {
		opt(&config)
	}

	httpRequest := httptransport.NewRequestBuilder().WithContext(ctx).
		WithMethod("PUT").
		WithPath("/orders/{id}").
		WithConfig(config).
		WithBody(updateOrderRequest).
		AddHeader("CONTENT-TYPE", "application/json").
		AddPathParam("id", id).
		WithContentType(httptransport.ContentTypeJSON).
		WithResponseContentType(httptransport.ContentTypeJSON).
		WithSecuritySchemes(nil).
		Build()

	httpClient := restClient.NewRestClient[Order, []byte](config, api.getHook())
	resp, err := httpClient.Call(*httpRequest)
	if err != nil {
		return nil, sharedmodels.NewOrdersSDKError[[]byte](err)
	}

	return &resp.Data, nil
}

func (api *Service) PatchOrder(ctx context.Context, id string, patchOrderRequest PatchOrderRequest, opts ...orderssdkconfig.RequestOption) (*Order, error) {
	config := *api.config()
	for _, opt := range api.patchOrderConfig {
		opt(&config)
	}
	for _, opt := range opts {
		opt(&config)
	}

	httpRequest := httptransport.NewRequestBuilder().WithContext(ctx).
		WithMethod("PATCH").
		WithPath("/orders/{id}").
		WithConfig(config).
		WithBody(patchOrderRequest).
		AddHeader("CONTENT-TYPE", "application/json").
		AddPathParam("id", id).
		WithContentType(httptransport.ContentTypeJSON).
		WithResponseContentType(httptransport.ContentTypeJSON).
		WithSecuritySchemes(nil).
		Build()

	httpClient := restClient.NewRestClient[Order, []byte](config, api.getHook())
	resp, err := httpClient.Call(*httpRequest)
	if err != nil {
		return nil, sharedmodels.NewOrdersSDKError[[]byte](err)
	}

	return &resp.Data, nil
}

func (api *Service) DeleteOrder(ctx context.Context, id string, opts ...orderssdkconfig.RequestOption) (any, error) {
	config := *api.config()
	for _, opt := range api.deleteOrderConfig {
		opt(&config)
	}
	for _, opt := range opts {
		opt(&config)
	}

	httpRequest := httptransport.NewRequestBuilder().WithContext(ctx).
		WithMethod("DELETE").
		WithPath("/orders/{id}").
		WithConfig(config).
		AddPathParam("id", id).
		WithContentType(httptransport.ContentTypeJSON).
		WithResponseContentType(httptransport.ContentTypeJSON).
		WithSecuritySchemes(nil).
		Build()

	httpClient := restClient.NewRestClient[any, []byte](config, api.getHook())
	resp, err := httpClient.Call(*httpRequest)
	if err != nil {
		return nil, sharedmodels.NewOrdersSDKError[[]byte](err)
	}

	return resp.Data, nil
}
