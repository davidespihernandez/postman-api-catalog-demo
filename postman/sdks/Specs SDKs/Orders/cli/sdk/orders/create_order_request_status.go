package orders

// Initial order status
type CreateOrderRequestStatus string

const (
	CreateOrderRequestStatusPending    CreateOrderRequestStatus = "pending"
	CreateOrderRequestStatusProcessing CreateOrderRequestStatus = "processing"
	CreateOrderRequestStatusShipped    CreateOrderRequestStatus = "shipped"
	CreateOrderRequestStatusCancelled  CreateOrderRequestStatus = "cancelled"
)
