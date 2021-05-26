package org.matsim.run;

import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.VehicleEntersTrafficEvent;
import org.matsim.api.core.v01.events.VehicleLeavesTrafficEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleLeavesTrafficEventHandler;

public class MyEventHandler2 implements PersonArrivalEventHandler, PersonDepartureEventHandler, VehicleEntersTrafficEventHandler, VehicleLeavesTrafficEventHandler {
    private double timePersonOnTravel = 0.0;
    private double timeVehicleInTraffic = 0.0 ;
    private double counter = 0.0;
    public double getTotalTravelTime() {
        return this.timePersonOnTravel ;
    }
    public double getTotalVehicleInTrafficTime() {
        return this.timeVehicleInTraffic ;
    }
    public double getTotalNumberOfTravel() {
        return this.counter ;
    }
    @Override
    public void reset(int iteration){
        this.timePersonOnTravel = 0.0;
        this.timeVehicleInTraffic = 0.0 ;
        this.counter=0.0;

    }


    @Override
    public void handleEvent(PersonArrivalEvent event) {
        this.timePersonOnTravel += event.getTime();
        this.counter+=1;

    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        this.timePersonOnTravel -= event.getTime();

    }

    @Override
    public void handleEvent(VehicleEntersTrafficEvent event) {
        this.timeVehicleInTraffic -= event.getTime() ;

    }

    @Override
    public void handleEvent(VehicleLeavesTrafficEvent event) {
        this.timeVehicleInTraffic += event.getTime() ;

    }
}
