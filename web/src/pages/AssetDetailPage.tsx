import { useParams, useNavigate } from "react-router-dom";
import { MOCK_ASSETS } from "@/data/mock";
import { useAuth } from "@/contexts/AuthContext";
import { AppLayout } from "@/components/AppLayout";
import { StatusBadge } from "@/components/StatusBadge";
import { Button } from "@/components/ui/button";
import { ArrowLeft, Edit, Trash2, Calendar, Tag, Hash } from "lucide-react";
import { useToast } from "@/hooks/use-toast";

const AssetDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAdmin, isStudent } = useAuth();
  const { toast } = useToast();
  const asset = MOCK_ASSETS.find((a) => a.id === id);

  if (!asset) {
    return (
      <AppLayout>
        <div className="flex items-center justify-center h-full">
          <p className="text-muted-foreground">Asset not found.</p>
        </div>
      </AppLayout>
    );
  }

  const handleBorrow = () => {
    toast({ title: "Request submitted!", description: `Your borrow request for ${asset.name} has been sent.` });
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
                <Button className="flex-1" onClick={handleBorrow}>
                  Submit Borrow Request
                </Button>
              )}
              {isAdmin && (
                <>
                  <Button variant="outline" className="gap-2" onClick={() => navigate(`/assets/edit/${asset.id}`)}>
                    <Edit className="h-4 w-4" /> Edit
                  </Button>
                  <Button variant="outline" className="gap-2 text-destructive hover:text-destructive" onClick={() => {
                    toast({ title: "Asset deleted", variant: "destructive" });
                    navigate("/dashboard");
                  }}>
                    <Trash2 className="h-4 w-4" /> Delete
                  </Button>
                </>
              )}
            </div>
          </div>
        </div>
      </div>
    </AppLayout>
  );
};

export default AssetDetailPage;
