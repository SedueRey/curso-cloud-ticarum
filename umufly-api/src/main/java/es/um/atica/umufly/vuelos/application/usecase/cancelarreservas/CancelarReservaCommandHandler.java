package es.um.atica.umufly.vuelos.application.usecase.cancelarreservas;

import org.springframework.stereotype.Component;

import es.um.atica.fundewebjs.umubus.domain.cqrs.SyncCommandHandler;
import es.um.atica.umufly.vuelos.application.port.ReservasVueloReadRepository;
import es.um.atica.umufly.vuelos.application.port.ReservasVueloWritePort;
import es.um.atica.umufly.vuelos.application.port.ReservasVueloWriteRepository;
import es.um.atica.umufly.vuelos.domain.model.ReservaVuelo;

@Component
public class CancelarReservaCommandHandler implements SyncCommandHandler<ReservaVuelo, CancelarReservaCommand> {

	private final ReservasVueloReadRepository reservasVueloReadRepository;
	private final ReservasVueloWriteRepository reservasVueloWriteRepository;
	private final ReservasVueloWritePort formalizacionReservasVueloPort;

	public CancelarReservaCommandHandler( ReservasVueloReadRepository reservasVueloRepository, ReservasVueloWriteRepository reservasVueloWriteRepository, ReservasVueloWritePort formalizacionReservasVueloPort ) {
		this.reservasVueloReadRepository = reservasVueloRepository;
		this.reservasVueloWriteRepository = reservasVueloWriteRepository;
		this.formalizacionReservasVueloPort = formalizacionReservasVueloPort;
	}

	@Override
	public ReservaVuelo handle( CancelarReservaCommand command ) throws Exception {
		// 1. Recuperamos la reserva
		ReservaVuelo reservaVuelo = reservasVueloReadRepository.findReservaById( command.getIdReserva() );

		// 2. Cancelamos la reserva en el fronOffice
		reservasVueloWriteRepository.cancelReserva( reservaVuelo.getId() );

		// 3. Cancelamos la reserva llamando al backoffice para que se haga eco de la cancelacion
		formalizacionReservasVueloPort.cancelarReservaVuelo( command.getDocumentoIdentidadTitular(), command.getIdReserva() );

		return reservaVuelo;
	}

}
