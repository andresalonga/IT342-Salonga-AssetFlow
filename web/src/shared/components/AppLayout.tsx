import { ReactNode, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import {
  LayoutDashboard,
  Package,
  ClipboardList,
  History,
  LogOut,
  PlusCircle,
  Shield,
  GraduationCap,
} from "lucide-react";
import { Button } from "@/shared/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/shared/ui/dialog";
import { cn } from "@/lib/utils";
import { UserRole } from "@/types";

const navItems = {
  all: [
    { to: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  ],
  admin: [
    { to: "/requests", label: "Borrow Requests", icon: ClipboardList },
    { to: "/assets/new", label: "Add Asset", icon: PlusCircle },
  ],
  student: [
    { to: "/my-transactions", label: "My Transactions", icon: History },
  ],
};

const roleIcons: Record<UserRole, ReactNode> = {
  admin: <Shield className="h-3.5 w-3.5" />,
  lab_keeper: <Shield className="h-3.5 w-3.5" />,
  student: <GraduationCap className="h-3.5 w-3.5" />,
};

export const AppLayout = ({ children }: { children: ReactNode }) => {
  const { user, logout, isAdmin } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [logoutOpen, setLogoutOpen] = useState(false);

  if (!user) return null;

  const links = [
    ...navItems.all,
    ...(isAdmin ? navItems.admin : navItems.student),
  ];

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <div className="flex h-screen">
      {/* Sidebar */}
      <aside className="w-64 shrink-0 bg-sidebar text-sidebar-foreground flex flex-col border-r border-sidebar-border">
        <div className="p-5 border-b border-sidebar-border">
          <div className="flex items-center gap-2.5">
            <div className="h-8 w-8 rounded-lg bg-sidebar-primary flex items-center justify-center">
              <Package className="h-4.5 w-4.5 text-sidebar-primary-foreground" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-base font-bold tracking-tight">AssetFlow</h1>
                {user.role === "admin" && (
                  <span className="inline-flex items-center gap-1 rounded-full bg-sidebar-accent px-2 py-0.5 text-[10px] font-medium text-sidebar-accent-foreground capitalize">
                    {roleIcons[user.role]}
                    Admin
                  </span>
                )}
              </div>
              <p className="text-[10px] text-sidebar-muted uppercase tracking-widest">Inventory System</p>
            </div>
          </div>
        </div>

        <nav className="flex-1 p-3 space-y-1">
          {links.map((item) => {
            const active = location.pathname === item.to;
            return (
              <Link
                key={item.to}
                to={item.to}
                className={cn(
                  "flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-colors",
                  active
                    ? "bg-sidebar-accent text-sidebar-accent-foreground font-medium"
                    : "text-sidebar-muted hover:text-sidebar-foreground hover:bg-sidebar-accent/50"
                )}
              >
                <item.icon className="h-4 w-4" />
                {item.label}
              </Link>
            );
          })}
        </nav>

        {/* User info and logout */}
        <div className="p-3 border-t border-sidebar-border">
          <div className="flex items-center gap-3 px-3 py-2">
            <div className="h-8 w-8 rounded-full bg-sidebar-accent flex items-center justify-center text-xs font-medium overflow-hidden">
              <span>{user.firstName?.[0] || "U"}{user.lastName?.[0] || ""}</span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium truncate">{user.firstName} {user.lastName}</p>
              <p className="text-[10px] text-sidebar-muted truncate">{user.email}</p>
            </div>
            <Button
              variant="ghost"
              size="icon"
              className="h-7 w-7 text-sidebar-muted hover:text-sidebar-foreground"
              onClick={() => setLogoutOpen(true)}
              title="Logout"
            >
              <LogOut className="h-3.5 w-3.5" />
            </Button>
          </div>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 overflow-auto">
        {children}
      </main>

      <Dialog open={logoutOpen} onOpenChange={setLogoutOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Log out?</DialogTitle>
            <DialogDescription>
              You will need to sign in again to access your account.
            </DialogDescription>
          </DialogHeader>
          <div className="flex justify-end gap-2">
            <Button variant="outline" onClick={() => setLogoutOpen(false)}>
              Cancel
            </Button>
            <Button
              variant="destructive"
              onClick={() => {
                setLogoutOpen(false);
                handleLogout();
              }}
            >
              Log out
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
};
