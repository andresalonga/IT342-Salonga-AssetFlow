import { useState, useMemo } from "react";
import { MOCK_ASSETS } from "@/data/mock";
import { AssetCard } from "@/components/AssetCard";
import { AssetStatus } from "@/types";
import { useAuth } from "@/contexts/AuthContext";
import { AppLayout } from "@/components/AppLayout";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Search, Package, AlertTriangle, CheckCircle, Clock } from "lucide-react";
import { cn } from "@/lib/utils";

const statusFilters: { value: AssetStatus | "all"; label: string; icon: React.ElementType }[] = [
  { value: "all", label: "All", icon: Package },
  { value: "available", label: "Available", icon: CheckCircle },
  { value: "borrowed", label: "Borrowed", icon: Clock },
  { value: "maintenance", label: "Maintenance", icon: AlertTriangle },
];

const DashboardPage = () => {
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<AssetStatus | "all">("all");
  const { user, isAdmin } = useAuth();

  const filtered = useMemo(() => {
    return MOCK_ASSETS.filter((a) => {
      const matchesSearch = a.name.toLowerCase().includes(search.toLowerCase()) ||
        a.serialNumber.toLowerCase().includes(search.toLowerCase()) ||
        a.category.toLowerCase().includes(search.toLowerCase());
      const matchesStatus = statusFilter === "all" || a.status === statusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [search, statusFilter]);

  const stats = useMemo(() => ({
    total: MOCK_ASSETS.length,
    available: MOCK_ASSETS.filter((a) => a.status === "available").length,
    borrowed: MOCK_ASSETS.filter((a) => a.status === "borrowed").length,
    maintenance: MOCK_ASSETS.filter((a) => a.status === "maintenance").length,
  }), []);

  return (
    <AppLayout>
      <div className="p-6 lg:p-8 max-w-7xl mx-auto space-y-6">
        {/* Header */}
        <div className="animate-fade-in">
          <h1 className="text-2xl font-bold">Inventory Dashboard</h1>
          <p className="text-muted-foreground text-sm mt-1">
            Welcome back, {user?.firstName}. {isAdmin ? "Manage your organization's assets." : "Browse available assets."}
          </p>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 animate-fade-in">
          {[
            { label: "Total Assets", value: stats.total, icon: Package, color: "text-foreground" },
            { label: "Available", value: stats.available, icon: CheckCircle, color: "text-success" },
            { label: "Borrowed", value: stats.borrowed, icon: Clock, color: "text-warning" },
            { label: "Maintenance", value: stats.maintenance, icon: AlertTriangle, color: "text-destructive" },
          ].map((s) => (
            <div key={s.label} className="bg-card rounded-xl border p-4 flex items-center gap-3">
              <div className={cn("p-2 rounded-lg bg-muted", s.color)}>
                <s.icon className="h-4 w-4" />
              </div>
              <div>
                <p className="text-2xl font-bold">{s.value}</p>
                <p className="text-xs text-muted-foreground">{s.label}</p>
              </div>
            </div>
          ))}
        </div>

        {/* Search + Filters */}
        <div className="flex flex-col sm:flex-row gap-3 animate-fade-in">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="Search assets by name, serial number, or category..."
              className="pl-10"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <div className="flex gap-1.5">
            {statusFilters.map((f) => (
              <Button
                key={f.value}
                variant={statusFilter === f.value ? "default" : "outline"}
                size="sm"
                className="gap-1.5 text-xs"
                onClick={() => setStatusFilter(f.value)}
              >
                <f.icon className="h-3.5 w-3.5" />
                {f.label}
              </Button>
            ))}
          </div>
        </div>

        {/* Grid */}
        {filtered.length === 0 ? (
          <div className="text-center py-16 text-muted-foreground animate-fade-in">
            <Package className="h-12 w-12 mx-auto mb-3 opacity-30" />
            <p className="font-medium">No assets found</p>
            <p className="text-sm">Try adjusting your search or filter.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {filtered.map((asset) => (
              <AssetCard key={asset.id} asset={asset} />
            ))}
          </div>
        )}
      </div>
    </AppLayout>
  );
};

export default DashboardPage;
