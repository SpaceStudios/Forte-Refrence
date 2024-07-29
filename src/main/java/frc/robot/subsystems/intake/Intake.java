// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.intake;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Constants.IntakeConstants;
import frc.util.LoggedTunableNumber;

public class Intake extends SubsystemBase {
  public IntakeIO io;
  public IntakeIOInputsAutoLogged inputs = new IntakeIOInputsAutoLogged();

  private ArmFeedforward pivotFF;
  private SimpleMotorFeedforward rollerFF;

  private LoggedTunableNumber kPPivot = new LoggedTunableNumber("Intake/kPPivot");
  
  private LoggedTunableNumber kPRoller = new LoggedTunableNumber("Intake/kPRoller");
  private LoggedTunableNumber kSRoller = new LoggedTunableNumber("Intake/kSRoller");


  /** Creates a new Intake. */
  public Intake(IntakeIO io) {
    this.io = io;

    switch (Constants.currentMode) {
      case REAL:
        kPPivot.initDefault(IntakeConstants.kPPivotReal);

        kPRoller.initDefault(IntakeConstants.kPRollerReal);
        kSRoller.initDefault(IntakeConstants.kSRollerReal);
        break;

      case SIM:
        kPPivot.initDefault(IntakeConstants.kPPivotSim);

        kPRoller.initDefault(IntakeConstants.kPRollerSim);
        kSRoller.initDefault(IntakeConstants.kSRollerSim);
        break;

      case REPLAY:
        kPPivot.initDefault(IntakeConstants.kPPivotReplay);

        kPRoller.initDefault(IntakeConstants.kPRollerReplay);
        kSRoller.initDefault(IntakeConstants.kSRollerReplay);
        break;

      default:
        kPPivot.initDefault(0.0);

        kPRoller.initDefault(0.0);
        kSRoller.initDefault(0.0);
    }

    io.setPivotPID(kPPivot.getAsDouble(), 0.0, 0.0);
    pivotFF = new ArmFeedforward(0.0, IntakeConstants.kGPivot, IntakeConstants.kVPivot, IntakeConstants.kAPivot);

    io.setRollerPID(kPRoller.getAsDouble(), 0.0, 0.0);
    rollerFF = new SimpleMotorFeedforward(kSRoller.getAsDouble(), IntakeConstants.kVRoller, IntakeConstants.kARoller);
  }

  @Override
  public void periodic() {
    io.processInputs(inputs);
    Logger.processInputs("Intake", inputs);

    if (inputs.pivotCurrentAmps > 55) {
      stopPivot();
    }
  }

  public Command setPivotTarget(DoubleSupplier radians) {
          return this.run(
              () -> {
                  io.setPivotTarget(radians.getAsDouble(), pivotFF);
                  inputs.pivotTargetPosition = Rotation2d.fromRadians(radians.getAsDouble() + IntakeConstants.simOffset);
              }
          );
      }

  public Command setRollerRPM(IntSupplier rpm) {
    return this.run(
      () -> {
        io.setRollerRPM(rpm.getAsInt(), rollerFF);
        inputs.rollerTargetRPM = rpm.getAsInt();
      }
    );
  }

  public double getAngleRadians() {
    return inputs.pivotPosition.getRadians() + IntakeConstants.simOffset;
  }

  public double getTargetRadians() {
    return inputs.pivotTargetPosition.getRadians();
  }

  public void stopPivot() {
    io.setPivotVoltage(0);
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
