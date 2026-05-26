import { Toaster } from "@/shared/ui/toaster";
import { Toaster as Sonner } from "@/shared/ui/sonner";
import { TooltipProvider } from "@/shared/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "@/contexts/AuthContext";
import LoginPage from "@/features/auth/pages/LoginPage";
import RegisterPage from "@/features/auth/pages/RegisterPage";
import OAuthCallbackPage from "@/features/auth/pages/OAuthCallbackPage";
import DashboardPage from "@/features/assets/pages/DashboardPage";
import AssetDetailPage from "@/features/assets/pages/AssetDetailPage";
import BorrowRequestsPage from "@/features/borrow/pages/BorrowRequestsPage";
import MyTransactionsPage from "@/features/borrow/pages/MyTransactionsPage";
import AddAssetPage from "@/features/assets/pages/AddAssetPage";
import NotFound from "./pages/NotFound";

const queryClient = new QueryClient();

const ProtectedRoute = ({ children, adminOnly = false }: { children: React.ReactNode; adminOnly?: boolean }) => {
  const { user, isAdmin } = useAuth();
  if (!user) return <Navigate to="/" replace />;
  if (adminOnly && !isAdmin) return <Navigate to="/dashboard" replace />;
  return <>{children}</>;
};

const AppRoutes = () => {
  const { user } = useAuth();

  return (
    <Routes>
      <Route path="/" element={user ? <Navigate to="/dashboard" replace /> : <LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/oauth/callback" element={<OAuthCallbackPage />} />
      <Route path="/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
      <Route path="/assets/:id" element={<ProtectedRoute><AssetDetailPage /></ProtectedRoute>} />
      <Route path="/assets/new" element={<ProtectedRoute adminOnly><AddAssetPage /></ProtectedRoute>} />
      <Route path="/assets/edit/:id" element={<ProtectedRoute adminOnly><AddAssetPage /></ProtectedRoute>} />
      <Route path="/requests" element={<ProtectedRoute adminOnly><BorrowRequestsPage /></ProtectedRoute>} />
      <Route path="/my-transactions" element={<ProtectedRoute><MyTransactionsPage /></ProtectedRoute>} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
};

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <Toaster />
      <Sonner />
      <BrowserRouter>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </BrowserRouter>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
