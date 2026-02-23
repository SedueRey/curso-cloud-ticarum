package es.um.atica.umufly.vuelos.application.usecase.obtenerreservas;

import java.util.UUID;

import es.um.atica.fundewebjs.umubus.domain.cqrs.Query;
import es.um.atica.umufly.vuelos.domain.model.ReservaVuelo;

public class ObtenerReservaQuery extends Query<ReservaVuelo> {

	private final UUID idReserva;

	private ObtenerReservaQuery( UUID idReserva ) {
		this.idReserva = idReserva;
	}

	public static ObtenerReservaQuery of( UUID idReserva ) {
		return new ObtenerReservaQuery( idReserva );
	}

	public UUID getIdReserva() {
		return idReserva;
	}
}
