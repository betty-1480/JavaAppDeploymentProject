package com.udacity.catpoint.security.service;

import com.udacity.catpoint.image.service.ImageService;
import com.udacity.catpoint.security.application.StatusListener;
import com.udacity.catpoint.security.data.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @InjectMocks
    SecurityService service; //object of the class under test with all the fake dependencies injected for you to the constructor

    @Mock
    ImageService fakeImageService; //Dependency

    @Mock
    SecurityRepository fakeSecurityRepository; //Dependency

    @Mock
    private StatusListener statusListener;

    private Sensor sensor; // SecurityService methods parameter, no direct dependency

    @BeforeEach
    void init() {
        sensor = new Sensor("Sensor", SensorType.DOOR);
        service.addSensor(sensor);
    }

/* * 1.If alarm is armed and a sensor becomes activated, put the system into pending alarm status
    Class:SecurityService, Method:handleSensorActivated() Line: 85
    Condition Testing:switch(securityRepository.getAlarmStatus()) {
                           case NO_ALARM -> setAlarmStatus(AlarmStatus.PENDING_ALARM);  */
    @Test
    void checkIf_armedAlarmAndActivatedSensor_setAlarmStatsToPENDING_ALARM() {
      Mockito.when(fakeSecurityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
      Mockito.when(fakeSecurityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM); //arrange - we must provide input values / return values to a fake dependency class. Why? because we are not interested in what they do
      service.changeSensorActivationStatus(sensor, true); //act, method under test from the SecurityService.
      // Assertions.assertEquals(AlarmStatus.PENDING_ALARM, service.getAlarmStatus()); assertion won't work because there nothing to Assert as the changeSensorActivationStatus() method's return type is void
      Mockito.verify(fakeSecurityRepository,Mockito.times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM); // Spying was this method called exactly once as part of executing changeSensorActivationStatus()
    }

/* * 2. If alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm.
    *  Class:SecurityService, Method:handleSensorActivated() Line: 85
     Condition Testing:switch(securityRepository.getAlarmStatus()) {
                            case PENDING_ALARM -> setAlarmStatus(AlarmStatus.ALARM); */
   @Test
    void checkIf_alarmArmedAndActivatedSensorAndPendingAlarmSystem_setAlarmStatusToALARM(){
        Mockito.when(fakeSecurityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME); //arrange
        Mockito.when(fakeSecurityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM); //arrange
        service.changeSensorActivationStatus(sensor,true); //act
        Mockito.verify(fakeSecurityRepository,Mockito.times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    /* * 3. If pending alarm and all sensors are inactive, return to no alarm state.
    * Class:SecurityService, Method:handleSensorDeactivated() Line: 98
    * Condition Testing: switch(securityRepository.getAlarmStatus()) {
                            case PENDING_ALARM -> setAlarmStatus(AlarmStatus.NO_ALARM); */
    @Test
        void checkIf_alarmStatusPendingAndSensorsInactive_setAlarmStatusToNO_ALARM(){
        Mockito.when(fakeSecurityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM); // for fake dependencies we must provide the value
        sensor.setActive(true);
        service.changeSensorActivationStatus(sensor,false); // SecurityService class under test
        Mockito.verify(fakeSecurityRepository,Mockito.times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

/* *4. If alarm is active, change in sensor state should not affect the alarm state.
        Question: which statement are we testing here???????????
     * */
    @Test
    void checkIf_activeAlarmAndChangeSensorActivationStatus_noChangeToAlarmState(){
       sensor.setActive(true);
       Mockito.when(fakeSecurityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
       service.changeSensorActivationStatus(sensor,false);
       Mockito.verify(fakeSecurityRepository,Mockito.never()).setAlarmStatus(Mockito.any(AlarmStatus.class));
    }

/* *5. If a sensor is activated while already active and the system is in pending state, change it to alarm state.
    Question: which statement/ condition of SecurityService are we testing here?
    Question: When does the sensor get activated for the first time? SensorPanel.java, Line:36
    Question: When the app starts is the sensor already active? Yes with SensorType: DOOR
    Question: How do you say sensor is already active? It's already been active SecurityServiceTest.java, Line:30
    * */
    @Test
    void checkIf_activateAlreadyActivateSensorAndAlarmStatusPending_setAlarmStateToALARM(){
        sensor.setActive(true);
        Mockito.when(fakeSecurityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM); //arrange - we must provide a return value of the Fake dependency
        service.changeSensorActivationStatus(sensor,true); //act
        Mockito.verify(fakeSecurityRepository,Mockito.times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

/* * 6. If a sensor is deactivated while already inactive, make no changes to the alarm state.
    *  which statement are you testing? Class:SecurityService, Method:handleSensorDeactivated()
    * Condition Testing: else if (sensor.getActive() && !active) {
            handleSensorDeactivated();
        } Line: 113
    * Question: How do you say System is inactive? There is no methods available in the repository or service class??!!
    * Question: How will system deactivate the sensor? using the internal method handleSensorDeactivated()
    * */
/*    @Test - UnnecessaryStubbingException
    void checkIf_deactivatedSensorDeactivatedAgain_noChangesToAlarmState(){
        sensor.setActive(false);
        Mockito.when(fakeSecurityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        service.changeSensorActivationStatus(sensor,false);
        Mockito.verify(fakeSecurityRepository,Mockito.never()).setAlarmStatus(Mockito.any(AlarmStatus.class));
    }*/

/* * 7. If the image service identifies an image containing a cat while the system is armed-home, put the system into alarm status.
       * Which statement are me testing? Class:SecurityService, Method:catDetected()
       * Condition under test: if(cat && getArmingStatus() == ArmingStatus.ARMED_HOME) { Line: 49
            setAlarmStatus(AlarmStatus.ALARM); }
       * * */
    @Test
     void checkIf_armedHomeAndCatIdentified_setAlarmStatusIntoALARM(){
        //Question: How do you check if image contains cat?
        //Class: FakeImageService, Method:  boolean imageContainsCat(BufferedImage image, float confidenceThreshhold)
        Mockito.when(fakeImageService.imageContainsCat(Mockito.any(),Mockito.anyFloat())).thenReturn(true);
        Mockito.when(fakeSecurityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        //Question: What to do to detect a cat? call processImage() method
        service.processImage(Mockito.mock(BufferedImage.class)); //act
        Mockito.verify(fakeSecurityRepository).setAlarmStatus(AlarmStatus.ALARM); //spy the mockSecurityRepository for AlarmStatus
    }

/* * 8. If the image service identifies an image that does not contain a cat, change the status to no alarm as long as the sensors are not active.
    //Question: How will you say Sensors are not active?
    * */
    @Test
    void checkIf_nonCatImageDetectedAndSensorsNotActive_setAlarmStatusToNOALARM(){
        //Question: How will you say image service identifies an image that does not contain a cat?
        Mockito.when(fakeImageService.imageContainsCat(Mockito.any(),Mockito.anyFloat())).thenReturn(false);
        //Question: How will you say Sensors are not active?
        sensor.setActive(false);
        service.processImage(Mockito.mock(BufferedImage.class)); //act
        Mockito.verify(fakeSecurityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);     //assert
    }

/* 9. If the system is disarmed, set the status to no alarm.
    *  */
    @Test
    void checkIf_disarmedSystem_setAlarmStatusToNOALARM(){
        service.setArmingStatus(ArmingStatus.DISARMED);
        Mockito.verify(fakeSecurityRepository,Mockito.times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

/* 10. If the system is armed, reset all sensors to inactive.
    * */
  @ParameterizedTest
  @EnumSource(ArmingStatus.class)
    void checkIf_armedSystem_setAllSensorsToInactive(ArmingStatus armingStatus){
    service.setArmingStatus(armingStatus);
    sensor.setActive(true);
    service.getSensors().forEach(s1-> Assertions.assertEquals(false,s1.getActive()));
    }

/* 11. If the system is armed-home while the camera shows a cat, set the alarm status to alarm.
* */
@Test
    void checkIf_armedHomeAndCatDetected_setAlarmToALARM(){
    Mockito.when(fakeSecurityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
    Mockito.when(fakeImageService.imageContainsCat(Mockito.any(),Mockito.anyFloat())).thenReturn(true);
    service.processImage(Mockito.mock(BufferedImage.class));
    Mockito.verify(fakeSecurityRepository).setAlarmStatus(AlarmStatus.ALARM);
}

    @Test
    public void checking_to_see_if_change_in_sensor_activation_status_deactivate_sensor(){
        fakeSecurityRepository.setAlarmStatus(AlarmStatus.PENDING_ALARM);
        service.changeSensorActivationStatus(sensor, false);
        assert sensor.getActive().equals(false);
        Mockito.verify(fakeSecurityRepository, Mockito.times(1)).setAlarmStatus(Mockito.any(AlarmStatus.class));
    }

    @Test
    public void test_add_and_remove_status_listener(){
        service.addStatusListener(statusListener);
        service.removeStatusListener(statusListener);
    }

    @Test
    public void test_add_and_remove_sensor(){
        fakeSecurityRepository.addSensor(sensor);
        fakeSecurityRepository.removeSensor(sensor);
    }

    @ParameterizedTest
    @EnumSource(ArmingStatus.class)
    public void checking_to_see_if_arming_status_can_run_three_times(ArmingStatus armingStatus){
        fakeSecurityRepository.setArmingStatus(armingStatus);
    }

    @Test
    public void test_to_see_if_sensor_can_be_updated(){
        fakeSecurityRepository.addSensor(sensor);
        fakeSecurityRepository.updateSensor(sensor);
    }

    @Test
    public void if_noCatDetectedAndSensorsNotActive_changeStatusToNOALARM(){
       service.changeSensorActivationStatus(sensor, false);
        Mockito.when(fakeImageService.imageContainsCat(Mockito.any(), Mockito.anyFloat())).thenReturn(false);
        service.processImage(Mockito.mock(BufferedImage.class));
        Mockito.verify(fakeSecurityRepository, Mockito.times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @ParameterizedTest
    @EnumSource(ArmingStatus.class)
    public void if_armedSystem_resetAllSensorsToInactive(ArmingStatus armingStatus){
        fakeSecurityRepository.setArmingStatus(armingStatus);
        sensor.setActive(false);
        fakeSecurityRepository.getSensors().forEach(sensor -> {
            assert Boolean.FALSE.equals(sensor.getActive());
        });
    }

    @Test
    public void check_to_see_if_handSensor_is_activated_when_repository_is_disarmed_and_alarm_should_trigger_handle_sensor_to_deactivate(){

        fakeSecurityRepository.setArmingStatus(ArmingStatus.DISARMED);
        Mockito.when(fakeSecurityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        sensor.setActive(true);
        service.changeSensorActivationStatus(sensor, false);
    }

    @Test
    public void checkIf_armingStatusIsARMEDHOMEandcatDetected_setAlarmStatusToALARM(){
        Mockito.when(fakeSecurityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        Mockito.when(fakeImageService.imageContainsCat(Mockito.any(), Mockito.anyFloat())).thenReturn(true);
        service.processImage(Mockito.eq(Mockito.any(BufferedImage.class)));
        Mockito.verify(fakeSecurityRepository, Mockito.times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

}

