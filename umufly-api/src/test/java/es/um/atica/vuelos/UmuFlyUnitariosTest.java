package es.um.atica.vuelos;

//Importante el import estatico
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;

import es.um.atica.umufly.vuelos.domain.exception.LimiteReservasPorPasajeroEnVueloSuperadoException;
import es.um.atica.umufly.vuelos.domain.exception.VueloIniciadoException;
import es.um.atica.umufly.vuelos.domain.exception.VueloNoReservableException;
import es.um.atica.umufly.vuelos.domain.exception.VueloSinPlazasException;
import es.um.atica.umufly.vuelos.domain.model.Avion;
import es.um.atica.umufly.vuelos.domain.model.ClaseAsientoReserva;
import es.um.atica.umufly.vuelos.domain.model.CorreoElectronico;
import es.um.atica.umufly.vuelos.domain.model.DocumentoIdentidad;
import es.um.atica.umufly.vuelos.domain.model.EstadoReserva;
import es.um.atica.umufly.vuelos.domain.model.EstadoVuelo;
import es.um.atica.umufly.vuelos.domain.model.Itinerario;
import es.um.atica.umufly.vuelos.domain.model.Nacionalidad;
import es.um.atica.umufly.vuelos.domain.model.NombreCompleto;
import es.um.atica.umufly.vuelos.domain.model.Pasajero;
import es.um.atica.umufly.vuelos.domain.model.ReservaVuelo;
import es.um.atica.umufly.vuelos.domain.model.TipoDocumento;
import es.um.atica.umufly.vuelos.domain.model.TipoVuelo;
import es.um.atica.umufly.vuelos.domain.model.Vuelo;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
public class UmuFlyUnitariosTest {

	// --- Conjunto de datos de prueba fijos
	private static final LocalDateTime SALIDA = LocalDateTime.of(2025, 6, 15, 10, 0);
	private static final LocalDateTime LLEGADA = LocalDateTime.of(2025, 6, 15, 12, 0);
	private static final LocalDateTime ANTES_DE_SALIDA = SALIDA.minusHours(2);
	private static final LocalDateTime DESPUES_DE_SALIDA = SALIDA.plusMinutes(30);

	// --- Variables para el test
	private DocumentoIdentidad titular;
	private Pasajero pasajero;
	private Vuelo vueloPendiente;
	private Vuelo vueloCancelado;
	private Vuelo vueloCompletado;

	// Antes de cada test
	@BeforeEach
	void setUp() {
		titular = new DocumentoIdentidad(TipoDocumento.NIF, "12345678Z");
		pasajero = Pasajero.of(titular, new NombreCompleto("Juan", "García", "López"),
				new CorreoElectronico("juan@ejemplo.com"), new Nacionalidad("Española"));

		Itinerario itinerario = new Itinerario(SALIDA, LLEGADA, "MAD", "BCN");
		Avion avion = new Avion(180);

		vueloPendiente = Vuelo.of(UUID.randomUUID(), itinerario, TipoVuelo.NACIONAL, EstadoVuelo.PENDIENTE, avion);
		vueloCancelado = Vuelo.of(UUID.randomUUID(), itinerario, TipoVuelo.NACIONAL, EstadoVuelo.CANCELADO, avion);
		vueloCompletado = Vuelo.of(UUID.randomUUID(), itinerario, TipoVuelo.NACIONAL, EstadoVuelo.COMPLETADO, avion);
	}

	// ##################  Validamos construcciones con el metodo "of"
	@Nested
	@DisplayName("constructores_of_validos")
	@Order(1)
	class TestOf {

		@Test
		void lanza_excepcion_si_el_id_es_nulo() {
			IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
					() -> ReservaVuelo.of(null, titular, pasajero, vueloPendiente, ClaseAsientoReserva.ECONOMICA,
							ANTES_DE_SALIDA, EstadoReserva.PENDIENTE));
			assertEquals("El id de la reserva no puede ser nulo", ex.getMessage());
		}

		@Test
		@DisplayName("lanza_excepcion_si_el_titular_es_nulo")
		void lanza_excepcion_si_el_titular_es_nulo() {
			IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
					() -> ReservaVuelo.of(UUID.randomUUID(), null, pasajero, vueloPendiente,
							ClaseAsientoReserva.ECONOMICA, ANTES_DE_SALIDA, EstadoReserva.PENDIENTE));
			assertEquals("El titular de la reserva no puede ser nulo", ex.getMessage());
		}

		@Test
		void lanza_excepcion_si_el_pasajero_es_nulo() {

			assertThrows(IllegalArgumentException.class, () -> ReservaVuelo.of(UUID.randomUUID(), titular, null,
					vueloPendiente, ClaseAsientoReserva.ECONOMICA, ANTES_DE_SALIDA, EstadoReserva.PENDIENTE));
		}

		@Test
		void deberia_lanzar_excepcion_si_vuelo_null() {
			assertThrows(IllegalArgumentException.class, () -> ReservaVuelo.of(UUID.randomUUID(), titular, pasajero,
					null, ClaseAsientoReserva.ECONOMICA, ANTES_DE_SALIDA, EstadoReserva.PENDIENTE));
		}

		@Test
		void deberia_lanzar_excepcion_si_clase_null() {
			assertThrows(IllegalArgumentException.class, () -> ReservaVuelo.of(UUID.randomUUID(), titular, pasajero,
					vueloPendiente, null, ANTES_DE_SALIDA, EstadoReserva.PENDIENTE));
		}

		@Test
		void lanza_excepcion_si_el_estado_es_nulo() {
			assertThrows(IllegalArgumentException.class, () -> ReservaVuelo.of(UUID.randomUUID(), titular, pasajero,
					vueloPendiente, ClaseAsientoReserva.ECONOMICA, ANTES_DE_SALIDA, null));
		}
	}

	// ##################  Validamos solicitar reserva

	@Nested
	@DisplayName("solicitarReserva_correcta")
	@Order(2)
	class TestSolicitarReserva {

		//Testeamos caso normal
		@Nested
		@DisplayName("Cuando la solicitud es válida")
		class CuandoValida {

			@Test
			void crear_reserva_debe_quedar_pendiente() {
				ReservaVuelo reserva = ReservaVuelo.solicitarReserva(titular, pasajero, vueloPendiente,
						ClaseAsientoReserva.ECONOMICA, ANTES_DE_SALIDA, 0, 10);
				assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
			}

			@Test
			void reseva_debe_tener_id_unico() {
				ReservaVuelo r1 = ReservaVuelo.solicitarReserva(titular, pasajero, vueloPendiente,
						ClaseAsientoReserva.ECONOMICA, ANTES_DE_SALIDA, 0, 10);
				ReservaVuelo r2 = ReservaVuelo.solicitarReserva(titular, pasajero, vueloPendiente,
						ClaseAsientoReserva.BUSINESS, ANTES_DE_SALIDA, 0, 10);

				assertNotEquals(r1.getId(), r2.getId());
			}

			@Test
			void al_crear_reserva_los_datos_deben_guardarse_bien() {
				ReservaVuelo reserva = ReservaVuelo.solicitarReserva(titular, pasajero, vueloPendiente,
						ClaseAsientoReserva.BUSINESS, ANTES_DE_SALIDA, 0, 10);

				assertEquals(titular, reserva.getIdentificadorTitular());
				assertEquals(pasajero, reserva.getPasajero());
				assertEquals(vueloPendiente, reserva.getVuelo());
				assertEquals(ClaseAsientoReserva.BUSINESS, reserva.getClase());
			}

			@Test
			void deberia_reservar_vuelo_retrasado_con_plazas() {
				Vuelo vueloRetrasado = Vuelo.of(UUID.randomUUID(), new Itinerario(SALIDA, LLEGADA, "MAD", "BCN"),
						TipoVuelo.NACIONAL, EstadoVuelo.RETRASADO, new Avion(180));
				assertDoesNotThrow(() -> ReservaVuelo.solicitarReserva(titular, pasajero, vueloRetrasado,
						ClaseAsientoReserva.ECONOMICA, ANTES_DE_SALIDA, 0, 10));
			}
		}
		//Testeamos casos erroneos
		@Nested
		@DisplayName("Cuando el pasajero supera el límite de reservas")
		class CuandoLimiteReservas {

			@Test
			void deberia_lanzar_excepcion_si_pasajero_ya_tiene_reserva() {
				LimiteReservasPorPasajeroEnVueloSuperadoException ex = assertThrows(
						LimiteReservasPorPasajeroEnVueloSuperadoException.class,
						() -> ReservaVuelo.solicitarReserva(titular, pasajero, vueloPendiente,
								ClaseAsientoReserva.ECONOMICA, ANTES_DE_SALIDA, 1, 10));
				assertEquals("Sólo puede haber una reserva por pasajero en un vuelo", ex.getMessage());
			}
		}

		@Nested
		@DisplayName("Cuando el vuelo no es reservable")
		class CuandoVueloNoReservable {

			@Test
			void deberia_lanzar_excepcion_si_vuelo_cancelado() {
				VueloNoReservableException ex = assertThrows(VueloNoReservableException.class,
						() -> ReservaVuelo.solicitarReserva(titular, pasajero, vueloCancelado,
								ClaseAsientoReserva.ECONOMICA, ANTES_DE_SALIDA, 0, 10));
				assertEquals("El vuelo se encuentra completado o cancelado", ex.getMessage());
			}

			@Test
			void deberia_lanzar_excepcion_si_vuelo_completado() {
				assertThrows(VueloNoReservableException.class, () -> ReservaVuelo.solicitarReserva(titular, pasajero,
						vueloCompletado, ClaseAsientoReserva.ECONOMICA, ANTES_DE_SALIDA, 0, 10));
			}
		}

		@Nested
		@DisplayName("Cuando el vuelo ya ha iniciado")
		class CuandoVueloIniciado {

			@Test
			void deberia_lanzar_excepcion_si_fecha_igual_a_salida() {
				assertThrows(VueloIniciadoException.class, () -> ReservaVuelo.solicitarReserva(titular, pasajero,
						vueloPendiente, ClaseAsientoReserva.ECONOMICA, SALIDA, 0, 10));
			}

			@Test
			void deberia_lanzar_excepcion_si_fecha_posterior_a_salida() {
				assertThrows(VueloIniciadoException.class, () -> ReservaVuelo.solicitarReserva(titular, pasajero,
						vueloPendiente, ClaseAsientoReserva.ECONOMICA, DESPUES_DE_SALIDA, 0, 10));
			}
		}

		@Nested
		@DisplayName("Cuando no hay plazas disponibles")
		class CuandoSinPlazas {

			@Test
			void deberia_lanzar_excepcion_si_plazas_cero() {
				VueloSinPlazasException ex = assertThrows(VueloSinPlazasException.class,
						() -> ReservaVuelo.solicitarReserva(titular, pasajero, vueloPendiente,
								ClaseAsientoReserva.ECONOMICA, ANTES_DE_SALIDA, 0, 0));
				assertEquals("No hay plazas disponibles en el avión", ex.getMessage());
			}

			@Test
			void deberia_lanzar_excepcion_si_plazas_negativas() {
				assertThrows(VueloSinPlazasException.class, () -> ReservaVuelo.solicitarReserva(titular, pasajero,
						vueloPendiente, ClaseAsientoReserva.ECONOMICA, ANTES_DE_SALIDA, 0, -1));
			}
		}
	}

	// ##################  Validamos formalizar reserva

	@Nested
	@DisplayName("formalizarReserva_correcta")
	@Order(3)
	class CuandoFormalizar {

		@Test
		void reserva_deberia_cambiar_estado_pendiente_a_activa() {
			ReservaVuelo reserva = ReservaVuelo.solicitarReserva(titular, pasajero, vueloPendiente,
					ClaseAsientoReserva.ECONOMICA, ANTES_DE_SALIDA, 0, 10);

			reserva.formalizarReserva();

			assertEquals(EstadoReserva.ACTIVA, reserva.getEstado());
		}
	}

	// ##################  Validamos cancelar reserva

	@Nested
	@DisplayName("cancelarReserva_correcta")
	@Order(4)
	class CuandoCancelar {

		@Test
		void cancelar_deberia_cancelar_reserva_si_vuelo_no_iniciado() {
			ReservaVuelo reserva = ReservaVuelo.solicitarReserva(titular, pasajero, vueloPendiente,
					ClaseAsientoReserva.ECONOMICA, ANTES_DE_SALIDA, 0, 10);
			reserva.cancelarReserva(ANTES_DE_SALIDA);
			assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
		}

		@Test
		void cancelar_deberia_lanzar_excepcion_si_vuelo_iniciado() {
			ReservaVuelo reserva = ReservaVuelo.solicitarReserva(titular, pasajero, vueloPendiente,
					ClaseAsientoReserva.ECONOMICA, ANTES_DE_SALIDA, 0, 10);
			assertThrows(VueloIniciadoException.class,	() -> reserva.cancelarReserva(DESPUES_DE_SALIDA));
		}
	}

}
