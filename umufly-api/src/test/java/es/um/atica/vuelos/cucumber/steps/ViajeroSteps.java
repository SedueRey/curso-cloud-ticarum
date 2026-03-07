package es.um.atica.vuelos.cucumber.steps;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import es.um.atica.umufly.vuelos.adaptors.api.rest.v2.dto.ClaseAsientoReserva;
import es.um.atica.umufly.vuelos.adaptors.api.rest.v2.dto.DocumentoIdentidadDTO;
import es.um.atica.umufly.vuelos.adaptors.api.rest.v2.dto.PasajeroDTO;
import es.um.atica.umufly.vuelos.adaptors.api.rest.v2.dto.ReservaVueloDTO;
import es.um.atica.umufly.vuelos.adaptors.api.rest.v2.dto.TipoDocumento;
import es.um.atica.umufly.vuelos.adaptors.api.rest.v2.dto.VueloDTO;
import es.um.atica.umufly.vuelos.domain.model.EstadoVuelo;
import es.um.atica.vuelos.cucumber.VuelosWrapper;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;

public class ViajeroSteps {

	@LocalServerPort
	private int port;

	private WebTestClient webTestClient;
	private WebTestClient.ResponseSpec response;

	private String usuario;

	private List<VueloDTO> vuelos;

	private ReservaVueloDTO reserva;

	@Dado("un viajero con NIF {string}")
	public void cargo_datos_usuario(String nif) {
		this.usuario = nif;
	}

	@Cuando("lista de vuelos con página {int} y tamaño {int}")
	public void listo_vuelos_disponibles(int page, int size) {
		// Creo cliente
		this.webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
		// Hago llamada
		response = webTestClient.get()
				.uri(uriBuilder -> uriBuilder.path("/private/v2.0/vuelos").queryParam("page", page)
						.queryParam("size", size).build())
				.header("UMU-Usuario", "NIF:" + usuario).accept(MediaType.APPLICATION_JSON).exchange();
		// Guardo los vuelos en una variable para usarla en otros casos
		VuelosWrapper vuelosLista = response.expectBody(VuelosWrapper.class).returnResult().getResponseBody();
		vuelos = vuelosLista.getVuelos();
	}

	@Entonces("la respuesta debe tener status {int}")
	public void verificar_status(int estadoEsperado) {
		response.expectStatus().isEqualTo(estadoEsperado);
	}

	@Y("devolver una lista de vuelos rellena o vacia")
	public void verifica_lista_vuelos() {
		assertNotNull(vuelos);
	}

	@Y("reserva el primero libre")
	public void reserva_el_primero_libre() {
		ReservaVueloDTO nuevaReserva = new ReservaVueloDTO();
		PasajeroDTO nuevoPasajero = new PasajeroDTO();
		DocumentoIdentidadDTO documento = new DocumentoIdentidadDTO();
		documento.setNumero(usuario);
		documento.setTipo(TipoDocumento.NIF);
		nuevoPasajero.setNombre("Prueba");
		nuevoPasajero.setDocumentoIdentidad(documento);
		nuevoPasajero.setNacionalidad("Espaniola");
		nuevoPasajero.setPrimerApellido("ASD");
		nuevoPasajero.setSegundoApellido("FGH");
		nuevoPasajero.setCorreoElectronico("asd@asd.com");
		// setter con los campos que tenga ReservaVueloDTO
		VueloDTO vueloLibre = null;
		for (VueloDTO vuelo : vuelos) {
			// Por simplicidad vamos a obviar la comprobacion de la capacidad
			if (EstadoVuelo.PENDIENTE.name().equals(vuelo.getEstado().name())) {
				vueloLibre = vuelo;
				break;
			}
		}

		nuevaReserva.setFechaReserva(LocalDateTime.now());
		nuevaReserva.setVuelo(vueloLibre);
		nuevaReserva.setClaseAsiento(ClaseAsientoReserva.BUSINESS);
		nuevaReserva.setDocumentoIdentidadTitular(documento);
		nuevaReserva.setPasajero(nuevoPasajero);

		response = webTestClient.post().uri("/private/v2.0/reservas-vuelo") // ajusta la URL con tus Constants
				.header("UMU-Usuario", "NIF:" + usuario).contentType(MediaType.APPLICATION_JSON).bodyValue(nuevaReserva)
				.exchange();
		reserva = response.expectBody(ReservaVueloDTO.class).returnResult().getResponseBody();
	}

	@Entonces("la reserva se realiza")
	public void reserva_realizada() {
		assertNotNull(reserva);
		assertNotNull(reserva.getId());
	}

}
