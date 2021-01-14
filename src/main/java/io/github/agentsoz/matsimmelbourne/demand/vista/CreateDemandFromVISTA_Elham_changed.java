package io.github.agentsoz.matsimmelbourne.demand.vista;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import io.github.agentsoz.matsimmelbourne.utils.MMUtils;
import org.apache.log4j.Logger;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup.ModeRoutingParams;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * This is probably chain based
 * 
 * @author (of documentation) kainagel
 *
 */
final class CreateDemandFromVISTA_Elham_changed<csvFormat> {
	private static final String pusPersonsFile = "data/p_final_Mel.csv" ;
	private static final String pusTripsFile = "data/t_final_Mel.csv" ;

	private static final String zonesFile = "data/SA1_2011_AUST.shp";

	private static final Logger log = Logger.getLogger( CreateDemandFromVISTA_Elham_changed.class ) ;
	private static int counter = 0;



	public static Coord createRandomCoordinateInCcdZone(Random rnd, Map<String, SimpleFeature> featureMap,
														String SA1, Record record, CoordinateTransformation ct) {



		// get corresponding feature:

		SimpleFeature ft = featureMap.get(SA1) ;



		if ( ft==null ) {
			log.error("unknown SA1=" + SA1 ); // yyyyyy look at this again
			log.error( record.toString() );
			double xmin = 271704. ; double xmax = 421000. ;
			double xx = xmin + rnd.nextDouble()*(xmax-xmin) ;
			double ymin = 5784843. ; double ymax = 5866000. ;
			double yy =ymin + rnd.nextDouble()*(ymax-ymin) ;
			counter++;
			System.out.println(counter);
			return CoordUtils.createCoord( xx, yy) ;


//			return CoordUtils.createCoord(271704., 5784843. ) ; // dummy coordinate; should be around Geelong.  kai, nov'17
		}


		// get random coordinate in feature:
		Point point = MMUtils.getRandomPointInFeature(rnd, ft) ;

		Coord coordInOrigCRS = CoordUtils.createCoord( point.getX(), point.getY() ) ;

		Coord coordOrigin = ct.transform(coordInOrigCRS) ;
		return coordOrigin;
	}




	public final static class Record {
		// needs to be public, otherwise one gets some incomprehensible exception.  kai, nov'17

		@CsvBindByName private String TRIPID ;
		@CsvBindByName private String PERSID ;
		@CsvBindByName private String ORIGLGA;
		@CsvBindByName private String ORIGSA1;
		@CsvBindByName private String DESTSA1;
		@CsvBindByName private String DESTLGA;
		@CsvBindByName private String ORIGPURP1 ;
		@CsvBindByName private String DESTPURP1 ;
		@CsvBindByName(column="STARTIME") private String trip_start_time ;
		@CsvBindByName private String DEPTIME ;
		@CsvBindByName private String Mode_Group ;
		/*@CsvBindByName private String AGE ;*/
		/*@CsvBindByName private String CARLICENCE ;*/



		@Override public String toString() {
			return this.PERSID

					+ "\t" + this.TRIPID
					+ "\t" + this.ORIGLGA
					+ "\t" + this.ORIGSA1
					+ "\t" + this.ORIGPURP1
					+ "\t" + this.trip_start_time
					+ "\t" + this.Mode_Group
					+ "\t" + this.DESTLGA
					+ "\t" + this.DESTSA1
					+ "\t" + this.DESTPURP1
					+ "\t" + this.DEPTIME;
			       /* + "\t" + this.AGE*/
			        /*+ "\t" + this.CARLICENCE;*/

		}
	}


	private final Scenario scenario;
	private final ArrayList<Id<Person>> activePeople = new ArrayList<>();
	private final CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84,"EPSG:28355");
	// yyyyyy the "from" of this is probably not right; should be GCS_GDA_1994 (EPSG:4283)

	private final Set<String> modes = new TreeSet<>() ;
	private final Set<String> activityTypes = new TreeSet<>() ;

	Random random = new Random(4711) ;

	CreateDemandFromVISTA_Elham_changed() throws IOException {
		this.scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
	}

	public void run() throws IOException {
		this.createPUSPersons();// create all the people and add a plan to each
		this.createPUSPlans();// create the plans according to the trip files (pusTripsFile)
		this.removeNonActivePeople();//remove the people who has no trip
		/*this.matchFirstAndLastAct();*/// add a trip to the end or beginning of the plan to match the first and last activity to have a tour based plan
		this.matchHomeCoord(); //since in adding the activities the coordinates have been randomised in for each destination, agents have different home locations, this method is need to set all the home location to a single one.
		this.populationWriting();

	}

	private void createPUSPersons() {
		/*
		 * For convenience and code readability store population and population factory in a local variable 
		 */
		Population population = this.scenario.getPopulation();   
		PopulationFactory populationFactory = population.getFactory();



		/*
		 * Read the PUS file
		 */
		try ( BufferedReader bufferedReader = new BufferedReader(new FileReader(pusPersonsFile)) ) {
			bufferedReader.readLine(); //skip header

			int index_personId = 0;
			int index_age = 7;
			int index_sex = 82; //when male 1 when woman 0
			int CarAvailability_index= 83;
			int totalvehs_index= 78;
			int num_cars_index= 79;
			int hhinc_index = 80;
			int hhsize_index = 81;

			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String parts[] = line.split(",");
				/*
				 * Create a person and add it to the population
				 */
				Person person = populationFactory.createPerson(Id.create(parts[index_personId].trim(), Person.class));
				person.getAttributes().putAttribute("age", parts[index_age].trim());
				person.getAttributes().putAttribute("sex", parts[index_sex].trim());
				person.getAttributes().putAttribute("hasLicense", parts[CarAvailability_index].trim());
				person.getAttributes().putAttribute("totalVehs", parts[totalvehs_index].trim());
				person.getAttributes().putAttribute("numCars", parts[num_cars_index].trim());
				person.getAttributes().putAttribute("hhIncome", parts[hhinc_index].trim());
				person.getAttributes().putAttribute("hhSize", parts[hhsize_index].trim());

				population.addPerson(person);

				/*
				 * Create a day plan and add it to the person
				 */
				Plan plan = populationFactory.createPlan();
				person.addPlan(plan);
				person.setSelectedPlan(plan);
			}
			bufferedReader.close();
		} // end try
		catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("population done" + "\n" + population);
	} // end of createPUSPersons

	void createPUSPlans() throws IOException {
		// ===

		/*
		 * For convenience and code readability store population and population factory in a local variable 
		 */

		Population population = this.scenario.getPopulation();   
		PopulationFactory pf = population.getFactory();

		// ===



		SimpleFeatureSource fts = ShapeFileReader.readDataFile(zonesFile); //reads the shape file in
		Random rnd = new Random();

		Map<String,SimpleFeature> featureMap = new LinkedHashMap<>() ;

		//Iterator to iterate over the features from the shape file
		try ( SimpleFeatureIterator it = fts.getFeatures().features() ) {
			while (it.hasNext()) {

				// get feature
				SimpleFeature ft = it.next(); //A feature contains a geometry (in this case a polygon) and an arbitrary number

				featureMap.put( (String) ft.getAttribute("SA1_MAIN11") , ft ) ;
			}
			it.close();
		} catch ( Exception ee ) {
			throw new RuntimeException(ee) ;
		}


		// ===

		try ( final FileReader reader = new FileReader(pusTripsFile) ) { 
			final CsvToBeanBuilder<Record> builder = new CsvToBeanBuilder<>(reader)  ;
			builder.withType(Record.class);
			builder.withSeparator(',') ;
			final CsvToBean<Record> reader2 = builder.build();
//			int ii=0 ;
			Id<Person> previousPersonId = null;
			Coord coordOrigin = null ;
			for (Iterator<Record> it = reader2.iterator(); it.hasNext() ; ) {
//				ii++ ; if ( ii>10 ) break ;
				Record record = it.next() ;
				Id<Person> personId = Id.createPersonId(record.PERSID);
				Person person = population.getPersons().get(personId);

				/*person.getAttributes().putAttribute("age", record.AGE);
				person.getAttributes().putAttribute("license", record.CARLICENCE);*/
				Gbl.assertNotNull(person);
				Plan plan = person.getSelectedPlan();
				Gbl.assertNotNull(plan);

				if (!personId.equals(previousPersonId) ) { // a new person

					//add the original place
					coordOrigin = createRandomCoordinateInCcdZone(rnd, featureMap, record.ORIGSA1.trim(), record, ct );

					final String actType = record.ORIGPURP1.trim();
					activityTypes.add(actType) ; 
					Activity activity = pf.createActivityFromCoord( actType , coordOrigin);
					activity.setEndTime( fuzzifiedTimeInSecs(record.trip_start_time) ) ;
					plan.addActivity(activity);

					addLegActPair(pf, rnd, featureMap, record, plan);

				} else { // previous person

					addLegActPair(pf, rnd, featureMap, record, plan);

				}
				previousPersonId = personId;
			}
		} // end of for loop

		System.out.println("plnas done");
	}// end of createPUSPlans
	private void removeNonActivePeople(){
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(pusPersonsFile));
			bufferedReader.readLine(); //skip header

			int index_personId = 0;

			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String parts[] = line.split(",");

				Id<Person> checkPersonId = Id.create(parts[index_personId].trim(), Person.class);

				if (this.scenario.getPopulation().getPersons().get(checkPersonId).getSelectedPlan().getPlanElements().isEmpty())
				{
					this.scenario.getPopulation().getPersons().remove(checkPersonId);
				}
				else
				{
					activePeople.add(checkPersonId);
				}
			}
			bufferedReader.close();
		} // end try
		catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("removing done");
	}
/*	public void matchFirstAndLastAct()
	{
		Population population = this.scenario.getPopulation();
		PopulationFactory populationFactory = population.getFactory();

		for (int i = 0 ; i < activePeople.size() ; i++)
		{
			Person eachPerson = this.scenario.getPopulation().getPersons().get(activePeople.get(i));
			Plan eachPlan = eachPerson.getSelectedPlan();
			int NoOfPlans = eachPlan.getPlanElements().size() - 1;
			Activity currentFirstAct = (Activity) eachPlan.getPlanElements().get(0);
			Activity currentLastAct = (Activity) eachPlan.getPlanElements().get(NoOfPlans) ;
			String currentFirstActType = currentFirstAct.getType().toString().trim();
			String currentLastActType = currentLastAct.getType().toString().trim();
			Leg leg = (Leg) eachPlan.getPlanElements().get(1);

			//to make a different acts
			String actType = "";
			Coord newActCoord = new Coord(0, 0);
			Activity newAct = populationFactory.createActivityFromCoord(actType, newActCoord);


			if (!(currentFirstActType.equals(currentLastActType)))
			{
				//if the first act is home make the last act home too
				if (currentFirstActType.equals("At or Go Home"))
				{
					actType = currentFirstAct.getType();
					newActCoord = new Coord(currentFirstAct.getCoord().getX(), currentFirstAct.getCoord().getY());
					newAct = populationFactory.createActivityFromCoord(actType, newActCoord);

					Activity secondLast = (Activity) eachPlan.getPlanElements().get(NoOfPlans - 2);
					String secondLastActType = secondLast.getType().toString().trim();


					currentLastAct.setEndTime(secondLast.getEndTime() + 18000);
					if (currentLastAct.getEndTime() > 129600)
					{
						System.out.println( eachPerson.getId());
						currentLastAct.setEndTime(129600);
					}

					eachPlan.getPlanElements().add(populationFactory.createLeg(leg.getMode()));
					eachPlan.getPlanElements().add(newAct);
				}

				// match the first act with last
				else
				{
					actType = currentLastAct.getType();
					newActCoord = new Coord(currentLastAct.getCoord().getX(), currentLastAct.getCoord().getY());
					newAct = populationFactory.createActivityFromCoord(actType, newActCoord);
					eachPlan.getPlanElements().add(0, newAct);
					eachPlan.getPlanElements().add(1, populationFactory.createLeg(leg.getMode()));

					Activity newFirstAct = (Activity) eachPlan.getPlanElements().get(0);
					Activity timeRefAct = (Activity) eachPlan.getPlanElements().get(2);
					String timeRefActType = timeRefAct.getType().toString().trim();


					newFirstAct.setEndTime(timeRefAct.getEndTime() - 18000);

					//if after the process start time of a trip is minus, it will be set to 04:00am which is the earliest travel in vista trips
					if (newFirstAct.getEndTime() < 0)
					{
						System.out.println( eachPerson.getId());
						newFirstAct.setEndTime(14400);
					}
				}
			}
		}
		System.out.println("matching done");

	}*/

	public void matchHomeCoord()
	{
		Population population = this.scenario.getPopulation();
		PopulationFactory populationFactory = population.getFactory();

		for (int i = 0 ; i < activePeople.size() ; i++)
		{
			Coord homeCoord = new Coord(0,0);
			Person eachPerson = this.scenario.getPopulation().getPersons().get(activePeople.get(i));
			Plan eachPlan = eachPerson.getSelectedPlan();
			int NoOfPlans = eachPlan.getPlanElements().size();
			/*System.out.println(NoOfPlans + " ID is " + activePeople.get(i));*/
			for (int j = 0 ; j < NoOfPlans ; j+=2)
			{
				Activity activityToCheck = (Activity) eachPlan.getPlanElements().get(j);
				String activityToCheckType = activityToCheck.getType().toString().trim();
				/*System.out.println(activityToCheckType + " coor is " + activityToCheck.getCoord());*/
				if (activityToCheckType.equals("At or Go Home"))
				{
					homeCoord = activityToCheck.getCoord();
				    break;
				}
			}

			for (int k = 0 ; k < NoOfPlans ; k+=2)
			{
				Activity activityToCheck = (Activity) eachPlan.getPlanElements().get(k);
				String activityToCheckType = activityToCheck.getType().toString().trim();
				if (activityToCheckType.equals("At or Go Home"))
				{
					activityToCheck.setCoord(homeCoord);
				}

			}
			 

		}
	}


	private double fuzzifiedTimeInSecs(final String time) {
		return 60. * Double.parseDouble( time ) + (random.nextDouble()-0.5)*1800.;
	}

	private void addLegActPair(PopulationFactory pf, Random rnd, Map<String, SimpleFeature> featureMap,
							   Record record, Plan plan) {
		// and the first travel leg
		String mode = record.Mode_Group;
		if ( "Vehicle Driver".equals(mode) || "Other".equals(mode) ) {
			mode = TransportMode.car ; // not necessary, but easier to use the matsim default.  kai, nov'17
		}
		else if ("Bus".equals(mode) || "Tram".equals(mode) || "Train".equals(mode)){
			mode = TransportMode.pt ;
		}
		else if ("Bicycle".equals(mode)){
			mode = TransportMode.bike ;
		}
		else if ("Walking".equals(mode)){
			mode = TransportMode.walk ;
		}
		else if ("Vehicle Passenger".equals(mode)){
			mode = TransportMode.ride;
		}
		modes.add(mode) ;
		plan.addLeg(pf.createLeg(mode));

		// add the destination
		Coord coordDestination = createRandomCoordinateInCcdZone(rnd, featureMap, record.DESTSA1.trim(), record, ct );
		String activityType = record.DESTPURP1.trim();
		activityTypes.add(activityType) ;
		Activity activity1 = pf.createActivityFromCoord(activityType, coordDestination);
		if ( ! ( record.DEPTIME.equals("N/A") ) ) {
			activity1.setEndTime( fuzzifiedTimeInSecs( record.DEPTIME ) ) ;
			// otherwise, it should be the last activity, in which case we just don't set it, which is the preferred matsim 
			// convention anyways. kai, nov'17
		}
		plan.addActivity(activity1);
	}
	
	void populationWriting(){
		PopulationWriter populationWriter = new PopulationWriter(this.scenario.getPopulation(), this.scenario.getNetwork());
		populationWriter.write("plansCoM.xml.gz");
		
		Config config = ConfigUtils.createConfig() ;
		
		for ( String type : activityTypes ) {
			ActivityParams params = new ActivityParams(type) ;
			config.planCalcScore().addActivityParams(params);
		}
		List<String> networkModes = Arrays.asList(new String [] {TransportMode.car}) ;
		for ( String mode : modes ) {
			ModeParams params = new ModeParams(mode) ;
			config.planCalcScore().addModeParams(params);

			if ( !networkModes.contains(mode) ) {
				ModeRoutingParams pars = new ModeRoutingParams( mode ) ;
				pars.setTeleportedModeSpeed(20.); // m/s
				config.plansCalcRoute().addModeRoutingParams(pars);
			}
		}
		
		ConfigUtils.writeMinimalConfig(config,"config.xml");
		
		System.out.println("writing done");
	}
	
	public static void main(String[] args) throws IOException {

		CreateDemandFromVISTA_Elham_changed createDemand = new CreateDemandFromVISTA_Elham_changed();
		createDemand.run();
		System.out.println("DONE");

	}



}
