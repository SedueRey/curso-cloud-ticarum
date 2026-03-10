package es.um.atica.umufly.cucumber;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import es.um.atica.umufly.vuelos.adaptors.api.rest.AuthService;
import es.um.atica.umufly.vuelos.adaptors.api.rest.v2.ReservasCommandEndpointV2;
import es.um.atica.umufly.vuelos.adaptors.api.rest.v2.ReservasModelAssemblerV2;
import es.um.atica.umufly.vuelos.adaptors.api.rest.v2.VuelosModelAssemblerV2;
import es.um.atica.umufly.vuelos.adaptors.api.rest.v2.VuelosQueryEndpointV2;
import es.um.atica.umufly.vuelos.adaptors.api.rest.v2.dto.ClaseAsientoReserva;
import es.um.atica.umufly.vuelos.adaptors.api.rest.v2.dto.DocumentoIdentidadDTO;
import es.um.atica.umufly.vuelos.adaptors.api.rest.v2.dto.EstadoReserva;
import es.um.atica.umufly.vuelos.adaptors.api.rest.v2.dto.PasajeroDTO;
import es.um.atica.umufly.vuelos.adaptors.api.rest.v2.dto.ReservaVueloDTO;
import es.um.atica.umufly.vuelos.adaptors.api.rest.v2.dto.TipoDocumento;
import es.um.atica.umufly.vuelos.adaptors.api.rest.v2.dto.VueloDTO;
import es.um.atica.umufly.vuelos.application.dto.VueloAmpliadoDTO;
import es.um.atica.umufly.vuelos.application.usecase.cancelarreservas.CancelarReservaCommandHandler;
import es.um.atica.umufly.vuelos.application.usecase.crearreservas.CrearReservaCommandHandler;
import es.um.atica.umufly.vuelos.application.usecase.listarvuelos.ListaVuelosQueryHandler;
import es.um.atica.umufly.vuelos.application.usecase.obtenervuelos.ObtenerVueloQueryHandler;
import es.um.atica.umufly.vuelos.domain.model.DocumentoIdentidad;
import es.um.atica.umufly.vuelos.domain.model.ReservaVuelo;
import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

public class ReservaVueloSteps {

	private static final UUID PRIMER_VUELO_LIBRE_ID = UUID.fromString( "11111111-1111-1111-1111-111111111111" );

	private AuthService authService;
	private ListaVuelosQueryHandler listaVuelosQueryHandler;
	private PagedResourcesAssembler<VueloAmpliadoDTO> pagedResourcesAssembler;
	private VuelosModelAssemblerV2 vuelosModelAssemblerV2;
	private ObtenerVueloQueryHandler obtenerVueloQueryHandler;
	private CrearReservaCommandHandler crearReservaCommandHandler;
	private CancelarReservaCommandHandler cancelarReservaCommandHandler;
	private ReservasModelAssemblerV2 reservasModelAssemblerV2;
	private VuelosQueryEndpointV2 vuelosQueryEndpointV2;
	private ReservasCommandEndpointV2 reservasCommandEndpointV2;

	private String nif;
	private int listadoVuelosStatus;
	private int reservaStatus;
	private ReservaVueloDTO reservaRespuesta;

	@Before
	public void limpiarEstado() {
		authService = Mockito.mock( AuthService.class );
		listaVuelosQueryHandler = Mockito.mock( ListaVuelosQueryHandler.class );
		pagedResourcesAssembler = Mockito.mock( PagedResourcesAssembler.class );
		vuelosModelAssemblerV2 = Mockito.mock( VuelosModelAssemblerV2.class );
		obtenerVueloQueryHandler = Mockito.mock( ObtenerVueloQueryHandler.class );
		crearReservaCommandHandler = Mockito.mock( CrearReservaCommandHandler.class );
		cancelarReservaCommandHandler = Mockito.mock( CancelarReservaCommandHandler.class );
		reservasModelAssemblerV2 = Mockito.mock( ReservasModelAssemblerV2.class );

		vuelosQueryEndpointV2 = new VuelosQueryEndpointV2( obtenerVueloQueryHandler, listaVuelosQueryHandler, vuelosModelAssemblerV2, pagedResourcesAssembler, authService );
		reservasCommandEndpointV2 = new ReservasCommandEndpointV2( crearReservaCommandHandler, cancelarReservaCommandHandler, reservasModelAssemblerV2, authService );

		nif = null;
		listadoVuelosStatus = 0;
		reservaStatus = 0;
		reservaRespuesta = null;
	}

	@Dado("un viajero con NIF {string}")
	public void unViajeroConNif( String nif ) {
		this.nif = nif;
	}

	@Cuando("lista de vuelos con página {int} y tamaño {int}")
	public void listaDeVuelosConPaginacion( int pagina, int tamanyo ) throws Exception {
		String usuarioHeader = "NIF:" + nif;

		if ( "INVALIDO".equalsIgnoreCase( nif ) ) {
			when( authService.parseUserHeader( usuarioHeader ) )
					.thenThrow( new ResponseStatusException( HttpStatus.BAD_REQUEST, "NIF inválido" ) );
		} else {
			when( authService.parseUserHeader( usuarioHeader ) )
					.thenReturn( new DocumentoIdentidad( es.um.atica.umufly.vuelos.domain.model.TipoDocumento.PASAPORTE, nif ) );
			when( listaVuelosQueryHandler.handle( any() ) )
					.thenReturn( new PageImpl<>( List.of( new VueloAmpliadoDTO() ), PageRequest.of( pagina, tamanyo ), 1 ) );
			when( pagedResourcesAssembler.toModel( any(), eq( vuelosModelAssemblerV2 ) ) )
					.thenReturn( PagedModel.of(
							List.of( new VueloDTO( PRIMER_VUELO_LIBRE_ID, null, null, null, null ) ),
							new PagedModel.PageMetadata( tamanyo, pagina, 1 )
					) );
		}

		try {
			vuelosQueryEndpointV2.getVuelos( usuarioHeader, pagina, tamanyo );
			listadoVuelosStatus = 200;
		} catch ( ResponseStatusException ex ) {
			listadoVuelosStatus = ex.getStatusCode().value();
		}
	}

	@Cuando("reserva el primero libre")
	public void reservaElPrimeroLibre() throws Exception {
		assertEquals( 200, listadoVuelosStatus );

		ReservaVueloDTO respuestaReserva = new ReservaVueloDTO();
		respuestaReserva.setId( UUID.fromString( "22222222-2222-2222-2222-222222222222" ) );
		respuestaReserva.setEstado( EstadoReserva.PENDIENTE );
		respuestaReserva.setClaseAsiento( ClaseAsientoReserva.ECONOMICA );

		when( crearReservaCommandHandler.handle( any() ) )
				.thenReturn( org.mockito.Mockito.mock( ReservaVuelo.class ) );
		when( reservasModelAssemblerV2.toModel( any() ) ).thenReturn( respuestaReserva );

		ReservaVueloDTO nuevaReserva = new ReservaVueloDTO();
		nuevaReserva.setVuelo( new VueloDTO( PRIMER_VUELO_LIBRE_ID, null, null, null, null ) );
		nuevaReserva.setClaseAsiento( ClaseAsientoReserva.ECONOMICA );
		nuevaReserva.setPasajero( crearPasajeroValido() );

		reservaRespuesta = reservasCommandEndpointV2.creaReserva( "NIF:" + nif, nuevaReserva );
		reservaStatus = 200;
	}

	@Entonces("la reserva se realiza")
	public void laReservaSeRealiza() throws Exception {
		assertEquals( 200, reservaStatus );
		assertNotNull( reservaRespuesta );
		assertEquals( EstadoReserva.PENDIENTE, reservaRespuesta.getEstado() );
	}

	@Entonces("la respuesta debe tener status {int}")
	public void laRespuestaDebeTenerStatus( int status ) {
		assertEquals( status, listadoVuelosStatus );
	}

	private PasajeroDTO crearPasajeroValido() {
		PasajeroDTO pasajero = new PasajeroDTO();
		DocumentoIdentidadDTO documento = new DocumentoIdentidadDTO();
		documento.setTipo( TipoDocumento.PASAPORTE );
		documento.setNumero( "AB1234567" );
		pasajero.setDocumentoIdentidad( documento );
		pasajero.setNombre( "Ada" );
		pasajero.setPrimerApellido( "Lovelace" );
		pasajero.setSegundoApellido( "Byron" );
		pasajero.setCorreoElectronico( "ada@example.com" );
		pasajero.setNacionalidad( "Espana" );
		return pasajero;
	}
}
