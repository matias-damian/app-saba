package cl.saba.operaciones.domain

data class Parking(val id: String, val name: String, val commune: String, val spaces: Int)

data class ParkingCatalog(val parkings: List<Parking>, val selectedId: String?)
