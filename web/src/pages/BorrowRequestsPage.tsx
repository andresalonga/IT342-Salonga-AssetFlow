import { useState } from "react";
import { MOCK_REQUESTS } from "@/data/mock";
import { AppLayout } from "@/components/AppLayout";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { CheckCircle, XCircle, RotateCcw } from "lucide-react";
import { useToast } from "@/hooks/use-toast";
import { cn } from "@/lib/utils";

const statusStyles: Record<string, string> = {
  pending: "bg-warning/15 text-warning border-warning/30",
  approved: "bg-success/15 text-success border-success/30",
  rejected: "bg-destructive/15 text-destructive border-destructive/30",
  returned: "bg-info/15 text-info border-info/30",
};

const BorrowRequestsPage = () => {
  const [requests, setRequests] = useState(MOCK_REQUESTS);
  const { toast } = useToast();

  const updateStatus = (id: string, status: "approved" | "rejected" | "returned") => {
    setRequests((prev) => prev.map((r) => (r.id === id ? { ...r, status } : r)));
    toast({ title: `Request ${status}`, description: `The borrow request has been ${status}.` });
  };

  return (
    <AppLayout>
      <div className="p-6 lg:p-8 max-w-6xl mx-auto space-y-6 animate-fade-in">
        <div>
          <h1 className="text-2xl font-bold">Borrow Requests</h1>
          <p className="text-muted-foreground text-sm mt-1">Review and manage student borrow requests.</p>
        </div>

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
                {requests.map((r) => (
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
        </div>
      </div>
    </AppLayout>
  );
};

export default BorrowRequestsPage;
