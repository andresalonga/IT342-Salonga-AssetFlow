import { useState, useEffect } from "react";
import { AppLayout } from "@/shared/components/AppLayout";
import { Badge } from "@/shared/ui/badge";
import { Button } from "@/shared/ui/button";
import { Input } from "@/shared/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/shared/ui/select";
import { cn } from "@/lib/utils";
import { Package, Calendar, Clock } from "lucide-react";
import { borrowApi, getToken } from "@/lib/api";
import { BorrowRequest } from "@/types";
import { useToast } from "@/shared/hooks/use-toast";

const statusStyles: Record<string, string> = {
  pending: "bg-warning/15 text-warning border-warning/30",
  approved: "bg-success/15 text-success border-success/30",
  rejected: "bg-destructive/15 text-destructive border-destructive/30",
  returned: "bg-info/15 text-info border-info/30",
};

const MyTransactionsPage = () => {
  const [myRequests, setMyRequests] = useState<BorrowRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");
  const [sortOrder, setSortOrder] = useState("newest");
  const { toast } = useToast();

  const pageSize = 6;

  useEffect(() => {
    const token = getToken();
    if (!token) return;

    const loadMyRequests = async () => {
      try {
        const response = await fetch("http://localhost:3000/api/assets/my-requests", {
          method: "GET",
          headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        });

        if (response.ok) {
          const data = await response.json();
          setMyRequests(data as BorrowRequest[]);
        } else {
          console.error("Failed to fetch requests:", response.status);
          toast({ title: "Error", description: "Failed to load your transactions", variant: "destructive" });
        }
      } catch (error) {
        console.error("Error loading requests:", error);
        toast({ title: "Error", description: "Failed to load your transactions", variant: "destructive" });
      } finally {
        setLoading(false);
      }
    };

    loadMyRequests();

    const intervalId = window.setInterval(() => {
      loadMyRequests();
    }, 5000);

    return () => {
      window.clearInterval(intervalId);
    };
  }, [toast]);

  const filteredRequests = myRequests.filter((r) => {
    const haystack = [
      r.assetName,
      r.status,
      r.requestDate,
      r.dueDate,
    ]
      .filter(Boolean)
      .join(" ")
      .toLowerCase();
    return haystack.includes(search.toLowerCase());
  });

  const sortedRequests = [...filteredRequests].sort((a, b) => {
    const aTime = new Date(a.requestDateTime || a.requestDate || 0).getTime();
    const bTime = new Date(b.requestDateTime || b.requestDate || 0).getTime();
    if (sortOrder === "oldest") {
      return aTime - bTime;
    }
    return bTime - aTime;
  });

  const totalPages = Math.max(1, Math.ceil(sortedRequests.length / pageSize));
  const pagedRequests = sortedRequests.slice((page - 1) * pageSize, page * pageSize);

  const pageNumbers = (() => {
    const maxButtons = 5;
    if (totalPages <= maxButtons) {
      return Array.from({ length: totalPages }, (_, i) => i + 1);
    }

    const start = Math.max(1, Math.min(page - 2, totalPages - (maxButtons - 1)));
    return Array.from({ length: maxButtons }, (_, i) => start + i);
  })();

  useEffect(() => {
    setPage(1);
  }, [myRequests.length, search, sortOrder]);

  const formatPst = (value?: string) => {
    if (!value) return "-";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return new Intl.DateTimeFormat("en-PH", {
      dateStyle: "medium",
      timeStyle: "short",
      timeZone: "Asia/Manila",
    }).format(date);
  };

  const formatPstDate = (value?: string) => {
    if (!value) return "-";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return new Intl.DateTimeFormat("en-PH", {
      dateStyle: "medium",
      timeZone: "Asia/Manila",
    }).format(date);
  };

  return (
    <AppLayout>
      <div className="p-6 lg:p-8 max-w-4xl mx-auto space-y-6 animate-fade-in">
        <div>
          <h1 className="text-2xl font-bold">My Transactions</h1>
          <p className="text-muted-foreground text-sm mt-1">Track your borrow requests and return history.</p>
        </div>

        {loading ? (
          <div className="text-center py-16 text-muted-foreground">
            <p className="font-medium">Loading your transactions...</p>
          </div>
        ) : myRequests.length === 0 ? (
          <div className="text-center py-16 text-muted-foreground">
            <Package className="h-12 w-12 mx-auto mb-3 opacity-30" />
            <p className="font-medium">No transactions yet</p>
            <p className="text-sm">Browse the inventory to submit a borrow request.</p>
          </div>
        ) : (
          <>
            <div className="flex flex-col sm:flex-row gap-3">
              <Input
                placeholder="Search by asset, status, or date..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
              <Select value={sortOrder} onValueChange={setSortOrder}>
                <SelectTrigger className="w-full sm:w-56">
                  <SelectValue placeholder="Sort" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="newest">Newest to Oldest</SelectItem>
                  <SelectItem value="oldest">Oldest to Newest</SelectItem>
                </SelectContent>
              </Select>
            </div>
            {sortedRequests.length === 0 ? (
              <div className="text-center py-12 text-muted-foreground">
                <p className="font-medium">No matching transactions found</p>
              </div>
            ) : (
            <div className="space-y-3">
              {pagedRequests.map((r) => (
              <div key={r.id} className="bg-card rounded-xl border p-4 flex items-center gap-4 hover:shadow-sm transition-shadow">
                <div className="h-10 w-10 rounded-lg bg-muted flex items-center justify-center shrink-0">
                  <Package className="h-5 w-5 text-muted-foreground" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="font-medium text-sm">{r.assetName}</p>
                  <div className="flex items-center gap-3 mt-1 text-xs text-muted-foreground">
                    <span className="flex items-center gap-1">
                      <Calendar className="h-3 w-3" /> Requested: {formatPst(r.requestDateTime || r.requestDate)}
                    </span>
                    <span className="flex items-center gap-1">
                      <Clock className="h-3 w-3" /> Due: {formatPstDate(r.dueDate)}
                    </span>
                  </div>
                  {r.status === "rejected" && r.rejectionNote && (
                    <div className="text-[10px] text-muted-foreground mt-1">
                      Note: {r.rejectionNote}
                    </div>
                  )}
                </div>
                <div className="flex flex-col items-end gap-1">
                  <Badge variant="outline" className={cn("text-xs capitalize shrink-0", statusStyles[r.status])}>
                    {r.status}
                  </Badge>
                  <span className="text-[10px] text-muted-foreground">{formatPst(r.statusUpdatedAt)}</span>
                </div>
              </div>
              ))}
            </div>
            )}
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

export default MyTransactionsPage;
