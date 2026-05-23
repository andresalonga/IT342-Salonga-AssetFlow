import { useState, useEffect } from "react";
import { AppLayout } from "@/shared/components/AppLayout";
import { Button } from "@/shared/ui/button";
import { Badge } from "@/shared/ui/badge";
import { CheckCircle, XCircle, RotateCcw } from "lucide-react";
import { useToast } from "@/shared/hooks/use-toast";
import { borrowApi, getToken } from "@/lib/api";
import { BorrowRequest } from "@/types";
import { cn } from "@/lib/utils";

const statusStyles: Record<string, string> = {
  pending: "bg-warning/15 text-warning border-warning/30",
  approved: "bg-success/15 text-success border-success/30",
  rejected: "bg-destructive/15 text-destructive border-destructive/30",
  returned: "bg-info/15 text-info border-info/30",
};

const BorrowRequestsPage = () => {
  const [requests, setRequests] = useState<BorrowRequest[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const { toast } = useToast();

  const pageSize = 10;

  useEffect(() => {
    const token = getToken();
    if (!token) return;

    const loadRequests = async () => {
      try {
        const data = await borrowApi.getAll(token);
        setRequests(data as BorrowRequest[]);
      } catch (error) {
        console.error("Error loading requests:", error);
        toast({ title: "Error", description: "Failed to load borrow requests", variant: "destructive" });
      } finally {
        setLoading(false);
      }
    };

    loadRequests();
  }, [toast]);

  const updateStatus = async (id: string, status: "approved" | "rejected" | "returned") => {
    const token = getToken();
    if (!token) return;

    try {
      await borrowApi.updateStatus(id, status, token);
      setRequests((prev) => prev.map((r) => (r.id === id ? { ...r, status } : r)));
      toast({ title: `Request ${status}`, description: `The borrow request has been ${status}.` });
    } catch (error) {
      toast({ title: "Error", description: "Failed to update request status", variant: "destructive" });
    }
  };

  const totalPages = Math.max(1, Math.ceil(requests.length / pageSize));
  const pagedRequests = requests.slice((page - 1) * pageSize, page * pageSize);

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
  }, [requests.length]);

  return (
    <AppLayout>
      <div className="p-6 lg:p-8 max-w-6xl mx-auto space-y-6 animate-fade-in">
        <div>
          <h1 className="text-2xl font-bold">Borrow Requests</h1>
          <p className="text-muted-foreground text-sm mt-1">Review and manage student borrow requests.</p>
        </div>

        {loading ? (
          <div className="text-center py-12">
            <p className="text-muted-foreground">Loading requests...</p>
          </div>
        ) : requests.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-muted-foreground">No borrow requests yet.</p>
          </div>
        ) : (
          <div className="bg-card rounded-xl border overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b bg-muted/50">
                    <th className="text-left p-4 font-medium text-muted-foreground">Student</th>
                    <th className="text-left p-4 font-medium text-muted-foreground">Asset</th>
                    <th className="text-left p-4 font-medium text-muted-foreground">Request Date</th>
                    <th className="text-left p-4 font-medium text-muted-foreground">Due Date</th>
                    <th className="text-left p-4 font-medium text-muted-foreground">Status</th>
                    <th className="text-right p-4 font-medium text-muted-foreground">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {pagedRequests.map((r) => (
                    <tr key={r.id} className="border-b last:border-0 hover:bg-muted/30 transition-colors">
                      <td className="p-4 font-medium">{r.userName}</td>
                      <td className="p-4">{r.assetName}</td>
                      <td className="p-4 text-muted-foreground">{r.requestDate}</td>
                      <td className="p-4 text-muted-foreground">{r.dueDate}</td>
                      <td className="p-4">
                        <Badge variant="outline" className={cn("text-xs capitalize", statusStyles[r.status])}>
                          {r.status}
                        </Badge>
                      </td>
                      <td className="p-4 text-right">
                        <div className="flex gap-1.5 justify-end">
                          {r.status === "pending" && (
                            <>
                              <Button size="sm" variant="outline" className="h-7 gap-1 text-xs text-success hover:text-success" onClick={() => updateStatus(r.id, "approved")}>
                                <CheckCircle className="h-3 w-3" /> Approve
                              </Button>
                              <Button size="sm" variant="outline" className="h-7 gap-1 text-xs text-destructive hover:text-destructive" onClick={() => updateStatus(r.id, "rejected")}>
                                <XCircle className="h-3 w-3" /> Reject
                              </Button>
                            </>
                          )}
                          {r.status === "approved" && (
                            <Button size="sm" variant="outline" className="h-7 gap-1 text-xs" onClick={() => updateStatus(r.id, "returned")}>
                              <RotateCcw className="h-3 w-3" /> Mark Returned
                            </Button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {totalPages > 1 && (
              <div className="flex items-center justify-between p-4 border-t bg-muted/30">
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
          </div>
        )}
      </div>
    </AppLayout>
  );
};

export default BorrowRequestsPage;
