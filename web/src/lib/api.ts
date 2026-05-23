import { Asset, AssetStatus } from "@/types";

const API_BASE_URL = "http://localhost:3000/api/auth";
const API_ROOT_URL = "http://localhost:3000";

interface AuthResponse {
  message: string;
  userId?: number;
  name: string;
  email: string;
  role: string;
  token: string;
  success: boolean;
}

interface RegisterData {
  name: string;
  email: string;
  password: string;
}

interface LoginData {
  email: string;
  password: string;
}

export const authApi = {
  register: async (data: RegisterData): Promise<AuthResponse> => {
    const response = await fetch(`${API_BASE_URL}/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    });
    
    const result = await response.json();
    return result;
  },

  login: async (data: LoginData): Promise<AuthResponse> => {
    const response = await fetch(`${API_BASE_URL}/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    });
    
    const result = await response.json();
    return result;
  },

  getCurrentUser: async (token: string) => {
    const response = await fetch(`${API_BASE_URL}/me`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });
    
    if (!response.ok) {
      throw new Error("Failed to get current user");
    }
    
    return response.json();
  },
};

export const saveToken = (token: string) => {
  localStorage.setItem("token", token);
};

export const getToken = () => {
  return localStorage.getItem("token");
};

export const removeToken = () => {
  localStorage.removeItem("token");
};

// Borrow Request + Asset API
const API_ASSET_URL = "http://localhost:3000/api/assets";

const DEFAULT_ASSET_IMAGE =
  "https://images.unsplash.com/photo-1488590528505-98d2b5aba04b?w=400&h=300&fit=crop";

interface AssetDto {
  id: string;
  name: string;
  serialNumber: string;
  category: string;
  status: string;
  description?: string;
  imageUrl?: string;
  createdAt?: string;
}

interface AssetCreateData {
  name: string;
  serialNumber: string;
  category: string;
  status: AssetStatus;
  description?: string;
  imageUrl?: string;
}

interface AssetUpdateData {
  name?: string;
  serialNumber?: string;
  category?: string;
  status?: AssetStatus;
  description?: string;
  imageUrl?: string;
}

interface BorrowRequestData {
  assetId: string;
  dueDate: string;
}

interface BorrowRequestResponse {
  id: string;
  userId: string;
  userName: string;
  assetId: string;
  assetName: string;
  requestDate: string;
  dueDate: string;
  status: "pending" | "approved" | "rejected" | "returned";
}

// Initialize default borrow requests from mock data
const initializeBorrowRequests = () => {
  const existing = localStorage.getItem("borrowRequests");
  if (!existing) {
    // Start with empty array - requests will be added as they're submitted
    localStorage.setItem("borrowRequests", JSON.stringify([]));
  }
};

initializeBorrowRequests();

const mapAssetDto = (dto: AssetDto): Asset => {
  const addedDate = dto.createdAt ? dto.createdAt.split("T")[0] : new Date().toISOString().split("T")[0];
  const imageUrl = dto.imageUrl ? (dto.imageUrl.startsWith("/") ? `${API_ROOT_URL}${dto.imageUrl}` : dto.imageUrl) : "";

  return {
    id: String(dto.id),
    name: dto.name,
    serialNumber: dto.serialNumber,
    category: dto.category,
    status: (dto.status as AssetStatus) || "available",
    image: imageUrl || DEFAULT_ASSET_IMAGE,
    description: dto.description || "",
    addedDate,
  };
};

export const assetApi = {
  getAll: async (token?: string): Promise<Asset[]> => {
    const response = await fetch(API_ASSET_URL, {
      method: "GET",
      headers: {
        "Authorization": token ? `Bearer ${token}` : "",
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      throw new Error("Failed to fetch assets");
    }

    const data: AssetDto[] = await response.json();
    return data.map(mapAssetDto);
  },

  getById: async (id: string, token?: string): Promise<Asset> => {
    const response = await fetch(`${API_ASSET_URL}/${id}`, {
      method: "GET",
      headers: {
        "Authorization": token ? `Bearer ${token}` : "",
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      throw new Error("Failed to fetch asset");
    }

    const data: AssetDto = await response.json();
    return mapAssetDto(data);
  },

  create: async (data: AssetCreateData, token?: string): Promise<Asset> => {
    const response = await fetch(API_ASSET_URL, {
      method: "POST",
      headers: {
        "Authorization": token ? `Bearer ${token}` : "",
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      throw new Error("Failed to create asset");
    }

    const result: AssetDto = await response.json();
    return mapAssetDto(result);
  },

  update: async (id: string, data: AssetUpdateData, token?: string): Promise<Asset> => {
    const response = await fetch(`${API_ASSET_URL}/${id}`, {
      method: "PUT",
      headers: {
        "Authorization": token ? `Bearer ${token}` : "",
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      throw new Error("Failed to update asset");
    }

    const result: AssetDto = await response.json();
    return mapAssetDto(result);
  },

  delete: async (id: string, token?: string): Promise<void> => {
    const response = await fetch(`${API_ASSET_URL}/${id}`, {
      method: "DELETE",
      headers: {
        "Authorization": token ? `Bearer ${token}` : "",
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      throw new Error("Failed to delete asset");
    }
  },

  uploadImage: async (file: File, token?: string): Promise<string> => {
    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch(`${API_ASSET_URL}/upload`, {
      method: "POST",
      headers: {
        "Authorization": token ? `Bearer ${token}` : "",
      },
      body: formData,
    });

    if (!response.ok) {
      throw new Error("Failed to upload image");
    }

    const result = await response.json();
    return result.url as string;
  },
};

export const categoryApi = {
  getAll: async (token?: string): Promise<string[]> => {
    const response = await fetch("http://localhost:3000/api/categories", {
      method: "GET",
      headers: {
        "Authorization": token ? `Bearer ${token}` : "",
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      throw new Error("Failed to fetch categories");
    }

    return response.json();
  },
};

export const borrowApi = {
  submitRequest: async (data: BorrowRequestData, token: string): Promise<BorrowRequestResponse> => {
    try {
      // Try calling backend API first
      const response = await fetch(`${API_ASSET_URL}/${data.assetId}/borrow`, {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ dueDate: data.dueDate }),
      });
      
      if (response.ok) {
        const result = await response.json();
        console.log("Borrow request saved to backend:", result);
        return result;
      } else {
        console.error("Backend error:", response.status);
        // Fallback to localStorage if backend fails
        return storeBorrowRequestLocal(data);
      }
    } catch (error) {
      console.log("Backend not available, saving to localStorage:", error);
      // Fallback: use localStorage
      return storeBorrowRequestLocal(data);
    }
  },

  getAll: async (token: string): Promise<BorrowRequestResponse[]> => {
    try {
      // Try calling backend API first
      const response = await fetch(`${API_ASSET_URL}/borrow-requests`, {
        method: "GET",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });
      
      if (response.ok) {
        const data = await response.json();
        console.log("Fetched requests from backend:", data);
        return data;
      } else {
        console.error("Backend error:", response.status);
        // Fallback to localStorage
        return getBorrowRequestsLocal();
      }
    } catch (error) {
      console.log("Backend not available, fetching from localStorage:", error);
      // Fallback: use localStorage
      return getBorrowRequestsLocal();
    }
  },

  updateStatus: async (id: string, status: "approved" | "rejected" | "returned", token: string) => {
    try {
      // Try calling backend API first
      const response = await fetch(`${API_ASSET_URL}/borrow-requests/${id}`, {
        method: "PATCH",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ status }),
      });
      
      if (response.ok) {
        const result = await response.json();
        console.log("Request updated in backend:", result);
        return result;
      } else {
        console.error("Backend error:", response.status);
        // Fallback to localStorage
        return updateBorrowRequestLocal(id, status);
      }
    } catch (error) {
      console.log("Backend not available, updating localStorage:", error);
      // Fallback: use localStorage
      return updateBorrowRequestLocal(id, status);
    }
  },
};

// Local storage helpers for borrow requests
const storeBorrowRequestLocal = (data: BorrowRequestData, backendResult?: any): BorrowRequestResponse => {
  const requests = getBorrowRequestsLocal();
  const user = JSON.parse(localStorage.getItem("user") || "{}");
  const asset = JSON.parse(localStorage.getItem("currentAsset") || "{}");
  
  // Create new request object
  const newRequest: BorrowRequestResponse = backendResult || {
    id: `r${Date.now()}`,
    userId: user.id || "unknown",
    userName: `${user.firstName || ""} ${user.lastName || ""}`.trim() || "Unknown User",
    assetId: data.assetId,
    assetName: asset.name || "Unknown Asset",
    requestDate: new Date().toISOString().split("T")[0],
    dueDate: data.dueDate,
    status: "pending",
  };
  
  // Check if this request already exists
  const exists = requests.find((r) => r.id === newRequest.id);
  if (!exists) {
    requests.push(newRequest);
    localStorage.setItem("borrowRequests", JSON.stringify(requests));
    console.log("Borrow request saved to localStorage:", newRequest);
  }
  
  return newRequest;
};

const getBorrowRequestsLocal = (): BorrowRequestResponse[] => {
  const stored = localStorage.getItem("borrowRequests");
  const requests = stored ? JSON.parse(stored) : [];
  console.log("Fetched requests from localStorage:", requests);
  return requests;
};

const updateBorrowRequestLocal = (id: string, status: "approved" | "rejected" | "returned"): BorrowRequestResponse | null => {
  const requests = getBorrowRequestsLocal();
  const request = requests.find((r) => r.id === id);
  
  if (request) {
    request.status = status;
    localStorage.setItem("borrowRequests", JSON.stringify(requests));
    console.log("Borrow request updated in localStorage:", request);
  }
  
  return request || null;
};
