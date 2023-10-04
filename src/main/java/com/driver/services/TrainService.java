package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;



    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        Train train = new Train();
        train.setDepartureTime(trainEntryDto.getDepartureTime());

        // set train route
        String route = "";
        for(Station station: trainEntryDto.getStationRoute()){
            route += "," + station.toString();
        }
        train.setRoute(route);
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        Train savedTrain = trainRepository.save(train);
        Integer id = savedTrain.getTrainId();
        return id;
    }
    private Integer getIndex(String arr[], String s){
        for(int i=0;i<arr.length;i++){
            if(arr[i].equals(s)){
                return i;
            }
        }
        return -1;
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
        Optional<Train> trainOptional = trainRepository.findById(seatAvailabilityEntryDto.getTrainId());
        if(trainOptional.get()==null){
            throw new RuntimeException("Train Not Found");
        }

        Train train = trainOptional.get();
        String [] stations = train.getRoute().split(",");

        int bookedCount = 0;
        for(Ticket ticket: train.getBookedTickets()){
            Integer fromStationIndex = getIndex(stations, ticket.getFromStation().toString());
            Integer toStationIndex = getIndex(stations, ticket.getToStation().toString());

            Integer checkFromStation = getIndex(stations,seatAvailabilityEntryDto.getFromStation().toString());
            Integer checkToStation = getIndex(stations,seatAvailabilityEntryDto.getToStation().toString());

            if(toStationIndex<checkFromStation){
                continue; // getting out before me
            }
            if(fromStationIndex> checkToStation){
                continue; // getting in after me
            }
            bookedCount++;
        }


        return train.getNoOfSeats()- bookedCount;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        Optional<Train> trainOptional = trainRepository.findById(trainId);
        if(trainOptional.get()==null){
            throw new RuntimeException("Train Not Found");
        }

        Train train = trainOptional.get();

        String [] stations = train.getRoute().split(",");

        // check if the train goes to correct stations

        boolean stationFound = false;
        for(String routeStation: stations) {

            if (routeStation.equals(station.toString())){
                stationFound = true;
            }
        }

        if(!stationFound){
            throw new Exception("Train is not passing from this station");
        }
        Integer count  =0 ;
        for(Ticket ticket: train.getBookedTickets()){
            if(ticket.getFromStation().equals(station) || ticket.getToStation().equals(station)){
                count+= ticket.getPassengersList().size();
            }
        }
        return count;

    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Optional<Train> trainOptional = trainRepository.findById(trainId);
        if(trainOptional.get()==null){
            throw new RuntimeException("Train Not Found");
        }

        Train train = trainOptional.get();

        Integer maxAge = 0;
        for(Ticket ticket: train.getBookedTickets()){

            for(Passenger passenger: ticket.getPassengersList()){
                if(passenger.getAge()> maxAge){
                    maxAge = passenger.getAge();
                }
            }
        }
        return maxAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.
        List<Train> trainList = trainRepository.findAll();
        List<Integer> trainIdList = new ArrayList<>();
        for(Train train: trainList){
            String [] stations  = train.getRoute().split(",");
            Integer stationIndex = -1;
            for(int i=0;i<stations.length;i++){
                if(stations[i].equals(station.toString())){
                    stationIndex = i;
                    break;
                }
            }
            if(stationIndex==-1){
                continue;
            }
            LocalTime timeReached = train.getDepartureTime().plusHours(stationIndex);
            if((timeReached.isAfter(startTime) || timeReached.equals(startTime)) && (timeReached.isBefore(endTime) || timeReached.equals(endTime))){
                trainIdList.add(train.getTrainId());
            }
        }

        return trainIdList;
    }

}
