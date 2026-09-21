package orders

type PatchOrderRequestStatus string

const (
	PatchOrderRequestStatusPending    PatchOrderRequestStatus = "pending"
	PatchOrderRequestStatusProcessing PatchOrderRequestStatus = "processing"
	PatchOrderRequestStatusShipped    PatchOrderRequestStatus = "shipped"
	PatchOrderRequestStatusCancelled  PatchOrderRequestStatus = "cancelled"
)
