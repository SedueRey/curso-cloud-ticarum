package es.um.atica.vuelos.cucumber;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import es.um.atica.umufly.vuelos.adaptors.api.rest.v2.dto.VueloDTO;

//Clase necesaria porque la respuesta es por HATEOAS y no queremos cargar beans de Spring
public class VuelosWrapper {
	@JsonProperty("_embedded")
	private Embedded embedded;

	public List<VueloDTO> getVuelos() {
		return embedded != null ? embedded.vuelos : Collections.emptyList();
	}

	public class Embedded {
		@JsonProperty("vuelos")
		private List<VueloDTO> vuelos;
	}
}
