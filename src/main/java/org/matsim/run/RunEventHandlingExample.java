package org.matsim.run;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

public class RunEventHandlingExample {
    public static void main(String[] args) {
       /* Network network = NetworkUtils.createNetwork();
        NetworkFactory factory	= network.getFactory();*/

        /*String inputFile = "C:/Users/ehajhashemi/Desktop/Files/MATSim codes/matsim_melbourne_outputs/output_500_4_fin/output_events.xml";*/
        String inputFile = "C:/Users/ehajhashemi/Desktop/Files/MATSim codes/matsim-melbourne/output/output_events.xml";


       /* new MatsimNetworkReader(network).readFile("C:/Users/ehajhashemi/Desktop/Files/MATSim codes/matsim-melbourne/scenarios/2017-11-scenario-by-kai-from-vista/net.xml");*/

        EventsManager eventsManager = EventsUtils.createEventsManager();

        // SimpleEventHandler eventHandler = new SimpleEventHandler();
       MyEventHandler2 eventHandler = new MyEventHandler2();
       MyEventHandler3 handler3 = new MyEventHandler3();

        eventsManager.addHandler(eventHandler);
        eventsManager.addHandler(handler3);


        eventsManager.initProcessing();
        MatsimEventsReader eventsReader = new MatsimEventsReader(eventsManager);
        eventsReader.readFile(inputFile);
        eventsManager.finishProcessing();

        System.out.println("average travel time: " + eventHandler.getTotalTravelTime());
        System.out.println("total num of trip: " + eventHandler.getTotalNumberOfTravel());
        System.out.println("average travel time per person: " + eventHandler.getTotalTravelTime()/eventHandler.getTotalNumberOfTravel());
        handler3.writeChart("output/departuresPerHour.png");


    }





}
