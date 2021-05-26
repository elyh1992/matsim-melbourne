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

/*
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.av.robotaxi.fares.drt.DrtFareModule;
import org.matsim.contrib.av.robotaxi.fares.drt.DrtFaresConfigGroup;
import org.matsim.contrib.drt.run.DrtControlerCreator;
import org.matsim.contrib.drt.run.MultiModeDrtConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType;
import org.matsim.core.config.groups.QSimConfigGroup.TrafficDynamics;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vis.otfvis.OTFVisConfigGroup;


*/
/**
 * @author nagel
 *
 *//*

public class RunMelbourne_drt {
	private static final String drt_CONFIG = "scenarios/2017-11-scenario-by-kai-from-vista/config_drt.xml";
	public static void run(Config config, boolean otfvis) {

		Controler controler = DrtControlerCreator.createControlerWithSingleModeDrt(config, otfvis);



		controler.addOverridingModule(new DrtFareModule());
		controler.run();
	}

	public static void main(String[] args) {

		Config config = ConfigUtils.loadConfig(drt_CONFIG, new MultiModeDrtConfigGroup(),
				new DvrpConfigGroup(),
				new OTFVisConfigGroup(), new DrtFaresConfigGroup());

		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setRoutingAlgorithmType( RoutingAlgorithmType.FastAStarLandmarks);
		config.qsim().setTrafficDynamics(TrafficDynamics.kinematicWaves);
		config.plansCalcRoute().setInsertingAccessEgressWalk(true);


		run(config, false);
	}

	
}
*/
