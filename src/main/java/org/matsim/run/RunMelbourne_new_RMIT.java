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
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType;
import org.matsim.core.config.groups.QSimConfigGroup.TrafficDynamics;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.Arrays;

/**
 * @author nagel
 *
 */
public class RunMelbourne_new_RMIT {
	private static final Logger log = Logger.getLogger(RunMelbourne_new_RMIT.class) ;

	public static void main(String[] args) {

		Config config = prepareConfig(args);
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		
		Scenario scenario = prepareScenario(config);

		Controler controler = prepareControler(scenario);
		
		controler.run();
	}
	
	static Controler prepareControler(Scenario scenario) {
		final Controler controler = new Controler(scenario);
		
		return controler;
	}
	
	static Scenario prepareScenario(Config config) {
		
		final Scenario scenario = ScenarioUtils.loadScenario(config);
		

		return scenario;
	}
	static Config prepareConfig(String[] args) {
		Config config = null;

		if (args != null && args.length >= 1) {
			config = ConfigUtils.loadConfig(args[0]);
		} else {
			// === default config start (if no config file provided)

			config = ConfigUtils.loadConfig("scenarios/2017-11-scenario-by-kai-from-vista/updated_config_without_drt_rmit.xml");
			config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);

		}

		config.controler().setRoutingAlgorithmType( RoutingAlgorithmType.FastAStarLandmarks);
		config.plansCalcRoute().setInsertingAccessEgressWalk(true);
		config.qsim().setTrafficDynamics(TrafficDynamics.kinematicWaves);
		ConfigUtils.loadConfig(config,"overridingConfig.xml");
		return config;
	}


}
