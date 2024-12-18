// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.shooter;

import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Constants.ShooterConstants;
import frc.util.LoggedTunableNumber;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import org.littletonrobotics.junction.Logger;

/** Add your docs here. */
public class Shooter extends SubsystemBase {
  public ShooterIO io;
  public ShooterIOInputsAutoLogged inputs = new ShooterIOInputsAutoLogged();

  private SimpleMotorFeedforward shooterFF;

  private LoggedTunableNumber kPShooter = new LoggedTunableNumber("Shooter/kPShooter");
  private LoggedTunableNumber kIShooter = new LoggedTunableNumber("Shooter/kIShooter");
  private LoggedTunableNumber kSShooter = new LoggedTunableNumber("Shooter/kSShooter");

  public Shooter(ShooterIO io) {
    this.io = io;

    switch (Constants.currentMode) {
      case REAL:
        kPShooter.initDefault(ShooterConstants.kPShooterReal);
        kIShooter.initDefault(ShooterConstants.kIShooterReal);
        kSShooter.initDefault(ShooterConstants.kSShooterReal);
        break;
      case SIM:
        kPShooter.initDefault(ShooterConstants.kPShooterSim);
        kIShooter.initDefault(ShooterConstants.kIShooterSim);
        kSShooter.initDefault(ShooterConstants.kSShooterSim);
        break;
      case REPLAY:
        kPShooter.initDefault(ShooterConstants.kPShooterReplay);
        kIShooter.initDefault(ShooterConstants.kIShooterReplay);
        kSShooter.initDefault(ShooterConstants.kSShooterReplay);
        break;
      default:
        kPShooter.initDefault(0.0);
        kIShooter.initDefault(0.0);
        kSShooter.initDefault(0.0);
    }
    io.setShooterPID(kPShooter.getAsDouble(), kIShooter.getAsDouble(), 0.0);
    shooterFF =
        new SimpleMotorFeedforward(
            kSShooter.getAsDouble(), ShooterConstants.kVShooter, ShooterConstants.kAShooter);
  }

  public void periodic() {
    io.processInputs(inputs);
    Logger.processInputs("Shooter", inputs);
  }

  public Command setRPM(IntSupplier rpm, double differential) {
    return this.run(
        () -> {
          io.setRPM(rpm.getAsInt(), shooterFF, differential);
          inputs.leftTargetRPM = rpm.getAsInt();
          inputs.rightTargetRPM = rpm.getAsInt() * differential;
        });
  }

  public Command setLeftRPM(IntSupplier rpm) {
    return this.run(
        () -> {
          io.setLeftRPM(rpm.getAsInt(), shooterFF);
          inputs.leftTargetRPM = rpm.getAsInt();
        });
  }

  public Command setLeftVoltage(DoubleSupplier volts) {
    return this.run(
        () -> {
          io.setLeftVoltage(volts.getAsDouble());
        });
  }

  public Command setRightVoltage(DoubleSupplier volts) {
    return this.run(
        () -> {
          io.setRightVoltage(volts.getAsDouble());
        });
  }

  public Command setRightRPM(IntSupplier rpm) {
    return this.run(
        () -> {
          io.setRightRPM(rpm.getAsInt(), shooterFF);
          inputs.rightTargetRPM = rpm.getAsInt();
        });
  }

  public Command stopShooter() {
    return this.run(
        () -> {
          io.setRightVoltage(0);
          io.setLeftVoltage(0);
        });
  }

  public boolean atSetpoint() {
    return Math.abs(inputs.leftTargetRPM - inputs.leftSpeedRPM) <= (0.05 * inputs.leftTargetRPM)
        && Math.abs(inputs.rightTargetRPM - inputs.rightSpeedRPM) <= (0.05 * inputs.rightTargetRPM);
  }
}
