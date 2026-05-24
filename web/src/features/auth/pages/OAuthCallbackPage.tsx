import { useEffect, useState } from "react";
import { useLocation, useNavigate, Link } from "react-router-dom";
import { saveToken } from "@/lib/api";
import { useAuth } from "@/contexts/AuthContext";
import { Button } from "@/shared/ui/button";

const OAuthCallbackPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { setUserFromOAuth } = useAuth();
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const token = params.get("token");
    const message = params.get("error");

    if (message) {
      setError(decodeURIComponent(message));
      return;
    }

    if (!token) {
      setError("Missing authentication token.");
      return;
    }

    const userId = params.get("userId") || "";
    const name = params.get("name") || "";
    const email = params.get("email") || "";
    const role = params.get("role") || "USER";

    const nameParts = name.split(" ");
    const userData = {
      id: userId,
      firstName: nameParts[0] || "",
      lastName: nameParts.slice(1).join(" ") || "",
      email,
      role: role === "ADMIN" ? "admin" : "student",
    };

    saveToken(token);
    setUserFromOAuth(userData);
    navigate("/dashboard", { replace: true });
  }, [location.search, navigate, setUserFromOAuth]);

  if (!error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background">
        <p className="text-muted-foreground">Signing you in...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-6">
      <div className="max-w-md text-center space-y-4">
        <p className="text-destructive">{error}</p>
        <Button asChild variant="outline">
          <Link to="/">Back to login</Link>
        </Button>
      </div>
    </div>
  );
};

export default OAuthCallbackPage;
