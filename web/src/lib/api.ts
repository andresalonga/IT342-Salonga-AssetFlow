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

const DEFAULT_ASSET_IMAGE = "/assetflow-default.svg";

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

const mapAssetDto = (dto: AssetDto): Asset => {
  const addedDate = dto.createdAt ? dto.createdAt.split("T")[0] : new Date().toISOString().split("T")[0];
  const rawUrl = dto.imageUrl ? (dto.imageUrl.startsWith("/") ? `${API_ROOT_URL}${dto.imageUrl}` : dto.imageUrl) : "";
  const imageUrl = rawUrl.replace("10.0.2.2", "localhost");

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
    const response = await fetch(`${API_ASSET_URL}/${data.assetId}/borrow`, {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ dueDate: data.dueDate }),
    });

    if (!response.ok) {
      throw new Error("Failed to submit borrow request");
    }

    return response.json();
  },

  getAll: async (token: string): Promise<BorrowRequestResponse[]> => {
    const response = await fetch(`${API_ASSET_URL}/borrow-requests`, {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      throw new Error("Failed to fetch borrow requests");
    }

    return response.json();
  },

  updateStatus: async (id: string, status: "approved" | "rejected" | "returned", token: string, note?: string) => {
    const response = await fetch(`${API_ASSET_URL}/borrow-requests/${id}`, {
      method: "PATCH",
      headers: {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ status, note }),
    });

    if (!response.ok) {
      throw new Error("Failed to update borrow request");
    }

    return response.json();
  },
};
