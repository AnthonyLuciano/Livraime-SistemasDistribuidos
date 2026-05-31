import { useState } from "react";
import { RefreshCw, Shield } from "lucide-react";
import { api } from "@/config/api";
import { Button } from "@/components/ui/button";
import { useToast } from "@/hooks/use-toast";

export default function AdminHeader() {
  const [isSyncing, setIsSyncing] = useState(false);
  const { toast } = useToast();

  async function handleSync() {
    try {
      setIsSyncing(true);
      await api.post("/admins/sync");
      toast({
        title: "Sincronização iniciada",
        description: "Os bancos de dados começaram a ser sincronizados.",
      });
    } catch (error: any) {
      toast({
        title: "Erro ao sincronizar",
        description: error?.response?.data || error?.message || "Não foi possível sincronizar os bancos.",
      });
    } finally {
      setIsSyncing(false);
    }
  }

  return (
    <div className="flex flex-col md:flex-row items-start md:items-center justify-between mb-8 gap-4">
      <div>
        <h1 className="text-3xl font-bold text-foreground mb-2">
          Dashboard <span className="text-primary">Administrativo</span>
        </h1>
        <p className="text-muted-foreground">Gerencie usuários, assinaturas e parceiros da plataforma</p>
      </div>

      <div className="flex items-center gap-3">
        <Button
          type="button"
          onClick={handleSync}
          disabled={isSyncing}
          className="bg-gradient-secondary hover:opacity-90 shadow-button"
        >
          <RefreshCw className="h-4 w-4" />
          {isSyncing ? "Sincronizando..." : "Sincronizar DB"}
        </Button>
        <Shield className="h-8 w-8 text-primary" />
      </div>
    </div>
  );
}
