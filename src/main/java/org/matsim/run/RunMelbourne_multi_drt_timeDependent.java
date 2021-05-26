/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.run;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
/*import org.matsim.contrib.av.robotaxi.fares.drt.DrtFareModule;
import org.matsim.contrib.av.robotaxi.fares.drt.DrtFaresConfigGroup;*/
import org.matsim.contrib.drt.routing.DrtRoute;
import org.matsim.contrib.drt.routing.DrtRouteFactory;
import org.matsim.contrib.drt.run.DrtConfigs;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.drt.run.MultiModeDrtModule;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.contrib.dvrp.run.DvrpQSimComponents;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.network.NetworkChangeEvent;
import org.matsim.core.population.routes.RouteFactories;

import java.util.Set;

/**
 * @author nagel
 *
 */
public class RunMelbourne_multi_drt_timeDependent {
	private static final Logger log = Logger.getLogger(RunMelbourne_multi_drt_timeDependent.class) ;

	public static void main(String[] args) {
		for (String arg : args) {
			log.info( arg );
		}

		if ( args.length==0 ) {
			/*args = new String[] {"scenarios/2017-11-scenario-by-kai-from-vista/updated_config_all_ridesourcing.xml"}  ;*/
			args = new String[] {"scenarios/2017-11-scenario-by-kai-from-vista/updated_config_drt.xml"}  ;
		}
		// yyyyyy increase memory!

		Config config = prepareConfig(args);
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.network().setTimeVariantNetwork(true);

		Scenario scenario = prepareScenario(config);

		Controler controler = prepareControler(scenario);

		controler.run();
	}

	static Controler prepareControler(Scenario scenario) {
		Controler controler = RunMelbourne_new.prepareControler( scenario ) ;
		// drt + dvrp module
		controler.addOverridingModule(new MultiModeDrtModule());
		controler.addOverridingModule(new DvrpModule());
		controler.configureQSimComponents(DvrpQSimComponents.activateAllModes(MultiModeDrtConfigGroup.get(controler.getConfig())));
		/*controler.addOverridingModule(new DrtFareModule());*/

	/*	MelbournePlanScoringFunctionFactory initialPlanScoringFuctionFactory = new MelbournePlanScoringFunctionFactory(controler.getScenario());
		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				this.bindScoringFunctionFactory().toInstance(initialPlanScoringFuctionFactory);
			}
		});*/
		Set<String> allowed_mode;

		for ( Link link : scenario.getNetwork().getLinks().values() ){
			double speed=link.getFreespeed();
			allowed_mode= link.getAllowedModes();
			double num_lane = link.getNumberOfLanes();
			double main_capacity = link.getCapacity();
			double HOV_capacity = 0;

			double new_capacity = 0;
			if (num_lane > 1)   {

				HOV_capacity = main_capacity / num_lane;
				new_capacity = main_capacity- (main_capacity / num_lane) ;

			}  else if (num_lane == 1) {
				new_capacity = main_capacity/2;
				HOV_capacity=new_capacity;
				/* System.out.println(linkId + "  this is  one lane");*/

			}
			if (!allowed_mode.contains("ridesourcing") ){
				{
					NetworkChangeEvent event = new NetworkChangeEvent(0*3600.);
					event.setFreespeedChange(new NetworkChangeEvent.ChangeValue( NetworkChangeEvent.ChangeType.ABSOLUTE_IN_SI_UNITS,  0 ));
					event.setFlowCapacityChange(new NetworkChangeEvent.ChangeValue( NetworkChangeEvent.ChangeType.ABSOLUTE_IN_SI_UNITS,  0 ));
					System.out.println(link.getCapacity());
				}

				{
					NetworkChangeEvent event = new NetworkChangeEvent(7.*3600.);
					event.setFreespeedChange(new NetworkChangeEvent.ChangeValue( NetworkChangeEvent.ChangeType.ABSOLUTE_IN_SI_UNITS, speed));
					event.setFlowCapacityChange(new NetworkChangeEvent.ChangeValue( NetworkChangeEvent.ChangeType.ABSOLUTE_IN_SI_UNITS,  HOV_capacity ));
				}

				{
					NetworkChangeEvent event = new NetworkChangeEvent(17.*3600.);
					event.setFreespeedChange(new NetworkChangeEvent.ChangeValue( NetworkChangeEvent.ChangeType.ABSOLUTE_IN_SI_UNITS,  0 ));
					event.setFlowCapacityChange(new NetworkChangeEvent.ChangeValue( NetworkChangeEvent.ChangeType.ABSOLUTE_IN_SI_UNITS,  0 ));
				}

			}
			if (allowed_mode.contains("ridesourcing") ){
				{
					NetworkChangeEvent event = new NetworkChangeEvent(7*3600.);
					event.setFlowCapacityChange(new NetworkChangeEvent.ChangeValue( NetworkChangeEvent.ChangeType.ABSOLUTE_IN_SI_UNITS,  new_capacity ));
				}
				{
					NetworkChangeEvent event = new NetworkChangeEvent(17*3600.);
					event.setFlowCapacityChange(new NetworkChangeEvent.ChangeValue( NetworkChangeEvent.ChangeType.ABSOLUTE_IN_SI_UNITS,  main_capacity ));
				}
			}
		}



		return controler;
	}

	static Scenario prepareScenario(Config config) {

		Scenario scenario = RunMelbourne_new.prepareScenario( config );
		RouteFactories routeFactories = scenario.getPopulation().getFactory().getRouteFactories();
		routeFactories.setRouteFactory(DrtRoute.class, new DrtRouteFactory());

		//added section



        // end of added section

		return scenario;
	}

	static Config prepareConfig(String[] args) {
		Config config = RunMelbourne_new.prepareConfig(args, new MultiModeDrtConfigGroup(), new DvrpConfigGroup() ) ;

		DrtConfigs.adjustMultiModeDrtConfig(MultiModeDrtConfigGroup.get(config), config.planCalcScore(), config.plansCalcRoute());




			// === default config start (if no config file provided)




		// === everything from here on applies to _all_ runs, that is, it overrides the base config.





		return config ;


	}

}
