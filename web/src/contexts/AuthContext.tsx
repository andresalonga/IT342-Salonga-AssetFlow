import React, { createContext, useContext, useState, useEffect, ReactNode } from "react";
import { authApi, getToken, removeToken, saveToken } from "@/lib/api";

const AuthContext = createContext(undefined);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check for existing token on mount
    const token = getToken();
    if (token) {
      // For now, just set user as logged in with stored data
      // In production, you'd validate the token with the backend
      const storedUser = localStorage.getItem("user");
      if (storedUser) {
        setUser(JSON.parse(storedUser));
      }
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    try {
      const response = await authApi.login({ email, password });
      
      if (response.success) {
        // Save token
        saveToken(response.token);
        
        // Create user object (backend returns name as single field)
        const nameParts = response.name.split(" ");
        const userData = {
          id: response.userId,
          firstName: nameParts[0] || "",
          lastName: nameParts.slice(1).join(" ") || "",
          email: response.email,
          role: response.role === "ADMIN" ? "admin" : "student",
        };
        
        setUser(userData);
        localStorage.setItem("user", JSON.stringify(userData));
        return { success: true };
      } else {
        return { success: false, message: response.message };
      }
    } catch (error) {
      console.error("Login error:", error);
      return { success: false, message: "An error occurred during login" };
    }
  };

  const register = async (name, email, password) => {
    try {
      const response = await authApi.register({ name, email, password });
      
      if (response.success) {
        return { success: true, message: response.message };
      } else {
        return { success: false, message: response.message };
      }
    } catch (error) {
      console.error("Registration error:", error);
      return { success: false, message: "An error occurred during registration" };
    }
  };

  const logout = () => {
    removeToken();
    localStorage.removeItem("user");
    setUser(null);
  };

  const setUserFromOAuth = (userData) => {
    setUser(userData);
    localStorage.setItem("user", JSON.stringify(userData));
  };

  const isAdmin = user?.role === "admin";
  const isStudent = user?.role === "student";

  return (
    <AuthContext.Provider value={{ 
      user, 
      login, 
      logout, 
      register,
      setUserFromOAuth,
      isAdmin, 
      isStudent,
      loading 
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
};
