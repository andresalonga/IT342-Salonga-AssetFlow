import { AssetStatus } from "@/types";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

const statusConfig: Record<AssetStatus, { label: string; className: string }> = {
  available: { label: "Available", className: "bg-success/15 text-success border-success/30" },
  borrowed: { label: "Borrowed", className: "bg-warning/15 text-warning border-warning/30" },
  maintenance: { label: "Maintenance", className: "bg-destructive/15 text-destructive border-destructive/30" },
};

export const StatusBadge = ({ status }: { status: AssetStatus }) => {
  const config = statusConfig[status];
  return (
    <Badge variant="outline" className={cn("text-xs font-medium", config.className)}>
      {config.label}
    </Badge>
  );
};
