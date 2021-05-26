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
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType;
import org.matsim.core.config.groups.QSimConfigGroup.TrafficDynamics;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.network.NetworkChangeEvent;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.CollectionUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

/**
 * @author nagel
 *
 */
public class RunMelbourne_TimeDependentNetwork {
	private static final Logger log = Logger.getLogger(RunMelbourne_TimeDependentNetwork.class) ;

	public static void main(String[] args) {
		for (String arg : args) {
			log.info( arg );
		}

		if ( args.length==0 ) {
			args = new String[] {"scenarios/2017-11-scenario-by-kai-from-vista/updated_config_without_drt.xml"}  ;
		}
		// yyyyyy increase memory!

		Config config = prepareConfig(args);
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		
		Scenario scenario = prepareScenario(config);

		Controler controler = prepareControler(scenario);
		
		controler.run();
	}
	
	static Controler prepareControler(Scenario scenario) {
		final Controler controler = new Controler(scenario);
		MelbournePlanScoringFunctionFactory initialPlanScoringFuctionFactory = new MelbournePlanScoringFunctionFactory(controler.getScenario());
		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				this.bindScoringFunctionFactory().toInstance(initialPlanScoringFuctionFactory);
			}
		});

		/*controler.addOverridingModule( new AbstractModule() {
			@Override
			public void install() {
				addTravelTimeBinding( TransportMode.ride ).to( networkTravelTime() );
				addTravelDisutilityFactoryBinding( TransportMode.ride ).to( carTravelDisutilityFactoryKey() );
			}
		} );*/


		
		return controler;
	}
	
	static Scenario prepareScenario(Config config) {
		
		final Scenario scenario = ScenarioUtils.loadScenario(config);


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





		/*System.out.print(matrix[1][0]);*/
		/*System.out.print(matrix.length);*/

		
        //
		return scenario;
	}
	public static Config prepareConfig(String [] args, ConfigGroup... customModules) {


		String[] typedArgs = Arrays.copyOfRange( args, 1, args.length );


		ConfigGroup[] customModulesAll = new ConfigGroup[customModules.length ];

		int counter = 0;
		for (ConfigGroup customModule : customModules) {
			customModulesAll[counter] = customModule;
			counter++;
		}



		final Config config = ConfigUtils.loadConfig( args[ 0 ], customModulesAll );
		//added part
		config.network().setTimeVariantNetwork(true);
		//


		config.controler().setRoutingAlgorithmType( RoutingAlgorithmType.FastAStarLandmarks);


	/*	config.plansCalcRoute().setInsertingAccessEgressWalk(true);*/

		config.qsim().setTrafficDynamics(TrafficDynamics.kinematicWaves);
		/*config.plansCalcRoute().removeModeRoutingParams(TransportMode.ride);*/


		ConfigUtils.loadConfig(config,"overridingConfig.xml");

		return config;
	}
	
}
