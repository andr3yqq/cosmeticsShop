package com.andr3yqq.cosmeticsshop.shipment;

public interface ShipmentService {
    Shipment createShipment(ShipmentDTO shipmentDTO);
    Shipment updateShipment(ShipmentDTO shipmentDTO);
    Shipment updateShipmentStatus(ShipmentStatus shipmentStatus);
}
