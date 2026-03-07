# language: es
Característica: Reserva de vuelo

  Escenario: Listar los vuelos existentes
    Dado un viajero con NIF "04523853Y"
    Cuando lista de vuelos con página 0 y tamaño 25
    Entonces la respuesta debe tener status 200
    Y devolver una lista de vuelos rellena o vacia
    
  Escenario: Realizar una reserva
  	Dado un viajero con NIF "04523853Y"
  	Cuando lista de vuelos con página 0 y tamaño 25
  	Y reserva el primero libre
  	Entonces la reserva se realiza

    