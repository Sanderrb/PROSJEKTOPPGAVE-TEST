package edu.ntnu.stud;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TrainDispatchSystem {
    private List<TrainDeparture> departures;
    private LocalTime currentTime;

    public TrainDispatchSystem() {
        departures = new ArrayList<>();
        this.currentTime = LocalTime.MIDNIGHT;
    }
    
    public String getCurrentTime() {
        return currentTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    // Legger til en ny togavgang
    public boolean addTrainDeparture(TrainDeparture departure) {
        if (departures.stream().anyMatch(d -> d.getTrainNumber().equals(departure.getTrainNumber()))) {
            return false; // Tognummeret finnes allerede, kan dermed ikke sette inn
        }
        departures.add(departure);
        departures.sort(Comparator.comparing(TrainDeparture::getDepartureTime));
        return true;
    }

    // Setter spor for en togavgang
    public boolean setTrackForDeparture(String trainNumber, int track) {
        for (TrainDeparture departure : departures) {
            if (departure.getTrainNumber().equals(trainNumber)) {
                departure.setTrack(track);
                return true;
            }
        }
        return false;
    }

    // Legger til forsinkelse for en togavgang
    public boolean addDelayToDeparture(String trainNumber, String delay) {
        for (TrainDeparture departure : departures) {
            if (departure.getTrainNumber().equals(trainNumber)) {
                departure.setDelay(delay);
                return true;
            }
        }
        return false;
    }

    // Søker etter en togavgang basert på tognummer
    public TrainDeparture searchByTrainNumber(String trainNumber) {
        for (TrainDeparture departure : departures) {
            if (departure.getTrainNumber().equals(trainNumber)) {
                return departure;
            }
        }
        return null;
    }

    // Søker etter togavganger basert på destinasjon
    public List<TrainDeparture> searchByDestination(String destination) {
        return departures.stream()
                .filter(departure -> departure.getDestination().equalsIgnoreCase(destination))
                .collect(Collectors.toList());
    }

    // Oppdaterer nåværende tid
    public boolean setCurrentTime(String newTimeStr) {
        LocalTime newTime = LocalTime.parse(newTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        if (newTime.isAfter(currentTime) || newTime.equals(currentTime)) {
            currentTime = newTime;
            return true;
        }
        return false;
    }

    // Viser alle togavganger, sortert etter avreisetidspunkt
    public void displayDepartures() {
        System.out.println("+------------+------+-------+--------------+------------+-------+");
        System.out.println("| Avgangstid | Linje | TogNr | Destinasjon  | Forsinkelse | Spor|");
        System.out.println("+------------+------+-------+--------------+------------+-------+");
        for (TrainDeparture departure : departures) {
            System.out.println(departure.getFormattedDepartureInfo());
        }
        System.out.println("+------------+------+-------+--------------+------------+-------+");
    }
    
    

    // Viser togavganger som er planlagt etter nåværende tidspunkt
    public void displayDeparturesAfterCurrentTime() {
        LocalTime current = currentTime;

        List<TrainDeparture> filteredDepartures = departures.stream()
                .filter(departure -> LocalTime.parse(departure.getDepartureTime()).isAfter(current))
                .sorted(Comparator.comparing(TrainDeparture::getDepartureTime))
                .collect(Collectors.toList());

        if (filteredDepartures.isEmpty()) {
            System.out.println("Ingen avganger etter nåværende tidspunkt.");
        } else {
            filteredDepartures.forEach(TrainDeparture::displayInformation);
        }

    }
    public void removeDeparturesBefore(String time) {
        LocalTime cutoff = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));

        departures.removeIf(departure -> {
            LocalTime departureTime = LocalTime.parse(departure.getDepartureTime(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime adjustedDepartureTime = departureTime;

            // Håndterer forsinkelsen
            if (!"00:00".equals(departure.getDelay()) && departure.getDelay() != null) {
                String[] delayParts = departure.getDelay().split(":");
                int delayHours = Integer.parseInt(delayParts[0]);
                int delayMinutes = Integer.parseInt(delayParts[1]);
                adjustedDepartureTime = departureTime.plusHours(delayHours).plusMinutes(delayMinutes);
            }

            // Sjekker om den justerte avgangstiden er før cutoff
            return adjustedDepartureTime.isBefore(cutoff);
        });
    }
    
    public void initializeTestData() {
        addTrainDeparture(new TrainDeparture("08:30", "L1", "1001", "Oslo", 1));
        addTrainDeparture(new TrainDeparture("09:45", "L2", "1002", "Bergen", 2));
        addTrainDeparture(new TrainDeparture("10:15", "L3", "1003", "Trondheim", -1));
        addTrainDeparture(new TrainDeparture("11:00", "L1", "1004", "Oslo", 3));
        addTrainDeparture(new TrainDeparture("12:30", "L2", "1005", "Bergen", -1));

        // Sett forsinkelse på en av avgangene. Som test
        addDelayToDeparture("1003", "00:30");
    }
}

