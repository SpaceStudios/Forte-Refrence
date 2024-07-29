// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.shooter;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Constants.ShooterConstants;
import frc.util.LoggedTunableNumber;

/** Add your docs here. */
public class Shooter extends SubsystemBase {
    public ShooterIO io;
    public ShooterIOInputsAutoLogged inputs = new ShooterIOInputsAutoLogged();

    public BeambreakIO feederBeambreak;
    public BeambreakIOInputsAutoLogged feederBeambreakInputs;
    public BeambreakIO shooterBeambreak;
    public BeambreakIOInputsAutoLogged shooterBeambreakInputs;

    private ArmFeedforward pivotFF;
    private SimpleMotorFeedforward feederFF;
    private SimpleMotorFeedforward leftFF;
    private SimpleMotorFeedforward rightFF;

    private LoggedTunableNumber kPPivot = new LoggedTunableNumber("Shooter/kPPivot");

    private LoggedTunableNumber kPFeeder = new LoggedTunableNumber("Shooter/kPFeeder");
    private LoggedTunableNumber kVFeeder = new LoggedTunableNumber("Shooter/kVFeeder");

    private LoggedTunableNumber kPShooter = new LoggedTunableNumber("Shooter/kPShooter");
    private LoggedTunableNumber kSShooter = new LoggedTunableNumber("Shooter/kSShooter");


    public Shooter(ShooterIO io, BeambreakIO feederBeambreak, BeambreakIO shooterBeambreak) {
      this.io = io;

      switch (Constants.currentMode) {
        case REAL:
          kPPivot.initDefault(ShooterConstants.kPPivotReal);

          kPFeeder.initDefault(ShooterConstants.kPFeederReal);
          kVFeeder.initDefault(ShooterConstants.kVFeederReal);

          kPShooter.initDefault(ShooterConstants.kPShooterReal);
          kSShooter.initDefault(ShooterConstants.kSShooterReal);
          break;
        case SIM:
          kPPivot.initDefault(ShooterConstants.kPPivotSim);

          kPFeeder.initDefault(ShooterConstants.kPFeederSim);
          kVFeeder.initDefault(ShooterConstants.kVFeederSim);

          kPShooter.initDefault(ShooterConstants.kPShooterSim);
          kSShooter.initDefault(ShooterConstants.kSShooterSim);
          break;
        case REPLAY:
          kPPivot.initDefault(ShooterConstants.kPPivotReplay);

          kPFeeder.initDefault(ShooterConstants.kPFeederReplay);
          kVFeeder.initDefault(ShooterConstants.kVFeederReplay);

          kPShooter.initDefault(ShooterConstants.kPShooterReplay);
          kSShooter.initDefault(ShooterConstants.kSShooterReplay);
          break;
        default:
          kPPivot.initDefault(0.0);

          kPFeeder.initDefault(0.0);
          kVFeeder.initDefault(0.0);

          kPShooter.initDefault(0.0);
          kSShooter.initDefault(0.0);
      }
        io.setPivotPID(kPPivot.getAsDouble(), 0.0, 0.0);
        pivotFF = new ArmFeedforward(0.0, ShooterConstants.kGPivot, ShooterConstants.kVPivot, ShooterConstants.kAPivot);

        io.setFeederPID(kPFeeder.getAsDouble(), 0.0, 0.0);
        feederFF = new SimpleMotorFeedforward(0.0, kVFeeder.getAsDouble());

        io.setShooterPID(kPShooter.getAsDouble(), 0.0, 0.0);
        leftFF = new SimpleMotorFeedforward(kSShooter.getAsDouble(), ShooterConstants.kVShooter, ShooterConstants.kAShooter);
        rightFF = new SimpleMotorFeedforward(kSShooter.getAsDouble(), ShooterConstants.kVShooter, ShooterConstants.kAShooter);
  }

  @Override
  public void periodic() {
    io.processInputs(inputs);
    Logger.processInputs("Shooter", inputs);
  }

  public Command setPivotTarget(DoubleSupplier radians) {
    return this.run(
      () -> {
        io.setPivotTarget(radians.getAsDouble(), pivotFF);
        inputs.pivotTargetPosition = Rotation2d.fromRadians(radians.getAsDouble());
      }
    );
  }

  public Command setFeederRPM(IntSupplier rpm) {
    return this.run(
      () -> {
        io.setFeederRPM(rpm.getAsInt(), feederFF);
        inputs.feederTargetRPM = rpm.getAsInt();
      }
    );
  }

  public Command setLeftRPM(IntSupplier rpm) {
    return this.run(
      () -> {
        io.setLeftRPM(rpm.getAsInt(), leftFF);
        inputs.leftTargetRPM = rpm.getAsInt();
      }
    );
  }

  public Command setRightRPM(IntSupplier rpm) {
    return this.run(
      () -> {
        io.setRightRPM(rpm.getAsInt(), rightFF);
        inputs.rightTargetRPM = rpm.getAsInt();
      }
    );
  }

  public double getAngleRadians() {
    return inputs.pivotPosition.getRadians();
  }

  public double getTargetRadians() {
    return inputs.pivotTargetPosition.getRadians();
  }

  public Command setPivotVoltage(DoubleSupplier volts) {
    return this.run(
      () -> {
        io.setPivotVoltage(volts.getAsDouble());
        inputs.pivotAppliedVolts = volts.getAsDouble();
      }
    );
  }
}
