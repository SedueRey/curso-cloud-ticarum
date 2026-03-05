package es.um.atica.umufly.parking.adaptors.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table( name = "VWEXT_TIPO_ESTACIONAMIENTO", schema = "FORMACION_TICARUM" )
public class TipoEstacionamientoViewExtEntity {

	@Id
	@NotNull
	@Column( name = "TIPO", nullable = false, length = 2 )
	@Enumerated( value = EnumType.STRING )
	private TipoEstacionamientoEnum tipo;

	@NotNull
	@Column( name = "PRECIO", nullable = false )
	private double precio;

	public TipoEstacionamientoEnum getTipo() {
		return tipo;
	}

	public void setTipo( TipoEstacionamientoEnum tipo ) {
		this.tipo = tipo;
	}

	public double getPrecio() {
		return precio;
	}

	public void setPrecio( double precio ) {
		this.precio = precio;
	}
}