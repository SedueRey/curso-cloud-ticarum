# language: es
Característica: Reserva de vuelo

  Escenario: Reservar vuelo PENDIENTE
    Dado un viajero con NIF "58231461A"
    Cuando lista de vuelos con página 0 y tamaño 25
    Y reserva el primero libre
    Entonces la reserva se realiza

  Escenario: NIF inválido
    Dado un viajero con NIF "INVALIDO"
    Cuando lista de vuelos con página 0 y tamaño 25
    Entonces la respuesta debe tener status 400
