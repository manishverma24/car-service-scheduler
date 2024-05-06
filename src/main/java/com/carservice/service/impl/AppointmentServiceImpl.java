package com.carservice.service.impl;

import com.carservice.exception.UserHandledException;
import com.carservice.model.Appointment;
import com.carservice.model.ServiceOperator;
import com.carservice.repository.ServiceOperatorRepository;
import com.carservice.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private final ServiceOperatorRepository serviceOperatorRepository;

    @Autowired
    public AppointmentServiceImpl(ServiceOperatorRepository serviceOperatorRepository) {
        this.serviceOperatorRepository = serviceOperatorRepository;
    }

    @Override
    public Appointment bookAppointment(String operatorId, Appointment appointment) throws UserHandledException {
        try {
            isRequestedSlotValid(appointment);
            ServiceOperator operator = serviceOperatorRepository.findById(operatorId).orElseThrow(
                    () -> new UserHandledException("No operator found for this operatorId ", HttpStatus.NOT_FOUND)
            );
            if (isSlotAvailable(operator, appointment)) {
                if (!isOverlap(operator, appointment)) {
                    appointment.setId(generateAppointmentId());
                    operator.getAppointments().add(appointment);
                    serviceOperatorRepository.save(operator);
                    return appointment;
                } else {
                    throw new UserHandledException("Requested time slot overlaps with an existing appointment", HttpStatus.BAD_REQUEST);
                }
            } else {
                throw new UserHandledException("Requested time slot is not available", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            logger.error("error occurred while booking the appointment: ", ex);
            throw new UserHandledException("An error occurred while booking the appointment", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Appointment rescheduleAppointment(String operatorId, String appointmentId, Appointment newAppointment) throws UserHandledException {
        try {
            ServiceOperator operator = serviceOperatorRepository.findById(operatorId).orElseThrow(
                    () -> new UserHandledException("No operator found for this operatorId ", HttpStatus.NOT_FOUND)
            );
            Appointment existingAppointment = findAppointment(operator, appointmentId);
            if (isSlotAvailable(operator, newAppointment) && !isOverlap(operator, newAppointment)) {
                existingAppointment.setStartTime(newAppointment.getStartTime());
                existingAppointment.setEndTime(newAppointment.getEndTime());
                serviceOperatorRepository.save(operator);
            }
            return newAppointment;
        } catch (Exception ex) {
            logger.error("error occurred while rescheduling the appointment: ", ex);
            throw new UserHandledException("An error occurred while rescheduling the appointment", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void cancelAppointment(String operatorId, String appointmentId) throws UserHandledException {
        try {
            ServiceOperator operator = serviceOperatorRepository.findById(operatorId).orElseThrow(
                    () -> new UserHandledException("No operator found for this operatorId ", HttpStatus.NOT_FOUND)
            );
            Appointment appointment = findAppointment(operator, appointmentId);
            operator.getAppointments().remove(appointment);
            serviceOperatorRepository.save(operator);
        } catch (Exception ex) {
            logger.error("error occurred while cancelling the appointment", ex);
            throw new UserHandledException("An error occurred while cancelling the appointment", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<Appointment> getAllAppointments(String operatorId) throws UserHandledException {
        ServiceOperator operator = serviceOperatorRepository.findById(operatorId).orElseThrow(
                () -> new UserHandledException("No operator found for this operatorId ", HttpStatus.NOT_FOUND)
        );
        return (operator.getAppointments() != null) ? operator.getAppointments() : Collections.emptyList();
    }

    @Override
    public List<String> getOpenSlots(String operatorId) throws UserHandledException {
        try {
            ServiceOperator operator = serviceOperatorRepository.findById(operatorId).orElseThrow(
                    () -> new UserHandledException("No operator found for this operatorId", HttpStatus.NOT_FOUND)
            );

            List<Appointment> appointments = operator.getAppointments();
            List<String> mergedSlots = new ArrayList<>();
            LocalTime startTime = null;
            LocalTime endTime = null;

            // Iterate over each hour of the day
            for (int i = 0; i < 24; i++) {
                LocalTime currentTime = LocalTime.of(i, 0); // Current hour
                boolean slotOpen = appointments.stream()
                        .noneMatch(appointment -> appointment.getStartTime().equals(currentTime));

                if (slotOpen) {
                    // Start a new time range
                    if (startTime == null)
                        startTime = currentTime;
                    endTime = currentTime.plusHours(1);
                } else {
                    // If the slot is not open and a time range is being tracked, add it to the list
                    if (startTime != null) {
                        mergedSlots.add(formatTimeRange(startTime, endTime));
                        startTime = null; // Reset variables
                        endTime = null;
                    }
                }
            }
            // Add the last time range if it exists
            if (startTime != null) {
                mergedSlots.add(formatTimeRange(startTime, endTime));
            }
            return mergedSlots;
        } catch (Exception ex) {
            logger.error("Error occurred while fetching open slots: ", ex);
            throw new UserHandledException("An error occurred while fetching open slots", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String formatTimeRange(LocalTime startTime, LocalTime endTime) {
        int startHour = startTime.getHour();
        int endHour = endTime.getHour();
        // Check if the end hour is less than the start hour (crosses over to the next day)
        if (endHour < startHour) {
            return startHour + "-24";
        } else {
            return startHour + "-" + (endHour == 0 ? 24 : endHour);
        }
    }

    private Appointment findAppointment(ServiceOperator operator, String appointmentId) throws UserHandledException {
        List<Appointment> appointments = operator.getAppointments();
        for (Appointment appointment : appointments) {
            if (appointment.getId().equals(appointmentId)) {
                return appointment;
            }
        }
        throw new UserHandledException("no existing appointment found for this appointmentId", HttpStatus.NOT_FOUND);
    }

    private String generateAppointmentId() {
        return "APPT_" + UUID.randomUUID();
    }

    private boolean isOverlap(ServiceOperator operator, Appointment appointment) {
        List<Appointment> appointments = operator.getAppointments();
        // If there are no existing appointments, no overlap
        if (appointments.isEmpty()) {
            return false;
        }
        for (Appointment existingAppointment : appointments) {
            // Check for overlap
            if (appointment.getStartTime().isBefore(existingAppointment.getEndTime()) &&
                    appointment.getEndTime().isAfter(existingAppointment.getStartTime())) {
                return true; // Overlap found
            }
        }
        // No overlap found
        return false;
    }

    private boolean isSlotAvailable(ServiceOperator operator, Appointment appointment) {
        List<Appointment> appointmentList = operator.getAppointments();
        if (appointmentList.isEmpty()) {
            return true;
        }
        // Check if the requested time slot overlaps with any existing appointments
        for (Appointment existingAppointment : appointmentList) {
            if (appointment.getStartTime().isBefore(existingAppointment.getEndTime()) &&
                    appointment.getEndTime().isAfter(existingAppointment.getStartTime())) {
                return false; // Slot is not available
            }
        }
        // No overlapping appointments found for the requested time slot, so it's available
        return true;
    }

    private void isRequestedSlotValid(Appointment appointment) throws UserHandledException {
        Duration duration = Duration.between(appointment.getStartTime(), appointment.getEndTime());
        if (duration.getSeconds() != 3600) {
            throw new UserHandledException("Invalid appointment slot duration. The slot must be exactly one hour.",
                    HttpStatus.BAD_REQUEST);
        }
    }

}
