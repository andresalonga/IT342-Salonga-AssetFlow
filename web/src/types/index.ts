export type UserRole = "admin" | "lab_keeper" | "student";

export type AssetStatus = "available" | "borrowed" | "maintenance";

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  avatar?: string;
}

export interface Asset {
  id: string;
  name: string;
  serialNumber: string;
  category: string;
  status: AssetStatus;
  image: string;
  description: string;
  addedDate: string;
}

export interface BorrowRequest {
  id: string;
  userId: string;
  userName: string;
  userEmail?: string;
  assetId: string;
  assetName: string;
  requestDate: string;
  dueDate: string;
  status: "pending" | "approved" | "rejected" | "returned";
  requestDateTime?: string;
  statusUpdatedAt?: string;
  rejectionNote?: string;
}
