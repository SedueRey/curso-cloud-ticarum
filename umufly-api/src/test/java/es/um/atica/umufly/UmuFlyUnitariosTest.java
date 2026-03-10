package es.um.atica.umufly;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import es.um.atica.umufly.vuelos.domain.exception.LimiteReservasPorPasajeroEnVueloSuperadoException;
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

public class UmuFlyUnitariosTest {

	@Test
	void formalizarReserva_deberiaPasarReservaAEstadoActiva() {
		ReservaVuelo reserva = crearReservaPendienteValida();

		reserva.formalizarReserva();

		assertEquals( EstadoReserva.ACTIVA, reserva.getEstado() );
	}

	@Test
	void crearNuevaReserva_deberiaQuedarEnPendiente() {
		ReservaVuelo reserva = crearReservaPendienteValida();

		assertEquals( EstadoReserva.PENDIENTE, reserva.getEstado() );
	}

	@Test
	void todaReserva_deberiaTenerIdUnico() {
		Set<UUID> ids = new HashSet<>();

		for ( int i = 0; i < 10; i++ ) {
			DocumentoIdentidad titular = new DocumentoIdentidad( TipoDocumento.PASAPORTE, "P" + i );
			Pasajero pasajero = Pasajero.of(
				titular,
				new NombreCompleto( "Pasajero", "Numero" + i, "Test" ),
				new CorreoElectronico( "pasajero" + i + "@um.es" ),
				new Nacionalidad( "Espana" )
			);

			ReservaVuelo reserva = ReservaVuelo.solicitarReserva(
				titular,
				pasajero,
				crearVueloReservable( EstadoVuelo.PENDIENTE ),
				ClaseAsientoReserva.ECONOMICA,
				LocalDateTime.now(),
				0,
				10
			);

			ids.add( reserva.getId() );
		}

		assertEquals( 10, ids.size() );
	}

	@Test
	void reservaValida_deberiaContenerTodosSusDatosValidos() {
		DocumentoIdentidad titular = new DocumentoIdentidad( TipoDocumento.NIF, "00000000T" );
		Pasajero pasajero = Pasajero.of(
			titular,
			new NombreCompleto( "Ana", "Lopez", "Garcia" ),
			new CorreoElectronico( "ana.lopez@um.es" ),
			new Nacionalidad( "Espana" )
		);
		Vuelo vuelo = crearVueloReservable( EstadoVuelo.PENDIENTE );
		LocalDateTime fechaReserva = LocalDateTime.now();

		ReservaVuelo reserva = ReservaVuelo.solicitarReserva(
			titular,
			pasajero,
			vuelo,
			ClaseAsientoReserva.BUSINESS,
			fechaReserva,
			0,
			vuelo.getAvion().capacidad()
		);

		assertNotNull( reserva.getId() );
		assertEquals( titular, reserva.getIdentificadorTitular() );
		assertEquals( pasajero, reserva.getPasajero() );
		assertEquals( vuelo, reserva.getVuelo() );
		assertEquals( ClaseAsientoReserva.BUSINESS, reserva.getClase() );
		assertEquals( fechaReserva, reserva.getFechaReserva() );
		assertEquals( EstadoReserva.PENDIENTE, reserva.getEstado() );
	}

	@Test
	void deberiaPoderSolicitarReserva_enVueloRetrasadoConPlazasLibres() {
		ReservaVuelo reserva = ReservaVuelo.solicitarReserva(
			new DocumentoIdentidad( TipoDocumento.NIF, "00000000T" ),
			crearPasajeroValido(),
			crearVueloReservable( EstadoVuelo.RETRASADO ),
			ClaseAsientoReserva.ECONOMICA,
			LocalDateTime.now(),
			0,
			5
		);

		assertNotNull( reserva );
		assertEquals( EstadoReserva.PENDIENTE, reserva.getEstado() );
	}

	@Test
	void unPasajeroSoloPuedeTenerUnaReserva_paraElMismoVuelo() {
		LimiteReservasPorPasajeroEnVueloSuperadoException excepcion = assertThrows(
			LimiteReservasPorPasajeroEnVueloSuperadoException.class,
			() -> ReservaVuelo.solicitarReserva(
				new DocumentoIdentidad( TipoDocumento.NIF, "00000000T" ),
				crearPasajeroValido(),
				crearVueloReservable( EstadoVuelo.PENDIENTE ),
				ClaseAsientoReserva.ECONOMICA,
				LocalDateTime.now(),
				1,
				5
			)
		);

		assertEquals( "Sólo puede haber una reserva por pasajero en un vuelo", excepcion.getMessage() );
	}

	private ReservaVuelo crearReservaPendienteValida() {
		DocumentoIdentidad titular = new DocumentoIdentidad( TipoDocumento.NIF, "00000000T" );
		Pasajero pasajero = Pasajero.of(
			titular,
			new NombreCompleto( "Maria", "Ruiz", "Lopez" ),
			new CorreoElectronico( "maria.ruiz@um.es" ),
			new Nacionalidad( "Espana" )
		);

		return ReservaVuelo.solicitarReserva(
			titular,
			pasajero,
			crearVueloReservable( EstadoVuelo.PENDIENTE ),
			ClaseAsientoReserva.ECONOMICA,
			LocalDateTime.now(),
			0,
			10
		);
	}

	private Pasajero crearPasajeroValido() {
		DocumentoIdentidad documento = new DocumentoIdentidad( TipoDocumento.NIF, "00000000T" );
		return Pasajero.of(
			documento,
			new NombreCompleto( "Lucia", "Perez", "Sanchez" ),
			new CorreoElectronico( "lucia.perez@um.es" ),
			new Nacionalidad( "Espana" )
		);
	}

	private Vuelo crearVueloReservable( EstadoVuelo estado ) {
		LocalDateTime salida = LocalDateTime.now().plusDays( 2 );
		Itinerario itinerario = new Itinerario( salida, salida.plusHours( 2 ), "MAD", "BCN" );
		return Vuelo.of( UUID.randomUUID(), itinerario, TipoVuelo.NACIONAL, estado, new Avion( 180 ) );
	}

}
