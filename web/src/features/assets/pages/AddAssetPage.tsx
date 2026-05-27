import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { AppLayout } from "@/shared/components/AppLayout";
import { Button } from "@/shared/ui/button";
import { Input } from "@/shared/ui/input";
import { Label } from "@/shared/ui/label";
import { Textarea } from "@/shared/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/shared/ui/select";
import { useToast } from "@/shared/hooks/use-toast";
import { Upload } from "lucide-react";
import { assetApi, categoryApi, getToken } from "@/lib/api";
import { AssetStatus } from "@/types";

const AddAssetPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { toast } = useToast();
  const isEdit = useMemo(() => Boolean(id), [id]);
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [categories, setCategories] = useState<string[]>([]);
  useEffect(() => {
    let active = true;
    const loadCategories = async () => {
      try {
        const token = getToken();
        const data = await categoryApi.getAll(token || undefined);
        if (active && Array.isArray(data)) {
          setCategories(data);
        }
      } catch (error) {
        if (active) {
          setCategories([]);
        }
      }
    };

    loadCategories();
    return () => {
      active = false;
    };
  }, []);

  const [imageFile, setImageFile] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string>("");
  const [form, setForm] = useState({
    name: "",
    serialNumber: "",
    category: "",
    status: "available" as AssetStatus,
    description: "",
    imageUrl: "",
  });

  const update = (field: string, value: string) =>
    setForm((f) => ({ ...f, [field]: value }));

  useEffect(() => {
    if (!id) {
      return;
    }

    let active = true;
    const loadAsset = async () => {
      setIsLoading(true);
      try {
        const token = getToken();
        const asset = await assetApi.getById(id, token || undefined);
        if (active) {
          setForm({
            name: asset.name,
            serialNumber: asset.serialNumber,
            category: asset.category,
            status: asset.status,
            description: asset.description,
            imageUrl: "",
          });
          setImagePreview(asset.image || "");
        }
      } catch (error) {
        if (active) {
          toast({
            title: "Asset not found",
            description: "Please select another asset.",
            variant: "destructive",
          });
          navigate("/dashboard");
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
  }, [id, navigate, toast]);

  const handleFileChange = (file: File | null) => {
    if (!file) {
      setImageFile(null);
      return;
    }

    if (!/[.]png$|[.]jpg$|[.]jpeg$/i.test(file.name)) {
      toast({
        title: "Invalid file type",
        description: "Only PNG or JPG images are allowed.",
        variant: "destructive",
      });
      return;
    }

    if (file.size > 1024 * 1024) {
      toast({
        title: "File too large",
        description: "Please upload an image up to 1MB.",
        variant: "destructive",
      });
      return;
    }

    setImageFile(file);
    setImagePreview(URL.createObjectURL(file));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.category) {
      toast({
        title: "Category required",
        description: "Please select a category before saving.",
        variant: "destructive",
      });
      return;
    }
    setIsSaving(true);
    try {
      const token = getToken();
      let imageUrl = form.imageUrl;
      if (imageFile) {
        setIsUploading(true);
        imageUrl = await assetApi.uploadImage(imageFile, token || undefined);
      }

      if (isEdit && id) {
        await assetApi.update(
          id,
          {
            name: form.name,
            serialNumber: form.serialNumber,
            category: form.category,
            status: form.status,
            description: form.description,
            imageUrl: imageUrl || undefined,
          },
          token || undefined
        );
        toast({ title: "Asset updated!", description: `${form.name} has been updated.` });
      } else {
        await assetApi.create(
          {
            name: form.name,
            serialNumber: form.serialNumber,
            category: form.category,
            status: form.status,
            description: form.description,
            imageUrl: imageUrl || undefined,
          },
          token || undefined
        );
        toast({ title: "Asset created!", description: `${form.name} has been added to the inventory.` });
      }
      navigate("/dashboard");
    } catch (error) {
      toast({
        title: "Save failed",
        description: "Please check the fields and try again.",
        variant: "destructive",
      });
    } finally {
      setIsUploading(false);
      setIsSaving(false);
    }
  };

  return (
    <AppLayout>
      <div className="p-6 lg:p-8 max-w-2xl mx-auto space-y-6 animate-fade-in">
        <div>
          <h1 className="text-2xl font-bold">{isEdit ? "Edit Asset" : "Add New Asset"}</h1>
          <p className="text-muted-foreground text-sm mt-1">
            {isEdit ? "Update asset details." : "Add a new item to the organization's inventory."}
          </p>
        </div>

        <form onSubmit={handleSubmit} className="bg-card rounded-xl border p-6 space-y-5">
          <div className="space-y-2">
            <Label>Asset Name</Label>
            <Input placeholder="e.g., Digital Oscilloscope" value={form.name} onChange={(e) => update("name", e.target.value)} required disabled={isLoading} />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label>Serial Number</Label>
              <Input placeholder="e.g., OSC-2024-001" className="font-mono" value={form.serialNumber} onChange={(e) => update("serialNumber", e.target.value)} required disabled={isLoading} />
            </div>
            <div className="space-y-2">
              <Label>Category</Label>
              <Select value={form.category} onValueChange={(v) => update("category", v)} disabled={isLoading}>
                <SelectTrigger><SelectValue placeholder="Select category" /></SelectTrigger>
                <SelectContent>
                  {categories.length === 0 ? (
                    <SelectItem value="__none__" disabled>No categories available</SelectItem>
                  ) : (
                    categories.map((c) => <SelectItem key={c} value={c}>{c}</SelectItem>)
                  )}
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="space-y-2">
            <Label>Status</Label>
            <Select value={form.status} onValueChange={(v) => update("status", v)} disabled={isLoading}>
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
            <Textarea placeholder="Brief description of the asset..." value={form.description} onChange={(e) => update("description", e.target.value)} rows={3} disabled={isLoading} />
          </div>

          <div className="space-y-2">
            <Label>Upload Image (PNG/JPG, max 1MB)</Label>
            <div className="border-2 border-dashed rounded-xl p-8 text-center text-muted-foreground hover:border-primary/50 transition-colors">
              <Upload className="h-8 w-8 mx-auto mb-2 opacity-50" />
              <p className="text-sm">Click to upload</p>
              <p className="text-xs mt-1">PNG or JPG up to 1MB</p>
              <Input
                type="file"
                accept="image/png,image/jpeg"
                className="mt-4"
                disabled={isLoading || isUploading}
                onChange={(e) => handleFileChange(e.target.files?.[0] || null)}
              />
            </div>
            {imagePreview && (
              <img src={imagePreview} alt="Asset preview" className="h-32 w-48 object-cover rounded-lg border" />
            )}
          </div>

          <div className="flex gap-3 pt-2">
            <Button type="submit" className="flex-1" disabled={isSaving || isLoading || isUploading}>
              {isSaving || isUploading ? "Saving..." : isEdit ? "Save Changes" : "Save Asset"}
            </Button>
            <Button type="button" variant="outline" onClick={() => navigate("/dashboard")} disabled={isSaving}>Cancel</Button>
          </div>
        </form>
      </div>
    </AppLayout>
  );
};

export default AddAssetPage;
