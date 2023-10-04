package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
        //And the end return the ticketId that has come from db
        Optional<Train> trainOptional = trainRepository.findById(bookTicketEntryDto.getTrainId());
        if(!trainOptional.isPresent()) {
            throw new Exception("Train Not Found");
        }
        Train train = trainOptional.get();
        // get no of tickets already booked
        int numBooked = 0;
        for(Ticket ticket: train.getBookedTickets()){
            numBooked += ticket.getPassengersList().size();
        }

        if(train.getNoOfSeats()-numBooked < bookTicketEntryDto.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }
        String [] stations = train.getRoute().split(",");

        // check if the train goes to correct stations
        boolean fromStationFound = false;
        boolean toStationFound = false;
        int fare = 0;
        for(String station: stations) {
            if (station.equals(bookTicketEntryDto.getToStation().toString())) {
                toStationFound = true;
            }
            if (station.equals(bookTicketEntryDto.getFromStation().toString())) {
                fromStationFound = true;
            }
            if(fromStationFound && !toStationFound){

                fare+= 300;
            }
        }

        if(!fromStationFound || !toStationFound){
            throw new Exception("Invalid stations");
        }

        Ticket ticket = new Ticket();
        for(Integer passengerId: bookTicketEntryDto.getPassengerIds()){
            Optional<Passenger> passengerOptional = passengerRepository.findById(passengerId);
            if(passengerOptional.get()==null){
                throw new Exception("Passenger not found");
            }
            ticket.getPassengersList().add(passengerOptional.get());
        }
        ticket.setTrain(train);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTotalFare(fare*bookTicketEntryDto.getNoOfSeats());

        Optional<Passenger> bookingPassengerOptional = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId());
        if(bookingPassengerOptional.get()==null){
            throw new Exception("Passenger not found");
        }
        Ticket savedTicket = ticketRepository.save(ticket);

        Passenger bookingPassenger = bookingPassengerOptional.get();
        bookingPassenger.getBookedTickets().add(savedTicket);
        train.getBookedTickets().add(savedTicket);
        trainRepository.save(train);
        passengerRepository.save(bookingPassenger);
        Integer ticketId = savedTicket.getTicketId();



        return ticketId;

    }
}
