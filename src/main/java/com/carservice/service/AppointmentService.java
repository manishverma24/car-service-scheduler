package com.carservice.service;

import com.carservice.exception.UserHandledException;
import com.carservice.model.Appointment;
import com.carservice.model.TimeSlot;

import java.util.List;

public interface AppointmentService {

    Appointment bookAppointment(String operatorId, Appointment appointment) throws UserHandledException;

    Appointment rescheduleAppointment(String operatorId, String appointmentId, Appointment newAppointment) throws UserHandledException;

    void cancelAppointment(String operatorId, String appointmentId) throws UserHandledException;

    List<Appointment> getAllAppointments(String operatorId) throws UserHandledException;

    List<String> getOpenSlots(String operatorId) throws UserHandledException;
}
