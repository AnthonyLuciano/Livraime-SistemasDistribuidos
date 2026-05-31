package Livraime.Unp.Livraime.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import Livraime.Unp.Livraime.controller.dto.request.ParceiroEditRequest;
import Livraime.Unp.Livraime.controller.dto.request.UsuarioEditRequest;
import Livraime.Unp.Livraime.controller.dto.response.MetricDto;
import Livraime.Unp.Livraime.modelo.Admin;
import Livraime.Unp.Livraime.modelo.Endereco;
import Livraime.Unp.Livraime.modelo.Parceiro;
import Livraime.Unp.Livraime.modelo.Phone;
import Livraime.Unp.Livraime.modelo.Usuario;
import Livraime.Unp.Livraime.repositorio.DonationRepository;
import Livraime.Unp.Livraime.repositorio.PartnerRepository;
import Livraime.Unp.Livraime.repositorio.SubscriptionRepository;
import Livraime.Unp.Livraime.repositorio.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private DonationRepository donationRepository;

    @Mock
    private PartnerRepository partnerRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    private AdminController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminController(subscriptionRepository, donationRepository, partnerRepository,
                usuarioRepository, null);
    }

    @Test
    void createAndListAdmins_shouldReturnCreatedAdmin() {
        Admin admin = new Admin(1, "admin", "a@a.com", "pwd", "root");
        Admin created = controller.criarAdmin(admin);
        assertNotNull(created);
        List<Admin> admins = controller.listarAdmins();
        assertEquals(1, admins.size());
        assertEquals("admin", admins.get(0).getNome());
    }

    @Test
    void buscarPorId_whenExists_shouldReturnAdmin() {
        Admin admin = new Admin(42, "adm42", "adm@x.com", "pwd", "ops");
        controller.criarAdmin(admin);
        Admin found = controller.buscarPorId(42);
        assertNotNull(found);
        assertEquals(42, found.getId());
    }

    @Test
    void buscarMetricasUltimos6Meses_shouldReturnSixItemsWithRepoValues() {
        when(subscriptionRepository.countByCreatedAtBetween(ArgumentMatchers.any(LocalDateTime.class),
                ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(10L);
        when(donationRepository.sumBooksDonatedBetween(ArgumentMatchers.any(LocalDateTime.class),
                ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(20L);
        when(partnerRepository.countActiveUntil(ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(3L);

        List<MetricDto> metrics = controller.buscarMetricasUltimos6Meses();
        assertNotNull(metrics);
        assertEquals(6, metrics.size());

        for (MetricDto m : metrics) {
            assertEquals(10L, m.getInscricoes());
            assertEquals(20L, m.getLivrosDoados());
            assertEquals(3L, m.getParceiros());
            assertNotNull(m.getMes());
        }

        verify(subscriptionRepository, atLeastOnce()).countByCreatedAtBetween(any(LocalDateTime.class),
                any(LocalDateTime.class));
        verify(donationRepository, atLeastOnce()).sumBooksDonatedBetween(any(LocalDateTime.class),
                any(LocalDateTime.class));
        verify(partnerRepository, atLeastOnce()).countActiveUntil(any(LocalDateTime.class));
    }

    @Test
    void editarUsuario_whenExists_shouldUpdateFields() {
        Usuario u = new Usuario("oldName", "old@email", "cpf", "pwd", new Endereco(), new Phone(),
                "cod",
                false);
        // 1, "oldName", "old@mail", "cpf", "pwd", new Endereco(), "0000", Plano.BASICO,
        // true, null, false
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(u));

        var expectedAddress = new Endereco("Rua padilha", "15", null, "UNP Salgadinho", "Natal", "RN", null);
        var expectedPhone = new Phone("84", "999999999");

        UsuarioEditRequest req = new UsuarioEditRequest("newName", "new@mail", expectedAddress, expectedPhone);

        var resp = controller.editarUsuario(1, req);
        assertTrue(resp.getStatusCode().is2xxSuccessful());
        Usuario body = resp.getBody();
        assertNotNull(body);
        assertEquals("newName", body.getNome());
        assertEquals("new@mail", body.getEmail());
        assertEquals(expectedAddress, body.getEndereco());
        assertEquals(expectedPhone, body.getTelefone());
        verify(usuarioRepository).save(body);
    }

    @Test
    void editarUsuario_whenNotFound_shouldReturn404() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());
        UsuarioEditRequest req = new UsuarioEditRequest("n", "n", new Endereco(), new Phone());
        var resp = controller.editarUsuario(99, req);
        assertEquals(404, resp.getStatusCode().value());
    }

    @Test
    void desabilitarUsuario_whenExists_shouldSetAtivoFalse() {
        Usuario u = new Usuario("nome", "emial@guemei.com", "cepefi", "senia", new Endereco(), new Phone(), "cod",
                true);
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(u));

        var resp = controller.desabilitarUsuario(2);
        assertTrue(resp.getStatusCode().is2xxSuccessful());
        assertEquals("Usuário desabilitado com sucesso.", resp.getBody());
        assertFalse(u.isAtivo());
        verify(usuarioRepository).save(u);
    }

    @Test
    void desabilitarUsuario_whenNotFound_shouldReturn404() {
        when(usuarioRepository.findById(123)).thenReturn(Optional.empty());
        var resp = controller.desabilitarUsuario(123);
        assertEquals(404, resp.getStatusCode().value());
    }

    @Test
    void editarParceiro_whenExists_shouldUpdateFields() {
        Parceiro p = new Parceiro(1L, "oldName", "sebo", "oldAddr", "0000", "old@mail", "desc", true);
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(p));

        ParceiroEditRequest req = new ParceiroEditRequest(
                "newName", "autor_independente", "newAddr", "9999", "new@mail", "nova desc");

        var resp = controller.editarParceiro(1L, req);
        assertTrue(resp.getStatusCode().is2xxSuccessful());

        Parceiro body = resp.getBody();
        assertNotNull(body);
        assertEquals("newName", body.getNome());
        assertEquals("autor_independente", body.getTipo());
        assertEquals("newAddr", body.getEndereco());
        assertEquals("9999", body.getTelefone());
        assertEquals("new@mail", body.getEmail());
        assertEquals("nova desc", body.getDescricaoServicos());
        verify(partnerRepository).save(body);
    }

    @Test
    void editarParceiro_whenNotFound_shouldReturn404() {
        when(partnerRepository.findById(99L)).thenReturn(Optional.empty());
        ParceiroEditRequest req = new ParceiroEditRequest("n", "n", "n", "n", "n", "n");
        var resp = controller.editarParceiro(99L, req);
        assertEquals(404, resp.getStatusCode().value());
    }

    @Test
    void desativarParceiro_whenExists_shouldSetInactiveAndDeletedAt() {
        Parceiro p = new Parceiro(2L, "x", "sebo", "addr", "999", "x@x", "desc", true);
        when(partnerRepository.findById(2L)).thenReturn(Optional.of(p));

        var resp = controller.desativarParceiro(2L);
        assertTrue(resp.getStatusCode().is2xxSuccessful());
        assertEquals("Parceiro desativado com sucesso.", resp.getBody());
        assertFalse(p.isActive());
        assertNotNull(p.getDeletedAt());
        verify(partnerRepository).save(p);
    }

    @Test
    void desativarParceiro_whenNotFound_shouldReturn404() {
        when(partnerRepository.findById(123L)).thenReturn(Optional.empty());
        var resp = controller.desativarParceiro(123L);
        assertEquals(404, resp.getStatusCode().value());
    }
}