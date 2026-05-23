import { Asset } from "@/types";
import { Card, CardContent } from "@/shared/ui/card";
import { StatusBadge } from "@/shared/components/StatusBadge";
import { useNavigate } from "react-router-dom";

export const AssetCard = ({ asset }: { asset: Asset }) => {
  const navigate = useNavigate();

  return (
    <Card
      className="group cursor-pointer overflow-hidden transition-all hover:shadow-md hover:-translate-y-0.5 animate-fade-in"
      onClick={() => navigate(`/assets/${asset.id}`)}
    >
      <div className="aspect-[4/3] overflow-hidden bg-muted">
        <img
          src={asset.image}
          alt={asset.name}
          className="h-full w-full object-cover transition-transform group-hover:scale-105"
          loading="lazy"
        />
      </div>
      <CardContent className="p-4 space-y-2">
        <div className="flex items-start justify-between gap-2">
          <h3 className="font-semibold text-sm leading-tight line-clamp-2">{asset.name}</h3>
          <StatusBadge status={asset.status} />
        </div>
        <p className="text-xs text-muted-foreground font-mono">{asset.serialNumber}</p>
        <p className="text-xs text-muted-foreground">{asset.category}</p>
      </CardContent>
    </Card>
  );
};
