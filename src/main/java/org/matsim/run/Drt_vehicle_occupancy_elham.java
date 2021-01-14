package org.matsim.run;

import com.google.common.collect.UnmodifiableIterator;
import java.util.Iterator;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.matsim.contrib.drt.schedule.DrtStopTask;
import org.matsim.contrib.drt.schedule.DrtTask;
import org.matsim.contrib.dvrp.fleet.DvrpVehicle;
import org.matsim.contrib.dvrp.fleet.Fleet;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.util.TimeDiscretizer;
import org.matsim.contrib.dvrp.util.TimeDiscretizer.Type;

public class Drt_vehicle_occupancy_elham {
    private Fleet fleet;

    private  TimeDiscretizer timeDiscretizer;
    private  long[] idleVehicleProfileInSeconds;
    private  long[][] vehicleOccupancyProfilesInSeconds;
    private  double[] idleVehicleProfileRelative;
    private  double[][] vehicleOccupancyProfilesRelative;

    public void DrtVehicleOccupancyProfileCalculator(Fleet fleet, int timeInterval) {
        this.fleet = fleet;
        Max maxCapacity = new Max();
        Max maxTime = new Max();
        UnmodifiableIterator var5 = fleet.getVehicles().values().iterator();
        fleet.getVehicles().values().asList();

        while(var5.hasNext()) {
            DvrpVehicle v = (DvrpVehicle)var5.next();
            maxCapacity.increment((double)v.getCapacity());
            maxTime.increment(v.getSchedule().getEndTime());
        }

        int intervalCount = (int)Math.ceil((maxTime.getResult() + 1.0D) / (double)timeInterval);
        this.timeDiscretizer = new TimeDiscretizer(intervalCount * timeInterval, timeInterval, Type.ACYCLIC);
        int occupancyProfilesCount = (int)maxCapacity.getResult() + 1;
        this.vehicleOccupancyProfilesInSeconds = new long[occupancyProfilesCount][this.timeDiscretizer.getIntervalCount()];
        this.idleVehicleProfileInSeconds = new long[this.timeDiscretizer.getIntervalCount()];
        this.vehicleOccupancyProfilesRelative = new double[occupancyProfilesCount][this.timeDiscretizer.getIntervalCount()];
        this.idleVehicleProfileRelative = new double[this.timeDiscretizer.getIntervalCount()];
    }

    public void calculate() {
        UnmodifiableIterator var1 = this.fleet.getVehicles().values().iterator();

        while(var1.hasNext()) {
            DvrpVehicle v = (DvrpVehicle)var1.next();
            this.updateProfiles(v);
        }

        for(int t = 0; t < this.timeDiscretizer.getIntervalCount(); ++t) {
            this.idleVehicleProfileRelative[t] = (double)this.idleVehicleProfileInSeconds[t] / (double)this.timeDiscretizer.getTimeInterval();

            for(int o = 0; o < this.vehicleOccupancyProfilesInSeconds.length; ++o) {
                this.vehicleOccupancyProfilesRelative[o][t] = (double)this.vehicleOccupancyProfilesInSeconds[o][t] / (double)this.timeDiscretizer.getTimeInterval();
            }
        }

    }

    public int getMaxCapacity() {
        return this.vehicleOccupancyProfilesInSeconds.length - 1;
    }

    public double[] getIdleVehicleProfile() {
        return this.idleVehicleProfileRelative;
    }

    public double[][] getVehicleOccupancyProfiles() {
        return this.vehicleOccupancyProfilesRelative;
    }

    public TimeDiscretizer getTimeDiscretizer() {
        return this.timeDiscretizer;
    }

    private void updateProfiles(DvrpVehicle vehicle) {
        int occupancy = 0;
        Iterator var3 = vehicle.getSchedule().getTasks().iterator();

        while(var3.hasNext()) {
            Task t = (Task)var3.next();
            DrtTask drtTask = (DrtTask)t;
            switch(drtTask.getDrtTaskType()) {
                case DRIVE:
                    this.increment(this.vehicleOccupancyProfilesInSeconds[occupancy], drtTask);
                    break;
                case STOP:
                    DrtStopTask stopTask = (DrtStopTask)drtTask;
                    occupancy -= stopTask.getDropoffRequests().size();
                    this.increment(this.vehicleOccupancyProfilesInSeconds[occupancy], drtTask);
                    occupancy += stopTask.getPickupRequests().size();
                    break;
                case STAY:
                    this.increment(this.idleVehicleProfileInSeconds, drtTask);
            }
        }

    }

    private void increment(long[] values, Task task) {
        int timeInterval = this.timeDiscretizer.getTimeInterval();
        int fromIdx = this.timeDiscretizer.getIdx(task.getBeginTime());
        int toIdx = this.timeDiscretizer.getIdx(task.getEndTime());

        for(int i = fromIdx; i < toIdx; ++i) {
            values[i] += (long)timeInterval;
        }

        values[fromIdx] -= (long)((int)task.getBeginTime() % timeInterval);
        values[toIdx] += (long)((int)task.getEndTime() % timeInterval);
    }
}
