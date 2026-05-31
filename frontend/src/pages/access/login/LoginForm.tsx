import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { AuthContext } from "@/contexts/AuthContext";
import { useLogin } from "@/hooks/tanstack-query/auth/useLogin";
import { useToast } from "@/hooks/use-toast";
import { LoginFormData, LoginResponse } from "@/types/login.type";
import { loginSchema } from "@/types/validators/login.schema";
import { zodResolver } from "@hookform/resolvers/zod";
import axios from "axios";
import { Eye, EyeOff, Loader2 } from "lucide-react";
import { useContext, useState } from "react";
import { useForm } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";

export function LoginForm() {
  const [showPassword, setShowPassword] = useState(false);
  const { login, isPending } = useLogin();
  const { login: storeUser } = useContext(AuthContext);

  const navigate = useNavigate();
  const { toast } = useToast();

  const form = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: "",
      senha: "",
    },
  });

  const onSubmit = (data: LoginFormData) => {
    login(data, {
      onSuccess: (response) => {
        storeUser(response.user);

        toast({
          title: "Login realizado com sucesso!",
          description: "Você será redirecionado para a página principal.",
        });
        navigate("/");
      },
      onError: (error) => {
        console.log("Erro:", error);

        const defaultMessage = "E-mail ou senha inválidos.";
        const errorMessage = axios.isAxiosError(error)
          ? error.response?.data?.message || defaultMessage
          : "Erro desconhecido";

        toast({
          title: "Erro ao realizar login",
          description: errorMessage,
          variant: "destructive",
        });
      },
    });
  };

  return (
    <Card className="shadow-soft border-border/50">
      <CardHeader className="text-center space-y-2">
        <CardTitle className="text-2xl font-bold">Bem-vindo de volta!</CardTitle>
        <CardDescription>Acesse sua conta para continuar transformando vidas</CardDescription>
      </CardHeader>

      <CardContent>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="email"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>E-mail</FormLabel>
                  <FormControl>
                    <Input
                      type="email"
                      placeholder="seu@email.com"
                      {...field}
                      className="focus:ring-primary focus:border-primary"
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="senha"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Senha</FormLabel>
                  <FormControl>
                    <div className="relative">
                      <Input
                        type={showPassword ? "text" : "password"}
                        placeholder="••••••••"
                        {...field}
                        className="pr-10 focus:ring-primary focus:border-primary"
                      />
                      <button
                        type="button"
                        onClick={() => setShowPassword(!showPassword)}
                        className="absolute right-3 top-1/2 transform -translate-y-1/2 text-muted-foreground hover:text-primary"
                      >
                        {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                      </button>
                    </div>
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <div className="flex items-center justify-between">
              <Link to="/recuperar-senha" className="text-sm text-primary hover:underline">
                Esqueci minha senha
              </Link>
            </div>

            <Button
              type="submit"
              disabled={isPending}
              className="w-full bg-gradient-hero hover:opacity-90 shadow-button"
            >
              {isPending ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : "Entrar"}
            </Button>
          </form>
        </Form>

        <div className="mt-6 text-center">
          <p className="text-sm text-muted-foreground">
            Ainda não tem conta?{" "}
            <Link to="/cadastro" className="text-primary hover:underline font-medium">
              Cadastre-se agora
            </Link>
          </p>
        </div>
      </CardContent>
    </Card>
  );
}
