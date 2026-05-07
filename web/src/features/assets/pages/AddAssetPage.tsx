import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { AppLayout } from "@/components/AppLayout";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { CATEGORIES } from "@/data/mock";
import { useToast } from "@/hooks/use-toast";
import { Upload } from "lucide-react";

const AddAssetPage = () => {
  const navigate = useNavigate();
  const { toast } = useToast();
  const [form, setForm] = useState({
    name: "",
    serialNumber: "",
    category: "",
    status: "available",
    description: "",
  });

  const update = (field: string, value: string) =>
    setForm((f) => ({ ...f, [field]: value }));

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    toast({ title: "Asset created!", description: `${form.name} has been added to the inventory.` });
    navigate("/dashboard");
  };

  return (
    <AppLayout>
      <div className="p-6 lg:p-8 max-w-2xl mx-auto space-y-6 animate-fade-in">
        <div>
          <h1 className="text-2xl font-bold">Add New Asset</h1>
          <p className="text-muted-foreground text-sm mt-1">Add a new item to the organization's inventory.</p>
        </div>

        <form onSubmit={handleSubmit} className="bg-card rounded-xl border p-6 space-y-5">
          <div className="space-y-2">
            <Label>Asset Name</Label>
            <Input placeholder="e.g., Digital Oscilloscope" value={form.name} onChange={(e) => update("name", e.target.value)} required />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label>Serial Number</Label>
              <Input placeholder="e.g., OSC-2024-001" className="font-mono" value={form.serialNumber} onChange={(e) => update("serialNumber", e.target.value)} required />
            </div>
            <div className="space-y-2">
              <Label>Category</Label>
              <Select value={form.category} onValueChange={(v) => update("category", v)}>
                <SelectTrigger><SelectValue placeholder="Select category" /></SelectTrigger>
                <SelectContent>
                  {CATEGORIES.map((c) => <SelectItem key={c} value={c}>{c}</SelectItem>)}
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="space-y-2">
            <Label>Status</Label>
            <Select value={form.status} onValueChange={(v) => update("status", v)}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value="available">Available</SelectItem>
                <SelectItem value="borrowed">Borrowed</SelectItem>
                <SelectItem value="maintenance">Under Maintenance</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label>Description</Label>
            <Textarea placeholder="Brief description of the asset..." value={form.description} onChange={(e) => update("description", e.target.value)} rows={3} />
          </div>

          <div className="space-y-2">
            <Label>Upload File (Image / PDF)</Label>
            <div className="border-2 border-dashed rounded-xl p-8 text-center text-muted-foreground hover:border-primary/50 transition-colors cursor-pointer">
              <Upload className="h-8 w-8 mx-auto mb-2 opacity-50" />
              <p className="text-sm">Click to upload or drag and drop</p>
              <p className="text-xs mt-1">PNG, JPG, PDF up to 10MB</p>
            </div>
          </div>

          <div className="flex gap-3 pt-2">
            <Button type="submit" className="flex-1">Save Asset</Button>
            <Button type="button" variant="outline" onClick={() => navigate("/dashboard")}>Cancel</Button>
          </div>
        </form>
      </div>
    </AppLayout>
  );
};

export default AddAssetPage;
