package org.matsim.run;

import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;

public class RunEventsHandler {
    public static void main(String[] args) {
        String inputFile = "C:/Users/ehajhashemi/Desktop/Files/MATSim codes/matsim_melbourne_outputs/output_4_fin_drt_not_HOV/output_events.xml";

        EventsManager eventsManager = EventsUtils.createEventsManager();

        AverageTravelTimeCalculator averageTravelTimeCalculator = new AverageTravelTimeCalculator();
        eventsManager.addHandler(averageTravelTimeCalculator);

        MatsimEventsReader eventsReader = new MatsimEventsReader(eventsManager);
        eventsReader.readFile(inputFile);

        System.out.println("average travel time: " + averageTravelTimeCalculator.getAverageTravelTime());
        System.out.println("sum travel time: " + averageTravelTimeCalculator.getTravelTimeSum());
        System.out.println("count travel time: " + averageTravelTimeCalculator.getTravelTimeCount());

    }
}
