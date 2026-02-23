package es.um.atica.umufly.vuelos.application.usecase.listarreservas;

import org.springframework.data.domain.Page;

import es.um.atica.fundewebjs.umubus.domain.cqrs.Query;
import es.um.atica.umufly.vuelos.domain.model.ReservaVuelo;


public class ListaReservasQuery extends Query<Page<ReservaVuelo>> {

	private final Integer pagina;
	private final Integer tamanioPagina;

	private ListaReservasQuery( int pagina, int tamanioPagina ) {
		this.pagina = pagina;
		this.tamanioPagina = tamanioPagina;
	}

	public static ListaReservasQuery of( int pagina, int tamanioPagina ) {
		return new ListaReservasQuery( pagina, tamanioPagina );
	}

	public Integer getPagina() {
		return pagina;
	}

	public Integer getTamanioPagina() {
		return tamanioPagina;
	}
}
