package frc.robot.custom;
import frc.robot.Constants.*;
import frc.robot.subsystems.Drivebase;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.CANSparkMax;
import com.revrobotics.REVLibError;
import com.revrobotics.SparkMaxPIDController;

import static frc.robot.Constants.SparkMaxConsants.*;
import com.revrobotics.SparkMaxAnalogSensor;


public class LunaSparkMax extends CANSparkMax {

    private final Presets preset;
    private final int can_id;
    private RelativeEncoder encoder;
    private SparkMaxAnalogSensor analogSensor;
    private SparkMaxPIDController pid;

    private static final int[] PRIMES = new int[]{0, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109};

    public enum Presets {
        kNone, kDrivebase, kDiggingBelt, kDiggingLeadscrew, kDiggingActuator, kDumping;
    }

    /**
     * Constructs an LunaSparkMax
     * 
     * @param can_id Spark Max's CAN ID
     * @param m_type The type of motor connected to the Spark Max
     * @param preset The Motor Preset this object will utilize
     */
    public LunaSparkMax (int can_id, MotorType m_type, Presets preset) {
        super(can_id, m_type);
        this.preset = preset;
        this.can_id = can_id;
        setMotorProperties();
    }

    /**
     * 
     * @param can_id Spark Max's CAN ID
     * @param m_type The type of motor connected to the Spark Max
     */
    public LunaSparkMax(int can_id, MotorType m_type) {
        super(can_id, m_type);
        this.preset = Presets.kNone;
        this.can_id = can_id;
        setMotorProperties();
    }

    /**
     * Applies default motor properties and overrides as specified in Constants.java
     */
    public void setMotorProperties() {
        this.restoreFactoryDefaults();
        this.setOpenLoopRampRate(DEFAULT_OPEN_LOOP_RAMP_RATE);
        this.setIdleMode(DEFAULT_IDLE_MODE);
        this.setSmartCurrentLimit(DEFAULT_SMART_CURRENT_LIMIT);
        this.setSecondaryCurrentLimit(DEFAULT_SECONDARY_CURRENT_LIMIT);
        /*
        this.setPeriodicFramePeriod(PeriodicFrame.kStatus0, PRIMES[can_id]);
		this.setPeriodicFramePeriod(PeriodicFrame.kStatus1, PRIMES[can_id+10]);
		this.setPeriodicFramePeriod(PeriodicFrame.kStatus2, PRIMES[can_id+10]);
        */
        switch(this.preset) {
            case kDrivebase:
                this.setSmartCurrentLimit(DrivebaseConstants.CURRENT_LIMIT_STALL, DrivebaseConstants.CURRENT_LIMIT_FREE);
                this.setSecondaryCurrentLimit(DrivebaseConstants.SECONDARY_CURRENT_LIMIT);
                this.setOpenLoopRampRate(DrivebaseConstants.RAMP_RATE_SECONDS);
                this.setIdleMode(DrivebaseConstants.DRIVEBASE_IDLE_MODE);
                this.getEncoder();
                break;
            case kDiggingBelt:
                this.setInverted(DiggingConstants.BELT_INVERT);
                this.setSmartCurrentLimit(DiggingConstants.BELT_CURRENT_LIMIT_STALL, DiggingConstants.BELT_CURRENT_LIMIT_FREE);
                this.setSecondaryCurrentLimit(DiggingConstants.BELT_SECNDARY_CURRENT_LIMIT);
                break;
            case kDiggingLeadscrew:
                this.setSmartCurrentLimit(DiggingConstants.LEADSCREW_CURRENT_LIMIT_STALL, DiggingConstants.LEADSCREW_CURRENT_LIMIT_FREE);
                this.setSecondaryCurrentLimit(DiggingConstants.LEADSCREW_SECNDARY_CURRENT_LIMIT);
                this.setInverted(DiggingConstants.LEADSCREW_INVERT);
                this.setIdleMode(DiggingConstants.LEADSCREW_IDLE_MODE);
                break;
            default: 
                break;
        }
        this.getPIDController();
        
        this.set(0);
    }
    
    @Override
    public RelativeEncoder getEncoder() {
        if (this.encoder != null) return this.encoder;
        RelativeEncoder encoder = super.getEncoder();
        
        switch(this.preset){
            case kDrivebase:
                if(DrivebaseConstants.APPLY_VELOCITY_SCALAR) {
                    encoder.setVelocityConversionFactor(MathConstants.RPM_TO_MPS*DrivebaseConstants.DRIVE_WHEEL_RADIUS/DrivebaseConstants.DRIVE_GEARBOX_RATIO);
                    break;
                }
            default: 
                break;
        }
        this.encoder = encoder;
        return encoder;
    }

    public SparkMaxAnalogSensor getAnalogSensor() {
        if (this.analogSensor != null) return this.analogSensor;
        SparkMaxAnalogSensor analogSensor = super.getAnalog(SparkMaxAnalogSensor.Mode.kAbsolute);
    
        this.analogSensor = analogSensor;
        return analogSensor;
    }
    
    /**
     * @return An object for interfacing with SparkMax integrated PID Controller
     */
    @Override
    public SparkMaxPIDController getPIDController() {
        if (this.pid != null) return this.pid;
        SparkMaxPIDController pid = super.getPIDController();

        switch(this.preset) {
            case kDrivebase:
                pid.setSmartMotionMaxVelocity(DrivebaseConstants.MAX_VEL, 0);
                pid.setSmartMotionMaxAccel(DrivebaseConstants.MAX_ACCEL, 0);
                pid.setSmartMotionMinOutputVelocity(DrivebaseConstants.MIN_VEL, 0);
                pid.setSmartMotionAllowedClosedLoopError(DrivebaseConstants.MAX_ERROR, 0);
                pid.setP(DrivebaseConstants.PID_kP);
                pid.setI(DrivebaseConstants.PID_kI);
                pid.setD(DrivebaseConstants.PID_kD);
                pid.setIZone(DrivebaseConstants.PID_kIZ);
                pid.setFF(DrivebaseConstants.PID_kFF);
                pid.setOutputRange(DrivebaseConstants.MIN_OUTPUT, DrivebaseConstants.MAX_OUTPUT);
                break;
            case kDiggingBelt:
                pid.setSmartMotionMaxVelocity(DiggingConstants.BELT_MAX_VEL, 0);
                pid.setSmartMotionMaxAccel(DiggingConstants.BELT_MAX_ACCEL, 0);
                pid.setSmartMotionMinOutputVelocity(DiggingConstants.BELT_MIN_VEL, 0);
                pid.setSmartMotionAllowedClosedLoopError(DiggingConstants.BELT_MAX_ERROR, 0);
                pid.setP(DiggingConstants.BELT_kP);
                pid.setI(DiggingConstants.BELT_kI);
                pid.setD(DiggingConstants.BELT_kD);
                pid.setIZone(DiggingConstants.BELT_kIZ);
                pid.setFF(DiggingConstants.BELT_kFF);
                pid.setOutputRange(DiggingConstants.BELT_MIN_OUTPUT, DiggingConstants.BELT_MAX_OUTPUT);
				break;
            case kDiggingLeadscrew:
                pid.setSmartMotionMaxVelocity(DiggingConstants.LEADSCREW_MAX_VEL, 0);
                pid.setSmartMotionMaxAccel(DiggingConstants.LEADSCREW_MAX_ACCEL, 0);
                pid.setSmartMotionMinOutputVelocity(DiggingConstants.LEADSCREW_MIN_VEL, 0);
                pid.setSmartMotionAllowedClosedLoopError(DiggingConstants.LEADSCREW_MAX_ERROR, 0);
                pid.setP(DiggingConstants.LEADSCREW_kP);
                pid.setI(DiggingConstants.LEADSCREW_kI);
                pid.setD(DiggingConstants.LEADSCREW_kD);
                pid.setIZone(DiggingConstants.LEADSCREW_kIZ);
                pid.setFF(DiggingConstants.LEADSCREW_kFF);
				pid.setOutputRange(DiggingConstants.LEADSCREW_MIN_OUTPUT, DiggingConstants.LEADSCREW_MAX_OUTPUT);
				break;
            case kDiggingActuator:
                pid.setP(DiggingConstants.DIGGING_LINEAR_kP);
                pid.setI(DiggingConstants.DIGGING_LINEAR_kI);
                pid.setD(DiggingConstants.DIGGING_LINEAR_kD);
                pid.setIZone(DiggingConstants.DIGGING_LINEAR_kIZ);
                pid.setFF(DiggingConstants.DIGGING_LINEAR_kFF);
                pid.setOutputRange(-1, 1);
            default: 
                break;
        }
        this.pid = pid;
        return pid;
    }
    
    
    @Override
    public REVLibError follow (final CANSparkMax leader) {
        return follow(leader, false);
    }
    
    @Override
    public REVLibError follow (final CANSparkMax leader, boolean invert) {
        this.set(0);
        return super.follow(leader, invert);
    }
}