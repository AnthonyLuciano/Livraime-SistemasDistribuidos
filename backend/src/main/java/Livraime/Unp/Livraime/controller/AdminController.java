package Livraime.Unp.Livraime.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Livraime.Unp.Livraime.controller.dto.request.ParceiroEditRequest;
import Livraime.Unp.Livraime.controller.dto.request.UsuarioEditRequest;

/**
 * Controller responsável pela administração do sistema.
 * Gerencia administradores e fornece métricas importantes do sistema
 * como número de assinaturas, livros doados e parceiros nos últimos 6 meses.
 * -Anthony
 */

import Livraime.Unp.Livraime.controller.dto.response.MetricDto;
import Livraime.Unp.Livraime.modelo.Admin;
import Livraime.Unp.Livraime.modelo.Parceiro;
import Livraime.Unp.Livraime.modelo.Usuario;
import Livraime.Unp.Livraime.repositorio.DonationRepository;
import Livraime.Unp.Livraime.repositorio.PartnerRepository;
import Livraime.Unp.Livraime.repositorio.SubscriptionRepository;
import Livraime.Unp.Livraime.repositorio.UsuarioRepository;
import Livraime.Unp.Livraime.servico.SupabaseSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admins")
@Tag(name = "Admins", description = "Gerenciamento de administradores")
@Repository
public class AdminController {

    private final SubscriptionRepository subscriptionRepository;
    private final DonationRepository donationRepository;
    private final PartnerRepository partnerRepository;
    private final UsuarioRepository usuarioRepository;
    private final SupabaseSyncService supabaseSyncService;

    public AdminController(SubscriptionRepository subscriptionRepository,
            DonationRepository donationRepository,
            PartnerRepository partnerRepository,
            UsuarioRepository usuarioRepository,
            SupabaseSyncService supabaseSyncService) {
        this.subscriptionRepository = subscriptionRepository;
        this.donationRepository = donationRepository;
        this.partnerRepository = partnerRepository;
        this.usuarioRepository = usuarioRepository;
        this.supabaseSyncService = supabaseSyncService;
    }

    private List<Admin> admins = new ArrayList<>();

    /**
     * Lista todos os administradores cadastrados no sistema.
     * -Anthony
     */
    @GetMapping
    @Operation(summary = "Listar todos os administradores")
    public List<Admin> listarAdmins() {
        return admins;
    }

    /**
     * Cadastra um novo administrador no sistema.
     * 
     * @param novoAdmin Dados do novo administrador
     *                  -Anthony
     */
    @PostMapping
    @Operation(summary = "Criar novo administrador")
    public Admin criarAdmin(@RequestBody Admin novoAdmin) {
        admins.add(novoAdmin);
        return novoAdmin;
    }

    /**
     * Busca um administrador específico pelo seu ID.
     * 
     * @param id ID do administrador a ser buscado
     *           -Anthony
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar administrador por ID")
    public Admin buscarPorId(@PathVariable int id) {
        return admins.stream().filter(a -> a.getId() == id).findFirst().orElse(null);
    }

    /**
     * Retorna métricas dos últimos 6 meses incluindo:
     * - Número de assinaturas
     * - Quantidade de livros doados
     * - Número de parceiros
     * -Anthony
     */
    @GetMapping("/metrics")
    @Operation(summary = "Métricas dos últimos 6 meses")
    public List<MetricDto> buscarMetricasUltimos6Meses() {
        List<MetricDto> resultado = new ArrayList<>();
        LocalDate hoje = LocalDate.now();

    // Alteração: incluir do mês atual até 5 meses atrás
    for (int i = 0; i < 6; i++) {
            LocalDate dataDoMes = hoje.minusMonths(i);
            LocalDate inicioDoMes = dataDoMes.withDayOfMonth(1);
            LocalDate fimDoMes = dataDoMes.withDayOfMonth(dataDoMes.lengthOfMonth());

            // Nome do mês em Português (ex: "mai", "jun")
            String mes = dataDoMes.getMonth().getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("pt-BR"));

            LocalDateTime inicio = inicioDoMes.atStartOfDay();
            LocalDateTime fim = fimDoMes.atTime(LocalTime.MAX);

            long subscriptions = countSubscriptionsBetween(inicio, fim);
            long donatedBooks = countDonatedBooksBetween(inicio, fim);
            long partners = countPartnersAtMonthEnd(fim);

        resultado.add(0, new MetricDto(mes, subscriptions, donatedBooks, partners)); // Inserir no início
        }

        return resultado;
    }

    // Métodos que chamam repositórios — adapte aos nomes/colunas das suas entidades
    private long countSubscriptionsBetween(LocalDateTime startInclusive, LocalDateTime endInclusive) {
        return subscriptionRepository.countByCreatedAtBetween(startInclusive, endInclusive);
    }

    private long countDonatedBooksBetween(LocalDateTime startInclusive, LocalDateTime endInclusive) {
        // Caso você precise somar quantidade de livros doados, use o método do
        // DonationRepository que retorna SUM
        Long sum = donationRepository.sumBooksDonatedBetween(startInclusive, endInclusive);
        return sum == null ? 0L : sum;
    }

    private long countPartnersAtMonthEnd(LocalDateTime monthEndDateTime) {
        // Conta parceiros ativos até a data (ex.: createdAt <= monthEndDateTime AND
        // active = true)
        return partnerRepository.countActiveUntil(monthEndDateTime);
    }

    /**
     * Edita campos básicos de um usuário. Campos editáveis:
     * - name -> nome
     * - email -> email
     * - address -> endereco
     * - phone -> telefone
     * Essa rota será usada tanto pelo Painel ADM quanto pela Área do Assinante.
     * 
     * @param id  id do usuário a ser editado
     * @param req payload contendo somente os campos editáveis
     *            -Anthony
     */
    @PatchMapping("/users/{id}")
    @Operation(summary = "Editar usuário (ADM / assinante)")
    public ResponseEntity<Usuario> editarUsuario(@PathVariable int id, @RequestBody UsuarioEditRequest req) {
        return usuarioRepository.findById(id)
                .map(u -> {
                    if (req.name() != null)
                        u.setNome(req.name());
                    if (req.email() != null)
                        u.setEmail(req.email());
                    if (req.address() != null)
                        u.setEndereco(req.address());
                    if (req.phone() != null)
                        u.setTelefone(req.phone());
                    usuarioRepository.save(u);
                    return ResponseEntity.ok(u);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Desabilita um usuário (marca como inativo).
     * Recebe o id do usuário via path variable.
     * 
     * @param id id do usuário a ser desabilitado
     *           -Anthony
     */
    @PatchMapping("/users/{id}/disable")
    @Operation(summary = "Desabilitar usuário")
    public ResponseEntity<?> desabilitarUsuario(@PathVariable int id) {
        return usuarioRepository.findById(id)
                .map(u -> {
                    u.setAtivo(false);
                    usuarioRepository.save(u);
                    return ResponseEntity.ok("Usuário desabilitado com sucesso.");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Reativa um usuário (marca como ativo).
     * Recebe o id do usuário via path variable.
     *
     * @param id id do usuário a ser reativado
     */
    @PatchMapping("/users/{id}/enable")
    @Operation(summary = "Reativar usuário")
    public ResponseEntity<?> reativarUsuario(@PathVariable int id) {
        return usuarioRepository.findById(id)
                .map(u -> {
                    u.setAtivo(true);
                    usuarioRepository.save(u);
                    return ResponseEntity.ok("Usuário reativado com sucesso.");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sync")
    @Operation(summary = "Sincronizar banco de dados com Supabase")
    public ResponseEntity<String> sincronizarBancoDeDados() {
        supabaseSyncService.syncAll();
        return ResponseEntity.ok("Sincronização iniciada.");
    }

    /**
     * Edita campos básicos de um parceiro. Campos editáveis:
     * - nome
     * - tipo (sebo ou autor_independente)
     * - endereco
     * - telefone
     * - email
     * - descricaoServicos
     * 
     * @param id  id do parceiro a ser editado
     * @param req payload contendo os campos editáveis
     *            -Anthony
     */
    @PatchMapping("/partners/{id}")
    @Operation(summary = "Editar parceiro")
    public ResponseEntity<Parceiro> editarParceiro(@PathVariable Long id, @RequestBody ParceiroEditRequest req) {
        return partnerRepository.findById(id)
                .map(p -> {
                    if (req.nome() != null)
                        p.setNome(req.nome());
                    if (req.tipo() != null)
                        p.setTipo(req.tipo());
                    if (req.endereco() != null)
                        p.setEndereco(req.endereco());
                    if (req.telefone() != null)
                        p.setTelefone(req.telefone());
                    if (req.email() != null)
                        p.setEmail(req.email());
                    if (req.descricaoServicos() != null)
                        p.setDescricaoServicos(req.descricaoServicos());
                    partnerRepository.save(p);
                    return ResponseEntity.ok(p);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Desativa um parceiro do sistema.
     * Marca como inativo e registra a data de deleção.
     * 
     * @param id id do parceiro a ser desativado
     *           -Anthony
     */
    @PatchMapping("/partners/{id}/disable")
    @Operation(summary = "Desativar parceiro")
    public ResponseEntity<?> desativarParceiro(@PathVariable Long id) {
        return partnerRepository.findById(id)
                .map(p -> {
                    p.setActive(false);
                    p.setDeletedAt(LocalDateTime.now());
                    partnerRepository.save(p);
                    return ResponseEntity.ok("Parceiro desativado com sucesso.");
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @PatchMapping("/partners/{id}/enable")
    @Operation(summary = "Ativar parceiro")
    public ResponseEntity<?> ativarParceiro(@PathVariable Long id){
        return partnerRepository.findById(id)
                        .map(p -> {
                            p.setActive(true);
                            p.setCreatedAt(LocalDateTime.now());
                            partnerRepository.save(p);
                            return ResponseEntity.ok("Parceiro ativado com sucesso.");
                        })
                        .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
