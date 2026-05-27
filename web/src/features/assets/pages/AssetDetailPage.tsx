import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { Asset } from "@/types";
import { useAuth } from "@/contexts/AuthContext";
import { AppLayout } from "@/shared/components/AppLayout";
import { StatusBadge } from "@/shared/components/StatusBadge";
import { Button } from "@/shared/ui/button";
import { Input } from "@/shared/ui/input";
import { Label } from "@/shared/ui/label";
import { ArrowLeft, Edit, Trash2, Calendar, Tag, Hash } from "lucide-react";
import { useToast } from "@/shared/hooks/use-toast";
import { assetApi, borrowApi, getToken } from "@/lib/api";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/shared/ui/dialog";

const AssetDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAdmin, isStudent } = useAuth();
  const { toast } = useToast();
  const [openBorrowDialog, setOpenBorrowDialog] = useState(false);
  const [dueDate, setDueDate] = useState("");
  const [isBorrowing, setIsBorrowing] = useState(false);
  const [asset, setAsset] = useState<Asset | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (!id) {
      return;
    }

    let active = true;
    const loadAsset = async () => {
      try {
        const token = getToken();
        const data = await assetApi.getById(id, token || undefined);
        if (active) {
          setAsset(data);
        }
      } catch (error) {
        if (active) {
          toast({
            title: "Asset not found",
            description: "Please select another asset.",
            variant: "destructive",
          });
        }
      } finally {
        if (active) {
          setIsLoading(false);
        }
      }
    };

    loadAsset();
    return () => {
      active = false;
    };
  }, [id, toast]);

  if (isLoading) {
    return (
      <AppLayout>
        <div className="flex items-center justify-center h-full">
          <p className="text-muted-foreground">Loading asset...</p>
        </div>
      </AppLayout>
    );
  }

  if (!asset) {
    return (
      <AppLayout>
        <div className="flex items-center justify-center h-full">
          <p className="text-muted-foreground">Asset not found.</p>
        </div>
      </AppLayout>
    );
  }

  const formatDate = (date: Date) => date.toISOString().split("T")[0];
  const today = new Date();
  const minDueDate = formatDate(today);
  const maxDueDate = formatDate(new Date(today.getFullYear(), today.getMonth(), today.getDate() + 7));

  const handleBorrowClick = () => {
    const defaultDate = new Date(today.getFullYear(), today.getMonth(), today.getDate() + 7);
    setDueDate(formatDate(defaultDate));
    setOpenBorrowDialog(true);
  };

  const handleSubmitBorrow = async () => {
    if (!dueDate) {
      toast({ title: "Error", description: "Please select a due date", variant: "destructive" });
      return;
    }

    if (dueDate < minDueDate || dueDate > maxDueDate) {
      toast({
        title: "Invalid due date",
        description: "Due date must be today or within the next 7 days.",
        variant: "destructive",
      });
      return;
    }

    setIsBorrowing(true);
    try {
      const token = getToken();
      await borrowApi.submitRequest(
        {
          assetId: asset.id,
          dueDate: dueDate,
        },
        token || ""
      );

      toast({ 
        title: "Success!", 
        description: `Your borrow request for ${asset.name} has been submitted to the admin.`
      });
      setOpenBorrowDialog(false);
      setDueDate("");
    } catch (error) {
      toast({ 
        title: "Error", 
        description: "Failed to submit borrow request",
        variant: "destructive"
      });
    } finally {
      setIsBorrowing(false);
    }
  };

  return (
    <AppLayout>
      <div className="p-6 lg:p-8 max-w-5xl mx-auto animate-fade-in">
        <Button variant="ghost" size="sm" className="mb-4 gap-2" onClick={() => navigate(-1)}>
          <ArrowLeft className="h-4 w-4" /> Back
        </Button>

        <div className="grid md:grid-cols-2 gap-8">
          {/* Image */}
          <div className="aspect-[4/3] rounded-xl overflow-hidden bg-muted">
            <img src={asset.image} alt={asset.name} className="h-full w-full object-cover" />
          </div>

          {/* Info */}
          <div className="space-y-6">
            <div>
              <div className="flex items-center gap-3 mb-2">
                <StatusBadge status={asset.status} />
              </div>
              <h1 className="text-2xl font-bold">{asset.name}</h1>
              <p className="text-muted-foreground text-sm mt-2">{asset.description}</p>
            </div>

            <div className="space-y-3 bg-muted/50 rounded-xl p-4">
              <div className="flex items-center gap-3 text-sm">
                <Hash className="h-4 w-4 text-muted-foreground" />
                <span className="text-muted-foreground">Serial Number</span>
                <span className="ml-auto font-mono font-medium">{asset.serialNumber}</span>
              </div>
              <div className="flex items-center gap-3 text-sm">
                <Tag className="h-4 w-4 text-muted-foreground" />
                <span className="text-muted-foreground">Category</span>
                <span className="ml-auto font-medium">{asset.category}</span>
              </div>
              <div className="flex items-center gap-3 text-sm">
                <Calendar className="h-4 w-4 text-muted-foreground" />
                <span className="text-muted-foreground">Added</span>
                <span className="ml-auto font-medium">{asset.addedDate}</span>
              </div>
            </div>

            {/* Actions */}
            <div className="flex gap-3">
              {isStudent && asset.status === "available" && (
                <Button className="flex-1" onClick={handleBorrowClick}>
                  Submit Borrow Request
                </Button>
              )}
              {isAdmin && (
                <>
                  <Button variant="outline" className="gap-2" onClick={() => navigate(`/assets/edit/${asset.id}`)}>
                    <Edit className="h-4 w-4" /> Edit
                  </Button>
                  <Button
                    variant="outline"
                    className="gap-2 text-destructive hover:text-destructive"
                    onClick={async () => {
                      try {
                        const token = getToken();
                        await assetApi.delete(asset.id, token || undefined);
                        toast({ title: "Asset deleted", variant: "destructive" });
                        navigate("/dashboard");
                      } catch (error) {
                        toast({
                          title: "Delete failed",
                          description: "Please try again.",
                          variant: "destructive",
                        });
                      }
                    }}
                  >
                    <Trash2 className="h-4 w-4" /> Delete
                  </Button>
                </>
              )}
            </div>
          </div>
        </div>

        {/* Borrow Request Dialog */}
        <Dialog open={openBorrowDialog} onOpenChange={setOpenBorrowDialog}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Request to Borrow</DialogTitle>
              <DialogDescription>
                Set when you plan to return the {asset.name}
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <Label htmlFor="due-date">Due Date</Label>
                <Input
                  id="due-date"
                  type="date"
                  value={dueDate}
                  onChange={(e) => setDueDate(e.target.value)}
                  min={minDueDate}
                  max={maxDueDate}
                />
              </div>
              <Button 
                className="w-full" 
                onClick={handleSubmitBorrow}
                disabled={isBorrowing}
              >
                {isBorrowing ? "Submitting..." : "Submit Request"}
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      </div>
    </AppLayout>
  );
};

export default AssetDetailPage;
