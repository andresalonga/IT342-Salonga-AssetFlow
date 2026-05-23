import { useEffect, useMemo, useState } from "react";
import { Asset } from "@/types";
import { AssetCard } from "@/features/assets/components/AssetCard";
import { AssetStatus } from "@/types";
import { useAuth } from "@/contexts/AuthContext";
import { AppLayout } from "@/shared/components/AppLayout";
import { Input } from "@/shared/ui/input";
import { Button } from "@/shared/ui/button";
import { Search, Package, AlertTriangle, CheckCircle, Clock } from "lucide-react";
import { cn } from "@/lib/utils";
import { assetApi, getToken } from "@/lib/api";
import { useToast } from "@/shared/hooks/use-toast";

const statusFilters: { value: AssetStatus | "all"; label: string; icon: React.ElementType }[] = [
  { value: "all", label: "All", icon: Package },
  { value: "available", label: "Available", icon: CheckCircle },
  { value: "borrowed", label: "Borrowed", icon: Clock },
  { value: "maintenance", label: "Maintenance", icon: AlertTriangle },
];

const DashboardPage = () => {
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<AssetStatus | "all">("all");
  const [page, setPage] = useState(1);
  const [assets, setAssets] = useState<Asset[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const { user, isAdmin } = useAuth();
  const { toast } = useToast();

  const pageSize = 9;

  useEffect(() => {
    let active = true;
    const loadAssets = async () => {
      try {
        const token = getToken();
        const data = await assetApi.getAll(token || undefined);
        if (active) {
          setAssets(data);
        }
      } catch (error) {
        if (active) {
          toast({
            title: "Failed to load assets",
            description: "Please check your connection and try again.",
            variant: "destructive",
          });
        }
      } finally {
        if (active) {
          setIsLoading(false);
        }
      }
    };

    loadAssets();
    return () => {
      active = false;
    };
  }, [toast]);

  const filtered = useMemo(() => {
    return assets.filter((a) => {
      const matchesSearch = a.name.toLowerCase().includes(search.toLowerCase()) ||
        a.serialNumber.toLowerCase().includes(search.toLowerCase()) ||
        a.category.toLowerCase().includes(search.toLowerCase());
      const matchesStatus = statusFilter === "all" || a.status === statusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [assets, search, statusFilter]);

  useEffect(() => {
    setPage(1);
  }, [search, statusFilter]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  const pagedAssets = useMemo(() => {
    const start = (page - 1) * pageSize;
    return filtered.slice(start, start + pageSize);
  }, [filtered, page]);

  const pageNumbers = useMemo(() => {
    const maxButtons = 5;
    if (totalPages <= maxButtons) {
      return Array.from({ length: totalPages }, (_, i) => i + 1);
    }

    const start = Math.max(1, Math.min(page - 2, totalPages - (maxButtons - 1)));
    return Array.from({ length: maxButtons }, (_, i) => start + i);
  }, [page, totalPages]);

  const stats = useMemo(() => ({
    total: assets.length,
    available: assets.filter((a) => a.status === "available").length,
    borrowed: assets.filter((a) => a.status === "borrowed").length,
    maintenance: assets.filter((a) => a.status === "maintenance").length,
  }), [assets]);

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
        {isLoading ? (
          <div className="text-center py-16 text-muted-foreground animate-fade-in">
            <Package className="h-12 w-12 mx-auto mb-3 opacity-30" />
            <p className="font-medium">Loading assets...</p>
          </div>
        ) : filtered.length === 0 ? (
          <div className="text-center py-16 text-muted-foreground animate-fade-in">
            <Package className="h-12 w-12 mx-auto mb-3 opacity-30" />
            <p className="font-medium">No assets found</p>
            <p className="text-sm">Try adjusting your search or filter.</p>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {pagedAssets.map((asset) => (
                <AssetCard key={asset.id} asset={asset} />
              ))}
            </div>
            {totalPages > 1 && (
              <div className="flex items-center justify-between pt-4">
                <p className="text-sm text-muted-foreground">
                  Page {page} of {totalPages}
                </p>
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setPage((p) => Math.max(1, p - 1))}
                    disabled={page === 1}
                  >
                    Previous
                  </Button>
                  {pageNumbers.map((pageNumber) => (
                    <Button
                      key={pageNumber}
                      variant={pageNumber === page ? "default" : "outline"}
                      size="sm"
                      onClick={() => setPage(pageNumber)}
                    >
                      {pageNumber}
                    </Button>
                  ))}
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                    disabled={page === totalPages}
                  >
                    Next
                  </Button>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </AppLayout>
  );
};

export default DashboardPage;
