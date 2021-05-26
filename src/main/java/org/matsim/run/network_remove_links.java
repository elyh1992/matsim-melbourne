package org.matsim.run;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.utils.collections.CollectionUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class network_remove_links {
   public static void main(String[] args) {
       Network network = NetworkUtils.createNetwork();
       NetworkFactory factory	= network.getFactory();
       /*new MatsimNetworkReader(network).readFile("C:/Users/ehajhashemi/Desktop/Files/MATSim codes/matsim-melbourne/scenarios/2017-11-scenario-by-kai-from-vista/cn.xml");*/
       new MatsimNetworkReader(network).readFile("C:/Users/ehajhashemi/Desktop/Files/MATSim codes/matsim_melbourne_outputs/output_4_fin_drt_HOV/output_network_HOV.xml");



 /*      List<String[]> rowList = new ArrayList<String[]>();
       try (BufferedReader br = new BufferedReader(new FileReader("C:/Users/ehajhashemi/Desktop/links used by ridesplitting_30.csv"))) {
           String line;
           while ((line = br.readLine()) != null) {
               String[] lineItems = line.split(",");
               rowList.add(lineItems);
           }
           br.close();
       }

       catch(Exception e){
           // Handle any I/O problems
       }
       String[][] matrix = new String[rowList.size()][];
       for (int i = 0; i < rowList.size(); i++) {
           String[] row = rowList.get(i);
           matrix[i] = row;
       }*/
       //this step is for fixing RMIT network:
      /* Set<String> allowed_mode;
       for (Link l : network.getLinks().values() ) {
           *//*l.setAllowedModes(CollectionUtils.stringToSet("car,ridesourcing,ridesplitting"));*//*
           allowed_mode= l.getAllowedModes();
           *//*System.out.println(allowed_mode);*//*
           if (!allowed_mode.contains("car")) {
               final Id<Link> id = l.getId();
               System.out.println(id);
               network.removeLink(id);
           }

       }*/
       //fixing RMIT network finishes here

       Set<String> allowed_mode;
       for (Link l : network.getLinks().values() ) {
           /*l.setAllowedModes(CollectionUtils.stringToSet("car,ridesourcing,ridesplitting"));*/
           allowed_mode= l.getAllowedModes();
           /*System.out.println(allowed_mode);*/
           if (!allowed_mode.contains("car")) {
               final Id<Link> id = l.getId();
               System.out.println(id);
               network.removeLink(id);
           }

       }




       /*System.out.print(matrix[1][0]);*/
       /*System.out.print(matrix.length);*/
      /* double counter = 0;

       for (int i = 0  ; i< matrix.length; i++) {
           String name_id = matrix[i][0] + "HOV";
           *//*System.out.println(name_id);*//*
           Id<Link> linkId = Id.create(matrix[i][0].trim(), Link.class);
           *//*Id.create(name_id,Link.class);*//*
           
           Link link = network.getLinks().get(linkId);
           Node node0 = network.getLinks().get(linkId).getFromNode();
           Node node1 = network.getLinks().get(linkId).getToNode();
           double num_lane = network.getLinks().get(linkId).getNumberOfLanes();
           double main_capacity = network.getLinks().get(linkId).getCapacity();
           double HOV_capacity = 0;

           double new_capacity;
           if (num_lane > 1)   {

               HOV_capacity = main_capacity / num_lane;
               new_capacity = main_capacity- (main_capacity / num_lane) ;
               network.getLinks().get(linkId).setCapacity(new_capacity);
               network.getLinks().get(linkId).setNumberOfLanes(num_lane - 1);
           }  else if (num_lane == 1) {
               HOV_capacity = main_capacity/2;
               network.getLinks().get(linkId).setCapacity(HOV_capacity);
              *//* System.out.println(linkId + "  this is  one lane");*//*
               counter++;
           }
           System.out.println(counter);
           double length = network.getLinks().get(linkId).getLength();
           double freeSpeed = network.getLinks().get(linkId).getFreespeed();


           Link link1	=	factory.createLink(Id.create(name_id,Link.class),node0,node1);
           *//*System.out.println(HOV_capacity);*//*
           link1.setCapacity(HOV_capacity);
           link1.setLength(length);
           link1.setFreespeed(freeSpeed);
           link1.setNumberOfLanes(1);
           link1.setAllowedModes(Collections.singleton("ridesplitting"));
           network.addLink(link1);

       }*/



       /*new NetworkWriter(network).write("C:/Users/ehajhashemi/Desktop/Files/MATSim codes/matsim-melbourne/scenarios/2017-11-scenario-by-kai-from-vista/cn.xml");*/
       /*new NetworkWriter(network).write("C:/Users/ehajhashemi/Desktop/Files/MATSim codes/matsim_melbourne_outputs/output_4_fin_drt_HOV/output_network_HOV_only.xml");*/
       new NetworkWriter(network).write("C:/Users/ehajhashemi/Desktop/Files/MATSim codes/matsim_melbourne_outputs/output_4_fin_drt_HOV/output_network_HOV_other.xml");

    }


}


