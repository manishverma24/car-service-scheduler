package com.carservice.controller;

import com.carservice.exception.UserHandledException;
import com.carservice.model.Appointment;
import com.carservice.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operators")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/{operatorId}/appointments")
    public ResponseEntity<Appointment> bookAppointment(@PathVariable String operatorId, @RequestBody Appointment appointment)
            throws UserHandledException {
        Appointment appointmentStatus = appointmentService.bookAppointment(operatorId, appointment);
        return ResponseEntity.ok(appointmentStatus);
    }

    @PutMapping("/{operatorId}/appointments/{appointmentId}")
    public ResponseEntity<Object> rescheduleAppointment(@PathVariable String operatorId, @PathVariable String appointmentId,
                                                        @RequestBody Appointment newAppointment) throws UserHandledException {
        Appointment appointmentStatus  = appointmentService.rescheduleAppointment(operatorId, appointmentId, newAppointment);
        return ResponseEntity.ok(appointmentStatus);
    }

    @DeleteMapping("/{operatorId}/appointments/{appointmentId}")
    public ResponseEntity<String> cancelAppointment(@PathVariable String operatorId, @PathVariable String appointmentId)
            throws UserHandledException {
        appointmentService.cancelAppointment(operatorId, appointmentId);
        return ResponseEntity.ok("Appointment cancelled successfully");
    }

    @GetMapping("/{operatorId}/appointments")
    public ResponseEntity<List<Appointment>> getAllAppointments(@PathVariable String operatorId) throws UserHandledException {
        List<Appointment> appointments = appointmentService.getAllAppointments(operatorId);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/{operatorId}/open-slots")
    public ResponseEntity<List<String>> getOpenSlots(@PathVariable String operatorId) throws UserHandledException {
        List<String> openSlots = appointmentService.getOpenSlots(operatorId);
        return ResponseEntity.ok(openSlots);
    }
}
