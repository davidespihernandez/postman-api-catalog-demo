package orders

type UpdateOrderRequestStatus string

const (
	UpdateOrderRequestStatusPending    UpdateOrderRequestStatus = "pending"
	UpdateOrderRequestStatusProcessing UpdateOrderRequestStatus = "processing"
	UpdateOrderRequestStatusShipped    UpdateOrderRequestStatus = "shipped"
	UpdateOrderRequestStatusCancelled  UpdateOrderRequestStatus = "cancelled"
)
