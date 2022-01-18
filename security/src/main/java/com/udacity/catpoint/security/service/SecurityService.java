package com.udacity.catpoint.security.service;

import com.udacity.catpoint.image.service.ImageService;
import com.udacity.catpoint.security.application.StatusListener;
import com.udacity.catpoint.security.data.AlarmStatus;
import com.udacity.catpoint.security.data.ArmingStatus;
import com.udacity.catpoint.security.data.SecurityRepository;
import com.udacity.catpoint.security.data.Sensor;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Service that receives information about changes to the security system. Responsible for
 * forwarding updates to the repository and making any decisions about changing the system state.
 *
 * This is the class that should contain most of the business logic for our system, and it is the
 * class you will be writing unit tests for.
 */
public class SecurityService {

    private ImageService imageService;
    private SecurityRepository securityRepository;
    private Set<StatusListener> statusListeners = new HashSet<>();

    private boolean catDetect = false;

    public SecurityService(SecurityRepository securityRepository, ImageService imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

    /**
     * Sets the current arming status for the system. Changing the arming status
     * may update both the alarm status.
     * @param armingStatus
     */
    public void setArmingStatus(ArmingStatus armingStatus) {
        switch(armingStatus) {
            case DISARMED -> {
                setAlarmStatus(AlarmStatus.NO_ALARM);
                break;
            }
            case ARMED_HOME, ARMED_AWAY -> {
               // if(catDetect){
                if(isCatDetect()){
                    setAlarmStatus(AlarmStatus.ALARM);
                }
                ConcurrentSkipListSet<Sensor> sensors = new ConcurrentSkipListSet<>(getSensors());
                sensors.forEach(sensor -> changeSensorActivationStatus(sensor, false));
                break;
            }
        }
        securityRepository.setArmingStatus(armingStatus);
        statusListeners.forEach(StatusListener::sensorStatusChanged);
    }

    /**
     * Internal method that handles alarm status changes based on whether
     * the camera currently shows a cat.
     * @param cat True if a cat is detected, otherwise false.
     */
    private void catDetected(Boolean cat) {
        //catDetect=cat;
        setCatDetect(cat);
        if((cat && getArmingStatus() == ArmingStatus.ARMED_HOME) || (cat && getArmingStatus() == ArmingStatus.ARMED_AWAY)){
            setAlarmStatus(AlarmStatus.ALARM);
        } else if(!cat && getAllSensorsState(false)){
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }
        statusListeners.forEach(sl -> sl.catDetected(cat));
    }
    private boolean getAllSensorsState(boolean state){
        return getSensors().stream().allMatch(sensor -> sensor.getActive() == state);
    }
    /**
     * Register the StatusListener for alarm system updates from within the SecurityService.
     * @param statusListener
     */
    public void addStatusListener(StatusListener statusListener) {
        statusListeners.add(statusListener);
    }

    public void removeStatusListener(StatusListener statusListener) {
        statusListeners.remove(statusListener);
    }

    /**
     * Change the alarm status of the system and notify all listeners.
     * @param status
     */
    public void setAlarmStatus(AlarmStatus status) {
        securityRepository.setAlarmStatus(status);
        statusListeners.forEach(sl -> sl.notify(status));
    }

    private void handleSensorActivated() {
        if(securityRepository.getArmingStatus() == ArmingStatus.DISARMED || securityRepository.getAlarmStatus() == AlarmStatus.ALARM) {
            return; //no problem if the system is disarmed
        }
        switch(securityRepository.getAlarmStatus()) {
            case NO_ALARM -> {
                setAlarmStatus(AlarmStatus.PENDING_ALARM);
            }
            case PENDING_ALARM -> {
                setAlarmStatus(AlarmStatus.ALARM);
            }
        }
    }

    /**
     * Internal method for updating the alarm status when a sensor has been deactivated
     */
    private void handleSensorDeactivated() {
        switch (securityRepository.getAlarmStatus()){
            case PENDING_ALARM -> {
                setAlarmStatus(AlarmStatus.NO_ALARM);
            }
            case NO_ALARM ->{
                setAlarmStatus(AlarmStatus.PENDING_ALARM);
            }
        }
    }

    /**
     * Change the activation status for the specified sensor and update alarm status if necessary.
     * @param sensor
     * @param active
     */
    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {
        AlarmStatus alarmStatus = securityRepository.getAlarmStatus();

        if (alarmStatus != AlarmStatus.ALARM) {
            if (active) {
                handleSensorActivated();
            } else if (sensor.getActive()) { //by default sensor.getActive() is false
                handleSensorDeactivated();
            }
        }
        sensor.setActive(active);
        securityRepository.updateSensor(sensor);
    }

    /**
     * Send an image to the SecurityService for processing. The securityService will use its provided
     * ImageService to analyze the image for cats and update the alarm status accordingly.
     * @param currentCameraImage
     */
    public void processImage(BufferedImage currentCameraImage) {
        catDetected(imageService.imageContainsCat(currentCameraImage, 50.0f));
    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }
    public boolean isCatDetect() {
        return catDetect;
    }
    public void setCatDetect(boolean catDetect) {
        this.catDetect = catDetect;
    }

}
