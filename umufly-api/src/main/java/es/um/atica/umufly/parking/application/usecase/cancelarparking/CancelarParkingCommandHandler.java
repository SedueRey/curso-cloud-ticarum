package es.um.atica.umufly.parking.application.usecase.cancelarparking;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Component;

import es.um.atica.fundewebjs.umubus.domain.cqrs.SyncCommandHandler;
import es.um.atica.umufly.parking.application.port.ReservasParkingReadRepository;
import es.um.atica.umufly.parking.application.port.ReservasParkingWritePort;
import es.um.atica.umufly.parking.application.port.ReservasParkingWriteRepository;
import es.um.atica.umufly.parking.domain.model.ReservaParking;

@Component
public class CancelarParkingCommandHandler implements SyncCommandHandler<ReservaParking, CancelarParkingCommand> {

	private final ReservasParkingReadRepository reservasParkingReadRepository;
	private final ReservasParkingWriteRepository reservasParkingWriteRepository;
	private final ReservasParkingWritePort reservasParkingWritePort;
	private final Clock clock;

	public CancelarParkingCommandHandler( ReservasParkingReadRepository reservasparkingRepository, ReservasParkingWriteRepository reservasParkingWriteRepository, ReservasParkingWritePort reservasParkingWritePort, Clock clock ) {
		this.reservasParkingReadRepository = reservasparkingRepository;
		this.reservasParkingWriteRepository = reservasParkingWriteRepository;
		this.reservasParkingWritePort = reservasParkingWritePort;
		this.clock = clock;
	}

	@Override
	public ReservaParking handle( CancelarParkingCommand command ) throws Exception {
		// 1. Recuperamos la reserva
		ReservaParking reserva = reservasParkingReadRepository.findParkingById( command.getDocumentoIdentidadTitular(), command.getIdParking() );

		// 2. Cancelamos la reserva en el fronOffice
		reserva.cancelarParking( LocalDateTime.now( clock ) );
		reservasParkingWriteRepository.cancelParking( reserva.getId() );

		// 3. Cancelamos la reserva llamando al backoffice para que se haga eco de la cancelacion
		UUID idReservaFormalizada = reservasParkingReadRepository.findIdFormalizadaByParkingById(command.getIdParking());
		reservasParkingWritePort.cancelarParking( command.getDocumentoIdentidadTitular(), idReservaFormalizada );

		return reserva;
	}

}
