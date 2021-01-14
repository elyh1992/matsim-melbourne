package org.matsim.run;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.*;


public class MelbournePlanScoringFunctionFactory implements ScoringFunctionFactory {
    private static final Logger Log = Logger.getLogger(MelbournePlanScoringFunctionFactory.class);
    private final Config config;
    private final Network network;
    private final ScoringParametersForPerson params;


    public MelbournePlanScoringFunctionFactory(final Scenario sc) {
        this(sc.getConfig(),new SubpopulationScoringParameters(sc), sc.getNetwork());

    }

    MelbournePlanScoringFunctionFactory (Config config, ScoringParametersForPerson params,Network network) {
        this.config = config;
        this.params = params;
        this. network = network;
    }

    @Override
    public ScoringFunction createNewScoringFunction(Person person) {
        final ScoringParameters parameters =  params.getScoringParameters(person);
        SumScoringFunction sumScoringFunction = new SumScoringFunction();
        sumScoringFunction.addScoringFunction(new CharyparNagelActivityScoring(parameters));
        sumScoringFunction.addScoringFunction(new CharyparNagelLegScoring(parameters, network));
        sumScoringFunction.addScoringFunction(new CharyparNagelAgentStuckScoring(parameters));
        double dailyHHIncome = 65.0;
        String hhIncomee = (String) person.getAttributes().getAttribute("hhIncome");
        String hhSizee = (String) person.getAttributes().getAttribute("hhSize");
        if (person.getAttributes().getAttribute("hhIncome") != null && person.getAttributes().getAttribute("hhSize") != null && hhIncomee.length() >0 && hhSizee.length() > 0 ){

            double hhSize = Double.valueOf((String)person.getAttributes().getAttribute("hhSize"));
            double hhWeeklyIncome = Double.valueOf((String) person.getAttributes().getAttribute("hhIncome"));
            if (hhSize >= 1.0){
               dailyHHIncome =  (hhWeeklyIncome * 52)/(hhSize * 240);
               if (dailyHHIncome < 65.0){
                   dailyHHIncome = 65.0;
               }

            }


        }
        double personSpecificMarginalUtilityOfMoney = 28.30 / dailyHHIncome;
        /*Log.warn("margUtlOfMoney=" + personSpecificMarginalUtilityOfMoney );*/


        person.getAttributes().putAttribute("marginalUtilityOfMoney",personSpecificMarginalUtilityOfMoney );
        sumScoringFunction.addScoringFunction(new CharyparNagelMoneyScoring(personSpecificMarginalUtilityOfMoney));
        

        return  sumScoringFunction;



    }
}
