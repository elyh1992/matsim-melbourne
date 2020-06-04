package org.matsim.run;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.List;


public class changeMode {


    public static void main(String[] args) {
        Config config;

        config = ConfigUtils.loadConfig("scenarios/2017-11-scenario-by-kai-from-vista/config.xml");


        Scenario scenario = ScenarioUtils.loadScenario(config);

        // We need another population, the PUS population


        for (Id<Person> personId:scenario.getPopulation().getPersons().keySet()){
            System.out.println(personId);
            Person eachPerson = scenario.getPopulation().getPersons().get(personId);
            System.out.println(eachPerson);
            Plan eachPlan = eachPerson.getSelectedPlan();
            System.out.println(eachPlan);
            int NoOfPlans = eachPlan.getPlanElements().size();
            System.out.println(NoOfPlans);
            for (int j = 1 ; j < NoOfPlans ; j+=2)
            {
                Leg legToCheck = (Leg) eachPlan.getPlanElements().get(j);
                String legToCheckMode = legToCheck.getMode().toString().trim() ;

                if (legToCheckMode.equals("Walking") ) legToCheck.setMode("walk") ;
                if (legToCheckMode.equals("Tram") || legToCheckMode.equals("Train") || legToCheckMode.equals("Bus") ) legToCheck.setMode("pt");
                if (legToCheckMode.equals("Vehicle Passenger") || legToCheckMode.equals("Other")  ) legToCheck.setMode("car");



            }
        }
        PopulationWriter populationWriter = new PopulationWriter(scenario.getPopulation());
        populationWriter.write("D:/matsim-melbourne/scenarios/2017-11-scenario-by-kai-from-vista/plans4mode.xml.gz");
//		new ObjectAttributesXmlWriter(this.scenarioPUS.getPopulation().getPersonAttributes()).writeFile("C:/Users/znavidikasha/Dropbox/1-PhDProject/YarraRanges/demand/zahra's/YRsPlansSubAtts.xml");
        System.out.println("writing done");






        /*Id<Person> total = Id.createPersonId(1);
        int numPeople =Id.get();
        for (int i = 0 ; i < .size() ; i++)
        Person eachPerson = scenario.getPopulation().getPersons(Id);
                (eachPerson.getId());
        for (Id<Person> personId : personToRemove){
            scenario.getPopulation().removePerson(personId);

    }

    }

    Person eachPerson = this.getPopulation().getPersons().get(activePeople.get(i));
    Plan eachPlan = eachPerson.getSelectedPlan()
    int NoOfPlans = eachPlan.getPlanElements().size()
    for (int j = 1 ; j < NoOfPlans ; j+=2)
    {
        Leg legToCheck = (Leg) eachPlan.getPlanElements().get(j);
        String legToCheckMode = legToCheck.getMode().toString().trim() ;
        if (legToCheckMode.equals("1") || legToCheckMode.equals("2") || legToCheckMode.equals("9") || legToCheckMode.equals("3") || legToCheckMode.equals("6")) legToCheck.setMode("car");
        if (legToCheckMode.equals("7") || legToCheckMode.equals("8") || legToCheckMode.equals("10") || legToCheckMode.equals("12")) legToCheck.setMode("pt");
    }

*/

    }

}
